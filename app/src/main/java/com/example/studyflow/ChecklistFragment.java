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
import com.example.studyflow.data.Checklist;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Fragmento que exibe a lista de todos os Checklists do usuário.
 */
public class ChecklistFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChecklistAdapter adapter;

    public ChecklistFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_check_list, container, false);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        recyclerView = view.findViewById(R.id.recyclerChecklists);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarChecklists();
    }

    private void carregarChecklists() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Checklist> listas = AppDatabase.getInstance(getContext()).checklistDao().buscarTodas();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter == null) {
                        adapter = new ChecklistAdapter(new ArrayList<>(listas));
                        adapter.setOnDataChangedListener(this::carregarChecklists);
                    } else {
                        adapter.setChecklists(new ArrayList<>(listas));
                    }
                    
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
