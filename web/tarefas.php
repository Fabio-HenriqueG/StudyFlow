<?php
$pageTitle = "Gerenciador de Tarefas";
require_once __DIR__ . '/includes/header.php';
?>

<div class="flex justify-between items-center mb-6">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Suas Tarefas</h1>
        <p class="text-sm text-gray-500">Organize e acompanhe seus deveres de estudo.</p>
    </div>
    <button id="btnNovaTarefa" class="bg-primary hover:bg-primaryDark text-white px-4 py-2.5 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
        <span class="material-icons-outlined text-lg">add</span> Nova Tarefa
    </button>
</div>

<!-- Modal / Formulário de Criação de Tarefa (Oculto por padrão) -->
<div id="modalTarefa" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative">
        <button id="btnFecharModal" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Cadastrar Nova Tarefa</h2>

        <form id="formTarefa" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Título</label>
                <input type="text" id="taskTitle" required class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Descrição</label>
                <textarea id="taskDesc" rows="3" class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary"></textarea>
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Prioridade</label>
                <select id="taskPriority" class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
                    <option value="0">Baixa</option>
                    <option value="1" selected>Média</option>
                    <option value="2">Alta</option>
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

    auth.onAuthStateChanged((user) => {
        if (!user) {
            window.location.href = 'login.php';
            return;
        }
        currentUserId = user.uid;
        carregarTarefas();
    });

    // Controle do Modal
    const modal = document.getElementById('modalTarefa');
    document.getElementById('btnNovaTarefa').addEventListener('click', () => modal.classList.remove('hidden'));
    document.getElementById('btnFecharModal').addEventListener('click', () => modal.classList.add('hidden'));

    // Salvar Tarefa
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
            dataLimite: Date.now() + (86400000 * 2) // Padrão de 2 dias
        })
        .then(() => {
            showToast("Tarefa criada com sucesso!");
            modal.classList.add('hidden');
            document.getElementById('formTarefa').reset();
            carregarTarefas();
        })
        .catch((err) => {
            showToast("Erro ao criar tarefa: " + err.message, true);
        });
    });

    function carregarTarefas() {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('tarefas').get()
            .then((snapshot) => {
                const container = document.getElementById('containerTarefas');
                container.innerHTML = '';

                if (snapshot.empty) {
                    container.innerHTML = `<div class="p-8 text-center text-gray-500 bg-white rounded-2xl border border-gray-100">Você ainda não possui tarefas cadastradas.</div>`;
                    return;
                }

                snapshot.forEach((doc) => {
                    renderizarTarefa(container, doc.id, doc.data());
                });
            });
    }

    function renderizarTarefa(container, id, data) {
        const prioridades = ['Baixa', 'Média', 'Alta'];
        const cores = ['bg-blue-100 text-blue-700', 'bg-amber-100 text-amber-700', 'bg-red-100 text-red-700'];

        const card = document.createElement('div');
        card.className = `card-md3 p-4 flex items-center justify-between border border-gray-100 ${data.concluida ? 'opacity-60 bg-gray-50' : ''}`;
        card.innerHTML = `
            <div class="flex items-center space-x-4">
                <input type="checkbox" ${data.concluida ? 'checked' : ''} class="w-5 h-5 text-primary rounded focus:ring-primary cursor-pointer" onchange="toggleConcluida('${id}', this.checked)">
                <div>
                    <h4 class="font-semibold text-gray-800 ${data.concluida ? 'line-through text-gray-500' : ''}">${data.titulo}</h4>
                    <p class="text-xs text-gray-500">${data.descricao || 'Sem descrição'}</p>
                </div>
            </div>
            <div class="flex items-center space-x-3">
                <span class="text-xs font-semibold px-2.5 py-1 rounded-full ${cores[data.prioridade || 0]}">${prioridades[data.prioridade || 0]}</span>
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
