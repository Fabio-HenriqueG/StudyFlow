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
import com.example.studyflow.data.AtividadeLog;
import com.example.studyflow.data.FirestoreService;
import com.example.studyflow.data.Flashcard;
import com.example.studyflow.data.Materia;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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

        FirestoreService.getInstance().getUserDoc().collection("atividades")
            .whereGreaterThanOrEqualTo("dataMillis", fInicio)
            .whereLessThanOrEqualTo("dataMillis", fFim)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int t = 0, m = 0, f = 0, c = 0;
                Set<String> materiasIds = new HashSet<>();
                
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    AtividadeLog log = doc.toObject(AtividadeLog.class);
                    if ("TAREFA".equals(log.tipo)) t++;
                    else if ("META".equals(log.tipo)) m++;
                    else if ("FLASHCARD".equals(log.tipo)) f++;
                    else if ("CHECKLIST".equals(log.tipo)) c++;
                    
                    if (log.materiaId != null && !log.materiaId.isEmpty()) {
                        materiasIds.add(log.materiaId);
                    }
                }

                final int ft = t, fm = m, ff = f, fmat = materiasIds.size();
                
                // Fetch Flashcard Stats separately
                FirestoreService.getInstance().getFlashcardsRef().get().addOnSuccessListener(flashSnap -> {
                    List<Flashcard> allCards = flashSnap.toObjects(Flashcard.class);
                    int total = allCards.size();
                    int dominados = 0;
                    for (Flashcard fc : allCards) if (fc.nivelDominio >= 5) dominados++;

                    if (getActivity() != null) {
                        txtTarefas.setText(String.valueOf(ft));
                        txtMetas.setText(String.valueOf(fm));
                        txtFlashcards.setText(String.valueOf(ff));
                        txtMaterias.setText("Você focou em " + fmat + " disciplinas neste período.");

                        if (total > 0) {
                            int percent = (dominados * 100) / total;
                            progressFlash.setProgress(percent);
                            txtPercentualFlash.setText(percent + "%");
                        } else {
                            progressFlash.setProgress(0);
                            txtPercentualFlash.setText("0%");
                        }
                    }
                });

                // Update Chart
                carregarGraficoSeteDias();

                // Detalhe Matérias
                FirestoreService.getInstance().buscarMaterias().addOnSuccessListener(matSnap -> {
                    layoutMaterias.removeAllViews();
                    for (Materia mat : matSnap.toObjects(Materia.class)) {
                        adicionarBarraMateria(mat);
                    }
                });
            });
    }

    private void carregarGraficoSeteDias() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_YEAR, -6);
        long inicioSemana = cal.getTimeInMillis();

        FirestoreService.getInstance().getUserDoc().collection("atividades")
            .whereGreaterThanOrEqualTo("dataMillis", inicioSemana)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                int[] cargaGeral = new int[7];
                int[] cargaTarefas = new int[7];
                int[] cargaMetas = new int[7];
                int maxG = 1, maxT = 1, maxM = 1;

                for (QueryDocumentSnapshot doc : querySnapshot) {
                    AtividadeLog log = doc.toObject(AtividadeLog.class);
                    long diaRelativo = (log.dataMillis - inicioSemana) / (24 * 60 * 60 * 1000);
                    int idx = (int) diaRelativo;
                    if (idx >= 0 && idx < 7) {
                        cargaGeral[idx]++;
                        if ("TAREFA".equals(log.tipo)) cargaTarefas[idx]++;
                        else if ("META".equals(log.tipo)) cargaMetas[idx]++;
                    }
                }

                for (int i = 0; i < 7; i++) {
                    if (cargaGeral[i] > maxG) maxG = cargaGeral[i];
                    if (cargaTarefas[i] > maxT) maxT = cargaTarefas[i];
                    if (cargaMetas[i] > maxM) maxM = cargaMetas[i];
                }

                final int fmg = maxG, fmt = maxT, fmm = maxM;
                if (getActivity() != null) {
                    povoarLayoutBarras(layoutCargaSemana, cargaGeral, fmg, 100);
                    povoarLayoutBarras(layoutCargaTarefas, cargaTarefas, fmt, 50);
                    povoarLayoutBarras(layoutCargaMetas, cargaMetas, fmm, 50);
                }
            });
    }

    private void adicionarBarraMateria(Materia m) {
        FirestoreService.getInstance().buscarFlashcardsPorMateria(m.id).addOnSuccessListener(snap -> {
            List<Flashcard> cards = snap.toObjects(Flashcard.class);
            int total = cards.size();
            int dominados = 0;
            for (Flashcard f : cards) if (f.nivelDominio >= 5) dominados++;

            if (getActivity() != null) {
                View item = getLayoutInflater().inflate(R.layout.item_stats_materia, null);
                TextView nome = item.findViewById(R.id.txtNomeMateriaStats);
                LinearProgressIndicator bar = item.findViewById(R.id.progressMateriaStats);
                TextView info = item.findViewById(R.id.txtInfoMateriaStats);

                nome.setText(m.nome);
                if (total > 0) {
                    int p = (dominados * 100) / total;
                    bar.setProgress(p);
                    bar.setIndicatorColor(m.cor);
                    info.setText(p + "% concluído (" + total + " cards)");
                } else {
                    bar.setProgress(0);
                    info.setText("Nenhum card criado");
                }
                layoutMaterias.addView(item);
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
}
