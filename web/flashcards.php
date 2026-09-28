<?php
$pageTitle = "Flashcards";
require_once __DIR__ . '/includes/header.php';
?>

<div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Flashcards para Revisão</h1>
        <p class="text-sm text-gray-500">Memorize conceitos usando repetição espaçada.</p>
    </div>
    <div class="flex items-center gap-2">
        <button id="btnNovaMateria" class="bg-purple-100 hover:bg-purple-200 text-primary px-4 py-2.5 rounded-xl font-medium text-sm transition shadow-sm flex items-center gap-2">
            <span class="material-icons-outlined text-lg">folder_open</span> Nova Matéria
        </button>
        <button id="btnNovoFlashcard" class="bg-primary hover:bg-primaryDark text-white px-4 py-2.5 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
            <span class="material-icons-outlined text-lg">add</span> Novo Flashcard
        </button>
    </div>
</div>

<!-- Modal Nova Matéria -->
<div id="modalMateria" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl relative">
        <button id="btnFecharModalMateria" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Cadastrar Nova Matéria</h2>
        <form id="formMateria" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Nome da Matéria</label>
                <input type="text" id="materiaNome" required placeholder="Ex: Biologia, Matemática..." class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
            </div>
            <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white py-2.5 rounded-xl font-medium shadow transition">
                Salvar Matéria
            </button>
        </form>
    </div>
</div>

<!-- Modal Novo Flashcard -->
<div id="modalFlashcard" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
    <div class="bg-white rounded-2xl max-w-lg w-full p-6 shadow-xl relative">
        <button id="btnFecharModalFlashcard" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
            <span class="material-icons-outlined">close</span>
        </button>
        <h2 class="text-lg font-bold text-gray-800 mb-4">Criar Novo Flashcard</h2>
        <form id="formFlashcard" class="space-y-4">
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Matéria</label>
                <select id="flashcardMateriaId" required class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary">
                    <option value="">Selecione a Matéria...</option>
                </select>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Pergunta (Frente)</label>
                <textarea id="flashcardPergunta" rows="2" required placeholder="O que é a mitocôndria?" class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary"></textarea>
            </div>
            <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">Resposta (Verso)</label>
                <textarea id="flashcardResposta" rows="3" required placeholder="Organela responsável pela respiração celular..." class="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none focus:ring-2 focus:ring-primary"></textarea>
            </div>
            <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white py-2.5 rounded-xl font-medium shadow transition">
                Salvar Flashcard
            </button>
        </form>
    </div>
</div>

