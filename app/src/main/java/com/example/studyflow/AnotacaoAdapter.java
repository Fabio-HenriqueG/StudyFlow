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
 * Adapter para exibir a lista de anotações na tela principal de Anotações.
 */
public class AnotacaoAdapter extends RecyclerView.Adapter<AnotacaoAdapter.AnotacaoViewHolder> {

    private final List<Anotacao> listaAnotacoes;
    private final OnAnotacaoClickListener listener;

    public interface OnAnotacaoClickListener {
        void onAnotacaoClick(Anotacao anotacao);
    }

    public AnotacaoAdapter(List<Anotacao> listaAnotacoes, OnAnotacaoClickListener listener) {
        this.listaAnotacoes = listaAnotacoes;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AnotacaoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anotacao, parent, false);
        return new AnotacaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AnotacaoViewHolder holder, int position) {
        Anotacao anotacao = listaAnotacoes.get(position);
        holder.textTitulo.setText(anotacao.titulo);

        // Formata a data da última edição
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dataFormatada = "Editado em: " + sdf.format(new Date(anotacao.dataUltimaEdicao));
        holder.textData.setText(dataFormatada);

        // Clique para abrir o editor
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnotacaoClick(anotacao);
        });
    }

    @Override
    public int getItemCount() {
        return listaAnotacoes.size();
    }

    static class AnotacaoViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textData;

        public AnotacaoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_anotacao_titulo);
            textData = itemView.findViewById(R.id.text_anotacao_data);
        }
    }
}
