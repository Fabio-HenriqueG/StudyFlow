<?php
$pageTitle = "Metas de Estudo";
require_once __DIR__ . '/includes/header.php';
?>

<div class="flex justify-between items-center mb-6">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Metas de Estudo</h1>
        <p class="text-sm text-gray-500">Defina e acompanhe seus objetivos diários e semanais.</p>
    </div>
    <button id="btnNovaMeta" class="bg-primary hover:bg-primaryDark text-white px-4 py-2.5 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
        <span class="material-icons-outlined text-lg">flag</span> Nova Meta
    </button>
</div>

<!-- Modal de Criação de Meta -->
<div id="modalMeta" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative">
        <button id="btnFecharModalMeta" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Nova Meta de Estudo</h2>

        <form id="formMeta" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Título da Meta</label>
                <input type="text" id="metaTitle" required placeholder="Ex: Estudar 2 horas de Matemática" class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
            </div>

            <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white py-2.5 rounded-xl font-medium shadow transition">
                Salvar Meta
            </button>
        </form>
    </div>
</div>

<div id="containerMetas" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
    <div class="p-8 text-center text-gray-400 bg-white rounded-2xl border border-gray-100 col-span-full">
        Carregando metas...
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
        carregarMetas();
    });

    const modal = document.getElementById('modalMeta');
    document.getElementById('btnNovaMeta').addEventListener('click', () => modal.classList.remove('hidden'));
    document.getElementById('btnFecharModalMeta').addEventListener('click', () => modal.classList.add('hidden'));

    document.getElementById('formMeta').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentUserId) return;

        const titulo = document.getElementById('metaTitle').value;
        const newDocRef = db.collection('users').doc(currentUserId).collection('metas').doc();

        newDocRef.set({
            id: newDocRef.id,
            userId: currentUserId,
            titulo: titulo,
            dataCriacao: Date.now(),
            ultimoCheckin: Date.now()
        })
        .then(() => {
            showToast("Meta criada com sucesso!");
            modal.classList.add('hidden');
            document.getElementById('formMeta').reset();
            carregarMetas();
        });
    });

    function carregarMetas() {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('metas').get()
            .then((snapshot) => {
                const container = document.getElementById('containerMetas');
                container.innerHTML = '';

                if (snapshot.empty) {
                    container.innerHTML = `<div class="p-8 text-center text-gray-500 bg-white rounded-2xl border border-gray-100 col-span-full">Nenhuma meta cadastrada.</div>`;
                    return;
                }

                snapshot.forEach((doc) => {
                    const data = doc.data();
                    const div = document.createElement('div');
                    div.className = 'card-md3 p-5 border border-gray-100 flex flex-col justify-between';
                    div.innerHTML = `
                        <div>
                            <div class="flex items-center gap-2 text-primary mb-2">
                                <span class="material-icons-outlined">track_changes</span>
                                <span class="text-xs font-bold uppercase tracking-wider">Meta Ativa</span>
                            </div>
                            <h3 class="font-bold text-gray-800 text-lg mb-4">${data.titulo}</h3>
                        </div>
                        <button onclick="fazerCheckin('${doc.id}')" class="w-full bg-purple-50 hover:bg-primary hover:text-white text-primary font-medium py-2 rounded-xl text-sm transition">
                            Fazer Check-in Hoje
                        </button>
                    `;
                    container.appendChild(div);
                });
            });
    }

    window.fazerCheckin = function(id) {
        if (!currentUserId) return;
        db.collection('users').doc(currentUserId).collection('metas').doc(id).update({ ultimoCheckin: Date.now() })
            .then(() => showToast("Check-in realizado com sucesso!"));
    };
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
