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
    private MetaHomeAdapter metaAdapter;
    private AnotacaoHomeAdapter anotacaoAdapter;
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
        
        if (btnIniciarRevisao != null) {
            btnIniciarRevisao.setOnClickListener(v -> navegarPara(new RevisaoFlashcardsFragment()));
        }
        if (btnVerColecaoHome != null) {
            btnVerColecaoHome.setOnClickListener(v -> navegarPara(new FlashcardsFragment()));
        }
        if (cardStreak != null) {
            cardStreak.setOnClickListener(v -> navegarPara(new DashboardFragment()));
        }

        
        View btnComecar = view.findViewById(R.id.btnComecarHome);

        // Configuração de Saudação Personalizada e Foto
        SharedPreferences prefs = requireContext().getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        String nomeUsuario = prefs.getString("user_name", "");
        String fotoPath = prefs.getString("user_profile_pic", "");

        if (!fotoPath.isEmpty() && imgPerfil != null) {
            imgPerfil.setImageURI(android.net.Uri.parse(fotoPath));
        }

        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saudacaoBase = (hora < 12) ? "Bom dia" : (hora < 18) ? "Boa tarde" : "Boa noite";
        
        if (lblSaudacao != null) {
            if (!nomeUsuario.isEmpty()) {
                lblSaudacao.setText(saudacaoBase + ", " + nomeUsuario + "!");
            } else {
                lblSaudacao.setText(saudacaoBase + "!");
            }
        }

        // Configuração de Data Atual
        if (lblDataAtual != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
            lblDataAtual.setText(sdf.format(new Date()));
        }

        // Navegação ao clicar no status das tarefas
        if (layoutStatus != null) {
            layoutStatus.setOnClickListener(v -> navegarPara(new TarefasFragment()));
        }

        // Abrir Configurações
        if (btnConfig != null) {
            btnConfig.setOnClickListener(v -> navegarPara(new ConfiguracoesFragment()));
        }

        // Abrir Menu Adicionar pelo Card de Estado Vazio
        if (btnComecar != null) {
            btnComecar.setOnClickListener(v -> {
                MenuMaisBottomSheet bottomSheet = new MenuMaisBottomSheet();
                bottomSheet.show(getParentFragmentManager(), "MenuMaisBottomSheet");
            });
        }

        // Configurar cliques nos cards de estado vazio para direcionar à criação
        if (cardEmptyTarefas != null) cardEmptyTarefas.setOnClickListener(v -> navegarPara(new CriaTarefaFragment()));
        if (cardEmptyAnotacoes != null) cardEmptyAnotacoes.setOnClickListener(v -> navegarPara(new EditorAnotacaoFragment()));
        if (cardEmptyMetas != null) cardEmptyMetas.setOnClickListener(v -> navegarPara(new CriaMetaFragment()));

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
                    if (txtStreakCount != null) txtStreakCount.setText(String.valueOf(streak));
                    // Efeito visual: Se o streak for 0, deixa o card mais transparente
                    if (cardStreak != null) cardStreak.setAlpha(streak > 0 ? 1.0f : 0.5f);
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
                    if (txtAtrasadas != null) txtAtrasadas.setText("Atrasadas: " + fAtrasadas);
                    if (txtPendentes != null) txtPendentes.setText("Pendentes: " + fPendentes);
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
                    if (metaAdapter == null) {
                        metaAdapter = new MetaHomeAdapter(metas, meta -> navegarPara(new MetasFragment()));
                    } else {
                        metaAdapter.setMetas(metas);
                    }
                    
                    if (recyclerMetasHome != null && recyclerMetasHome.getAdapter() == null) {
                        recyclerMetasHome.setAdapter(metaAdapter);
                    }
                    
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
                    if (anotacaoAdapter == null) {
                        anotacaoAdapter = new AnotacaoHomeAdapter(anotacoes, anotacao -> {
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
                    } else {
                        anotacaoAdapter.setAnotacoes(anotacoes);
                    }
                    
                    if (recyclerAnotacoesHome != null && recyclerAnotacoesHome.getAdapter() == null) {
                        recyclerAnotacoesHome.setAdapter(anotacaoAdapter);
                    }
                    
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
                    if (layoutSecaoFlashcards != null) {
                        if (total > 0) {
                            layoutSecaoFlashcards.setVisibility(View.VISIBLE);
                            if (cardRevisaoFlashcards != null) {
                                if (paraRevisar > 0) {
                                    cardRevisaoFlashcards.setVisibility(View.VISIBLE);
                                    if (cardEmptyFlashcards != null) cardEmptyFlashcards.setVisibility(View.GONE);
                                    if (txtFlashcardsCount != null) txtFlashcardsCount.setText("Você tem " + paraRevisar + " cartões para estudar.");
                                } else {
                                    cardRevisaoFlashcards.setVisibility(View.GONE);
                                    if (cardEmptyFlashcards != null) cardEmptyFlashcards.setVisibility(View.VISIBLE);
                                }
                            }
                        } else {
                            layoutSecaoFlashcards.setVisibility(View.GONE);
                        }
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
            if (cardEmptyState != null) cardEmptyState.setVisibility(View.VISIBLE);
            
            if (lblTituloTarefas != null) lblTituloTarefas.setVisibility(View.GONE);
            if (layoutStatus != null) layoutStatus.setVisibility(View.GONE);
            if (cardEmptyTarefas != null) cardEmptyTarefas.setVisibility(View.GONE);
            
            if (lblTituloAnotacoes != null) lblTituloAnotacoes.setVisibility(View.GONE);
            if (recyclerAnotacoesHome != null) recyclerAnotacoesHome.setVisibility(View.GONE);
            if (cardEmptyAnotacoes != null) cardEmptyAnotacoes.setVisibility(View.GONE);
            
            if (lblTituloMetas != null) lblTituloMetas.setVisibility(View.GONE);
            if (recyclerMetasHome != null) recyclerMetasHome.setVisibility(View.GONE);
            if (cardEmptyMetas != null) cardEmptyMetas.setVisibility(View.GONE);
        } else {
            // TEM ALGO: Esconde o card geral e gerencia as seções individualmente
            if (cardEmptyState != null) cardEmptyState.setVisibility(View.GONE);
            
            if (lblTituloTarefas != null) lblTituloTarefas.setVisibility(View.VISIBLE);
            if (countTarefas > 0) {
                if (layoutStatus != null) layoutStatus.setVisibility(View.VISIBLE);
                if (cardEmptyTarefas != null) cardEmptyTarefas.setVisibility(View.GONE);
            } else {
                if (layoutStatus != null) layoutStatus.setVisibility(View.GONE);
                if (cardEmptyTarefas != null) cardEmptyTarefas.setVisibility(View.VISIBLE);
            }

            if (lblTituloAnotacoes != null) lblTituloAnotacoes.setVisibility(View.VISIBLE);
            if (countAnotacoes > 0) {
                if (recyclerAnotacoesHome != null) recyclerAnotacoesHome.setVisibility(View.VISIBLE);
                if (cardEmptyAnotacoes != null) cardEmptyAnotacoes.setVisibility(View.GONE);
            } else {
                if (recyclerAnotacoesHome != null) recyclerAnotacoesHome.setVisibility(View.GONE);
                if (cardEmptyAnotacoes != null) cardEmptyAnotacoes.setVisibility(View.VISIBLE);
            }

            if (lblTituloMetas != null) lblTituloMetas.setVisibility(View.VISIBLE);
            if (countMetas > 0) {
                if (recyclerMetasHome != null) recyclerMetasHome.setVisibility(View.VISIBLE);
                if (cardEmptyMetas != null) cardEmptyMetas.setVisibility(View.GONE);
            } else {
                if (recyclerMetasHome != null) recyclerMetasHome.setVisibility(View.GONE);
                if (cardEmptyMetas != null) cardEmptyMetas.setVisibility(View.VISIBLE);
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
