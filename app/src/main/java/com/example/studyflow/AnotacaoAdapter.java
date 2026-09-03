package com.example.studyflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.Anotacao;
import com.example.studyflow.data.AppDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

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

        // Identifica e exibe o tipo da anotação
        if (anotacao.conteudoHtml != null && anotacao.conteudoHtml.startsWith("[")) {
            holder.textTipo.setText("CADERNO LIVRE");
        } else {
            holder.textTipo.setText("BLOCO DE NOTAS");
        }

        // Clique para abrir o editor

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onAnotacaoClick(anotacao);
        });

        // Configura o botão de opções (três pontinhos)
        holder.btnOpcoes.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Editar");
            popup.getMenu().add("Excluir");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Editar")) {
                    if (listener != null) listener.onAnotacaoClick(anotacao);
                    return true;
                } else if (item.getTitle().equals("Excluir")) {
                    confirmarExclusao(v, anotacao, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void confirmarExclusao(View view, Anotacao anotacao, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(view.getContext()).anotacaoDao().excluir(anotacao);
            
            view.post(() -> {
                listaAnotacoes.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listaAnotacoes.size());
                com.google.android.material.snackbar.Snackbar.make(view, "Anotação excluída", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            });
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

    static class AnotacaoViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textData, textTipo;
        ImageButton btnOpcoes;

        public AnotacaoViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_anotacao_titulo);
            textData = itemView.findViewById(R.id.text_anotacao_data);
            textTipo = itemView.findViewById(R.id.text_anotacao_tipo);
            btnOpcoes = itemView.findViewById(R.id.btn_opcoes_anotacao);
        }
    }

}
