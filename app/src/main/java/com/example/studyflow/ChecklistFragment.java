package com.example.studyflow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.Checklist;
import com.example.studyflow.data.FirestoreService;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragmento que exibe a lista de todos os Checklists do usuário.
 */
public class ChecklistFragment extends Fragment {

    private RecyclerView recyclerView;
    private ChecklistAdapter adapter;
    private View layoutEmptyChecklists;

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
        layoutEmptyChecklists = view.findViewById(R.id.layoutEmptyChecklists);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarChecklists();
    }

    private void carregarChecklists() {
        FirestoreService.getInstance().buscarChecklists()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Checklist> listas = queryDocumentSnapshots.toObjects(Checklist.class);
                if (getActivity() != null) {
                    if (layoutEmptyChecklists != null) {
                        layoutEmptyChecklists.setVisibility(listas.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    if (adapter == null) {
                        adapter = new ChecklistAdapter(new ArrayList<>(listas));
                        adapter.setOnDataChangedListener(this::carregarChecklists);
                    } else {
                        adapter.setChecklists(new ArrayList<>(listas));
                    }
                    
                    if (recyclerView.getAdapter() == null) {
                        recyclerView.setAdapter(adapter);
                    }
                }
            });
    }
}
