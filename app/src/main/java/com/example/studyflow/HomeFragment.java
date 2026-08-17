package com.example.studyflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Anotacao;
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
 * Fragmento da tela inicial que exibe o resumo de tarefas, anotações e metas.
 */
public class HomeFragment extends Fragment {

    private TextView txtAtrasadas, txtPendentes;
    private RecyclerView recyclerMetasHome, recyclerAnotacoesHome;
    private LinearLayout layoutStatus;

    public HomeFragment() {
        // Construtor vazio obrigatório
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Vinculação de componentes
        TextView lblSaudacao = view.findViewById(R.id.lblSaudacao);
        TextView lblDataAtual = view.findViewById(R.id.lblDataAtual);
        android.widget.ImageView imgPerfil = view.findViewById(R.id.imgPerfilHome);
        txtAtrasadas = view.findViewById(R.id.text_home_atrasadas);
        txtPendentes = view.findViewById(R.id.text_home_pendentes);
        recyclerMetasHome = view.findViewById(R.id.recycler_metas_home);
        recyclerAnotacoesHome = view.findViewById(R.id.recycler_anotacoes_home);
        layoutStatus = view.findViewById(R.id.layoutStatus);
        ImageButton btnConfig = view.findViewById(R.id.btnConfiguracoes);

        // Configuração de Saudação Personalizada e Foto
        SharedPreferences prefs = requireContext().getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        String nomeUsuario = prefs.getString("user_name", "");
        String fotoPath = prefs.getString("user_profile_pic", "");

        if (!fotoPath.isEmpty()) {
            imgPerfil.setImageURI(android.net.Uri.parse(fotoPath));
        }

        int hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String saudacaoBase = (hora < 12) ? "Bom dia" : (hora < 18) ? "Boa tarde" : "Boa noite";
        
        if (!nomeUsuario.isEmpty()) {
            lblSaudacao.setText(saudacaoBase + ", " + nomeUsuario + "!");
        } else {
            lblSaudacao.setText(saudacaoBase + "!");
        }

        // Configuração de Data Atual
        SimpleDateFormat sdf = new SimpleDateFormat("d 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        lblDataAtual.setText(sdf.format(new Date()));

        // Navegação ao clicar no status das tarefas
        layoutStatus.setOnClickListener(v -> navegarPara(new TarefasFragment()));

        // Abrir Configurações
        btnConfig.setOnClickListener(v -> navegarPara(new ConfiguracoesFragment()));

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarStatusTarefas();
        carregarMetasHome();
        carregarAnotacoesHome();
    }

    private void carregarStatusTarefas() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Tarefa> tarefas = AppDatabase.getInstance(getContext()).tarefaDao().buscarTodas();
            int atrasadas = 0, pendentes = 0;
            long agora = System.currentTimeMillis();

            for (Tarefa t : tarefas) {
                if (t.dataLimite < agora) atrasadas++;
                else pendentes++;
            }

            final int fAtrasadas = atrasadas;
            final int fPendentes = pendentes;
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    txtAtrasadas.setText("Atrasadas: " + fAtrasadas);
                    txtPendentes.setText("Pendentes: " + fPendentes);
                });
            }
        });
    }

    private void carregarMetasHome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Meta> metas = AppDatabase.getInstance(getContext()).metaDao().buscarTodas();
            Collections.sort(metas, (m1, m2) -> Long.compare(m1.dataCriacao, m2.dataCriacao));

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    MetaHomeAdapter adapter = new MetaHomeAdapter(metas, meta -> navegarPara(new MetasFragment()));
                    recyclerMetasHome.setAdapter(adapter);
                });
            }
        });
    }

    private void carregarAnotacoesHome() {
        Executors.newSingleThreadExecutor().execute(() -> {
            List<Anotacao> anotacoes = AppDatabase.getInstance(getContext()).anotacaoDao().buscarTodas();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    AnotacaoHomeAdapter adapter = new AnotacaoHomeAdapter(anotacoes, anotacao -> {
                        EditorAnotacaoFragment fragment = new EditorAnotacaoFragment();
                        Bundle args = new Bundle();
                        args.putSerializable("anotacao", anotacao);
                        fragment.setArguments(args);
                        navegarPara(fragment);
                    });
                    recyclerAnotacoesHome.setAdapter(adapter);
                });
            }
        });
    }

    private void navegarPara(Fragment fragment) {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }
}
