<?php
$pageTitle = "Gerenciador de Tarefas";
require_once __DIR__ . '/includes/header.php';
?>

<div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Suas Tarefas</h1>
        <p class="text-sm text-gray-500">Organize seus compromissos e prioridades de estudo.</p>
    </div>
    <button id="btnNovaTarefa" class="bg-primary hover:bg-primaryDark text-white px-5 py-2.5 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
        <span class="material-icons-outlined text-lg">add</span> Nova Tarefa
    </button>
</div>

<!-- Filtros de Prioridade (Chips M3 estilo Android) -->
<div class="flex items-center space-x-2 mb-6 overflow-x-auto pb-2">
    <button onclick="filtrarPrioridade('todas')" id="chip-todas" class="px-4 py-2 rounded-xl text-xs font-semibold bg-primary text-white transition shadow-sm">
        Todas
    </button>
    <button onclick="filtrarPrioridade('0')" id="chip-baixa" class="px-4 py-2 rounded-xl text-xs font-semibold chip-prioridade-baixa transition hover:opacity-80">
        Baixa Prioridade
    </button>
    <button onclick="filtrarPrioridade('1')" id="chip-media" class="px-4 py-2 rounded-xl text-xs font-semibold chip-prioridade-media transition hover:opacity-80">
        Média Prioridade
    </button>
    <button onclick="filtrarPrioridade('2')" id="chip-alta" class="px-4 py-2 rounded-xl text-xs font-semibold chip-prioridade-alta transition hover:opacity-80">
        Alta Prioridade
    </button>
</div>

<!-- Modal / Formulário de Criação de Tarefa -->
<div id="modalTarefa" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative">
        <button id="btnFecharModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Cadastrar Nova Tarefa</h2>

        <form id="formTarefa" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Título</label>
                <input type="text" id="taskTitle" required placeholder="Ex: Resolver lista de Física" class="w-full px-4 py-2 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-primary">
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Descrição</label>
                <textarea id="taskDesc" rows="3" placeholder="Detalhes adicionais..." class="w-full px-4 py-2 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-primary"></textarea>
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nível de Prioridade</label>
                <select id="taskPriority" class="w-full px-4 py-2 border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-primary">
                    <option value="0">Baixa Nível</option>
                    <option value="1" selected>Média Nível</option>
                    <option value="2">Alta Nível</option>
                </select>
            </div>

            <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white py-2.5 rounded-xl font-medium shadow transition">
                Salvar Tarefa
            </button>
        </form>
    </div>
</div>

<!-- Lista de Tarefas -->
<div id="containerTarefas" class="space-y-3">
    <div class="p-8 text-center text-gray-400 bg-white rounded-2xl border border-gray-100">
        Carregando tarefas...
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    let todasTarefas = [];
    let filtroAtual = 'todas';

    auth.onAuthStateChanged((user) => {
        if (!user) {
            window.location.href = 'login.php';
            return;
        }
        currentUserId = user.uid;
        carregarTarefas();
    });

    const modal = document.getElementById('modalTarefa');
    document.getElementById('btnNovaTarefa').addEventListener('click', () => modal.classList.remove('hidden'));
    document.getElementById('btnFecharModal').addEventListener('click', () => modal.classList.add('hidden'));

    document.getElementById('formTarefa').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentUserId) return;

        const titulo = document.getElementById('taskTitle').value;
        const descricao = document.getElementById('taskDesc').value;
        const prioridade = parseInt(document.getElementById('taskPriority').value);

        const newDocRef = db.collection('users').doc(currentUserId).collection('tarefas').doc();

        newDocRef.set({
            id: newDocRef.id,
            userId: currentUserId,
            titulo: titulo,
            descricao: descricao,
            prioridade: prioridade,
            concluida: false,
            dataLimite: Date.now() + (86400000 * 2)
        })
        .then(() => {
            showToast("Tarefa criada com sucesso!");
            modal.classList.add('hidden');
            document.getElementById('formTarefa').reset();
            carregarTarefas();
        });
    });

    function carregarTarefas() {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('tarefas').get()
            .then((snapshot) => {
                todasTarefas = [];
                snapshot.forEach((doc) => {
                    todasTarefas.push({ id: doc.id, ...doc.data() });
                });
                renderizarListaFiltrada();
            });
    }

    window.filtrarPrioridade = function(prioridade) {
        filtroAtual = prioridade;
        ['todas', '0', '1', '2'].forEach(p => {
            const btn = document.getElementById('chip-' + (p === 'todas' ? 'todas' : (p === '0' ? 'baixa' : (p === '1' ? 'media' : 'alta'))));
            if (btn) {
                if (p === prioridade) {
                    btn.className = 'px-4 py-2 rounded-xl text-xs font-semibold bg-primary text-white transition shadow-sm';
                } else {
                    btn.className = 'px-4 py-2 rounded-xl text-xs font-semibold bg-gray-100 text-gray-600 transition hover:bg-gray-200';
                }
            }
        });
        renderizarListaFiltrada();
    };

    function renderizarListaFiltrada() {
        const container = document.getElementById('containerTarefas');
        container.innerHTML = '';

        const filtradas = todasTarefas.filter(t => filtroAtual === 'todas' || t.prioridade == filtroAtual);

        if (filtradas.length === 0) {
            container.innerHTML = `<div class="p-8 text-center text-gray-500 bg-white rounded-2xl border border-gray-100">Nenhuma tarefa encontrada.</div>`;
            return;
        }

        filtradas.forEach((data) => {
            renderizarTarefa(container, data.id, data);
        });
    }

    function renderizarTarefa(container, id, data) {
        const prioridades = ['Baixa', 'Média', 'Alta'];
        const classesChip = ['chip-prioridade-baixa', 'chip-prioridade-media', 'chip-prioridade-alta'];

        const card = document.createElement('div');
        card.className = `card-md3 p-4 flex items-center justify-between border border-gray-100 ${data.concluida ? 'opacity-60 bg-gray-50' : ''}`;
        card.innerHTML = `
            <div class="flex items-center space-x-4">
                <input type="checkbox" ${data.concluida ? 'checked' : ''} class="w-5 h-5 text-primary rounded focus:ring-primary cursor-pointer" onchange="toggleConcluida('${id}', this.checked)">
                <div>
                    <h4 class="font-semibold text-gray-800 ${data.concluida ? 'line-through text-gray-400' : ''}">${data.titulo}</h4>
                    <p class="text-xs text-gray-500">${data.descricao || 'Sem descrição'}</p>
                </div>
            </div>
            <div class="flex items-center space-x-3">
                <span class="text-xs font-semibold px-3 py-1 rounded-full ${classesChip[data.prioridade || 0]}">${prioridades[data.prioridade || 0]}</span>
                <button onclick="deletarTarefa('${id}')" class="text-gray-400 hover:text-red-600 transition p-1">
                    <span class="material-icons-outlined text-lg">delete</span>
                </button>
            </div>
        `;
        container.appendChild(card);
    }

    window.toggleConcluida = function(id, status) {
        if (!currentUserId) return;
        db.collection('users').doc(currentUserId).collection('tarefas').doc(id).update({ concluida: status })
            .then(() => carregarTarefas());
    };

    window.deletarTarefa = function(id) {
        if (!currentUserId) return;
        if (confirm("Deseja realmente excluir esta tarefa?")) {
            db.collection('users').doc(currentUserId).collection('tarefas').doc(id).delete()
                .then(() => {
                    showToast("Tarefa removida!");
                    carregarTarefas();
                });
        }
    };
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
