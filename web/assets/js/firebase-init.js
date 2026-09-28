/**
 * Inicialização e Configuração do Firebase JS SDK
 */

const firebaseConfig = {
    apiKey: "AIzaSyDzG9uGdH_Y9kxXjym9Z91UqtfDwSbKFYM",
    authDomain: "studyflow-3dsetec.firebaseapp.com",
    projectId: "studyflow-3dsetec",
    storageBucket: "studyflow-3dsetec.firebasestorage.app",
    messagingSenderId: "809537622295",
    appId: "1:809537622295:web:studyflow_web_app"
};

// Inicializa o Firebase
if (!firebase.apps.length) {
    firebase.initializeApp(firebaseConfig);
}

const auth = firebase.auth();
const db = firebase.firestore();

// Provedores de Autenticação
const googleProvider = new firebase.auth.GoogleAuthProvider();

console.log("Firebase inicializado com sucesso para StudyFlow Web.");
