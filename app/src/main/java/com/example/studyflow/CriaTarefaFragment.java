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
import android.widget.Toast;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CriaTarefaFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CriaTarefaFragment extends Fragment {
    //Variáveis da classe de criar as tarefas
    private EditText txtTituloTarefa;
    private EditText txtDescricaoTarefa;
    private Button btnSalvarTarefa;
    private CalendarView calendarioTarefa;
    private Tarefa tarefaEmEdicao;
    private long dataSelecionada; // Guarda a data que o usuário clicou no calendário
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public CriaTarefaFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CriaTarefaFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CriaTarefaFragment newInstance(String param1, String param2) {
        CriaTarefaFragment fragment = new CriaTarefaFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Aqui ele chama o layout do fragment na tela
        return inflater.inflate(R.layout.fragment_cria_tarefa, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Vincular as variáveis Java aos IDs reais do seu XML
        txtTituloTarefa = view.findViewById(R.id.txtTituloTarefa);
        txtDescricaoTarefa = view.findViewById(R.id.txtDescricaoTarefa);
        btnSalvarTarefa = view.findViewById(R.id.btnSalvarTarefa);
        calendarioTarefa = view.findViewById(R.id.calendario_tarefa);

        // Define a data selecionada inicialmente como a data atual (hoje)
        dataSelecionada = calendarioTarefa.getDate();

        // Escuta quando o usuário clica em outro dia no calendário
        calendarioTarefa.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);
            dataSelecionada = calendar.getTimeInMillis();
        });

        // Verifica se recebemos uma tarefa para editar
        if (getArguments() != null && getArguments().containsKey("tarefa_editar")) {
            tarefaEmEdicao = (Tarefa) getArguments().getSerializable("tarefa_editar");
            if (tarefaEmEdicao != null) {
                txtTituloTarefa.setText(tarefaEmEdicao.titulo);
                txtDescricaoTarefa.setText(tarefaEmEdicao.descricao);
                btnSalvarTarefa.setText("Atualizar");
            }
        }

        // Configurar a ação de clique do botão Salvar
        btnSalvarTarefa.setOnClickListener(v -> salvarTarefaNoBanco());
    }

    // 3. Método responsável por capturar, validar e mandar para o Room
    private void salvarTarefaNoBanco() {
        String titulo = txtTituloTarefa.getText().toString().trim();
        String descricao = txtDescricaoTarefa.getText().toString().trim();

        // Validação obrigatória
        if (titulo.isEmpty()) {
            txtTituloTarefa.setError("O título da tarefa é obrigatório!");
            txtTituloTarefa.requestFocus();
            return;
        }

        // Capturamos o contexto da aplicação para usar dentro da Thread de forma segura
        Context context = getContext();
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        // Se estivermos editando, atualizamos o objeto existente. Se não, criamos um novo.
        if (tarefaEmEdicao != null) {
            tarefaEmEdicao.titulo = titulo;
            tarefaEmEdicao.descricao = descricao;
            tarefaEmEdicao.dataLimite = dataSelecionada; // Atualiza a data também na edição
            
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(appContext).tarefaDao().atualizar(tarefaEmEdicao);
                
                // Se o prazo for para hoje, agenda o alerta imediato
                if (DateUtils.isToday(tarefaEmEdicao.dataLimite)) {
                    agendarAlertaImediato(appContext, tarefaEmEdicao.id);
                }
                
                finalizarEDarFeedback(appContext, "Tarefa atualizada com sucesso!");
            });
        } else {
            // Frequência padrão
            int frequenciaPadrao = 1;

            // Criar o objeto Tarefa com os dados da tela e a data do calendário
            Tarefa novaTarefa = new Tarefa(titulo, descricao, dataSelecionada, frequenciaPadrao);

            // Executar a inserção em segundo plano
            Executors.newSingleThreadExecutor().execute(() -> {
                // Ao inserir, o banco gera um novo ID. Precisamos dele para o alerta.
                AppDatabase.getInstance(appContext).tarefaDao().inserir(novaTarefa);
                
                // Para pegar o ID que acabou de ser gerado, vamos buscar a última tarefa inserida
                // (ou poderíamos mudar o DAO para retornar o long do ID inserido)
                List<Tarefa> todas = AppDatabase.getInstance(appContext).tarefaDao().buscarTodas();
                if (!todas.isEmpty()) {
                    Tarefa inserida = todas.get(todas.size() - 1); // Simplificação
                    
                    // Se o prazo for para hoje, agenda o alerta de 10 segundos
                    if (DateUtils.isToday(inserida.dataLimite)) {
                        agendarAlertaImediato(appContext, inserida.id);
                    }
                }

                finalizarEDarFeedback(appContext, "Tarefa salva com sucesso!");
            });
        }
    }

    /**
     * Agenda uma notificação para aparecer 10 segundos depois de criar a tarefa de hoje.
     */
    private void agendarAlertaImediato(Context context, int tarefaId) {
        // Dados que o Worker vai precisar (ID da tarefa)
        Data inputData = new Data.Builder()
                .putInt("tarefa_id", tarefaId)
                .build();

        // Cria o pedido de trabalho único com delay de 10 segundos
        OneTimeWorkRequest alertaRequest = new OneTimeWorkRequest.Builder(NotificacaoImediataWorker.class)
                .setInitialDelay(10, TimeUnit.SECONDS)
                .setInputData(inputData)
                .build();

        // Envia para o WorkManager
        WorkManager.getInstance(context).enqueue(alertaRequest);
    }

    private void finalizarEDarFeedback(Context context, String mensagem) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(context, mensagem, Toast.LENGTH_SHORT).show();
                // Verifica se o fragmento ainda está anexado antes de fechar
                if (isAdded()) {
                    getParentFragmentManager().popBackStack();
                }
            });
        }
    }
}
