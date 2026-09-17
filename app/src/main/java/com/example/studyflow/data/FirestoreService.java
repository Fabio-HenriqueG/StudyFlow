package com.example.studyflow.data;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

public class FirestoreService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference tarefasRef = db.collection("tarefas");
    private final CollectionReference metasRef = db.collection("metas");

    private static FirestoreService instance;

    public static synchronized FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    // CREATE / UPDATE
    public Task<Void> salvarTarefa(Tarefa tarefa) {
        if (tarefa.id == null || tarefa.id.isEmpty()) {
            // Gera um novo ID se não existir
            tarefa.id = tarefasRef.document().getId();
        }
        return tarefasRef.document(tarefa.id).set(tarefa);
    }

    // READ - Ativas
    public Task<QuerySnapshot> buscarTarefasAtivas() {
        return tarefasRef.whereEqualTo("concluida", false)
                .orderBy("dataLimite", Query.Direction.ASCENDING)
                .get();
    }

    // READ - Histórico
    public Task<QuerySnapshot> buscarTarefasConcluidas() {
        return tarefasRef.whereEqualTo("concluida", true)
                .orderBy("dataConclusao", Query.Direction.DESCENDING)
                .get();
    }

    // DELETE
    public Task<Void> excluirTarefa(String id) {
        return tarefasRef.document(id).delete();
    }

    // --- METAS ---
    public Task<Void> salvarMeta(Meta meta) {
        if (meta.id == null || meta.id.isEmpty()) {
            meta.id = metasRef.document().getId();
        }
        return metasRef.document(meta.id).set(meta);
    }

    public Task<QuerySnapshot> buscarMetas() {
        return metasRef.orderBy("dataCriacao", Query.Direction.DESCENDING).get();
    }

    public Task<Void> excluirMeta(String id) {
        return metasRef.document(id).delete();
    }
}
