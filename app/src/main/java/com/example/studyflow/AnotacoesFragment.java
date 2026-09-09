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

import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.AppDatabase;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Fragmento que exibe a lista de todas as anotações do usuário.
 */
public class AnotacoesFragment extends Fragment {

    private RecyclerView recyclerAnotacoes;
    private AnotacaoAdapter adapter;

    public AnotacoesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_anotacoes, container, false);

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

        recyclerAnotacoes = view.findViewById(R.id.recyclerAnotacoes);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarAnotacoes();
    }

    private void carregarAnotacoes() {
        Context context = getContext();
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        Executors.newSingleThreadExecutor().execute(() -> {
            List<Anotacao> lista = AppDatabase.getInstance(appContext).anotacaoDao().buscarTodas();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    if (adapter == null) {
                        adapter = new AnotacaoAdapter(new ArrayList<>(lista), this::abrirEditor);
                    } else {
                        adapter.setAnotacoes(new ArrayList<>(lista));
                    }
                    
                    if (recyclerAnotacoes.getAdapter() == null) {
                        recyclerAnotacoes.setAdapter(adapter);
                    }
                });
            }
        });
    }

    private void abrirEditor(Anotacao anotacao) {
        Fragment fragment;
        if (anotacao != null && anotacao.conteudoHtml != null && anotacao.conteudoHtml.startsWith("[")) {
            fragment = new EditorAnotacaoFragment();
        } else {
            fragment = new EditorTextoFragment();
        }

        if (anotacao != null) {
            Bundle args = new Bundle();
            args.putSerializable("anotacao", anotacao);
            fragment.setArguments(args);
        }

        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
