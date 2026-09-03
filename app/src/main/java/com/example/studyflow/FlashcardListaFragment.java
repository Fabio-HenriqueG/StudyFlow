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
import com.example.studyflow.data.Flashcard;
import com.example.studyflow.data.Materia;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class FlashcardListaFragment extends Fragment {

    private RecyclerView recycler;
    private FlashcardAdapter adapter;
    private Materia materia;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_lista_flashcards, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        if (getArguments() != null) {
            materia = (Materia) getArguments().getSerializable("materia");
        }

        TextView lblTitulo = view.findViewById(R.id.lblTituloListaFlash);
        if (materia != null) lblTitulo.setText(materia.nome);

        recycler = view.findViewById(R.id.recyclerListaFlashcards);
        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarListaFlash);
        ImageButton btnStats = view.findViewById(R.id.btnVerStats);

        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        
        btnStats.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new DashboardFragment())
                    .addToBackStack(null)
                    .commit();
        });
        
        carregarFlashcards();
    }

    private void carregarFlashcards() {
        if (materia == null) return;
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Flashcard> cards = AppDatabase.getInstance(getContext()).flashcardDao().buscarPorMateria(materia.id);
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter == null) {
                        adapter = new FlashcardAdapter(new ArrayList<>(cards), f -> {
                            CriaFlashcardFragment fragment = new CriaFlashcardFragment();
                            Bundle args = new Bundle();
                            args.putSerializable("flashcard_editar", f);
                            fragment.setArguments(args);
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, fragment)
                                    .addToBackStack(null)
                                    .commit();
                        });
                    } else {
                        adapter.setFlashcards(new ArrayList<>(cards));
                    }
                    
                    if (recycler.getAdapter() == null) {
                        recycler.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
