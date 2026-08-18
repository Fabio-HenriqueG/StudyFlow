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
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);

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

        if (tarefaEmEdicao != null) {
            tarefaEmEdicao.titulo = titulo;
            tarefaEmEdicao.descricao = descricao;
            tarefaEmEdicao.dataLimite = dataSelecionada;
            
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(appContext).tarefaDao().atualizar(tarefaEmEdicao);
                if (DateUtils.isToday(tarefaEmEdicao.dataLimite)) {
                    agendarAlertaImediato(appContext, tarefaEmEdicao.id);
                }
                finalizarEDarFeedback(appContext, "Tarefa atualizada com sucesso!");
            });
        } else {
            Tarefa novaTarefa = new Tarefa(titulo, descricao, dataSelecionada, 1);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(appContext).tarefaDao().inserir(novaTarefa);
                List<Tarefa> todas = AppDatabase.getInstance(appContext).tarefaDao().buscarTodas();
                if (!todas.isEmpty()) {
                    Tarefa inserida = todas.get(todas.size() - 1);
                    if (DateUtils.isToday(inserida.dataLimite)) {
                        agendarAlertaImediato(appContext, inserida.id);
                    }
                }
                finalizarEDarFeedback(appContext, "Tarefa salva com sucesso!");
            });
        }
    }

    private void agendarAlertaImediato(Context context, int tarefaId) {
        Data inputData = new Data.Builder().putInt("tarefa_id", tarefaId).build();
        OneTimeWorkRequest alertaRequest = new OneTimeWorkRequest.Builder(NotificacaoImediataWorker.class)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build();
        WorkManager.getInstance(context).enqueue(alertaRequest);
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
