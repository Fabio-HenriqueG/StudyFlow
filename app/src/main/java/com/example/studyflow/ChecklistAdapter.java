package com.example.studyflow;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Checklist;
import com.example.studyflow.data.ChecklistItem;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

        // Fixar no topo
        holder.imgPin.setVisibility(checklist.isPinned ? View.VISIBLE : View.GONE);

        // Data de Validade
        if (checklist.dataValidade > 0) {
            holder.textValidade.setVisibility(View.VISIBLE);
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.textValidade.setText("Expira em: " + sdf.format(new Date(checklist.dataValidade)));
        } else {
            holder.textValidade.setVisibility(View.GONE);
        }

        // Progresso
        Executors.newSingleThreadExecutor().execute(() -> {
            List<ChecklistItem> itens = AppDatabase.getInstance(holder.itemView.getContext())
                    .checklistDao().buscarItensPorChecklist(checklist.id);
            
            int total = itens.size();
            int concluidos = 0;
            for (ChecklistItem item : itens) {
                if (item.isChecked) concluidos++;
            }

            final int fTotal = total;
            final int fConcluidos = concluidos;
            final int progresso = (total > 0) ? (concluidos * 100 / total) : 0;

            if (holder.itemView.getContext() instanceof AppCompatActivity) {
                ((AppCompatActivity) holder.itemView.getContext()).runOnUiThread(() -> {
                    holder.progress.setProgress(progresso);
                    holder.textPorcentagem.setText(progresso + "%");
                });
            }
        });

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
            popup.getMenu().add(checklist.isPinned ? "Desafixar" : "Fixar no Topo");
            popup.getMenu().add("Editar Nome / Data");
            popup.getMenu().add("Excluir Lista");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Fixar no Topo") || item.getTitle().equals("Desafixar")) {
                    alternarFixacao(v, checklist, position);
                    return true;
                } else if (item.getTitle().equals("Editar Nome / Data")) {
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

    private void alternarFixacao(View view, Checklist checklist, int position) {
        checklist.isPinned = !checklist.isPinned;
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(view.getContext()).checklistDao().atualizar(checklist);
            if (view.getContext() instanceof AppCompatActivity) {
                ((AppCompatActivity) view.getContext()).runOnUiThread(() -> {
                    // Recarregar tudo para aplicar a nova ordenação do banco
                    // ou simplesmente reorganizar a lista local
                    Toast.makeText(view.getContext(), checklist.isPinned ? "Fixado" : "Desafixado", Toast.LENGTH_SHORT).show();
                    // Aqui seria melhor chamar um método no Fragment para recarregar a lista do banco
                });
            }
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
        TextView textTitulo, textPorcentagem, textValidade;
        ImageButton btnOpcoes;
        ImageView imgPin;
        LinearProgressIndicator progress;

        public ChecklistViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_checklist_titulo);
            textPorcentagem = itemView.findViewById(R.id.text_checklist_porcentagem);
            textValidade = itemView.findViewById(R.id.text_checklist_validade);
            btnOpcoes = itemView.findViewById(R.id.btn_opcoes_checklist);
            imgPin = itemView.findViewById(R.id.img_pin);
            progress = itemView.findViewById(R.id.progress_checklist);
        }
    }
}
