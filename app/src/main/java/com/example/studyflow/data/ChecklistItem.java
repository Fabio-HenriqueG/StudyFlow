package com.example.studyflow.data;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import java.io.Serializable;

@Entity(tableName = "ChecklistItems",
        foreignKeys = @ForeignKey(entity = Checklist.class,
                parentColumns = "id",
                childColumns = "checklistId",
                onDelete = ForeignKey.CASCADE))
public class ChecklistItem implements Serializable {
    @PrimaryKey(autoGenerate = true)
    public int id;
    public int checklistId;
    public String texto;
    public boolean isChecked;

    public ChecklistItem(int checklistId, String texto) {
        this.checklistId = checklistId;
        this.texto = texto;
        this.isChecked = false;
    }
}
