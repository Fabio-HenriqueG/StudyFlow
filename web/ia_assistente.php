<?php
$pageTitle = "Assistente IA";
require_once __DIR__ . '/includes/header.php';
?>

<div class="max-w-4xl mx-auto flex flex-col h-[calc(100vh-12rem)]">
    <!-- Cabeçalho do Chat -->
    <div class="bg-white p-4 rounded-t-2xl shadow-sm border border-gray-100 flex items-center space-x-3">
        <div class="bg-purple-100 p-2.5 rounded-xl text-primary">
            <span class="material-icons-outlined text-2xl">psychology</span>
        </div>
        <div>
            <h2 class="font-bold text-gray-800">Tutor de Estudos IA</h2>
            <p class="text-xs text-gray-500">Tire dúvidas de matérias, crie resumos e organize seu cronograma.</p>
        </div>
    </div>

    <!-- Área de Mensagens do Chat -->
    <div id="chatBox" class="flex-1 bg-gray-50 p-6 overflow-y-auto space-y-4 border-x border-gray-100">
        <!-- Mensagem de Boas-Vindas da IA -->
        <div class="flex items-start space-x-3">
            <div class="bg-primary text-white p-2 rounded-xl text-xs font-bold">IA</div>
            <div class="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 max-w-2xl text-sm text-gray-800">
                Olá! Sou o seu assistente de estudos do StudyFlow. Em que matéria ou tarefa posso te ajudar hoje?
            </div>
        </div>
    </div>

    <!-- Sugestões de Perguntas Rápidas -->
    <div class="bg-white px-4 py-2 border-x border-gray-100 flex items-center gap-2 overflow-x-auto text-xs">
        <span class="text-gray-400 font-medium">Sugestões:</span>
        <button class="btnSugestao bg-purple-50 text-primary hover:bg-purple-100 px-3 py-1.5 rounded-full transition whitespace-nowrap">
            Como organizar um cronograma de estudos?
        </button>
        <button class="btnSugestao bg-purple-50 text-primary hover:bg-purple-100 px-3 py-1.5 rounded-full transition whitespace-nowrap">
            Me explique a técnica Pomodoro
        </button>
        <button class="btnSugestao bg-purple-50 text-primary hover:bg-purple-100 px-3 py-1.5 rounded-full transition whitespace-nowrap">
            Dicas para manter o foco na prova
        </button>
    </div>

    <!-- Formulário de Envio -->
    <div class="bg-white p-4 rounded-b-2xl border border-gray-100 shadow-md">
        <form id="formChat" class="flex items-center space-x-3">
            <input type="text" id="inputPrompt" required placeholder="Digite sua pergunta para o tutor IA..." class="flex-1 px-4 py-3 border border-gray-200 rounded-xl outline-none focus:ring-2 focus:ring-primary text-sm transition">
            <button type="submit" id="btnEnviar" class="bg-primary hover:bg-primaryDark text-white px-5 py-3 rounded-xl font-medium text-sm transition shadow flex items-center gap-2">
                <span>Enviar</span>
                <span class="material-icons-outlined text-base">send</span>
            </button>
        </form>
    </div>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const chatBox = document.getElementById('chatBox');
    const formChat = document.getElementById('formChat');
    const inputPrompt = document.getElementById('inputPrompt');
    const btnEnviar = document.getElementById('btnEnviar');

    // Botões de sugestão
    document.querySelectorAll('.btnSugestao').forEach(btn => {
        btn.addEventListener('click', () => {
            inputPrompt.value = btn.innerText;
            formChat.dispatchEvent(new Event('submit'));
        });
    });

    formChat.addEventListener('submit', (e) => {
        e.preventDefault();
        const prompt = inputPrompt.value.trim();
        if (!prompt) return;

        // Adiciona mensagem do usuário na tela
        adicionarMensagem('Você', prompt, true);
        inputPrompt.value = '';

        // Exibe indicador de carregamento da IA
        const typingId = adicionarTyping();

        btnEnviar.disabled = true;

        // Envia requisição para o backend PHP seguro
        fetch('api/chat_ia.php', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ prompt: prompt })
        })
        .then(res => res.json())
        .then(data => {
            removerTyping(typingId);
            btnEnviar.disabled = false;

            if (data.response) {
                adicionarMensagem('IA', data.response, false);
            } else if (data.error) {
                adicionarMensagem('IA', 'Ops! ' + data.error, false, true);
            }
        })
        .catch(err => {
            removerTyping(typingId);
            btnEnviar.disabled = false;
            adicionarMensagem('IA', 'Erro ao conectar com o servidor PHP: ' + err.message, false, true);
        });
    });

    function adicionarMensagem(autor, texto, isUser = false, isError = false) {
        const div = document.createElement('div');
        div.className = `flex items-start space-x-3 ${isUser ? 'flex-row-reverse space-x-reverse' : ''}`;

        const badge = document.createElement('div');
        badge.className = isUser
            ? 'bg-gray-700 text-white p-2 rounded-xl text-xs font-bold'
            : (isError ? 'bg-red-600 text-white p-2 rounded-xl text-xs font-bold' : 'bg-primary text-white p-2 rounded-xl text-xs font-bold');
        badge.innerText = autor;

        const content = document.createElement('div');
        content.className = isUser
            ? 'bg-primary text-white p-4 rounded-2xl shadow-sm max-w-2xl text-sm'
            : (isError ? 'bg-red-50 text-red-700 p-4 rounded-2xl border border-red-100 max-w-2xl text-sm' : 'bg-white p-4 rounded-2xl shadow-sm border border-gray-100 max-w-2xl text-sm text-gray-800');

        // Formata quebras de linha
        content.innerHTML = texto.replace(/\n/g, '<br>');

        div.appendChild(badge);
        div.appendChild(content);

        chatBox.appendChild(div);
        chatBox.scrollTop = chatBox.scrollHeight;
    }

    function adicionarTyping() {
        const id = 'typing-' + Date.now();
        const div = document.createElement('div');
        div.id = id;
        div.className = 'flex items-start space-x-3';
        div.innerHTML = `
            <div class="bg-primary text-white p-2 rounded-xl text-xs font-bold">IA</div>
            <div class="bg-white p-4 rounded-2xl shadow-sm border border-gray-100 max-w-2xl text-sm text-gray-400 animate-pulse">
                Digitando resposta...
            </div>
        `;
        chatBox.appendChild(div);
        chatBox.scrollTop = chatBox.scrollHeight;
        return id;
    }

    function removerTyping(id) {
        const el = document.getElementById(id);
        if (el) el.remove();
    }
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
