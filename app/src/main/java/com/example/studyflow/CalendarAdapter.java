package com.example.studyflow;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.List;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    private final List<CalendarDay> days;
    private final OnDateClickListener listener;

    public interface OnDateClickListener {
        void onDateClick(Calendar date);
    }

    public CalendarAdapter(List<CalendarDay> days, OnDateClickListener listener) {
        this.days = days;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar_day, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        CalendarDay day = days.get(position);
        
        if (day.date == null) {
            holder.textDay.setText("");
            holder.textDay.setBackground(null);
            return;
        }

        int dayOfMonth = day.date.get(Calendar.DAY_OF_MONTH);
        holder.textDay.setText(String.valueOf(dayOfMonth));

        // Estilização
        if (day.isSelected) {
            holder.textDay.setBackgroundResource(R.drawable.circle_background);
            holder.textDay.setTextColor(Color.WHITE);
            holder.textDay.setAlpha(1.0f);
        } else if (day.hasTasks) {
            holder.textDay.setBackgroundResource(R.drawable.circle_outline);
            // Usa a cor primária do tema para o número circulado
            android.util.TypedValue typedValue = new android.util.TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            holder.textDay.setTextColor(typedValue.data);
            holder.textDay.setAlpha(1.0f);
        } else {
            holder.textDay.setBackground(null);
            // Usa a cor OnSurface do tema (branco no dark, preto no light)
            android.util.TypedValue typedValue = new android.util.TypedValue();
            holder.itemView.getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
            holder.textDay.setTextColor(typedValue.data);
            
            if (day.isCurrentMonth) {
                holder.textDay.setAlpha(1.0f);
            } else {
                holder.textDay.setAlpha(0.3f);
            }
        }



        holder.itemView.setOnClickListener(v -> {
            if (day.date != null && listener != null) {
                listener.onDateClick(day.date);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;

        public CalendarViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.textDay);
        }
    }


    public static class CalendarDay {
        Calendar date;
        boolean isCurrentMonth;
        boolean hasTasks;
        boolean isSelected;

        public CalendarDay(Calendar date, boolean isCurrentMonth, boolean hasTasks, boolean isSelected) {
            this.date = date;
            this.isCurrentMonth = isCurrentMonth;
            this.hasTasks = hasTasks;
            this.isSelected = isSelected;
        }
    }
}
