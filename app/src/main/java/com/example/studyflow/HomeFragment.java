package com.example.studyflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.Meta;
import com.example.studyflow.data.Tarefa;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * Fragmento da tela inicial que exibe o resumo de tarefas, anotações e metas.
 */
public class HomeFragment extends Fragment {

    private TextView txtAtrasadas, txtPendentes, lblTituloAnotacoes, lblTituloMetas, lblTituloTarefas, txtFlashcardsCount, txtStreakCount;
    private RecyclerView recyclerMetasHome, recyclerAnotacoesHome;
    private LinearLayout layoutStatus;
    private View cardEmptyState, cardEmptyTarefas, cardEmptyAnotacoes, cardEmptyMetas, cardRevisaoFlashcards, cardEmptyFlashcards, layoutSecaoFlashcards, cardStreak;
    private int countMetas = -1, countAnotacoes = -1, countTarefas = -1, countFlashcards = -1;

    public HomeFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Vinculação de componentes
        TextView lblSaudacao = view.findViewById(R.id.lblSaudacao);
        TextView lblDataAtual = view.findViewById(R.id.lblDataAtual);
        android.widget.ImageView imgPerfil = view.findViewById(R.id.imgPerfilHome);
        txtAtrasadas = view.findViewById(R.id.text_home_atrasadas);
        txtPendentes = view.findViewById(R.id.text_home_pendentes);
        recyclerMetasHome = view.findViewById(R.id.recycler_metas_home);
        recyclerAnotacoesHome = view.findViewById(R.id.recycler_anotacoes_home);
        layoutStatus = view.findViewById(R.id.layoutStatus);
        ImageButton btnConfig = view.findViewById(R.id.btnConfiguracoes);
        
        lblTituloAnotacoes = view.findViewById(R.id.textView9);
        lblTituloMetas = view.findViewById(R.id.textView10);
        lblTituloTarefas = view.findViewById(R.id.lblTituloTarefas);
        
        cardEmptyState = view.findViewById(R.id.cardEmptyStateHome);
        cardEmptyTarefas = view.findViewById(R.id.cardEmptyTarefas);
        cardEmptyAnotacoes = view.findViewById(R.id.cardEmptyAnotacoes);
        cardEmptyMetas = view.findViewById(R.id.cardEmptyMetas);
        cardRevisaoFlashcards = view.findViewById(R.id.cardRevisaoFlashcards);
        cardEmptyFlashcards = view.findViewById(R.id.cardEmptyFlashcards);
        layoutSecaoFlashcards = view.findViewById(R.id.layoutSecaoFlashcards);
        txtFlashcardsCount = view.findViewById(R.id.txtFlashcardsCount);
        txtStreakCount = view.findViewById(R.id.txtStreakCount);
        cardStreak = view.findViewById(R.id.cardStreak);
        
        View btnIniciarRevisao = view.findViewById(R.id.btnIniciarRevisao);
        View btnVerColecaoHome = view.findViewById(R.id.btnVerColecaoHome);
        
        btnIniciarRevisao.setOnClickListener(v -> navegarPara(new RevisaoFlashcardsFragment()));
        btnVerColecaoHome.setOnClickListener(v -> navegarPara(new FlashcardsFragment()));
        cardStreak.setOnClickListener(v -> navegarPara(new DashboardFragment()));

        
        View btnComecar = view.findViewById(R.id.btnComecarHome);

        // Configuração de Saudação Personalizada e Foto
        SharedPreferences prefs = requireContext().getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        String nomeUsuario = prefs.getString("user_name", "");
        String fotoPath = prefs.getString("user_profile_pic", "");

        if (!fotoPath.isEmpty()) {
            imgPerfil.setImageURI(android.net.Uri.parse(fotoPath));
        }

        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saudacaoBase = (hora < 12) ? "Bom dia" : (hora < 18) ? "Boa tarde" : "Boa noite";
        