<!-- Container Principal -->
<div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
    <!-- Lista de Matérias / Decks (1 coluna) -->
    <div class="space-y-4">
        <h2 class="font-bold text-gray-800 text-lg flex items-center gap-2">
            <span class="material-icons-outlined text-primary">folder</span> Matérias / Decks
        </h2>
        <div id="containerMaterias" class="space-y-2">
            <div class="p-6 text-center text-gray-400 bg-white rounded-2xl border border-gray-100">
                Carregando matérias...
            </div>
        </div>
    </div>

    <!-- Área de Estudo / Flashcard Atual (2 colunas) -->
    <div class="lg:col-span-2 space-y-6">
        <div id="areaEstudo" class="hidden space-y-6">
            <div class="flex justify-between items-center">
                <h2 id="tituloMateriaAtual" class="font-bold text-gray-800 text-lg">Estudando...</h2>
                <span id="contadorFlashcards" class="text-xs font-semibold px-3 py-1 bg-purple-100 text-primary rounded-full">Cartão 1 de 1</span>
            </div>

            <!-- Card de Pergunta/Resposta com Efeito Flip -->
            <div id="flashcardContainer" class="card-md3 min-h-[280px] p-8 flex flex-col justify-between items-center text-center cursor-pointer border border-gray-200 select-none shadow-md transition transform duration-300">
                <div class="w-full flex justify-between text-xs text-gray-400">
                    <span id="labelTipoCard">PERGUNTA</span>
                    <span>Clique para ver a resposta</span>
                </div>
                <div class="my-auto py-6">
                    <h3 id="textoCard" class="text-xl font-bold text-gray-800">Selecione uma matéria ao lado para começar.</h3>
                </div>
                <div class="text-xs text-primary font-medium">
                    (Pressione para virar)
                </div>
            </div>

            <!-- Botões de Avaliação -->
            <div id="painelAvaliacao" class="hidden grid grid-cols-3 gap-4">
                <button onclick="avaliarCard(1)" class="bg-red-50 hover:bg-red-100 text-red-600 font-medium py-3 rounded-xl transition text-sm shadow-sm">
                    Difícil 🔴
                </button>
                <button onclick="avaliarCard(3)" class="bg-amber-50 hover:bg-amber-100 text-amber-600 font-medium py-3 rounded-xl transition text-sm shadow-sm">
                    Bom 🟡
                </button>
                <button onclick="avaliarCard(5)" class="bg-emerald-50 hover:bg-emerald-100 text-emerald-600 font-medium py-3 rounded-xl transition text-sm shadow-sm">
                    Fácil 🟢
                </button>
            </div>
        </div>

        <div id="placeholderEstudo" class="card-md3 p-12 text-center text-gray-400 border border-gray-100">
            <span class="material-icons-outlined text-5xl text-gray-300 mb-2">style</span>
            <p>Selecione uma matéria na lista ao lado para iniciar a revisão dos Flashcards ou crie um novo flashcard.</p>
        </div>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    let flashcardsAtuais = [];
    let indiceAtual = 0;
    let mostrandoResposta = false;

    auth.onAuthStateChanged((user) => {
        if (!user) {
            window.location.href = 'login.php';
            return;
        }
        currentUserId = user.uid;
        carregarMaterias();
    });

    // Modais
    const modalMateria = document.getElementById('modalMateria');
    const modalFlashcard = document.getElementById('modalFlashcard');

    document.getElementById('btnNovaMateria').addEventListener('click', () => modalMateria.classList.remove('hidden'));
    document.getElementById('btnFecharModalMateria').addEventListener('click', () => modalMateria.classList.add('hidden'));

    document.getElementById('btnNovoFlashcard').addEventListener('click', () => {
        carregarSelectMaterias();
        modalFlashcard.classList.remove('hidden');
    });
    document.getElementById('btnFecharModalFlashcard').addEventListener('click', () => modalFlashcard.classList.add('hidden'));

    // Salvar Matéria
    document.getElementById('formMateria').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentUserId) return;

        const nome = document.getElementById('materiaNome').value;
        const ref = db.collection('users').doc(currentUserId).collection('materias').doc();

        ref.set({
            id: ref.id,
            nome: nome,
            cor: -65536 // Cor padrão
        }).then(() => {
            showToast("Matéria criada com sucesso!");
            modalMateria.classList.add('hidden');
            document.getElementById('formMateria').reset();
            carregarMaterias();
        });
    });

    // Salvar Flashcard
    document.getElementById('formFlashcard').addEventListener('submit', (e) => {
        e.preventDefault();
        if (!currentUserId) return;

        const materiaId = document.getElementById('flashcardMateriaId').value;
        const pergunta = document.getElementById('flashcardPergunta').value;
        const resposta = document.getElementById('flashcardResposta').value;
        const ref = db.collection('users').doc(currentUserId).collection('flashcards').doc();

        ref.set({
            id: ref.id,
            materiaId: materiaId,
            pergunta: pergunta,
            resposta: resposta,
            explicacao: '',
            nivelDominio: 0,
            intervalo: 0,
            repeticoes: 0,
            facilidade: 2.5f = 2.5,
            dataProximaRevisao: Date.now(),
            dataCriacao: Date.now()
        }).then(() => {
            showToast("Flashcard criado com sucesso!");
            modalFlashcard.classList.add('hidden');
            document.getElementById('formFlashcard').reset();
            carregarMaterias();
        });
    });

    function carregarSelectMaterias() {
        if (!currentUserId) return;
        db.collection('users').doc(currentUserId).collection('materias').get()
            .then((snapshot) => {
                const select = document.getElementById('flashcardMateriaId');
                select.innerHTML = '<option value="">Selecione a Matéria...</option>';
                snapshot.forEach(doc => {
                    const data = doc.data();
                    const opt = document.createElement('option');
                    opt.value = doc.id;
                    opt.textContent = data.nome;
                    select.appendChild(opt);
                });
            });
    }

    function carregarMaterias() {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('materias').get()
            .then((snapshot) => {
                const container = document.getElementById('containerMaterias');
                container.innerHTML = '';

                if (snapshot.empty) {
                    container.innerHTML = `<div class="p-6 text-center text-gray-500 bg-white rounded-2xl border border-gray-100">Nenhuma matéria encontrada. Clique em "Nova Matéria" acima.</div>`;
                    return;
                }

                snapshot.forEach((doc) => {
                    const data = doc.data();
                    const btn = document.createElement('button');
                    btn.className = 'w-full text-left p-4 rounded-xl bg-white hover:bg-purple-50 hover:text-primary transition card-md3 border border-gray-100 flex items-center justify-between font-medium text-gray-700';
                    btn.innerHTML = `
                        <span class="flex items-center gap-2">
                            <span class="w-3 h-3 rounded-full bg-primary inline-block"></span> ${data.nome}
                        </span>
                        <span class="material-icons-outlined text-sm text-gray-400">chevron_right</span>
                    `;
                    btn.addEventListener('click', () => carregarFlashcardsDaMateria(doc.id, data.nome));
                    container.appendChild(btn);
                });
            });
    }

    function carregarFlashcardsDaMateria(materiaId, materiaNome) {
        if (!currentUserId) return;

        db.collection('users').doc(currentUserId).collection('flashcards')
            .where('materiaId', '==', materiaId).get()
            .then((snapshot) => {
                flashcardsAtuais = [];
                snapshot.forEach(doc => flashcardsAtuais.push({ id: doc.id, ...doc.data() }));

                if (flashcardsAtuais.length === 0) {
                    showToast("Esta matéria não possui flashcards cadastrados.", true);
                    return;
                }

                indiceAtual = 0;
                mostrandoResposta = false;

                document.getElementById('placeholderEstudo').classList.add('hidden');
                document.getElementById('areaEstudo').classList.remove('hidden');
                document.getElementById('tituloMateriaAtual').textContent = materiaNome;

                exibirCardAtual();
            });
    }

    function exibirCardAtual() {
        if (flashcardsAtuais.length === 0 || indiceAtual >= flashcardsAtuais.length) {
            document.getElementById('textoCard').textContent = "🎉 Parabéns! Você concluiu todos os flashcards desta matéria.";
            document.getElementById('labelTipoCard').textContent = "CONCLUÍDO";
            document.getElementById('painelAvaliacao').classList.add('hidden');
            document.getElementById('contadorFlashcards').textContent = "Fim da revisão";
            return;
        }

        const card = flashcardsAtuais[indiceAtual];
        mostrandoResposta = false;

        document.getElementById('labelTipoCard').textContent = "PERGUNTA";
        document.getElementById('textoCard').textContent = card.pergunta;
        document.getElementById('painelAvaliacao').classList.add('hidden');
        document.getElementById('contadorFlashcards').textContent = `Cartão ${indiceAtual + 1} de ${flashcardsAtuais.length}`;
    }

    // Clique no card para virar (Flip)
    document.getElementById('flashcardContainer').addEventListener('click', () => {
        if (flashcardsAtuais.length === 0 || indiceAtual >= flashcardsAtuais.length) return;
        const card = flashcardsAtuais[indiceAtual];

        mostrandoResposta = !mostrandoResposta;
        if (mostrandoResposta) {
            document.getElementById('labelTipoCard').textContent = "RESPOSTA";
            document.getElementById('textoCard').textContent = card.resposta;
            document.getElementById('painelAvaliacao').classList.remove('hidden');
        } else {
            document.getElementById('labelTipoCard').textContent = "PERGUNTA";
            document.getElementById('textoCard').textContent = card.pergunta;
            document.getElementById('painelAvaliacao').classList.add('hidden');
        }
    });

    window.avaliarCard = function(nota) {
        indiceAtual++;
        exibirCardAtual();
    };
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
