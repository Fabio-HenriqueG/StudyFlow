package com.example.studyflow;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

import java.util.concurrent.Executors;

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
    private Tarefa tarefaEmEdicao;
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

        // Se estivermos editando, atualizamos o objeto existente. Se não, criamos um novo.
        if (tarefaEmEdicao != null) {
            tarefaEmEdicao.titulo = titulo;
            tarefaEmEdicao.descricao = descricao;
            
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).tarefaDao().atualizar(tarefaEmEdicao);
                finalizarEDarFeedback("Tarefa atualizada com sucesso!");
            });
        } else {
            // Dados simulados para o banco aceitar o registro agora
            long dataLimiteSimulada = System.currentTimeMillis() + (24 * 60 * 60 * 1000); // 24 horas para frente
            int frequenciaPadrao = 1;

            // Criar o objeto Tarefa com os dados da tela
            Tarefa novaTarefa = new Tarefa(titulo, descricao, dataLimiteSimulada, frequenciaPadrao);

            // Executar a inserção em segundo plano
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).tarefaDao().inserir(novaTarefa);
                finalizarEDarFeedback("Tarefa salva com sucesso!");
            });
        }
    }

    private void finalizarEDarFeedback(String mensagem) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), mensagem, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}