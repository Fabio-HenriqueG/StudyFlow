package com.example.studyflow.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "ChecklistItems")
public class ChecklistItem implements Serializable {
    @PrimaryKey
    @NonNull
    public String id = "";
    public String checklistId; // String para Firebase
    public String texto;
    public boolean isChecked;

    // Construtor vazio para o Firebase
    public ChecklistItem() {
    }

    public ChecklistItem(String checklistId, String texto) {
        this.checklistId = checklistId;
        this.texto = texto;
        this.isChecked = false;
    }
}