        if (!nomeUsuario.isEmpty()) {
            lblSaudacao.setText(saudacaoBase + ", " + nomeUsuario + "!");
        } else {
            lblSaudacao.setText(saudacaoBase + "!");
        }

        // Configuração de Data Atual
        SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        lblDataAtual.setText(sdf.format(new Date()));

        // Navegação ao clicar no status das tarefas
        layoutStatus.setOnClickListener(v -> navegarPara(new TarefasFragment()));

        // Abrir Configurações
        btnConfig.setOnClickListener(v -> navegarPara(new ConfiguracoesFragment()));

        // Abrir Menu Adicionar pelo Card de Estado Vazio
        btnComecar.setOnClickListener(v -> {
            MenuMaisBottomSheet bottomSheet = new MenuMaisBottomSheet();
            bottomSheet.show(getParentFragmentManager(), "MenuMaisBottomSheet");
        });

        // Configurar cliques nos cards de estado vazio para direcionar à criação
        cardEmptyTarefas.setOnClickListener(v -> navegarPara(new CriaTarefaFragment()));
        cardEmptyAnotacoes.setOnClickListener(v -> navegarPara(new EditorAnotacaoFragment()));
        cardEmptyMetas.setOnClickListener(v -> navegarPara(new CriaMetaFragment()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        countMetas = -1;
        countAnotacoes = -1;
        countTarefas = -1;
        countFlashcards = -1;
        carregarStatusTarefas();
        carregarMetasHome();
        carregarAnotacoesHome();
        carregarFlashcardsHome();
        carregarStreak();
    }

    private void carregarStreak() {
        Executors.newSingleThreadExecutor().execute(() -> {
            int streak = ProdutividadeManager.calcularStreak(getContext());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtStreakCount.setText(String.valueOf(streak));
                    // Efeito visual: Se o streak for 0, deixa o card mais transparente
                    cardStreak.setAlpha(streak > 0 ? 1.0f : 0.5f);
                });
            }
        });
    }

    private void carregarStatusTarefas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Tarefa> tarefas = AppDatabase.getInstance(getContext()).tarefaDao().buscarAtivas();
            int atrasadas = 0, pendentes = 0;
            long agora = System.currentTimeMillis();

            for (Tarefa t : tarefas) {
                if (t.dataLimite < agora) atrasadas++;
                else pendentes++;
            }
            
            countTarefas = tarefas.size();

            final int fAtrasadas = atrasadas;
            final int fPendentes = pendentes;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtAtrasadas.setText("Atrasadas: " + fAtrasadas);
                    txtPendentes.setText("Pendentes: " + fPendentes);
                    atualizarVisibilidadeEstadoVazio();
                });
            }
        });
    }

    private void carregarMetasHome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Meta> metas = AppDatabase.getInstance(getContext()).metaDao().buscarTodas();
            Collections.sort(metas, (m1, m2) -> Long.compare(m1.dataCriacao, m2.dataCriacao));
            
            countMetas = metas.size();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    MetaHomeAdapter adapter = new MetaHomeAdapter(metas, meta -> navegarPara(new MetasFragment()));
                    recyclerMetasHome.setAdapter(adapter);
                    atualizarVisibilidadeEstadoVazio();
                });
            }
        });
    }

    private void carregarAnotacoesHome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Anotacao> anotacoes = AppDatabase.getInstance(getContext()).anotacaoDao().buscarTodas();
            
            countAnotacoes = anotacoes.size();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    AnotacaoHomeAdapter adapter = new AnotacaoHomeAdapter(anotacoes, anotacao -> {
                        Fragment fragment;
                        if (anotacao.conteudoHtml != null && anotacao.conteudoHtml.startsWith("[")) {
                            fragment = new EditorAnotacaoFragment();
                        } else {
                            fragment = new EditorTextoFragment();
                        }
                        
                        Bundle args = new Bundle();
                        args.putSerializable("anotacao", anotacao);
                        fragment.setArguments(args);
                        navegarPara(fragment);
                    });
                    recyclerAnotacoesHome.setAdapter(adapter);
                    atualizarVisibilidadeEstadoVazio();
                });
            }
        });
    }

    private void carregarFlashcardsHome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            long hoje = System.currentTimeMillis();
            int total = AppDatabase.getInstance(getContext()).flashcardDao().buscarTodos().size();
            int paraRevisar = AppDatabase.getInstance(getContext()).flashcardDao().contarParaRevisarHoje(hoje);
            
            countFlashcards = total;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (total > 0) {
                        layoutSecaoFlashcards.setVisibility(View.VISIBLE);
                        if (paraRevisar > 0) {
                            cardRevisaoFlashcards.setVisibility(View.VISIBLE);
                            cardEmptyFlashcards.setVisibility(View.GONE);
                            txtFlashcardsCount.setText("Você tem " + paraRevisar + " cartões para estudar.");
                        } else {
                            cardRevisaoFlashcards.setVisibility(View.GONE);
                            cardEmptyFlashcards.setVisibility(View.VISIBLE);
                        }
                    } else {
                        layoutSecaoFlashcards.setVisibility(View.GONE);
                    }
                    atualizarVisibilidadeEstadoVazio();
                });
            }
        });
    }

    /**
     * Gerencia o que deve ser mostrado na Home com base na existência de dados.
     */
    private void atualizarVisibilidadeEstadoVazio() {
        // Só executa quando todos os carregamentos terminarem
        if (countMetas == -1 || countAnotacoes == -1 || countTarefas == -1 || countFlashcards == -1) return;

        if (countMetas == 0 && countAnotacoes == 0 && countTarefas == 0 && countFlashcards == 0) {
            // TUDO VAZIO: Mostra o card de boas-vindas gigante e esconde o resto
            cardEmptyState.setVisibility(View.VISIBLE);
            
            lblTituloTarefas.setVisibility(View.GONE);
            layoutStatus.setVisibility(View.GONE);
            cardEmptyTarefas.setVisibility(View.GONE);
            
            lblTituloAnotacoes.setVisibility(View.GONE);
            recyclerAnotacoesHome.setVisibility(View.GONE);
            cardEmptyAnotacoes.setVisibility(View.GONE);
            
            lblTituloMetas.setVisibility(View.GONE);
            recyclerMetasHome.setVisibility(View.GONE);
            cardEmptyMetas.setVisibility(View.GONE);
        } else {
            // TEM ALGO: Esconde o card geral e gerencia as seções individualmente
            cardEmptyState.setVisibility(View.GONE);
            
            lblTituloTarefas.setVisibility(View.VISIBLE);
            if (countTarefas > 0) {
                layoutStatus.setVisibility(View.VISIBLE);
                cardEmptyTarefas.setVisibility(View.GONE);
            } else {
                layoutStatus.setVisibility(View.GONE);
                cardEmptyTarefas.setVisibility(View.VISIBLE);
            }

            lblTituloAnotacoes.setVisibility(View.VISIBLE);
            if (countAnotacoes > 0) {
                recyclerAnotacoesHome.setVisibility(View.VISIBLE);
                cardEmptyAnotacoes.setVisibility(View.GONE);
            } else {
                recyclerAnotacoesHome.setVisibility(View.GONE);
                cardEmptyAnotacoes.setVisibility(View.VISIBLE);
            }

            lblTituloMetas.setVisibility(View.VISIBLE);
            if (countMetas > 0) {
                recyclerMetasHome.setVisibility(View.VISIBLE);
                cardEmptyMetas.setVisibility(View.GONE);
            } else {
                recyclerMetasHome.setVisibility(View.GONE);
                cardEmptyMetas.setVisibility(View.VISIBLE);
            }
        }
    }

    private void navegarPara(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
