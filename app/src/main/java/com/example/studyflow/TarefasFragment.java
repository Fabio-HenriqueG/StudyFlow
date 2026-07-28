package com.example.studyflow;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Tarefa;

import java.util.List;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TarefasFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TarefasFragment extends Fragment {

    private RecyclerView recyclerTarefas;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public TarefasFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TarefasFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TarefasFragment newInstance(String param1, String param2) {
        TarefasFragment fragment = new TarefasFragment();
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
        View view = inflater.inflate(R.layout.fragment_tarefas, container, false);

        ImageButton btnVoltar = view.findViewById(R.id.btn_voltar);
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
        return view;

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Vincula o componente Java ao ID do RecyclerView que você colocou no XML
        recyclerTarefas = view.findViewById(R.id.recyclerTarefas);
    }

    @Override
    public void onResume() {
        super.onResume();
        // 2. Toda vez que o usuário abrir o app ou voltar da tela de criação, atualiza a lista
        carregarTarefasDoBanco();
    }

    private void carregarTarefasDoBanco() {
        // Capturamos o contexto antes de abrir a Thread para ser seguro
        Context context = getContext();
        if (context == null) return;
        Context appContext = context.getApplicationContext();

        // 3. Busca no banco em segundo plano (Thread separada) para o app não travar
        Executors.newSingleThreadExecutor().execute(() -> {

            // Puxa a lista de tarefas do Room usando o contexto seguro
            List<Tarefa> listaDoBanco = AppDatabase.getInstance(appContext).tarefaDao().buscarTodas();

            // 4. Volta para a Main Thread (linha principal) para desenhar na tela do celular
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {

                    // CHAMA O SEU ADAPTER PRONTO!
                    // Passamos a lista do banco para o construtor do seu TarefaAdapter
                    TarefaAdapter adapter = new TarefaAdapter(listaDoBanco);

                    // Conecta o seu adapter ao RecyclerView da tela
                    recyclerTarefas.setAdapter(adapter);

                });
            }
        });
    }
}
