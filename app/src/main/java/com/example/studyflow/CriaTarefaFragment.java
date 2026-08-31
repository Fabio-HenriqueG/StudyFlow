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

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.example.studyflow.data.AppDatabase;
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
    private ChipGroup chipGroupPrioridade;
    private ChipGroup toggleGroupInsistencia;
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
        toggleGroupInsistencia = view.findViewById(R.id.toggleGroupInsistenciaCria);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);
        
        // Padrões
        chipGroupPrioridade.check(R.id.chipMedia);
        toggleGroupInsistencia.check(R.id.btnInsistEquilibrado);

        btnVoltar.setOnClickListener(v -> voltarOuHome());

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

                // Marca a insistência correta
                if (tarefaEmEdicao.insistencia == 0) toggleGroupInsistencia.check(R.id.btnInsistDiscreto);
                else if (tarefaEmEdicao.insistencia == 2) toggleGroupInsistencia.check(R.id.btnInsistChato);
                else toggleGroupInsistencia.check(R.id.btnInsistEquilibrado);

                btnSalvarTarefa.setText("Atualizar");
            }
        }

        btnSalvarTarefa.setOnClickListener(v -> salvarTarefaNoBanco());
    }

    private void voltarOuHome() {
        if (getParentFragmentManager().getBackStackEntryCount() > 0) {
            getParentFragmentManager().popBackStack();
        } else {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
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

        Context appContext = requireContext().getApplicationContext();

        int prioridade = 1; // Média por padrão
        int selectedId = chipGroupPrioridade.getCheckedChipId();
        if (selectedId == R.id.chipBaixa) prioridade = 0;
        else if (selectedId == R.id.chipAlta) prioridade = 2;

        int insistencia = 1;
        int checkedInsistId = toggleGroupInsistencia.getCheckedChipId();
        if (checkedInsistId == R.id.btnInsistDiscreto) insistencia = 0;
        else if (checkedInsistId == R.id.btnInsistChato) insistencia = 2;

        if (tarefaEmEdicao != null) {
            tarefaEmEdicao.titulo = titulo;
            tarefaEmEdicao.descricao = descricao;
            tarefaEmEdicao.dataLimite = dataSelecionada;
            tarefaEmEdicao.prioridade = prioridade;
            tarefaEmEdicao.insistencia = insistencia;
            
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(appContext).tarefaDao().atualizar(tarefaEmEdicao);
                
                // Agendamento Inteligente
                NotificacaoScheduler.cancelarNotificacoesTarefa(appContext, tarefaEmEdicao.id);
                NotificacaoScheduler.agendarNotificacoesTarefa(appContext, tarefaEmEdicao);
                
                finalizarEDarFeedback(appContext, "Tarefa atualizada com sucesso!");
            });
        } else {
            Tarefa novaTarefa = new Tarefa(titulo, descricao, dataSelecionada, 1, prioridade, insistencia);

            Executors.newSingleThreadExecutor().execute(() -> {
                long id = AppDatabase.getInstance(appContext).tarefaDao().inserir(novaTarefa);
                novaTarefa.id = (int) id;

                // Registra a atividade de planejamento (criação) para o streak
                ProdutividadeManager.registrarAtividade(appContext, "PLANEJAMENTO", 0, 0);

                // Agendamento Inteligente
                NotificacaoScheduler.agendarNotificacoesTarefa(appContext, novaTarefa);

                finalizarEDarFeedback(appContext, "Tarefa salva com sucesso!");
            });
        }
    }

    private void finalizarEDarFeedback(Context context, String mensagem) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show();
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
