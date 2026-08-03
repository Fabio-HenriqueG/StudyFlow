package com.example.studyflow;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Meta;
import com.example.studyflow.data.Tarefa;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    private TextView txtAtrasadas, txtPendentes;
    private RecyclerView recyclerMetasHome;

    private LinearLayout layoutStatus;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);


        TextView lblSaudacao = view.findViewById(R.id.lblSaudacao);
        TextView lblDataAtual = view.findViewById(R.id.lblDataAtual);
        txtAtrasadas = view.findViewById(R.id.text_home_atrasadas);
        txtPendentes = view.findViewById(R.id.text_home_pendentes);
        recyclerMetasHome = view.findViewById(R.id.recycler_metas_home);
        layoutStatus = view.findViewById(R.id.layoutStatus);

        //Função para retornar bom dia/tarde/noite com base na hora (saudação)
        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saudacao;
        if (hora < 12) {
            saudacao = "Bom dia!";
        } else if (hora < 18) {
            saudacao = "Boa tarde!";
        } else {
            saudacao = "Boa noite!";
        }
        lblSaudacao.setText(saudacao);

        // Exibe a data atual formatada (Ex: 3 de Agosto de 2026)
        SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        String dataFormatada = sdf.format(new Date());
        lblDataAtual.setText(dataFormatada);


        //Clique para lista de tarefas
        layoutStatus.setOnClickListener(v -> navegarParaTarefas());
        return view;
    }
    //Method para trocar o fragmento para a lista das tarefas
    private void navegarParaTarefas() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new TarefasFragment())
                .addToBackStack(null)
                .commit();
    }
    @Override
    public void onResume() {
        super.onResume();
        carregarStatusTarefas();
        carregarMetasHome();
    }

    /**
     * Busca no banco de dados e conta quantas tarefas estão atrasadas
     * e quantas estão pendentes (dentro do prazo).
     */
    private void carregarStatusTarefas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            // Busca todas as tarefas
            List<Tarefa> todasAsTarefas = AppDatabase.getInstance(getContext()).tarefaDao().buscarTodas();
            
            int atrasadas = 0;
            int pendentes = 0;
            long agora = System.currentTimeMillis();

            for (Tarefa t : todasAsTarefas) {
                if (t.dataLimite < agora) {
                    atrasadas++;
                } else {
                    pendentes++;
                }
            }

            // Atualiza a interface
            final int countAtrasadas = atrasadas;
            final int countPendentes = pendentes;

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtAtrasadas.setText("Atrasadas: " + countAtrasadas);
                    txtPendentes.setText("Pendentes: " + countPendentes);
                });
            }
        });
    }

    /**
     * Carrega as metas no scroll horizontal da home, ordenando por tempo de atividade.
     */
    private void carregarMetasHome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Meta> todasAsMetas = AppDatabase.getInstance(getContext()).metaDao().buscarTodas();

            // Ordena pela data de criação mais antiga (quem tem mais tempo ativa)
            Collections.sort(todasAsMetas, (m1, m2) -> Long.compare(m1.dataCriacao, m2.dataCriacao));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    MetaHomeAdapter adapter = new MetaHomeAdapter(todasAsMetas, meta -> {
                        // Navega para o fragmento de Metas ao clicar
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container, new MetasFragment())
                                .addToBackStack(null)
                                .commit();
                    });
                    recyclerMetasHome.setAdapter(adapter);
                });
            }
        });
    }
}
