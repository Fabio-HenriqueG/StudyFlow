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
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class CalendarioTarefasFragment extends Fragment {

    private RecyclerView recyclerCalendarDays, recyclerTarefasDia;
    private TextView txtCurrentMonth;
    private List<Tarefa> todasTarefas = new ArrayList<>();
    private Calendar currentCalendar = Calendar.getInstance();
    private long dataSelecionada;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendario_tarefas, container, false);

        txtCurrentMonth = view.findViewById(R.id.txtCurrentMonth);
        recyclerCalendarDays = view.findViewById(R.id.recyclerCalendarDays);
        recyclerTarefasDia = view.findViewById(R.id.recyclerTarefasDia);
        ImageButton btnVoltar = view.findViewById(R.id.btn_voltar_calendario);
        ImageButton btnPrev = view.findViewById(R.id.btnPrevMonth);
        ImageButton btnNext = view.findViewById(R.id.btnNextMonth);

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        // Inicializa com a data de hoje e normaliza para o dia 1 para evitar bugs de rollover
        currentCalendar = Calendar.getInstance();
        currentCalendar.set(Calendar.DAY_OF_MONTH, 1);
        currentCalendar.set(Calendar.HOUR_OF_DAY, 0);
        currentCalendar.set(Calendar.MINUTE, 0);
        currentCalendar.set(Calendar.SECOND, 0);
        currentCalendar.set(Calendar.MILLISECOND, 0);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        dataSelecionada = today.getTimeInMillis();

        btnPrev.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, -1);
            setupCalendar();
        });

        btnNext.setOnClickListener(v -> {
            currentCalendar.add(Calendar.MONTH, 1);
            setupCalendar();
        });

        carregarTodasTarefas();

        return view;
    }

    private void carregarTodasTarefas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            todasTarefas = AppDatabase.getInstance(getContext()).tarefaDao().buscarAtivas();
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::setupCalendar);
            }
        });
    }


    private void setupCalendar() {
        currentCalendar.setFirstDayOfWeek(Calendar.SUNDAY);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", new Locale("pt", "BR"));
        String monthName = sdf.format(currentCalendar.getTime());
        txtCurrentMonth.setText(monthName.substring(0, 1).toUpperCase() + monthName.substring(1));

        List<CalendarAdapter.CalendarDay> days = new ArrayList<>();
        Calendar cal = (Calendar) currentCalendar.clone();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Identifica quantos dias voltar para chegar no Domingo anterior (ou no próprio dia se for Domingo)
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        int daysToSubtract = dayOfWeek - Calendar.SUNDAY;
        cal.add(Calendar.DAY_OF_MONTH, -daysToSubtract);

        Calendar calSel = Calendar.getInstance();
        calSel.setTimeInMillis(dataSelecionada);

        for (int i = 0; i < 42; i++) {
            boolean isSameMonth = cal.get(Calendar.MONTH) == currentCalendar.get(Calendar.MONTH) &&
                                 cal.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR);
            
            boolean hasTasks = verificarTarefaNoDia(cal);
            
            boolean isSelected = (cal.get(Calendar.YEAR) == calSel.get(Calendar.YEAR) &&
                                 cal.get(Calendar.DAY_OF_YEAR) == calSel.get(Calendar.DAY_OF_YEAR));

            days.add(new CalendarAdapter.CalendarDay((Calendar) cal.clone(), isSameMonth, hasTasks, isSelected));
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }

        CalendarAdapter adapter = new CalendarAdapter(days, date -> {
            // Normaliza a data selecionada para o início do dia
            Calendar normalized = (Calendar) date.clone();
            normalized.set(Calendar.HOUR_OF_DAY, 0);
            normalized.set(Calendar.MINUTE, 0);
            normalized.set(Calendar.SECOND, 0);
            normalized.set(Calendar.MILLISECOND, 0);
            dataSelecionada = normalized.getTimeInMillis();
            
            setupCalendar(); 
            filtrarTarefasPorData();
        });
        recyclerCalendarDays.setAdapter(adapter);
    }



    private boolean verificarTarefaNoDia(Calendar cal) {
        int ano = cal.get(Calendar.YEAR);
        int dia = cal.get(Calendar.DAY_OF_YEAR);
        for (Tarefa t : todasTarefas) {
            Calendar cT = Calendar.getInstance();
            cT.setTimeInMillis(t.dataLimite);
            if (cT.get(Calendar.YEAR) == ano && cT.get(Calendar.DAY_OF_YEAR) == dia) {
                return true;
            }
        }
        return false;
    }

    private void filtrarTarefasPorData() {
        List<Tarefa> filtradas = new ArrayList<>();
        Calendar calSel = Calendar.getInstance();
        calSel.setTimeInMillis(dataSelecionada);
        int ano = calSel.get(Calendar.YEAR);
        int dia = calSel.get(Calendar.DAY_OF_YEAR);

        for (Tarefa t : todasTarefas) {
            Calendar calTarefa = Calendar.getInstance();
            calTarefa.setTimeInMillis(t.dataLimite);
            if (calTarefa.get(Calendar.YEAR) == ano && calTarefa.get(Calendar.DAY_OF_YEAR) == dia) {
                filtradas.add(t);
            }
        }

        TarefaAdapter adapter = new TarefaAdapter(filtradas);
        recyclerTarefasDia.setAdapter(adapter);
    }
}
