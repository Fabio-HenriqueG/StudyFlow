package com.example.studyflow.data.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.studyflow.data.Checklist;
import com.example.studyflow.data.ChecklistItem;
import java.util.List;

@Dao
public interface ChecklistDao {
    // --- Lógica para as Listas (Checklists) ---
    @Insert
    long inserir(Checklist checklist);

    @Update
    void atualizar(Checklist checklist);

    @Delete
    void excluir(Checklist checklist);

    @Query("SELECT * FROM checklists ORDER BY isPinned DESC, id DESC")
    List<Checklist> buscarTodas();

    @Query("SELECT COUNT(*) FROM checklists")
    int contarTodas();

    // --- Lógica para os Tópicos (ChecklistItems) ---
    @Insert
    void inserirItem(ChecklistItem item);

    @Update
    void atualizarItem(ChecklistItem item);

    @Delete
    void excluirItem(ChecklistItem item);

    @Query("SELECT * FROM checklistitems WHERE checklistId = :checklistId ORDER BY id ASC")
    List<ChecklistItem> buscarItensPorChecklist(int checklistId);
}
