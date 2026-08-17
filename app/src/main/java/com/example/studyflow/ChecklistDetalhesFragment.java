package com.example.studyflow;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import com.example.studyflow.data.ChecklistItem;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class ChecklistDetalhesFragment extends Fragment {

    private Checklist checklist;
    private RecyclerView recyclerView;
    private ChecklistItemsAdapter adapter;
    private List<ChecklistItem> listaItens = new ArrayList<>();
    private EditText editNovoItem;
    private ChecklistItem itemEmEdicao = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checklist_detalhes, container, false);
        
        if (getArguments() != null) {
            checklist = (Checklist) getArguments().getSerializable("checklist");
        }

        TextView lblTitulo = view.findViewById(R.id.lblTituloChecklist);
        if (checklist != null) lblTitulo.setText(checklist.titulo);

        ImageButton btnVoltar = view.findViewById(R.id.btnVoltarChecklist);
        btnVoltar.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        ImageButton btnReset = view.findViewById(R.id.btnResetarLista);
        btnReset.setOnClickListener(v -> resetarLista());

        recyclerView = view.findViewById(R.id.recyclerItensChecklist);
        editNovoItem = view.findViewById(R.id.editNovoItem);
        Button btnAdd = view.findViewById(R.id.btnAdicionarItem);

        btnAdd.setOnClickListener(v -> salvarOuAtualizarItem());

        configurarAdapter();
        carregarItens();

        return view;
    }

    private void configurarAdapter() {
        adapter = new ChecklistItemsAdapter(listaItens, (item, position) -> {
            itemEmEdicao = item;
            editNovoItem.setText(item.texto);
            editNovoItem.requestFocus();
        });
        recyclerView.setAdapter(adapter);
    }

    private void carregarItens() {
        if (checklist == null) return;
        
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChecklistItem> doBanco = AppDatabase.getInstance(getContext()).checklistDao().buscarItensPorChecklist(checklist.id);
            
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    listaItens.clear();
                    listaItens.addAll(doBanco);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }

    private void resetarLista() {
        if (checklist == null) return;
        
        new AlertDialog.Builder(getContext())
                .setTitle("Reiniciar Lista")
                .setMessage("Deseja desmarcar todos os itens desta lista?")
                .setPositiveButton("Sim", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        for (ChecklistItem item : listaItens) {
                            item.isChecked = false;
                            AppDatabase.getInstance(getContext()).checklistDao().atualizarItem(item);
                        }
                        getActivity().runOnUiThread(() -> carregarItens());
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }

    private void salvarOuAtualizarItem() {
        String texto = editNovoItem.getText().toString().trim();
        if (texto.isEmpty()) return;

        if (itemEmEdicao != null) {
            itemEmEdicao.texto = texto;
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).checklistDao().atualizarItem(itemEmEdicao);
                itemEmEdicao = null;
                getActivity().runOnUiThread(() -> {
                    editNovoItem.setText("");
                    carregarItens();
                });
            });
        } else if (checklist != null) {
            ChecklistItem novo = new ChecklistItem(checklist.id, texto);
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(getContext()).checklistDao().inserirItem(novo);
                getActivity().runOnUiThread(() -> {
                    editNovoItem.setText("");
                    carregarItens();
                });
            });
        }
    }
}
