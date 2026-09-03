package com.example.studyflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import java.util.Calendar;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.example.studyflow.data.AppDatabase;
import com.example.studyflow.data.Meta;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MetaAdapter extends RecyclerView.Adapter<MetaAdapter.MetaViewHolder> {

    private final List<Meta> listaMetas;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable updateTimeRunnable = new Runnable() {
        @Override
        public void run() {
            notifyDataSetChanged(); // Atualiza a tela toda para refletir o tempo passando
            handler.postDelayed(this, 1000); // Roda a cada 1 segundo
        }
    };

    public MetaAdapter(List<Meta> listaMetas) {
        this.listaMetas = listaMetas;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        handler.post(updateTimeRunnable); // Inicia o timer quando o adapter é usado
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        handler.removeCallbacks(updateTimeRunnable); // Para o timer para economizar bateria
    }

    @NonNull
    @Override
    public MetaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meta, parent, false);
        return new MetaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MetaViewHolder holder, int position) {
        Meta meta = listaMetas.get(position);
        holder.textTitulo.setText(meta.titulo);

        // Lógica do Contador de Tempo
        long tempoDecorrido = System.currentTimeMillis() - meta.dataCriacao;
        
        long dias = TimeUnit.MILLISECONDS.toDays(tempoDecorrido);
        long horas = TimeUnit.MILLISECONDS.toHours(tempoDecorrido) % 24;
        long minutos = TimeUnit.MILLISECONDS.toMinutes(tempoDecorrido) % 60;
        long segundos = TimeUnit.MILLISECONDS.toSeconds(tempoDecorrido) % 60;

        String tempoFormatado = String.format("Ativa há: %dd %02d:%02d:%02d", dias, horas, minutos, segundos);
        holder.textContador.setText(tempoFormatado);

        // Lógica do Check-in Diário respeitando o Horário de Reset
        boolean jaFezCheckinHoje = verificarCheckin(holder.itemView.getContext(), meta.ultimoCheckin);
        
        if (jaFezCheckinHoje) {
            holder.btnCheckin.setText("Meta Cumprida!");
            holder.btnCheckin.setEnabled(false);
            holder.btnCheckin.setAlpha(0.6f);
        } else {
            holder.btnCheckin.setText("Confirmar Hoje");
            holder.btnCheckin.setEnabled(true);
            holder.btnCheckin.setAlpha(1.0f);
        }

        holder.btnCheckin.setOnClickListener(v -> {
            meta.ultimoCheckin = System.currentTimeMillis();
            
            // Registra a atividade no histórico
            ProdutividadeManager.registrarAtividade(v.getContext(), "META", meta.id, 0);

            Executors.newSingleThreadExecutor().execute(() -> {
                AppDatabase.getInstance(v.getContext()).metaDao().atualizar(meta);
                
                handler.post(() -> {
                    com.google.android.material.snackbar.Snackbar.make(v, "Parabéns! Meta confirmada por hoje.", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                });
            });
        });

        // Clique nos três pontinhos
        holder.btnOpcoes.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(v.getContext(), v);
            popup.getMenu().add("Editar");
            popup.getMenu().add("Excluir");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getTitle().equals("Editar")) {
                    abrirEdicao(v, meta);
                    return true;
                } else if (item.getTitle().equals("Excluir")) {
                    confirmarExclusao(v, meta, position);
                    return true;
                }
                return false;
            });
            popup.show();
        });
    }

    private void abrirEdicao(View view, Meta meta) {
        CriaMetaFragment fragment = new CriaMetaFragment();
        Bundle args = new Bundle();
        args.putSerializable("meta_editar", meta);
        fragment.setArguments(args);

        if (view.getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) view.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void confirmarExclusao(View view, Meta meta, int position) {
        Executors.newSingleThreadExecutor().execute(() -> {
            AppDatabase.getInstance(view.getContext()).metaDao().excluir(meta);
            
            handler.post(() -> {
                listaMetas.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, listaMetas.size());
                com.google.android.material.snackbar.Snackbar.make(view, "Meta excluída", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            });
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
                Meta old = listaMetas.get(oldItemPosition);
                Meta nova = novasMetas.get(newItemPosition);
                return old.titulo.equals(nova.titulo) && 
                       old.dataCriacao == nova.dataCriacao &&
                       old.ultimoCheckin == nova.ultimoCheckin;
            }
        });

        listaMetas.clear();
        listaMetas.addAll(novasMetas);
        result.dispatchUpdatesTo(this);
    }

    /**
     * Verifica se o check-in ainda é válido para o período atual
     * com base no horário de reset definido pelo usuário.
     */
    private boolean verificarCheckin(Context context, long ultimoCheckin) {
        if (ultimoCheckin == 0) return false;

        SharedPreferences prefs = context.getSharedPreferences("StudyFlowPrefs", Context.MODE_PRIVATE);
        String resetTime = prefs.getString("goal_reset_time", "00:00");
        String[] parts = resetTime.split(":");
        int resetHour = Integer.parseInt(parts[0]);
        int resetMin = Integer.parseInt(parts[1]);

        Calendar agora = Calendar.getInstance();
        Calendar limiteReset = Calendar.getInstance();
        limiteReset.set(Calendar.HOUR_OF_DAY, resetHour);
        limiteReset.set(Calendar.MINUTE, resetMin);
        limiteReset.set(Calendar.SECOND, 0);
        limiteReset.set(Calendar.MILLISECOND, 0);

        // Se agora ainda não passou do horário de reset de hoje, o "limite" foi ontem
        if (agora.before(limiteReset)) {
            limiteReset.add(Calendar.DAY_OF_YEAR, -1);
        }

        // Se o último check-in foi DEPOIS do último momento de reset, ele ainda é válido
        return ultimoCheckin > limiteReset.getTimeInMillis();
    }

    static class MetaViewHolder extends RecyclerView.ViewHolder {
        TextView textTitulo, textContador;
        Button btnCheckin;
        ImageButton btnOpcoes;

        public MetaViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitulo = itemView.findViewById(R.id.text_meta_titulo);
            textContador = itemView.findViewById(R.id.text_meta_contador);
            btnCheckin = itemView.findViewById(R.id.btn_meta_checkin);
            btnOpcoes = itemView.findViewById(R.id.btn_opcoes_meta);
        }
    }
}
