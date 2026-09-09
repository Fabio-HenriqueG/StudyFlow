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

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Meta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Fragmento que exibe a lista de todas as metas do usuário.
 */
public class MetasFragment extends Fragment {

    private RecyclerView recyclerMetas;
    private MetaAdapter adapter;

    public MetasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_metas, container, false);

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
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerMetas = view.findViewById(R.id.recyclerMetas);
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarMetas();
    }

    private void carregarMetas() {
        Context context = getContext();
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Meta> lista = AppDatabase.getInstance(appContext).metaDao().buscarTodas();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter == null) {
                        adapter = new MetaAdapter(new ArrayList<>(lista));
                    } else {
                        adapter.setMetas(new ArrayList<>(lista));
                    }
                    
                    if (recyclerMetas.getAdapter() == null) {
                        recyclerMetas.setAdapter(adapter);
                    }
                });
            }
        });
    }
}
