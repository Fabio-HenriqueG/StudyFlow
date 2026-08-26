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
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

public class EstatisticasFlashcardsFragment extends Fragment {

    private LinearProgressIndicator progressGeral;
    private TextView txtPercentualGeral, txtQtdDominados, txtQtdAprendendo;
    private LinearLayout layoutMaterias, layoutCargaSemana;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_estatisticas_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        progressGeral = view.findViewById(R.id.progressGeral);
        txtPercentualGeral = view.findViewById(R.id.txtPercentualGeral);
        txtQtdDominados = view.findViewById(R.id.txtQtdDominados);
        txtQtdAprendendo = view.findViewById(R.id.txtQtdAprendendo);
        layoutMaterias = view.findViewById(R.id.layoutMateriasStats);
        layoutCargaSemana = view.findViewById(R.id.layoutCargaSemana);
        
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarStats);
        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        carregarDadosEstatisticos();
    }

    private void carregarDadosEstatisticos() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                AppDatabase db = AppDatabase.getInstance(getContext());
                int total = db.flashcardDao().contarTotal();
                int dominados = db.flashcardDao().contarDominados();
                int aprendendo = db.flashcardDao().contarAprendendo();
                List<Materia> materias = db.materiaDao().buscarTodas();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        // 1. Update Geral
                        if (total > 0) {
                            int percent = (dominados * 100) / total;
                            progressGeral.setProgress(percent);
                            txtPercentualGeral.setText(percent + "%");
                        } else {
                            progressGeral.setProgress(0);
                            txtPercentualGeral.setText("0%");
                        }
                        txtQtdDominados.setText("🔥 " + dominados + " Dominados");
                        txtQtdAprendendo.setText("📖 " + aprendendo + " Estudando");

                        // 2. Update Matérias
                        layoutMaterias.removeAllViews();
                        for (Materia m : materias) {
                            adicionarBarraMateria(db, m);
                        }

                        // 3. Update Carga Semana
                        carregarPrevisaoCarga(db);
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    private void carregarPrevisaoCarga(AppDatabase db) {
        Executors.newSingleThreadExecutor().execute(() -> {
            int[] carga = new int[7];
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            int maxCarga = 1;
            for (int i = 0; i < 7; i++) {
                long inicio = cal.getTimeInMillis();
                cal.add(Calendar.DAY_OF_YEAR, 1);
                long fim = cal.getTimeInMillis();
                
                carga[i] = db.flashcardDao().contarPorIntervalo(inicio, fim);
                if (carga[i] > maxCarga) maxCarga = carga[i];
            }

            final int fMax = maxCarga;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    layoutCargaSemana.removeAllViews();
                    for (int c : carga) {
                        View bar = new View(getContext());
                        int heightPx = (int) ((c / (float) fMax) * 120 * getResources().getDisplayMetrics().density);
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, Math.max(10, heightPx));
                        lp.weight = 1;
                        lp.setMargins(8, 0, 8, 0);
                        bar.setLayoutParams(lp);
                        bar.setBackgroundResource(R.drawable.bar_indicator);
                        layoutCargaSemana.addView(bar);

                    }
                });
            }
        });
    }
}
