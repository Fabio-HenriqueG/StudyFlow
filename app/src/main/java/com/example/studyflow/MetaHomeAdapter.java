package com.example.studyflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.Meta;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MetaHomeAdapter extends RecyclerView.Adapter<MetaHomeAdapter.MetaHomeViewHolder> {

    private final List<Meta> listaMetas;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Meta meta);
    }

    public MetaHomeAdapter(List<Meta> listaMetas, OnItemClickListener listener) {
        this.listaMetas = listaMetas;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MetaHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meta_home, parent, false);
        return new MetaHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetaHomeViewHolder holder, int position) {
        Meta meta = listaMetas.get(position);
        holder.textTitulo.setText(meta.titulo);

        // Calcula apenas os dias
        long tempoDecorrido = System.currentTimeMillis() - meta.dataCriacao;
        long dias = TimeUnit.MILLISECONDS.toDays(tempoDecorrido);

        if (dias == 0) {
            holder.textDias.setText("Começou hoje");
        } else if (dias == 1) {
            holder.textDias.setText("1 dia ativo");
        } else {
            holder.textDias.setText(dias + " dias ativos");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(meta);
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaMetas.size();
    }

    public void setMetas(List<Meta> novasMetas) {
        androidx.recyclerview.widget.DiffUtil.DiffResult result = androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return listaMetas.size();
            }

            @Override
            public int getNewListSize() {
                return novasMetas.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return listaMetas.get(oldItemPosition).id == novasMetas.get(newItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Meta oldMeta = listaMetas.get(oldItemPosition);
                Meta newMeta = novasMetas.get(newItemPosition);
                return oldMeta.titulo.equals(newMeta.titulo) && oldMeta.dataCriacao == newMeta.dataCriacao;
            }
        });

        listaMetas.clear();
        listaMetas.addAll(novasMetas);
        result.dispatchUpdatesTo(this);
    }

    static class MetaHomeViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textDias;

        public MetaHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_meta_home_titulo);
            textDias = itemView.findViewById(R.id.text_meta_home_dias);
        }
    }
}
