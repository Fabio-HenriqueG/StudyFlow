package com.example.studyflow;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Flashcard;
import com.example.studyflow.data.Materia;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class FlashcardAdapter extends RecyclerView.Adapter<FlashcardAdapter.FlashcardViewHolder> {

    private final List<Flashcard> lista;
    private final OnFlashcardClickListener listener;

    public interface OnFlashcardClickListener {
        void onEdit(Flashcard flashcard);
    }

    public FlashcardAdapter(List<Flashcard> lista, OnFlashcardClickListener listener) {
        this.lista = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashcardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flashcard_lista, parent, false);
        return new FlashcardViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashcardViewHolder holder, int position) {
        Flashcard f = lista.get(position);
        holder.txtPergunta.setText(f.pergunta);
        
        // Busca o nome da matéria pelo ID
        Executors.newSingleThreadExecutor().execute(() -> {
            Materia m = AppDatabase.getInstance(holder.itemView.getContext()).materiaDao().buscarPorId(f.materiaId);
            holder.itemView.post(() -> {
                holder.txtMateria.setText(m != null ? m.nome : "Sem Categoria");
                if (m != null) holder.txtMateria.setTextColor(m.cor);
            });
        });

        // Status de Domínio
        String status;
        if (f.nivelDominio >= 5) status = "🔥 Dominado";
        else if (f.nivelDominio >= 3) status = "✅ Conhecido";
        else if (f.nivelDominio >= 1) status = "📖 Aprendendo";
        else status = "🆕 Novo";
        holder.txtStatus.setText(status);

        // Data de Revisão
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());
        holder.txtData.setText("Revisão: " + sdf.format(new Date(f.dataProximaRevisao)));

        holder.btnOpcoes.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Editar");
            popup.getMenu().add("Excluir");
            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Editar")) {
                    if (listener != null) listener.onEdit(f);
                } else {
                    excluirFlashcard(v, f, position);
                }
                return true;
            });
            popup.show();
        });
    }

    private void excluirFlashcard(View v, Flashcard f, int pos) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(v.getContext()).flashcardDao().excluir(f);
            v.post(() -> {
                lista.remove(pos);
                notifyItemRemoved(pos);
                notifyItemRangeChanged(pos, lista.size());
                Toast.makeText(v.getContext(), "Removido!", Toast.LENGTH_SHORT).show();
            });
        });
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    static class FlashcardViewHolder extends RecyclerView.ViewHolder {
        TextView txtPergunta, txtMateria, txtStatus, txtData;
        ImageButton btnOpcoes;
        public FlashcardViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPergunta = itemView.findViewById(R.id.txtPerguntaLista);
            txtMateria = itemView.findViewById(R.id.txtMateriaLista);
            txtStatus = itemView.findViewById(R.id.txtStatusDominio);
            txtData = itemView.findViewById(R.id.txtDataProxima);
            btnOpcoes = itemView.findViewById(R.id.btnOpcoesFlashcard);
        }
    }
}
