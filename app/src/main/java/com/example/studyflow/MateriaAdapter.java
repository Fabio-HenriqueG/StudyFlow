package com.example.studyflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.appcompat.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Flashcard;
import com.example.studyflow.data.Materia;
import java.util.List;
import java.util.concurrent.Executors;


public class MateriaAdapter extends RecyclerView.Adapter<MateriaAdapter.MateriaViewHolder> {

    private final List<Materia> lista;
    private final OnMateriaClickListener listener;

    public interface OnMateriaClickListener {
        void onClick(Materia materia);
    }

    public MateriaAdapter(List<Materia> lista, OnMateriaClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MateriaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_materia_flashcard, parent, false);
        return new MateriaViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MateriaViewHolder holder, int position) {
        Materia m = lista.get(position);
        holder.txtNome.setText(m.nome);
        holder.viewCor.setBackgroundColor(m.cor);

        // Busca a quantidade de flashcards de forma assíncrona
        Executors.newSingleThreadExecutor().execute(() -> {
            int qtd = AppDatabase.getInstance(holder.itemView.getContext()).flashcardDao().buscarPorMateria(m.id).size();
            holder.itemView.post(() -> {
                holder.txtQtd.setText(qtd + (qtd == 1 ? " cartão" : " cartões"));
            });
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(m);
        });

        holder.itemView.setOnLongClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Excluir Seção");
            popup.setOnMenuItemClickListener(item -> {
                confirmarExclusaoMateria(v, m, position);
                return true;
            });
            popup.show();
            return true;
        });
    }

    private void confirmarExclusaoMateria(View v, Materia m, int pos) {
        new android.app.AlertDialog.Builder(v.getContext())
                .setTitle("Excluir Seção")
                .setMessage("Isso apagará a seção '" + m.nome + "' e TODOS os flashcards dentro dela. Continuar?")
                .setPositiveButton("Sim", (d, w) -> {
                    Executors.newSingleThreadExecutor().execute(() -> {
                        AppDatabase db = AppDatabase.getInstance(v.getContext());
                        // 1. Deleta flashcards da matéria
                        List<Flashcard> cards = db.flashcardDao().buscarPorMateria(m.id);
                        for (Flashcard f : cards) db.flashcardDao().excluir(f);
                        // 2. Deleta a matéria
                        db.materiaDao().excluir(m);
                        
                        v.post(() -> {
                            lista.remove(pos);
                            notifyItemRemoved(pos);
                            notifyItemRangeChanged(pos, lista.size());
                        });
                    });
                })
                .setNegativeButton("Não", null)
                .show();
    }


    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class MateriaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNome, txtQtd;
        View viewCor;
        public MateriaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNome = itemView.findViewById(R.id.txtNomeMateria);
            txtQtd = itemView.findViewById(R.id.txtQtdFlashcards);
            viewCor = itemView.findViewById(R.id.viewCorMateria);
        }
    }
}
