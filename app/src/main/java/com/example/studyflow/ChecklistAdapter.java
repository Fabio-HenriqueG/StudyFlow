package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import java.util.List;
import java.util.concurrent.Executors;

public class ChecklistAdapter extends RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder> {

    private final List<Checklist> listaChecklists;

    public ChecklistAdapter(List<Checklist> listaChecklists) {
        this.listaChecklists = listaChecklists;
    }

    @NonNull
    @Override
    public ChecklistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist, parent, false);
        return new ChecklistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChecklistViewHolder holder, int position) {
        Checklist checklist = listaChecklists.get(position);
        holder.textTitulo.setText(checklist.titulo);

        // Clique no card abre os detalhes
        holder.itemView.setOnClickListener(v -> {
            ChecklistDetalhesFragment fragment = new ChecklistDetalhesFragment();
            Bundle args = new Bundle();
            args.putSerializable("checklist", checklist);
            fragment.setArguments(args);

            if (v.getContext() instanceof AppCompatActivity) {
                ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Clique nos três pontinhos
        holder.btnOpcoes.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Editar Nome");
            popup.getMenu().add("Excluir Lista");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Editar Nome")) {
                    abrirEdicao(v, checklist);
                    return true;
                } else if (item.getTitle().equals("Excluir Lista")) {
                    confirmarExclusao(v, checklist, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void abrirEdicao(View view, Checklist checklist) {
        CriaChecklistFragment fragment = new CriaChecklistFragment();
        Bundle args = new Bundle();
        args.putSerializable("checklist_editar", checklist);
        fragment.setArguments(args);

        if (view.getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) view.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void confirmarExclusao(View view, Checklist checklist, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(view.getContext()).checklistDao().excluir(checklist);
            
            if (view.getContext() instanceof AppCompatActivity) {
                ((AppCompatActivity) view.getContext()).runOnUiThread(() -> {
                    listaChecklists.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaChecklists.size());
                    Toast.makeText(view.getContext(), "Checklist excluído", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaChecklists.size();
    }

    static class ChecklistViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo;
        ImageButton btnOpcoes;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_checklist_titulo);
            btnOpcoes = itemView.findViewById(R.id.btn_opcoes_checklist);
        }
    }
}
