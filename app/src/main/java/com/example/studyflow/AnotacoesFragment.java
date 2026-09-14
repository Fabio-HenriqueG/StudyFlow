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
import com.example.studyflow.data.FirebaseHelper;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

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
        FirebaseHelper.getAnotacoesRef()
                .orderBy("dataUltimaEdicao", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) return;
                    
                    List<Anotacao> lista = new ArrayList<>();
                    if (value != null) {
                        for (QueryDocumentSnapshot doc : value) {
                            Anotacao a = doc.toObject(Anotacao.class);
                            a.id = doc.getId();
                            lista.add(a);
                        }
                    }

                    if (adapter == null) {
                        adapter = new AnotacaoAdapter(new ArrayList<>(lista), this::abrirEditor);
                        recyclerAnotacoes.setAdapter(adapter);
                    } else {
                        adapter.setAnotacoes(new ArrayList<>(lista));
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
