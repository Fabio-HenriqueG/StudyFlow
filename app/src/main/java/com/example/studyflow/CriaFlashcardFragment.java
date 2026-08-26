package com.example.studyflow;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Flashcard;
import com.example.studyflow.data.Materia;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class CriaFlashcardFragment extends Fragment {

    private EditText txtPergunta, txtResposta, txtExplicacao;
    private AutoCompleteTextView spinnerMaterias;
    private Flashcard flashcardEdicao;
    private List<Materia> listaMaterias = new ArrayList<>();
    private Materia materiaSelecionada;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cria_flashcard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtPergunta = view.findViewById(R.id.txtPergunta);
        txtResposta = view.findViewById(R.id.txtResposta);
        txtExplicacao = view.findViewById(R.id.txtExplicacao);
        spinnerMaterias = view.findViewById(R.id.spinnerMaterias);
        Button btnNovaMateria = view.findViewById(R.id.btnNovaMateria);
        Button btnSalvar = view.findViewById(R.id.btnSalvarFlashcard);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarFlashcard);
        
        carregarMaterias();

        btnNovaMateria.setOnClickListener(v -> mostrarDialogoNovaMateria());

        spinnerMaterias.setOnItemClickListener((parent, view1, position, id) -> {
            materiaSelecionada = listaMaterias.get(position);
        });

        if (getArguments() != null && getArguments().containsKey("flashcard_editar")) {
            flashcardEdicao = (Flashcard) getArguments().getSerializable("flashcard_editar");
            if (flashcardEdicao != null) {
                txtPergunta.setText(flashcardEdicao.pergunta);
                txtResposta.setText(flashcardEdicao.resposta);
                txtExplicacao.setText(flashcardEdicao.explicacao);
                btnSalvar.setText("Atualizar Flashcard");
                // Seleção da matéria será feita após carregar a lista
            }
        }

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnSalvar.setOnClickListener(v -> salvarFlashcard());
    }

    private void carregarMaterias() {
        Executors.newSingleThreadExecutor().execute(() -> {
            listaMaterias = AppDatabase.getInstance(getContext()).materiaDao().buscarTodas();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    ArrayAdapter<Materia> adapter = new ArrayAdapter<>(getContext(), 
                            android.R.layout.simple_dropdown_item_1line, listaMaterias);
                    spinnerMaterias.setAdapter(adapter);
                    
                    if (flashcardEdicao != null) {
                        for (Materia m : listaMaterias) {
                            if (m.id == flashcardEdicao.materiaId) {
                                materiaSelecionada = m;
                                spinnerMaterias.setText(m.nome, false);
                                break;
                            }
                        }
                    }
                });
            }
        });
    }

    private void mostrarDialogoNovaMateria() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getContext());
        builder.setTitle("Nova Seção / Matéria");
        
        final EditText input = new EditText(getContext());
        input.setHint("Ex: Anatomia, História...");
        builder.setView(input);

        builder.setPositiveButton("Criar", (dialog, which) -> {
            String nome = input.getText().toString().trim();
            if (!nome.isEmpty()) {
                Executors.newSingleThreadExecutor().execute(() -> {
                    Materia nova = new Materia(nome, Color.BLUE); // Cor padrão inicial
                    long id = AppDatabase.getInstance(getContext()).materiaDao().inserir(nova);
                    nova.id = (int) id;
                    getActivity().runOnUiThread(() -> {
                        carregarMaterias();
                        materiaSelecionada = nova;
                        spinnerMaterias.setText(nova.nome, false);
                    });
                });
            }
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }

    private void salvarFlashcard() {
        String pergunta = txtPergunta.getText().toString().trim();
        String resposta = txtResposta.getText().toString().trim();
        String explicacao = txtExplicacao.getText().toString().trim();

        if (pergunta.isEmpty() || resposta.isEmpty()) {
            Toast.makeText(getContext(), "Preencha pergunta e resposta!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (materiaSelecionada == null) {
            Toast.makeText(getContext(), "Selecione ou crie uma Matéria!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (flashcardEdicao != null) {
            flashcardEdicao.pergunta = pergunta;
            flashcardEdicao.resposta = resposta;
            flashcardEdicao.explicacao = explicacao;
            flashcardEdicao.materiaId = materiaSelecionada.id;
            
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).flashcardDao().atualizar(flashcardEdicao);
                finalizar("Flashcard atualizado!");
            });
        } else {
            Flashcard flashcard = new Flashcard(pergunta, resposta, explicacao, materiaSelecionada.id);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).flashcardDao().inserir(flashcard);
                finalizar("Flashcard criado!");
            });
        }
    }

    private void finalizar(String msg) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                getParentFragmentManager().popBackStack();
            });
        }
    }
}
