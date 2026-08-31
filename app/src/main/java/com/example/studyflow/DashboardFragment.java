package com.example.studyflow;

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
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Materia;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private TextView txtTarefas, txtMetas, txtFlashcards, txtMaterias, txtPercentualFlash;
    private LinearProgressIndicator progressFlash;
    private LinearLayout layoutCargaSemana, layoutCargaTarefas, layoutCargaMetas, layoutMaterias;
    private MaterialButtonToggleGroup toggleGroup;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarDashboard);
        txtTarefas = view.findViewById(R.id.txtCountTarefas);
        txtMetas = view.findViewById(R.id.txtCountMetas);
        txtFlashcards = view.findViewById(R.id.txtCountFlashcards);
        txtPercentualFlash = view.findViewById(R.id.txtPercentualFlash);
        progressFlash = view.findViewById(R.id.progressGeralFlash);

        txtMaterias = view.findViewById(R.id.txtMateriasEstudadas);
        layoutCargaSemana = view.findViewById(R.id.layoutCargaSemanaGeral);
        layoutCargaTarefas = view.findViewById(R.id.layoutCargaTarefas);
        layoutCargaMetas = view.findViewById(R.id.layoutCargaMetas);
        layoutMaterias = view.findViewById(R.id.layoutMateriasDashboard);
        toggleGroup = view.findViewById(R.id.toggleGroupPeriodo);


        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());


        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                carregarDadosPorPeriodo(checkedId);
            }
        });

        // Padrão: Hoje
        toggleGroup.check(R.id.btnPeriodoHoje);
        carregarDadosPorPeriodo(R.id.btnPeriodoHoje);
    }

    private void carregarDadosPorPeriodo(int buttonId) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        long inicio, fim = System.currentTimeMillis();

        if (buttonId == R.id.btnPeriodoHoje) {
            inicio = cal.getTimeInMillis();
        } else if (buttonId == R.id.btnPeriodoOntem) {
            fim = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            inicio = cal.getTimeInMillis();
        } else if (buttonId == R.id.btnPeriodoSemana) {
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
            inicio = cal.getTimeInMillis();
        } else { // Mês
            cal.set(Calendar.DAY_OF_MONTH, 1);
            inicio = cal.getTimeInMillis();
        }

        final long fInicio = inicio;
        final long fFim = fim;

        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase db = AppDatabase.getInstance(getContext());
            int t = db.atividadeLogDao().contarPorTipoEPeriodo("TAREFA", fInicio, fFim);
            int m = db.atividadeLogDao().contarPorTipoEPeriodo("META", fInicio, fFim);
            int f = db.atividadeLogDao().contarPorTipoEPeriodo("FLASHCARD", fInicio, fFim);
            int mat = db.atividadeLogDao().contarMateriasEstudadas(fInicio, fFim);

            // Flashcard Stats (Domínio)
            int flashTotal = db.flashcardDao().contarTotal();
            int flashDominados = db.flashcardDao().contarDominados();
            List<Materia> materias = db.materiaDao().buscarTodas();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtTarefas.setText(String.valueOf(t));
                    txtMetas.setText(String.valueOf(m));
                    txtFlashcards.setText(String.valueOf(f));
                    txtMaterias.setText("Você focou em " + mat + " disciplinas neste período.");

                    if (flashTotal > 0) {
                        int percent = (flashDominados * 100) / flashTotal;
                        progressFlash.setProgress(percent);
                        txtPercentualFlash.setText(percent + "%");
                    } else {
                        progressFlash.setProgress(0);
                        txtPercentualFlash.setText("0%");
                    }

                    // Gráfico de Carga 7 dias (Geral)
                    carregarGraficoSeteDias(db);

                    // Detalhe Matérias
                    layoutMaterias.removeAllViews();
                    for (Materia ma : materias) {
                        adicionarBarraMateria(db, ma);
                    }
                });
            }
        });
    }

    private void carregarGraficoSeteDias(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            int[] cargaGeral = new int[7];
            int[] cargaTarefas = new int[7];
            int[] cargaMetas = new int[7];
            
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            cal.add(Calendar.DAY_OF_YEAR, -6);

            int maxGeral = 1, maxTarefas = 1, maxMetas = 1;
            
            for (int i = 0; i < 7; i++) {
                long dInicio = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_YEAR, 1);
                long dFim = cal.getTimeInMillis();
                
                cargaTarefas[i] = db.atividadeLogDao().contarPorTipoEPeriodo("TAREFA", dInicio, dFim);
                cargaMetas[i] = db.atividadeLogDao().contarPorTipoEPeriodo("META", dInicio, dFim);
                
                cargaGeral[i] = cargaTarefas[i] + cargaMetas[i] +
                               db.atividadeLogDao().contarPorTipoEPeriodo("CHECKLIST", dInicio, dFim) +
                               db.atividadeLogDao().contarPorTipoEPeriodo("FLASHCARD", dInicio, dFim);
                
                if (cargaGeral[i] > maxGeral) maxGeral = cargaGeral[i];
                if (cargaTarefas[i] > maxTarefas) maxTarefas = cargaTarefas[i];
                if (cargaMetas[i] > maxMetas) maxMetas = cargaMetas[i];
            }

            final int fMaxG = maxGeral, fMaxT = maxTarefas, fMaxM = maxMetas;
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    povoarLayoutBarras(layoutCargaSemana, cargaGeral, fMaxG, 100);
                    povoarLayoutBarras(layoutCargaTarefas, cargaTarefas, fMaxT, 50);
                    povoarLayoutBarras(layoutCargaMetas, cargaMetas, fMaxM, 50);
                });
            }
        });
    }

    private void povoarLayoutBarras(LinearLayout layout, int[] dados, int max, int maxHeightDp) {
        layout.removeAllViews();
        for (int c : dados) {
            View bar = new View(getContext());
            int heightPx = (int) ((c / (float) max) * maxHeightDp * getResources().getDisplayMetrics().density);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Math.max(8, heightPx));
            lp.weight = 1;
            lp.setMargins(8, 0, 8, 0);
            bar.setLayoutParams(lp);
            bar.setBackgroundResource(R.drawable.bar_indicator);
            layout.addView(bar);
        }
    }


    private void adicionarBarraMateria(AppDatabase db, Materia m) {
        Executors.newSingleThreadExecutor().execute(() -> {
            int totalM = db.flashcardDao().buscarPorMateria(m.id).size();
            int dominadosM = db.flashcardDao().contarDominadosPorMateria(m.id);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    View item = getLayoutInflater().inflate(R.layout.item_stats_materia, null);
                    TextView nome = item.findViewById(R.id.txtNomeMateriaStats);
                    LinearProgressIndicator bar = item.findViewById(R.id.progressMateriaStats);
                    TextView info = item.findViewById(R.id.txtInfoMateriaStats);

                    nome.setText(m.nome);
                    if (totalM > 0) {
                        int p = (dominadosM * 100) / totalM;
                        bar.setProgress(p);
                        bar.setIndicatorColor(m.cor);
                        info.setText(p + "% concluído (" + totalM + " cards)");
                    } else {
                        bar.setProgress(0);
                        info.setText("Nenhum card criado");
                    }
                    layoutMaterias.addView(item);
                });
            }
        });
    }

}
