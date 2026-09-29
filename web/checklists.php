<?php
$pageTitle = "Checklists de Estudo";
require_once __DIR__ . '/includes/header.php';
?>

<div class="flex justify-between items-center mb-6">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Seus Checklists</h1>
        <p class="text-sm text-gray-500">Listas de verificação detalhadas com acompanhamento de progresso.</p>
    </div>
    <button id="btnNovoChecklist" class="bg-primary hover:bg-primaryDark text-white px-4 py-2.5 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
        <span class="material-icons-outlined text-lg">fact_check</span> Novo Checklist
    </button>
</div>

<!-- Modal Novo Checklist -->
<div id="modalChecklist" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative">
        <button id="btnFecharModalChecklist" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Criar Novo Checklist</h2>

        <form id="formChecklist" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Título do Checklist</label>
                <input type="text" id="checklistTitle" required placeholder="Ex: Preparativo para o ENEM" class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
            </div>

            <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white py-2.5 rounded-xl font-medium shadow transition">
                Salvar Checklist
            </button>
        </form>
    </div>
</div>

<!-- Lista de Checklists -->
<div id="containerChecklists" class="space-y-6">
    <div class="p-8 text-center text-gray-400 bg-white rounded-2xl border border-gray-100">
        Carregando checklists...
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
        carregarChecklists();
    });

    const modal = document.getElementById('modalChecklist');
    document.getElementById('btnNovoChecklist').addEventListener('click', () => modal.classList.remove('hidden'));
    document.getElementById('btnFecharModalChecklist').addEventListener('click', () => modal.classList.add('hidden'));

    document.getElementById('formChecklist').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentUserId) return;

        const titulo = document.getElementById('checklistTitle').value;
        const ref = db.collection('users').doc(currentUserId).collection('checklists').doc();

        ref.set({
            id: ref.id,
            titulo: titulo,
            dataCriacao: Date.now()
        }).then(() => {
            showToast("Checklist criado com sucesso!");
            modal.classList.add('hidden');
            document.getElementById('formChecklist').reset();
            carregarChecklists();
        });
    });

    function carregarChecklists() {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('checklists').get()
            .then((snapshot) => {
                const container = document.getElementById('containerChecklists');
                container.innerHTML = '';

                if (snapshot.empty) {
                    container.innerHTML = `<div class="p-8 text-center text-gray-500 bg-white rounded-2xl border border-gray-100">Nenhum checklist cadastrado.</div>`;
                    return;
                }

                snapshot.forEach((doc) => {
                    renderizarCardChecklist(container, doc.id, doc.data());
                });
            });
    }

    function renderizarCardChecklist(container, checklistId, data) {
        const card = document.createElement('div');
        card.className = 'card-md3 p-6 border border-gray-100 space-y-4';
        card.id = 'checklist-' + checklistId;

        card.innerHTML = `
            <div class="flex justify-between items-center">
                <h3 class="font-bold text-gray-800 text-lg flex items-center gap-2">
                    <span class="material-icons-outlined text-primary">fact_check</span> ${data.titulo}
                </h3>
                <button onclick="deletarChecklist('${checklistId}')" class="text-gray-400 hover:text-red-600 transition">
                    <span class="material-icons-outlined text-lg">delete</span>
                </button>
            </div>

            <!-- Barra de Progresso -->
            <div>
                <div class="flex justify-between text-xs text-gray-500 mb-1 font-medium">
                    <span>Progresso</span>
                    <span id="progressoTexto-${checklistId}">0% (0/0)</span>
                </div>
                <div class="progress-bar-bg h-2.5">
                    <div id="progressoBarra-${checklistId}" class="progress-bar-fill" style="width: 0%"></div>
                </div>
            </div>

            <!-- Lista de Itens do Checklist -->
            <div id="itensContainer-${checklistId}" class="space-y-2 pt-2 border-t border-gray-100">
                <p class="text-xs text-gray-400">Carregando itens...</p>
            </div>

            <!-- Formulário Adicionar Item -->
            <form onsubmit="adicionarItem(event, '${checklistId}')" class="flex gap-2 pt-2">
                <input type="text" id="inputNovoItem-${checklistId}" required placeholder="Adicionar item à lista..." class="flex-1 px-3 py-1.5 text-xs border border-gray-300 rounded-xl outline-none focus:ring-2 focus:ring-primary">
                <button type="submit" class="bg-purple-100 hover:bg-primary hover:text-white text-primary text-xs font-semibold px-3 py-1.5 rounded-xl transition">
                    + Adicionar
                </button>
            </form>
        `;

        container.appendChild(card);
        carregarItensChecklist(checklistId);
    }

    function carregarItensChecklist(checklistId) {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('checklists').doc(checklistId).collection('items').get()
            .then((snapshot) => {
                const container = document.getElementById(`itensContainer-${checklistId}`);
                container.innerHTML = '';

                let total = snapshot.size;
                let concluidos = 0;

                if (total === 0) {
                    container.innerHTML = `<p class="text-xs text-gray-400 italic">Sua lista está vazia. Adicione itens abaixo.</p>`;
                    atualizarProgresso(checklistId, 0, 0);
                    return;
                }

                snapshot.forEach((doc) => {
                    const item = doc.data();
                    if (item.concluido) concluidos++;

                    const div = document.createElement('div');
                    div.className = 'flex items-center justify-between text-sm py-1';
                    div.innerHTML = `
                        <label class="flex items-center space-x-2 cursor-pointer flex-1">
                            <input type="checkbox" ${item.concluido ? 'checked' : ''} onchange="toggleItem('${checklistId}', '${doc.id}', this.checked)" class="w-4 h-4 text-primary rounded focus:ring-primary">
                            <span class="${item.concluido ? 'line-through text-gray-400' : 'text-gray-700'}">${item.texto}</span>
                        </label>
                        <button onclick="deletarItem('${checklistId}', '${doc.id}')" class="text-gray-300 hover:text-red-500 transition text-xs">
                            <span class="material-icons-outlined text-sm">close</span>
                        </button>
                    `;
                    container.appendChild(div);
                });

                atualizarProgresso(checklistId, concluidos, total);
            });
    }

    function atualizarProgresso(checklistId, concluidos, total) {
        const pct = total > 0 ? Math.round((concluidos / total) * 100) : 0;
        const txt = document.getElementById(`progressoTexto-${checklistId}`);
        const bar = document.getElementById(`progressoBarra-${checklistId}`);

        if (txt) txt.textContent = `${pct}% (${concluidos}/${total})`;
        if (bar) bar.style.width = `${pct}%`;
    }

    window.adicionarItem = function(event, checklistId) {
        event.preventDefault();
        if (!currentUserId) return;

        const input = document.getElementById(`inputNovoItem-${checklistId}`);
        const texto = input.value.trim();
        if (!texto) return;

        const ref = db.collection('users').doc(currentUserId).collection('checklists').doc(checklistId).collection('items').doc();

        ref.set({
            id: ref.id,
            checklistId: checklistId,
            texto: texto,
            concluido: false
        }).then(() => {
            input.value = '';
            carregarItensChecklist(checklistId);
        });
    };

    window.toggleItem = function(checklistId, itemId, status) {
        if (!currentUserId) return;
        db.collection('users').doc(currentUserId).collection('checklists').doc(checklistId).collection('items').doc(itemId)
            .update({ concluido: status })
            .then(() => carregarItensChecklist(checklistId));
    };

    window.deletarItem = function(checklistId, itemId) {
        if (!currentUserId) return;
        db.collection('users').doc(currentUserId).collection('checklists').doc(checklistId).collection('items').doc(itemId)
            .delete()
            .then(() => carregarItensChecklist(checklistId));
    };

    window.deletarChecklist = function(checklistId) {
        if (!currentUserId) return;
        if (confirm("Deseja realmente excluir este checklist?")) {
            db.collection('users').doc(currentUserId).collection('checklists').doc(checklistId).delete()
                .then(() => {
                    showToast("Checklist excluído!");
                    carregarChecklists();
                });
        }
    };
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
