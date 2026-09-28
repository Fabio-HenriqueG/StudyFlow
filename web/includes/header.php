<?php
require_once __DIR__ . '/../config/env.php';
$currentPage = basename($_SERVER['PHP_SELF'], '.php');
?>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><?= isset($pageTitle) ? $pageTitle . ' - ' . APP_NAME : APP_NAME ?></title>

    <!-- Tailwind CSS CDN -->
    <script src="https://cdn.tailwindcss.com"></script>
    <script>
        tailwind.config = {
            theme: {
                extend: {
                    colors: {
                        primary: '#6750A4',
                        primaryDark: '#4F378B',
                        secondary: '#625B71',
                        surface: '#FEF7FF',
                        surfaceVariant: '#E7E0EC',
                        accent: '#7D5260'
                    }
                }
            }
        }
    </script>

    <!-- Material Icons & Google Fonts -->
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons+Outlined" rel="stylesheet">
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">

    <!-- Estilos Personalizados -->
    <link rel="stylesheet" href="assets/css/style.css">
</head>
<body class="bg-gray-50 text-gray-800 font-sans min-h-screen flex flex-col">

    <!-- Navbar Principal -->
    <header class="bg-primary text-white shadow-md sticky top-0 z-50">
        <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex justify-between items-center h-16">
            <!-- Logo e Título -->
            <div class="flex items-center space-x-3">
                <span class="material-icons-outlined text-3xl">auto_stories</span>
                <a href="index.php" class="font-bold text-xl tracking-wide">StudyFlow</a>
            </div>

            <!-- Links de Navegação (Desktop) -->
            <nav class="hidden md:flex space-x-1">
                <a href="index.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'index' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">dashboard</span> Dashboard
                </a>
                <a href="tarefas.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'tarefas' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">task_alt</span> Tarefas
                </a>
                <a href="metas.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'metas' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">track_changes</span> Metas
                </a>
                <a href="anotacoes.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'anotacoes' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">description</span> Anotações
                </a>
                <a href="anotacoes_livres.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'anotacoes_livres' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">brush</span> Quadro Livre
                </a>
                <a href="flashcards.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'flashcards' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">style</span> Flashcards
                </a>
                <a href="ia_assistente.php" class="px-3 py-2 rounded-lg text-sm font-medium transition flex items-center gap-1 <?= $currentPage === 'ia_assistente' ? 'bg-primaryDark text-white' : 'hover:bg-primaryDark/50' ?>">
                    <span class="material-icons-outlined text-sm">psychology</span> Assistente IA
                </a>
            </nav>

            <!-- Usuário / Login / Logout -->
            <div class="flex items-center space-x-3">
                <div id="userProfile" class="hidden flex items-center space-x-2">
                    <span id="userName" class="text-sm font-medium"></span>
                    <button id="btnLogout" class="bg-primaryDark hover:bg-red-700 text-white p-2 rounded-full transition flex items-center justify-center" title="Sair">
                        <span class="material-icons-outlined text-sm">logout</span>
                    </button>
                </div>
                <a id="btnLoginNav" href="login.php" class="hidden bg-white text-primary font-medium text-sm px-4 py-2 rounded-lg hover:bg-gray-100 transition shadow">
                    Entrar
                </a>
            </div>
        </div>
    </header>

    <!-- Conteúdo Principal -->
    <main class="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6">
