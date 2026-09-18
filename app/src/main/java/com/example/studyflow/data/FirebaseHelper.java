package com.example.studyflow.data;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseHelper {
    public static CollectionReference getAnotacoesRef() {
        return FirebaseFirestore.getInstance().collection("anotacoes");
    }
}
