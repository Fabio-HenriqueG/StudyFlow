<?php
$pageTitle = "Anotações";
require_once __DIR__ . '/includes/header.php';
?>

<div class="flex justify-between items-center mb-6">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Suas Anotações</h1>
        <p class="text-sm text-gray-500">Cadernos e resumos sincronizados com o seu aplicativo.</p>
    </div>
    <button id="btnNovaAnotacao" class="bg-primary hover:bg-primaryDark text-white px-4 py-2.5 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
        <span class="material-icons-outlined text-lg">add</span> Nova Anotação
    </button>
</div>

<!-- Modal de Criação de Anotação -->
<div id="modalAnotacao" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-lg w-full p-6 shadow-xl relative">
        <button id="btnFecharModalAnotacao" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Criar Nova Anotação</h2>

        <form id="formAnotacao" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Título</label>
                <input type="text" id="anotacaoTitle" required placeholder="Ex: Resumo de História - Revolução Francesa" class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
            </div>

            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Conteúdo</label>
                <textarea id="anotacaoContent" rows="6" required placeholder="Digite o conteúdo da sua anotação..." class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary"></textarea>
            </div>

            <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white py-2.5 rounded-xl font-medium shadow transition">
                Salvar Anotação
            </button>
        </form>
    </div>
</div>

<div id="containerAnotacoes" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
    <div class="p-8 text-center text-gray-400 bg-white rounded-2xl border border-gray-100 col-span-full">
        Carregando anotações do Firestore...
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
        carregarAnotacoes();
    });

    const modal = document.getElementById('modalAnotacao');
    document.getElementById('btnNovaAnotacao').addEventListener('click', () => modal.classList.remove('hidden'));
    document.getElementById('btnFecharModalAnotacao').addEventListener('click', () => modal.classList.add('hidden'));

    document.getElementById('formAnotacao').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentUserId) return;

        const titulo = document.getElementById('anotacaoTitle').value;
        const conteudo = document.getElementById('anotacaoContent').value;
        const newDocRef = db.collection('users').doc(currentUserId).collection('anotacoes').doc();

        newDocRef.set({
            id: newDocRef.id,
            titulo: titulo,
            conteudoHtml: conteudo,
            dataUltimaEdicao: Date.now()
        })
        .then(() => {
            showToast("Anotação criada com sucesso!");
            modal.classList.add('hidden');
            document.getElementById('formAnotacao').reset();
            carregarAnotacoes();
        })
        .catch(err => {
            showToast("Erro ao salvar anotação: " + err.message, true);
        });
    });

    function carregarAnotacoes() {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('anotacoes').orderBy('dataUltimaEdicao', 'desc').get()
            .then((snapshot) => {
                const container = document.getElementById('containerAnotacoes');
                container.innerHTML = '';

                if (snapshot.empty) {
                    container.innerHTML = `<div class="p-8 text-center text-gray-500 bg-white rounded-2xl border border-gray-100 col-span-full">Nenhuma anotação encontrada. Crie anotações pelo botão acima ou no aplicativo Android.</div>`;
                    return;
                }

                snapshot.forEach((doc) => {
                    const data = doc.data();
                    const dataFormatada = new Date(data.dataUltimaEdicao || Date.now()).toLocaleDateString('pt-BR');

                    const div = document.createElement('div');
                    div.className = 'card-md3 p-5 border border-gray-100 flex flex-col justify-between hover:border-primary/50 transition cursor-pointer';
                    div.onclick = () => window.location.href = `anotacoes_livres.php?id=${doc.id}`;
                    div.innerHTML = `
                        <div>
                            <div class="flex items-center justify-between mb-3">
                                <span class="material-icons-outlined text-primary text-3xl">description</span>
                                <span class="text-xs text-gray-400">${dataFormatada}</span>
                            </div>
                            <h3 class="font-bold text-gray-800 text-lg mb-2">${data.titulo || 'Sem Título'}</h3>
                            <p class="text-xs text-gray-500 line-clamp-3">${data.conteudoHtml ? data.conteudoHtml.replace(/<[^>]*>?/gm, '') : 'Anotação sem texto'}</p>
                        </div>
                        <div class="mt-4 pt-3 border-t border-gray-100 flex justify-between items-center text-xs">
                            <span class="text-primary font-medium flex items-center gap-1"><span class="material-icons-outlined text-sm">open_in_new</span> Abrir Quadro</span>
                            <button onclick="deletarAnotacao(event, '${doc.id}')" class="text-gray-400 hover:text-red-600 transition flex items-center gap-1">
                                <span class="material-icons-outlined text-base">delete</span> Excluir
                            </button>
                        </div>
                    `;
                    container.appendChild(div);
                });
            })
            .catch(err => {
                console.error("Erro ao carregar anotações:", err);
            });
    }

    window.deletarAnotacao = function(event, id) {
        event.stopPropagation();
        if (!currentUserId) return;
        if (confirm("Deseja realmente excluir esta anotação?")) {
            db.collection('users').doc(currentUserId).collection('anotacoes').doc(id).delete()
                .then(() => {
                    showToast("Anotação removida!");
                    carregarAnotacoes();
                });
        }
    };
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
