package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class HistoricoTarefasFragment extends Fragment {

    private RecyclerView recyclerHistorico;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_historico_tarefas, container, false);

        recyclerHistorico = view.findViewById(R.id.recyclerHistorico);
        ImageButton btnVoltar = view.findViewById(R.id.btn_voltar_historico);

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        carregarHistorico();

        return view;
    }

    private void carregarHistorico() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Tarefa> historico = AppDatabase.getInstance(getContext()).tarefaDao().buscarNoHistorico();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    TarefaAdapter adapter = new TarefaAdapter(new ArrayList<>(historico), true);
                    recyclerHistorico.setAdapter(adapter);
                });
            }
        });
    }
}
