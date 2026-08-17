package com.example.studyflow.data;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.studyflow.data.dao.AnotacaoDao;
import com.example.studyflow.data.dao.ChecklistDao;
import com.example.studyflow.data.dao.MetaDao;
import com.example.studyflow.data.dao.TarefaDao;


// Se no futuro vocês criarem outras entidades, adicionem aqui
@Database(entities = {Tarefa.class, Meta.class, Checklist.class, ChecklistItem.class, Anotacao.class}, version = 8, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    private static AppDatabase instance;

    // Avisa ao banco que ele deve gerenciar os comandos da Tarefa
    public abstract TarefaDao tarefaDao();

    // Gerencia os comandos da Meta
    public abstract MetaDao metaDao();

    // Gerencia os comandos do Checklist
    public abstract ChecklistDao checklistDao();

    // Gerencia os comandos das Anotações
    public abstract AnotacaoDao anotacaoDao();

    // Padrão Singleton: Garante que o app use apenas UMA conexão com o banco por vez (evita travar o celular)
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "sf_database")
                    .fallbackToDestructiveMigration() // Se vocês mudarem o banco no futuro, ele reinstala sem crashar
                    .build();
        }
        return instance;
    }
}