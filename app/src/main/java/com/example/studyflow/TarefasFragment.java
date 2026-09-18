package com.example.studyflow;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.FirestoreService;
import com.example.studyflow.data.Tarefa;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Fragmento que exibe a lista de todas as tarefas ativas do usuário.
 */
public class TarefasFragment extends Fragment {

    private RecyclerView recyclerTarefas;
    private View txtEmptyState, progressBar;
    private TarefaAdapter adapter;

    public TarefasFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tarefas, container, false);

        ImageButton btnVoltar = view.findViewById(R.id.btn_voltar);
        ImageButton btnCalendario = view.findViewById(R.id.btn_calendario);
        ImageButton btnHistorico = view.findViewById(R.id.btn_historico);

        btnVoltar.setOnClickListener(v -> {
            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new HomeFragment())
                        .commit();
            }
        });

        btnCalendario.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CalendarioTarefasFragment())
                    .addToBackStack(null)
                    .commit();
        });

        btnHistorico.setOnClickListener(v -> {
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HistoricoTarefasFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerTarefas = view.findViewById(R.id.recyclerTarefas);
        txtEmptyState = view.findViewById(R.id.txtEmptyTarefas);
        progressBar = view.findViewById(R.id.progressTarefas);
        configurarSwipe();
    }

    private void configurarSwipe() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getBindingAdapterPosition();
                if (adapter != null && position != RecyclerView.NO_POSITION) {
                    if (direction == ItemTouchHelper.RIGHT) {
                        adapter.concluirTarefa(position, getContext());
                        mostrarFeedback(getString(R.string.tarefa_concluida));
                    }
                }
            }
        }).attachToRecyclerView(recyclerTarefas);
    }

    private void mostrarFeedback(String msg) {
        Snackbar.make(recyclerTarefas, msg, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        carregarTarefasDoBanco();
    }

    private void carregarTarefasDoBanco() {
        Context context = getContext();
        if (context == null) return;
        
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
        
        FirestoreService.getInstance().buscarTarefasAtivas()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (progressBar != null) progressBar.setVisibility(View.GONE);
                List<Tarefa> listaDoBanco = queryDocumentSnapshots.toObjects(Tarefa.class);

                if (getActivity() != null) {
                    if (adapter == null) {
                        adapter = new TarefaAdapter(new ArrayList<>(listaDoBanco));
                        adapter.setOnDataChangedListener(count -> {
                            if (txtEmptyState != null) {
                                txtEmptyState.setVisibility(count == 0 ? View.VISIBLE : View.GONE);
                            }
                        });
                    } else {
                        adapter.setTarefas(new ArrayList<>(listaDoBanco));
                    }
                    
                    if (recyclerTarefas.getAdapter() == null) {
                        recyclerTarefas.setAdapter(adapter);
                    }

                    if (txtEmptyState != null) {
                        txtEmptyState.setVisibility(listaDoBanco.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                }
            })
            .addOnFailureListener(e -> {
                if (getContext() != null) {
                    Toast.makeText(getContext(), "Erro ao carregar tarefas", Toast.LENGTH_SHORT).show();
                }
            });
    }
}
