<?php
$pageTitle = "Dashboard";
require_once __DIR__ . '/includes/header.php';
?>

<!-- Banner Boas-Vindas -->
<div class="bg-gradient-to-r from-primary to-primaryDark text-white p-6 rounded-2xl shadow-md mb-8 flex justify-between items-center">
    <div>
        <h1 class="text-2xl font-bold mb-1">Olá, <span id="dashUserName">Estudante</span>! 👋</h1>
        <p class="text-purple-100 text-sm">Bem-vindo ao seu painel de estudos no StudyFlow.</p>
    </div>
    <a href="ia_assistente.php" class="bg-white text-primary font-semibold text-sm px-4 py-2.5 rounded-xl hover:bg-purple-50 transition shadow flex items-center gap-2">
        <span class="material-icons-outlined text-lg">psychology</span> Falar com IA
    </a>
</div>

<!-- Cartões de Resumo / Métricas -->
<div class="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
    <div class="card-md3 p-5 flex items-center space-x-4 border-l-4 border-primary">
        <div class="bg-purple-100 p-3 rounded-full text-primary">
            <span class="material-icons-outlined text-2xl">task_alt</span>
        </div>
        <div>
            <p class="text-xs text-gray-500 uppercase font-bold tracking-wider">Tarefas Pendentes</p>
            <h3 id="statTarefasPendentes" class="text-2xl font-bold text-gray-800">0</h3>
        </div>
    </div>

    <div class="card-md3 p-5 flex items-center space-x-4 border-l-4 border-emerald-500">
        <div class="bg-emerald-100 p-3 rounded-full text-emerald-600">
            <span class="material-icons-outlined text-2xl">check_circle</span>
        </div>
        <div>
            <p class="text-xs text-gray-500 uppercase font-bold tracking-wider">Tarefas Concluídas</p>
            <h3 id="statTarefasConcluidas" class="text-2xl font-bold text-gray-800">0</h3>
        </div>
    </div>

    <div class="card-md3 p-5 flex items-center space-x-4 border-l-4 border-amber-500">
        <div class="bg-amber-100 p-3 rounded-full text-amber-600">
            <span class="material-icons-outlined text-2xl">track_changes</span>
        </div>
        <div>
            <p class="text-xs text-gray-500 uppercase font-bold tracking-wider">Metas Ativas</p>
            <h3 id="statMetasAtivas" class="text-2xl font-bold text-gray-800">0</h3>
        </div>
    </div>
</div>

<!-- Seção do Conteúdo Principal -->
<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <!-- Lista de Tarefas Recentes (2 colunas) -->
    <div class="lg:col-span-2 space-y-4">
        <div class="flex justify-between items-center">
            <h2 class="text-lg font-bold text-gray-800 flex items-center gap-2">
                <span class="material-icons-outlined text-primary">format_list_bulleted</span> Próximas Tarefas
            </h2>
            <a href="tarefas.php" class="text-sm font-medium text-primary hover:underline">Ver todas</a>
        </div>

        <div id="dashListaTarefas" class="space-y-3">
            <div class="p-8 text-center text-gray-400 bg-white rounded-2xl border border-gray-100">
                Carregando tarefas do Firebase...
            </div>
        </div>
    </div>

    <!-- Coluna Lateral: Atalhos e Assistente (1 coluna) -->
    <div class="space-y-6">
        <div class="card-md3 p-5 border border-gray-100">
            <h3 class="font-bold text-gray-800 mb-3 flex items-center gap-2">
                <span class="material-icons-outlined text-primary">bolt</span> Ações Rápidas
            </h3>
            <div class="space-y-2">
                <a href="tarefas.php?action=nova" class="w-full flex items-center justify-between p-3 rounded-xl bg-gray-50 hover:bg-purple-50 hover:text-primary transition text-sm font-medium text-gray-700">
                    <span class="flex items-center gap-2"><span class="material-icons-outlined text-base">add_circle_outline</span> Criar Nova Tarefa</span>
                    <span class="material-icons-outlined text-sm">chevron_right</span>
                </a>
                <a href="metas.php?action=nova" class="w-full flex items-center justify-between p-3 rounded-xl bg-gray-50 hover:bg-purple-50 hover:text-primary transition text-sm font-medium text-gray-700">
                    <span class="flex items-center gap-2"><span class="material-icons-outlined text-base">flag</span> Nova Meta de Estudo</span>
                    <span class="material-icons-outlined text-sm">chevron_right</span>
                </a>
            </div>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    auth.onAuthStateChanged((user) => {
        if (!user) {
            window.location.href = 'login.php';
            return;
        }

        document.getElementById('dashUserName').textContent = user.displayName || user.email.split('@')[0];
        carregarDadosDashboard(user.uid);
    });

    function carregarDadosDashboard(userId) {
        // Busca tarefas no Firestore
        db.collection('users').doc(userId).collection('tarefas').get()
            .then((snapshot) => {
                let pendentes = 0;
                let concluidas = 0;
                const containerTarefas = document.getElementById('dashListaTarefas');
                containerTarefas.innerHTML = '';

                if (snapshot.empty) {
                    containerTarefas.innerHTML = `
                        <div class="p-6 text-center text-gray-500 bg-white rounded-2xl border border-gray-100">
                            Nenhuma tarefa cadastrada ainda. <a href="tarefas.php" class="text-primary font-medium underline">Criar tarefa</a>
                        </div>`;
                    return;
                }

                snapshot.forEach((doc) => {
                    const data = doc.data();
                    if (data.concluida) {
                        concluidas++;
                    } else {
                        pendentes++;
                        if (containerTarefas.children.length < 5) {
                            renderizarItemTarefa(containerTarefas, doc.id, data);
                        }
                    }
                });

                document.getElementById('statTarefasPendentes').textContent = pendentes;
                document.getElementById('statTarefasConcluidas').textContent = concluidas;
            })
            .catch((err) => {
                console.error("Erro ao carregar tarefas:", err);
            });

        // Busca metas no Firestore
        db.collection('users').doc(userId).collection('metas').get()
            .then((snapshot) => {
                document.getElementById('statMetasAtivas').textContent = snapshot.size;
            });
    }

    function renderizarItemTarefa(container, id, data) {
        const prioridades = ['Baixa', 'Média', 'Alta'];
        const cores = ['bg-blue-100 text-blue-700', 'bg-amber-100 text-amber-700', 'bg-red-100 text-red-700'];
        const pIndex = data.prioridade || 0;

        const div = document.createElement('div');
        div.className = 'card-md3 p-4 flex justify-between items-center border border-gray-100';
        div.innerHTML = `
            <div>
                <h4 class="font-semibold text-gray-800">${data.titulo}</h4>
                <p class="text-xs text-gray-500">${data.descricao || 'Sem descrição'}</p>
            </div>
            <span class="text-xs font-semibold px-2.5 py-1 rounded-full ${cores[pIndex]}">
                ${prioridades[pIndex]}
            </span>
        `;
        container.appendChild(div);
    }
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
