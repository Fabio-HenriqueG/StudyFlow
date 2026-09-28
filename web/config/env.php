<?php
/**
 * Configurações de Ambiente - StudyFlow Web
 */

if (!defined('APP_NAME')) {
    define('APP_NAME', 'StudyFlow');
}

// Configurações do Firebase
define('FIREBASE_API_KEY', 'AIzaSyDzG9uGdH_Y9kxXjym9Z91UqtfDwSbKFYM');
define('FIREBASE_AUTH_DOMAIN', 'studyflow-3dsetec.firebaseapp.com');
define('FIREBASE_PROJECT_ID', 'studyflow-3dsetec');
define('FIREBASE_STORAGE_BUCKET', 'studyflow-3dsetec.firebasestorage.app');
define('FIREBASE_MESSAGING_SENDER_ID', '809537622295');
define('FIREBASE_APP_ID', '1:809537622295:web:studyflow_web_app');

// Configuração de API de Inteligência Artificial (Google Gemini)
// Substitua pela sua chave obtida no Google AI Studio (https://aistudio.google.com/)
define('GEMINI_API_KEY', getenv('GEMINI_API_KEY') ?: 'SUA_GEMINI_API_KEY_AQUI');
define('GEMINI_API_URL', 'https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent');
