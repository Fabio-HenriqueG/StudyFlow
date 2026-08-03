package com.example.studyflow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

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

import java.util.List;
import java.util.concurrent.Executors;

/**
 * Fragmento que exibe a lista de todas as anotações do usuário.
 */
public class AnotacoesFragment extends Fragment {

    private RecyclerView recyclerAnotacoes;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public AnotacoesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AnotacoesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AnotacoesFragment newInstance(String param1, String param2) {
        AnotacoesFragment fragment = new AnotacoesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_anotacoes, container, false);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltar);
        btnVoltar.setOnClickListener(v -> {
            // Volta para o fragmento anterior se existir na pilha, ou volta para Home
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
        // Recarrega a lista sempre que voltamos para esta tela
        carregarAnotacoes();
    }

    /**
     * Busca as anotações no banco de dados Room.
     */
    private void carregarAnotacoes() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Anotacao> lista = AppDatabase.getInstance(getContext()).anotacaoDao().buscarTodas();
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    // Configura o Adapter com o clique para abrir o editor
                    AnotacaoAdapter adapter = new AnotacaoAdapter(lista, anotacao -> {
                        abrirEditor(anotacao);
                    });
                    recyclerAnotacoes.setAdapter(adapter);
                });
            }
        });
    }

    /**
     * Abre o fragmento do editor enviando a anotação selecionada.
     */
    private void abrirEditor(Anotacao anotacao) {
        EditorAnotacaoFragment fragment = new EditorAnotacaoFragment();
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
