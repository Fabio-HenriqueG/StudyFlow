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
import com.example.studyflow.data.Materia;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FlashcardsFragment extends Fragment {

    private RecyclerView recycler;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recycler = view.findViewById(R.id.recyclerListaFlashcards);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarListaFlash);
        ImageButton btnStats = view.findViewById(R.id.btnVerStats);
        
        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        btnStats.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new EstatisticasFlashcardsFragment())
                    .addToBackStack(null)
                    .commit();
        });
        
        carregarMaterias();
    }


    private void carregarMaterias() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Materia> materias = AppDatabase.getInstance(getContext()).materiaDao().buscarTodas();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    MateriaAdapter adapter = new MateriaAdapter(materias, m -> {
                        FlashcardListaFragment fragment = new FlashcardListaFragment();
                        Bundle args = new Bundle();
                        args.putSerializable("materia", m);
                        fragment.setArguments(args);
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, fragment)
                                .addToBackStack(null)
                                .commit();
                    });
                    recycler.setAdapter(adapter);
                });
            }
        });
    }
}
