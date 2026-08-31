package com.example.studyflow;
import android.content.Context;
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
    private boolean modoHistorico = false;

    // Construtor: O Adapter exige receber a lista de tarefas do banco
    public TarefaAdapter(List<Tarefa> listaTarefas) {
        this.listaTarefas = listaTarefas;
    }

    // Construtor para modo histórico
    public TarefaAdapter(List<Tarefa> listaTarefas, boolean modoHistorico) {
        this.listaTarefas = listaTarefas;
        this.modoHistorico = modoHistorico;
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

        // Formatando a data
        SimpleDateFormat formatador = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        String dataFormatada;
        if (modoHistorico) {
            dataFormatada = "Concluída em: " + formatador.format(new Date(tarefa.dataConclusao));
        } else {
            dataFormatada = formatador.format(new Date(tarefa.dataLimite));
        }
        holder.textData.setText(dataFormatada);

        // Cores de Prioridade
        int cor;
        switch (tarefa.prioridade) {
            case 0: cor = android.graphics.Color.parseColor("#4CAF50"); break; // Baixa - Verde
            case 2: cor = android.graphics.Color.parseColor("#F44336"); break; // Alta - Vermelha
            default: cor = android.graphics.Color.parseColor("#FFC107"); break; // Média - Amarela
        }
        holder.viewPrioridade.setBackgroundColor(cor);

        // Configura o botão "Concluir" direto no card
        if (modoHistorico) {
            holder.btnConcluir.setVisibility(View.GONE);
        } else {
            holder.btnConcluir.setVisibility(View.VISIBLE);
            holder.btnConcluir.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    concluirTarefa(v, tarefa, pos);
                }
            });
        }

        // Configura o botão de opções
        holder.btnOpcoes.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            if (modoHistorico) {
                popup.getMenu().add("Excluir Permanentemente");
            } else {
                popup.getMenu().add("Editar");
                popup.getMenu().add("Excluir");
            }

            popup.setOnMenuItemClickListener(item -> {
                String titulo = item.getTitle().toString();
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return false;

                if (titulo.equals("Editar")) {
                    abrirEdicao(v, tarefa);
                    return true;
                } else if (titulo.equals("Excluir") || titulo.equals("Excluir Permanentemente")) {
                    confirmarExclusao(v, tarefa, pos);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void concluirTarefa(View view, Tarefa tarefa, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            Context context = view.getContext();
            AppDatabase db = AppDatabase.getInstance(context);
            
            if (tarefa.prioridade == 0) {
                // Baixa Prioridade: Deleta na hora
                db.tarefaDao().excluir(tarefa);
            } else {
                // Média ou Alta: Vai para o histórico
                tarefa.concluida = true;
                tarefa.dataConclusao = System.currentTimeMillis();
                db.tarefaDao().atualizar(tarefa);
            }

            // Cancela notificações agendadas
            NotificacaoScheduler.cancelarNotificacoesTarefa(context, tarefa.id);

            // Registra a atividade no histórico (materiaId = 0 para geral)
            ProdutividadeManager.registrarAtividade(context, "TAREFA", tarefa.id, 0);

            view.post(() -> {
                if (position < listaTarefas.size()) {
                    listaTarefas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaTarefas.size());
                    String msg = tarefa.prioridade == 0 ? "Tarefa concluída e removida!" : "Tarefa movida para o histórico!";
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
                }
            });
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
            Context context = view.getContext();
            AppDatabase.getInstance(context).tarefaDao().excluir(tarefa);
            
            // Cancela notificações agendadas
            NotificacaoScheduler.cancelarNotificacoesTarefa(context, tarefa.id);

            view.post(() -> {
                if (position < listaTarefas.size()) {
                    listaTarefas.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listaTarefas.size());
                    Toast.makeText(context, "Tarefa excluída", Toast.LENGTH_SHORT).show();
                }
            });
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
        View viewPrioridade;
        ImageButton btnOpcoes;
        View btnConcluir;

        public TarefaViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_item_titulo);
            textDescricao = itemView.findViewById(R.id.text_item_descricao);
            textData = itemView.findViewById(R.id.text_item_data);
            viewPrioridade = itemView.findViewById(R.id.view_prioridade);
            btnOpcoes = itemView.findViewById(R.id.btn_opcoes_tarefa);
            btnConcluir = itemView.findViewById(R.id.btn_concluir_tarefa);
        }
    }

}
