package com.example.studyflow;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.ChecklistItem;
import java.util.List;
import java.util.concurrent.Executors;

public class ChecklistItemsAdapter extends RecyclerView.Adapter<ChecklistItemsAdapter.ItemViewHolder> {

    private final List<ChecklistItem> listaItens;
    private final OnItemInteractionListener listener;

    public interface OnItemInteractionListener {
        void onEditItem(ChecklistItem item, int position);
    }

    public ChecklistItemsAdapter(List<ChecklistItem> listaItens, OnItemInteractionListener listener) {
        this.listaItens = listaItens;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_checklist_topico, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ChecklistItem item = listaItens.get(position);
        holder.textView.setText(item.texto);
        holder.checkBox.setChecked(item.isChecked);
        
        atualizarEstiloTexto(holder.textView, item.isChecked);

        // Clique no CheckBox
        holder.checkBox.setOnClickListener(v -> {
            item.isChecked = holder.checkBox.isChecked();
            atualizarEstiloTexto(holder.textView, item.isChecked);
            
            if (item.isChecked) {
                // Registra a atividade no histórico
                ProdutividadeManager.registrarAtividade(v.getContext(), "CHECKLIST", item.id, 0);
            }

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(v.getContext()).checklistDao().atualizarItem(item);
            });
        });

        // Clique em Editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditItem(item, position);
        });

        // Clique em Excluir
        holder.btnExcluir.setOnClickListener(v -> {
            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(v.getContext()).checklistDao().excluirItem(item);
                
                holder.itemView.post(() -> {
                    listaItens.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaItens.size());
                });
            });
        });
    }

    private void atualizarEstiloTexto(TextView textView, boolean isChecked) {
        if (isChecked) {
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            textView.setAlpha(0.5f);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            textView.setAlpha(1.0f);
        }
    }

    @Override
    public int getItemCount() {
        return listaItens.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        CheckBox checkBox;
        TextView textView;
        ImageButton btnEditar, btnExcluir;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            checkBox = itemView.findViewById(R.id.check_item);
            textView = itemView.findViewById(R.id.text_item_checklist);
            btnEditar = itemView.findViewById(R.id.btn_editar_item);
            btnExcluir = itemView.findViewById(R.id.btn_excluir_item);
        }
    }
}
