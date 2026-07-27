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
import com.example.studyflow.data.Tarefa;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class TarefaAdapter extends RecyclerView.Adapter<TarefaAdapter.TarefaViewHolder> {

    private final List<Tarefa> listaTarefas;

    // Construtor: O Adapter exige receber a lista de tarefas do banco
    public TarefaAdapter(List<Tarefa> listaTarefas) {
        this.listaTarefas = listaTarefas;
    }

    // 1. Cria o desenho da linha (infla o XML)
    @NonNull
    @Override
    public TarefaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_tarefa, parent, false);
        return new TarefaViewHolder(view);
    }

    // 2. Coloca os dados de uma tarefa específica dentro dos componentes visuais
    @Override
    public void onBindViewHolder(@NonNull TarefaViewHolder holder, int position) {
        Tarefa tarefa = listaTarefas.get(position);

        holder.textTitulo.setText(tarefa.titulo);
        holder.textDescricao.setText(tarefa.descricao);

        // Formatando a data de milissegundos para DD/MM/AAAA
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dataFormatada = formatador.format(new Date(tarefa.dataLimite));
        holder.textData.setText(dataFormatada);

        // Configura o botão de opções
        holder.btnOpcoes.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Editar");
            popup.getMenu().add("Excluir");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Editar")) {
                    abrirEdicao(v, tarefa);
                    return true;
                } else if (item.getTitle().equals("Excluir")) {
                    confirmarExclusao(v, tarefa, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void abrirEdicao(View view, Tarefa tarefa) {
        // Cria o fragmento de criação/edição e passa a tarefa
        CriaTarefaFragment fragment = new CriaTarefaFragment();
        Bundle args = new Bundle();
        args.putSerializable("tarefa_editar", tarefa); // Tarefa precisa ser Serializable
        fragment.setArguments(args);

        // Navega para o fragmento
        if (view.getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) view.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void confirmarExclusao(View view, Tarefa tarefa, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(view.getContext()).tarefaDao().excluir(tarefa);
            
            if (view.getContext() instanceof AppCompatActivity) {
                ((AppCompatActivity) view.getContext()).runOnUiThread(() -> {
                    listaTarefas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaTarefas.size());
                    Toast.makeText(view.getContext(), "Tarefa excluída", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    // 3. Diz ao Android quantas tarefas existem na lista para ele saber o tamanho do scroll
    @Override
    public int getItemCount() {
        return listaTarefas != null ? listaTarefas.size() : 0;
    }

    // Minha "Caixa de ferramentas": Segura as variáveis do XML de uma linha só
    static class TarefaViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textDescricao, textData;
        ImageButton btnOpcoes;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_item_titulo);
            textDescricao = itemView.findViewById(R.id.text_item_descricao);
            textData = itemView.findViewById(R.id.text_item_data);
            btnOpcoes = itemView.findViewById(R.id.btn_opcoes_tarefa);
        }
    }
}
