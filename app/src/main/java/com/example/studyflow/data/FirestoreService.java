package com.example.studyflow.data;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

public class FirestoreService {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static FirestoreService instance;

    public static synchronized FirestoreService getInstance() {
        if (instance == null) {
            instance = new FirestoreService();
        }
        return instance;
    }

    private String getUserId() {
        if (auth.getCurrentUser() != null) {
            return auth.getCurrentUser().getUid();
        }
        return "anonymous"; // Fallback para testes sem login
    }

    public DocumentReference getUserDoc() {
        return db.collection("users").document(getUserId());
    }

    // --- TAREFAS ---
    public CollectionReference getTarefasRef() {
        return getUserDoc().collection("tarefas");
    }

    public Task<Void> salvarTarefa(Tarefa tarefa) {
        if (tarefa.id == null || tarefa.id.isEmpty()) {
            tarefa.id = getTarefasRef().document().getId();
        }
        Log.d("FirestoreService", "Salvando tarefa: " + tarefa.id + " para user: " + getUserId());
        return getTarefasRef().document(tarefa.id).set(tarefa);
    }

    public Task<QuerySnapshot> buscarTarefasAtivas() {
        return getTarefasRef().whereEqualTo("concluida", false).get();
    }

    public Task<QuerySnapshot> buscarTarefasConcluidas() {
        return getTarefasRef().whereEqualTo("concluida", true).get();
    }

    public Task<Void> excluirTarefa(String id) {
        return getTarefasRef().document(id).delete();
    }

    // --- METAS ---
    public CollectionReference getMetasRef() {
        return getUserDoc().collection("metas");
    }

    public Task<Void> salvarMeta(Meta meta) {
        if (meta.id == null || meta.id.isEmpty()) {
            meta.id = getMetasRef().document().getId();
        }
        return getMetasRef().document(meta.id).set(meta);
    }

    public Task<QuerySnapshot> buscarMetas() {
        return getMetasRef().get();
    }

    public Task<Void> excluirMeta(String id) {
        return getMetasRef().document(id).delete();
    }

    // --- ANOTAÇÕES ---
    public CollectionReference getAnotacoesRef() {
        return getUserDoc().collection("anotacoes");
    }

    public Task<Void> salvarAnotacao(Anotacao anotacao) {
        if (anotacao.id == null || anotacao.id.isEmpty()) {
            anotacao.id = getAnotacoesRef().document().getId();
        }
        return getAnotacoesRef().document(anotacao.id).set(anotacao);
    }

    public Task<QuerySnapshot> buscarAnotacoes() {
        return getAnotacoesRef().orderBy("dataUltimaEdicao", Query.Direction.DESCENDING).get();
    }

    public Task<Void> excluirAnotacao(String id) {
        return getAnotacoesRef().document(id).delete();
    }

    // --- CHECKLISTS ---
    public CollectionReference getChecklistsRef() {
        return getUserDoc().collection("checklists");
    }

    public Task<Void> salvarChecklist(Checklist checklist) {
        if (checklist.id == null || checklist.id.isEmpty()) {
            checklist.id = getChecklistsRef().document().getId();
        }
        return getChecklistsRef().document(checklist.id).set(checklist);
    }

    public Task<QuerySnapshot> buscarChecklists() {
        return getChecklistsRef().get();
    }

    public Task<Void> excluirChecklist(String id) {
        // Nota: Excluir itens vinculados deve ser feito separadamente ou via Cloud Function
        return getChecklistsRef().document(id).delete();
    }

    // --- CHECKLIST ITEMS ---
    public CollectionReference getChecklistItemsRef(String checklistId) {
        return getChecklistsRef().document(checklistId).collection("items");
    }

    public Task<Void> salvarChecklistItem(String checklistId, ChecklistItem item) {
        if (item.id == null || item.id.isEmpty()) {
            item.id = getChecklistItemsRef(checklistId).document().getId();
        }
        return getChecklistItemsRef(checklistId).document(item.id).set(item);
    }

    public Task<QuerySnapshot> buscarItensPorChecklist(String checklistId) {
        return getChecklistItemsRef(checklistId).get();
    }

    public Task<Void> excluirChecklistItem(String checklistId, String itemId) {
        return getChecklistItemsRef(checklistId).document(itemId).delete();
    }

    // --- MATÉRIAS / SEÇÕES ---
    public CollectionReference getMateriasRef() {
        return getUserDoc().collection("materias");
    }

    public Task<Void> salvarMateria(Materia materia) {
        if (materia.id == null || materia.id.isEmpty()) {
            materia.id = getMateriasRef().document().getId();
        }
        return getMateriasRef().document(materia.id).set(materia);
    }

    public Task<QuerySnapshot> buscarMaterias() {
        return getMateriasRef().get();
    }

    public Task<Void> excluirMateria(String id) {
        return getMateriasRef().document(id).delete();
    }

    // --- FLASHCARDS ---
    public CollectionReference getFlashcardsRef() {
        return getUserDoc().collection("flashcards");
    }

    public Task<Void> salvarFlashcard(Flashcard flashcard) {
        if (flashcard.id == null || flashcard.id.isEmpty()) {
            flashcard.id = getFlashcardsRef().document().getId();
        }
        return getFlashcardsRef().document(flashcard.id).set(flashcard);
    }

    public Task<QuerySnapshot> buscarFlashcardsPorMateria(String materiaId) {
        return getFlashcardsRef().whereEqualTo("materiaId", materiaId).get();
    }

    public Task<QuerySnapshot> buscarFlashcardsParaRevisar(long hoje) {
        return getFlashcardsRef().whereLessThanOrEqualTo("dataProximaRevisao", hoje).get();
    }

    public Task<Void> excluirFlashcard(String id) {
        return getFlashcardsRef().document(id).delete();
    }
}
