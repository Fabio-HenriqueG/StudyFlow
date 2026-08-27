package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.studyflow.data.AppDatabase;
import com.google.android.material.button.MaterialButtonToggleGroup;
import java.util.Calendar;
import java.util.concurrent.Executors;

public class DashboardFragment extends Fragment {

    private TextView txtTarefas, txtMetas, txtChecklists, txtFlashcards, txtMaterias;
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
        txtChecklists = view.findViewById(R.id.txtCountChecklists);
        txtFlashcards = view.findViewById(R.id.txtCountFlashcards);
        txtMaterias = view.findViewById(R.id.txtMateriasEstudadas);
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
            int c = db.atividadeLogDao().contarPorTipoEPeriodo("CHECKLIST", fInicio, fFim);
            int f = db.atividadeLogDao().contarPorTipoEPeriodo("FLASHCARD", fInicio, fFim);
            int mat = db.atividadeLogDao().contarMateriasEstudadas(fInicio, fFim);

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtTarefas.setText(String.valueOf(t));
                    txtMetas.setText(String.valueOf(m));
                    txtChecklists.setText(String.valueOf(c));
                    txtFlashcards.setText(String.valueOf(f));
                    txtMaterias.setText("Você focou em " + mat + " disciplinas neste período.");
                });
            }
        });
    }
}
