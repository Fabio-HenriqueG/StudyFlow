package com.example.studyflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.Anotacao;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter simplificado para exibir anotações no scroll horizontal da tela inicial.
 */
public class AnotacaoHomeAdapter extends RecyclerView.Adapter<AnotacaoHomeAdapter.AnotacaoHomeViewHolder> {

    private final List<Anotacao> listaAnotacoes;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Anotacao anotacao);
    }

    public AnotacaoHomeAdapter(List<Anotacao> listaAnotacoes, OnItemClickListener listener) {
        this.listaAnotacoes = listaAnotacoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnotacaoHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anotacao_home, parent, false);
        return new AnotacaoHomeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnotacaoHomeViewHolder holder, int position) {
        Anotacao anotacao = listaAnotacoes.get(position);
        holder.textTitulo.setText(anotacao.titulo);

        // Formata a data da última edição de forma curta
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.textData.setText(sdf.format(new Date(anotacao.dataUltimaEdicao)));

        // Identifica e exibe o tipo da anotação
        if (anotacao.conteudoHtml != null && anotacao.conteudoHtml.startsWith("[")) {
            holder.textTipo.setText("DESENHO");
        } else {
            holder.textTipo.setText("TEXTO");
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(anotacao);
        });
    }

    @Override
    public int getItemCount() {
        return listaAnotacoes.size();
    }

    public void setAnotacoes(List<Anotacao> novasAnotacoes) {
        androidx.recyclerview.widget.DiffUtil.DiffResult result = androidx.recyclerview.widget.DiffUtil.calculateDiff(new androidx.recyclerview.widget.DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return listaAnotacoes.size();
            }

            @Override
            public int getNewListSize() {
                return novasAnotacoes.size();
            }

            @Override
            public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                return listaAnotacoes.get(oldItemPosition).id == novasAnotacoes.get(newItemPosition).id;
            }

            @Override
            public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                Anotacao old = listaAnotacoes.get(oldItemPosition);
                Anotacao nova = novasAnotacoes.get(newItemPosition);
                return old.titulo.equals(nova.titulo) && 
                       old.dataUltimaEdicao == nova.dataUltimaEdicao &&
                       (old.conteudoHtml != null ? old.conteudoHtml.equals(nova.conteudoHtml) : nova.conteudoHtml == null);
            }
        });

        listaAnotacoes.clear();
        listaAnotacoes.addAll(novasAnotacoes);
        result.dispatchUpdatesTo(this);
    }

    static class AnotacaoHomeViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textData, textTipo;

        public AnotacaoHomeViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_anotacao_home_titulo);
            textData = itemView.findViewById(R.id.text_anotacao_home_data);
            textTipo = itemView.findViewById(R.id.text_anotacao_home_tipo);
        }
    }

}
