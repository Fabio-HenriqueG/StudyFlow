package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Flashcard;
import com.example.studyflow.data.Materia;
import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class RevisaoFlashcardsFragment extends Fragment {

    private TextView txtPergunta, txtResposta, lblProgresso, txtExplicacao, lblExplicacao;
    private LinearLayout layoutFrente, layoutVerso;
    private View dividerExplicacao;
    private MaterialCardView cardFlashcard;
    private com.google.android.material.button.MaterialButton btnErrei, btnDificil, btnBom, btnFacil;
    
    private List<Flashcard> listaRevisao = new ArrayList<>();
    private int indiceAtual = 0;
    private boolean mostrandoVerso = false;
    private boolean mostrandoExplicacao = false;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_revisao_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtPergunta = view.findViewById(R.id.txtPerguntaRevisao);
        txtResposta = view.findViewById(R.id.txtRespostaRevisao);
        txtExplicacao = view.findViewById(R.id.txtExplicacaoRevisao);
        lblExplicacao = view.findViewById(R.id.lblExplicacao);
        dividerExplicacao = view.findViewById(R.id.dividerExplicacao);
        lblProgresso = view.findViewById(R.id.lblProgressoRevisao);
        layoutFrente = view.findViewById(R.id.layoutFrente);
        layoutVerso = view.findViewById(R.id.layoutVerso);
        cardFlashcard = view.findViewById(R.id.cardFlashcard);
        btnErrei = view.findViewById(R.id.btnErrei);
        btnDificil = view.findViewById(R.id.btnDificil);
        btnBom = view.findViewById(R.id.btnBom);
        btnFacil = view.findViewById(R.id.btnFacil);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarRevisao);

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        cardFlashcard.setOnClickListener(v -> flipCard());
        
        btnErrei.setOnClickListener(v -> processarResposta(0));
        btnDificil.setOnClickListener(v -> processarResposta(1));
        btnBom.setOnClickListener(v -> processarResposta(2));
        btnFacil.setOnClickListener(v -> processarResposta(3));


        carregarFlashcards();
    }

    private void carregarFlashcards() {
        Executors.newSingleThreadExecutor().execute(() -> {
            long hoje = System.currentTimeMillis();
            listaRevisao = AppDatabase.getInstance(getContext()).flashcardDao().buscarParaRevisarHoje(hoje);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (listaRevisao.isEmpty()) {
                        Toast.makeText(getContext(), "Não há nada para revisar hoje! 🎉", Toast.LENGTH_LONG).show();
                        getParentFragmentManager().popBackStack();
                    } else {
                        exibirCardAtual();
                    }
                });
            }
        });
    }

    private void exibirCardAtual() {
        if (indiceAtual >= listaRevisao.size()) {
            Toast.makeText(getContext(), "Revisão concluída! Ótimo trabalho.", Toast.LENGTH_SHORT).show();
            getParentFragmentManager().popBackStack();
            return;
        }

        Flashcard atual = listaRevisao.get(indiceAtual);
        txtPergunta.setText(atual.pergunta);
        txtResposta.setText(atual.resposta);
        txtExplicacao.setText(atual.explicacao != null ? atual.explicacao : "");
        
        // Busca a matéria para mostrar no topo do card se desejar
        Executors.newSingleThreadExecutor().execute(() -> {
            Materia m = AppDatabase.getInstance(getContext()).materiaDao().buscarPorId(atual.materiaId);
            if (m != null && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Podemos colocar o nome da matéria no título ou subtítulo
                    lblProgresso.setText(m.nome + " • " + (indiceAtual + 1) + " / " + listaRevisao.size());
                });
            }
        });
        
        mostrandoVerso = false;
        mostrandoExplicacao = false;
        
        layoutFrente.setVisibility(View.VISIBLE);
        layoutVerso.setVisibility(View.GONE);
        txtExplicacao.setVisibility(View.GONE);
        lblExplicacao.setVisibility(View.GONE);
        dividerExplicacao.setVisibility(View.GONE);
        
        btnErrei.setEnabled(false);
        btnDificil.setEnabled(false);
        btnBom.setEnabled(false);
        btnFacil.setEnabled(false);
        
        btnErrei.setAlpha(0.5f);
        btnDificil.setAlpha(0.5f);
        btnBom.setAlpha(0.5f);
        btnFacil.setAlpha(0.5f);
        
        btnErrei.setText("Errei");
    }


    private void flipCard() {
        if (mostrandoVerso) return;

        cardFlashcard.animate()
                .rotationY(90)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    layoutFrente.setVisibility(View.GONE);
                    layoutVerso.setVisibility(View.VISIBLE);
                    cardFlashcard.setRotationY(-90);
                    cardFlashcard.animate()
                            .rotationY(0)
                            .setDuration(200)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .start();
                    mostrandoVerso = true;
                    
                    btnErrei.setEnabled(true);
                    btnDificil.setEnabled(true);
                    btnBom.setEnabled(true);
                    btnFacil.setEnabled(true);
                    
                    btnErrei.setAlpha(1.0f);
                    btnDificil.setAlpha(1.0f);
                    btnBom.setAlpha(1.0f);
                    btnFacil.setAlpha(1.0f);
                }).start();
    }

    private void processarResposta(int nota) {
        Flashcard atual = listaRevisao.get(indiceAtual);
        
        if (nota == 0 && atual.explicacao != null && !atual.explicacao.isEmpty() && !mostrandoExplicacao) {
            // Mostra a explicação antes de ir para o próximo
            txtExplicacao.setVisibility(View.VISIBLE);
            lblExplicacao.setVisibility(View.VISIBLE);
            dividerExplicacao.setVisibility(View.VISIBLE);
            mostrandoExplicacao = true;
            btnErrei.setText("Próximo ➡️");
            
            btnDificil.setEnabled(false);
            btnBom.setEnabled(false);
            btnFacil.setEnabled(false);
            btnDificil.setAlpha(0.5f);
            btnBom.setAlpha(0.5f);
            btnFacil.setAlpha(0.5f);
            return;
        }

        long agora = System.currentTimeMillis();
        
        // Algoritmo de Repetição Espaçada Científico (1, 7, 15, 30, 30...)
        if (nota == 0) {
            // Errou: Reseta o progresso para o início do ciclo
            atual.intervalo = 0;
            atual.repeticoes = 0;
            atual.nivelDominio = Math.max(0, atual.nivelDominio - 1);
            atual.dataProximaRevisao = agora; // Mantém para hoje para nova tentativa

            // Adiciona ao final da lista para repetir na mesma sessão
            Flashcard repeticao = new Flashcard(atual.pergunta, atual.resposta, atual.explicacao, atual.materiaId);
            repeticao.id = atual.id;
            listaRevisao.add(repeticao);
        } else {
            // Acertou: Define o próximo intervalo baseado no padrão científico
            switch (atual.repeticoes) {
                case 0:
                    atual.intervalo = 1; // 1 dia depois
                    break;
                case 1:
                    atual.intervalo = 7; // 7 dias depois
                    break;
                case 2:
                    atual.intervalo = 15; // 15 dias depois
                    break;
                case 3:
                default:
                    atual.intervalo = 30; // 30 dias depois (e subsequentes)
                    break;
            }

            atual.repeticoes++;
            atual.nivelDominio = Math.min(5, atual.nivelDominio + 1);
            atual.dataProximaRevisao = agora + (atual.intervalo * 24L * 60 * 60 * 1000);

            // Registra a atividade no histórico
            ProdutividadeManager.registrarAtividade(getContext(), "FLASHCARD", atual.id, atual.materiaId);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            // Se for um novo item adicionado na sessão (ID=0), não salva no banco ainda
            // Mas o 'atual' original que foi modificado precisa ser salvo
            AppDatabase.getInstance(getContext()).flashcardDao().atualizar(atual);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    indiceAtual++;
                    exibirCardAtual();
                });
            }
        });
    }

}
