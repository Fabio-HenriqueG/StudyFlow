package com.example.studyflow;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.material.chip.ChipGroup;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.FirestoreService;
import com.example.studyflow.data.Tarefa;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class CriaTarefaFragment extends Fragment {
    private EditText txtTituloTarefa;
    private EditText txtDescricaoTarefa;
    private Button btnSalvarTarefa;
    private CalendarView calendarioTarefa;
    private ChipGroup chipGroupPrioridade, chipGroupIntensidade;
    private Tarefa tarefaEmEdicao;
    private long dataSelecionada;

    public CriaTarefaFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cria_tarefa, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTituloTarefa = view.findViewById(R.id.txtTituloTarefa);
        txtDescricaoTarefa = view.findViewById(R.id.txtDescricaoTarefa);
        btnSalvarTarefa = view.findViewById(R.id.btnSalvarTarefa);
        calendarioTarefa = view.findViewById(R.id.calendario_tarefa);
        chipGroupPrioridade = view.findViewById(R.id.chipGroupPrioridade);
        chipGroupIntensidade = view.findViewById(R.id.chipGroupIntensidade);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);
        ImageButton btnInfo = view.findViewById(R.id.btnInfoIntensidade);
        
        // Padrões
        chipGroupPrioridade.check(R.id.chipMedia);
        chipGroupIntensidade.check(R.id.chipPadrao);

        btnVoltar.setOnClickListener(v -> voltarOuHome());
        if (btnInfo != null) {
            btnInfo.setOnClickListener(v -> mostrarExplicacaoIntensidade());
        }

        Calendar cal = Calendar.getInstance();
        configurarFimDoDia(cal);
        dataSelecionada = cal.getTimeInMillis();

        calendarioTarefa.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            configurarFimDoDia(calendar);
            dataSelecionada = calendar.getTimeInMillis();
        });

        if (getArguments() != null && getArguments().containsKey("tarefa_editar")) {
            tarefaEmEdicao = (Tarefa) getArguments().getSerializable("tarefa_editar");
            if (tarefaEmEdicao != null) {
                txtTituloTarefa.setText(tarefaEmEdicao.titulo);
                txtDescricaoTarefa.setText(tarefaEmEdicao.descricao);
                
                // Marca a prioridade correta
                if (tarefaEmEdicao.prioridade == 0) chipGroupPrioridade.check(R.id.chipBaixa);
                else if (tarefaEmEdicao.prioridade == 2) chipGroupPrioridade.check(R.id.chipAlta);
                else chipGroupPrioridade.check(R.id.chipMedia);

                // Marca a intensidade correta
                if (tarefaEmEdicao.insistencia == 0) chipGroupIntensidade.check(R.id.chipFocada);
                else if (tarefaEmEdicao.insistencia == 2) chipGroupIntensidade.check(R.id.chipIntensa);
                else chipGroupIntensidade.check(R.id.chipPadrao);

                btnSalvarTarefa.setText("Atualizar");
            }
        }

        btnSalvarTarefa.setOnClickListener(v -> salvarTarefaNoBanco());
    }

    private void voltarOuHome() {
        esconderTeclado();
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }

    private void mostrarExplicacaoIntensidade() {
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Níveis de Alerta")
                .setMessage("• Focada: Apenas avisos críticos na última hora.\n\n" +
                         "• Padrão: Notificações estratégicas (meio do prazo, 7 dias antes e dia final).\n\n" +
                         "• Intensa: Pressão total. Alertas aumentam conforme o prazo chega, com lembretes a cada 30 min no dia final!")
                .setPositiveButton("Entendi", null)
                .show();
    }

    private void esconderTeclado() {
        View view = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (view != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) 
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void salvarTarefaNoBanco() {
        String titulo = txtTituloTarefa.getText().toString().trim();
        String descricao = txtDescricaoTarefa.getText().toString().trim();

        if (titulo.isEmpty()) {
            txtTituloTarefa.setError("O título da tarefa é obrigatório!");
            txtTituloTarefa.requestFocus();
            return;
        }

        esconderTeclado();
        Context appContext = requireContext().getApplicationContext();

        int prioridade = 1; // Média por padrão
        int selectedId = chipGroupPrioridade.getCheckedChipId();
        if (selectedId == R.id.chipBaixa) prioridade = 0;
        else if (selectedId == R.id.chipAlta) prioridade = 2;

        int insistencia = 1; // Equilibrado por padrão
        int selectedIntId = chipGroupIntensidade.getCheckedChipId();
        if (selectedIntId == R.id.chipFocada) insistencia = 0;
        else if (selectedIntId == R.id.chipIntensa) insistencia = 2;

        if (tarefaEmEdicao != null) {
            tarefaEmEdicao.titulo = titulo;
            tarefaEmEdicao.descricao = descricao;
            tarefaEmEdicao.dataLimite = dataSelecionada;
            tarefaEmEdicao.prioridade = prioridade;
            tarefaEmEdicao.insistencia = insistencia;
            
            FirestoreService.getInstance().salvarTarefa(tarefaEmEdicao)
                .addOnSuccessListener(aVoid -> {
                    // Agendamento Inteligente
                    NotificacaoScheduler.cancelarNotificacoesTarefa(appContext, tarefaEmEdicao.id);
                    NotificacaoScheduler.agendarNotificacoesTarefa(appContext, tarefaEmEdicao);
                    
                    finalizarEDarFeedback(appContext, getString(R.string.tarefa_atualizada_sucesso));
                })
                .addOnFailureListener(e -> Toast.makeText(appContext, "Erro ao atualizar", Toast.LENGTH_SHORT).show());
        } else {
            Tarefa novaTarefa = new Tarefa(titulo, descricao, dataSelecionada, prioridade, insistencia);

            FirestoreService.getInstance().salvarTarefa(novaTarefa)
                .addOnSuccessListener(aVoid -> {
                    // Registra a atividade de planejamento (criação) para o streak
                    ProdutividadeManager.registrarAtividade(appContext, "PLANEJAMENTO", novaTarefa.getIntId(), 0);

                    // Agendamento Inteligente
                    NotificacaoScheduler.agendarNotificacoesTarefa(appContext, novaTarefa);

                    finalizarEDarFeedback(appContext, getString(R.string.tarefa_salva_sucesso));
                })
                .addOnFailureListener(e -> Toast.makeText(appContext, "Erro ao salvar", Toast.LENGTH_SHORT).show());
        }
    }

    private void finalizarEDarFeedback(Context context, String mensagem) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                com.google.android.material.snackbar.Snackbar.make(
                    getActivity().findViewById(android.R.id.content),
                    mensagem,
                    com.google.android.material.snackbar.Snackbar.LENGTH_SHORT
                ).show();
                
                if (isAdded()) {
                    voltarOuHome();
                }
            });
        }
    }

    private void configurarFimDoDia(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }
}
