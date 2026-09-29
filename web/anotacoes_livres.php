<?php
$pageTitle = "Quadro de Anotações Livres";
require_once __DIR__ . '/includes/header.php';
?>

<style>
/* Grid Quadriculado Borda a Borda */
.quadro-grid-full {
    background-color: #FFFFFF;
    background-image:
        linear-gradient(to right, #D1C4E9 1.5px, transparent 1.5px),
        linear-gradient(to bottom, #D1C4E9 1.5px, transparent 1.5px);
    background-size: 40px 40px;
}

/* Quando em Modo Imersivo, zera margens da página */
body.modo-imersivo-ativo main {
    padding: 0 !important;
    max-width: 100% !important;
    margin: 0 !important;
}

body.modo-imersivo-ativo footer {
    display: none !important;
}
</style>

<!-- Título Superior (Removido no Modo Imersivo) -->
<div id="secaoCabecalhoPagina" class="flex justify-between items-center mb-3 px-2">
    <div>
        <h1 class="text-2xl font-bold text-gray-800">Quadro de Anotações Livres</h1>
        <p class="text-sm text-gray-500">Desenhos, textos, imagens e stickers. Clique com botão direito nos itens para abrir opções.</p>
    </div>
</div>

<!-- Container do Editor -->
<div id="editorWrapper" class="relative w-full transition-all duration-300">

    <!-- Barra de Ferramentas Flutuante -->
    <div id="barraFerramentas" class="card-md3 p-3 mb-2 flex flex-wrap items-center justify-between gap-3 border border-gray-200 shadow-xl bg-white/95 backdrop-blur-md sticky top-16 z-40 rounded-2xl">
        <div class="flex items-center gap-2 flex-wrap">
            <!-- Botão Alternar Modo Imersivo -->
            <button id="btnToggleImersivo" class="px-3.5 py-2 rounded-xl text-xs font-semibold bg-purple-100 hover:bg-purple-200 text-primary transition flex items-center gap-1.5 shadow-sm">
                <span class="material-icons-outlined text-base">fullscreen</span> <span id="lblImersivo">Modo Imersivo</span>
            </button>

            <div class="h-6 w-[1px] bg-gray-200"></div>

            <!-- Ferramentas Principais -->
            <button id="btnToolSelecionar" class="px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 bg-primary text-white shadow">
                <span class="material-icons-outlined text-sm">near_me</span> Selecionar
            </button>
            <button id="btnToolPincel" class="px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 bg-gray-100 text-gray-700 hover:bg-gray-200">
                <span class="material-icons-outlined text-sm">brush</span> Pincel
            </button>
            <button id="btnToolTexto" class="px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 bg-gray-100 text-gray-700 hover:bg-gray-200">
                <span class="material-icons-outlined text-sm">text_fields</span> Texto
            </button>
            <button id="btnToolImagem" class="px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 bg-gray-100 text-gray-700 hover:bg-gray-200">
                <span class="material-icons-outlined text-sm">image</span> Imagem
            </button>
            <button id="btnToolSticker" class="px-3.5 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 bg-gray-100 text-gray-700 hover:bg-gray-200">
                <span class="material-icons-outlined text-sm">emoji_emotions</span> Sticker
            </button>

            <input type="file" id="inputImagem" accept="image/*" class="hidden">

            <!-- Controles de Cores e Espessura -->
            <div id="controlesPincel" class="hidden flex items-center space-x-2 border-l pl-2 border-gray-200">
                <div class="flex items-center space-x-1">
                    <button data-cor="#1C1B1F" class="btnCor w-5 h-5 rounded-full bg-gray-900 border border-white shadow hover:scale-110 transition"></button>
                    <button data-cor="#6750A4" class="btnCor w-5 h-5 rounded-full bg-primary border border-white shadow hover:scale-110 transition"></button>
                    <button data-cor="#B3261E" class="btnCor w-5 h-5 rounded-full bg-red-600 border border-white shadow hover:scale-110 transition"></button>
                    <button data-cor="#1F8A70" class="btnCor w-5 h-5 rounded-full bg-emerald-600 border border-white shadow hover:scale-110 transition"></button>
                </div>
                <input type="range" id="rangeEspessura" min="2" max="25" value="4" class="accent-primary cursor-pointer w-16">
            </div>
        </div>

        <!-- Histórico, Limpar e Botão Salvar Integrado -->
        <div class="flex items-center space-x-2">
            <button id="btnUndo" class="p-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs transition" title="Desfazer">
                <span class="material-icons-outlined text-base">undo</span>
            </button>
            <button id="btnRedo" class="p-1.5 bg-gray-100 hover:bg-gray-200 text-gray-700 rounded-xl text-xs transition" title="Refazer">
                <span class="material-icons-outlined text-base">redo</span>
            </button>
            <button id="btnClear" class="bg-red-50 hover:bg-red-100 text-red-600 px-3 py-1.5 rounded-xl text-xs font-semibold transition flex items-center gap-1">
                <span class="material-icons-outlined text-sm">delete_sweep</span> Limpar
            </button>

            <!-- Botão Salvar no Firebase -->
            <button id="btnSalvarQuadro" class="bg-primary hover:bg-primaryDark text-white px-4 py-1.5 rounded-xl font-semibold text-xs transition shadow flex items-center gap-1">
                <span class="material-icons-outlined text-base">save</span> Salvar
            </button>
        </div>
    </div>

    <!-- Menu Flutuante ao Clicar com Botão Direito no Item -->
    <div id="contextMenuSelection" class="hidden absolute z-50 bg-white/95 backdrop-blur-md border border-gray-200 rounded-2xl shadow-2xl py-1.5 w-48 text-xs font-semibold text-gray-700">
        <button id="menuOptCopiar" class="w-full text-left px-4 py-2 hover:bg-purple-50 hover:text-primary flex items-center gap-2 transition">
            <span class="material-icons-outlined text-base">content_copy</span> Copiar Item
        </button>
        <button id="menuOptFrente" class="w-full text-left px-4 py-2 hover:bg-purple-50 hover:text-primary flex items-center gap-2 transition">
            <span class="material-icons-outlined text-base">flip_to_front</span> Trazer para Frente
        </button>
        <button id="menuOptTras" class="w-full text-left px-4 py-2 hover:bg-purple-50 hover:text-primary flex items-center gap-2 transition">
            <span class="material-icons-outlined text-base">flip_to_back</span> Enviar para Trás
        </button>
        <div class="my-1 border-t border-gray-100"></div>
        <button id="menuOptExcluir" class="w-full text-left px-4 py-2 hover:bg-red-50 text-red-600 flex items-center gap-2 transition">
            <span class="material-icons-outlined text-base">delete</span> Excluir Item
        </button>
    </div>

    <!-- Modal de Stickers -->
    <div id="modalStickers" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
        <div class="bg-white rounded-2xl max-w-sm w-full p-6 shadow-xl relative">
            <button id="btnFecharModalStickers" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
                <span class="material-icons-outlined">close</span>
            </button>
            <h2 class="text-lg font-bold text-gray-800 mb-4">Escolher Sticker</h2>
            <div class="grid grid-cols-4 gap-4 text-3xl text-center">
                <button data-emoji="📚" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">📚</button>
                <button data-emoji="💡" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">💡</button>
                <button data-emoji="⭐" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">⭐</button>
                <button data-emoji="🔥" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">🔥</button>
                <button data-emoji="✅" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">✅</button>
                <button data-emoji="📌" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">📌</button>
                <button data-emoji="🚀" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">🚀</button>
                <button data-emoji="🎓" class="btnStickerItem p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">🎓</button>
            </div>
        </div>
    </div>

    <!-- ÁREA DO QUADRO QUADRICULADO -->
    <div id="canvasContainer" class="w-full card-md3 p-1 border border-gray-200 overflow-hidden relative shadow-md rounded-2xl transition-all duration-300">
        <canvas id="drawingCanvas" class="quadro-grid-full block w-full cursor-default touch-none rounded-xl"></canvas>
    </div>

</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    const canvas = document.getElementById('drawingCanvas');
    const ctx = canvas.getContext('2d');
    const navbarGlobal = document.querySelector('header');
    const secaoCabecalho = document.getElementById('secaoCabecalhoPagina');
    const btnToggleImersivo = document.getElementById('btnToggleImersivo');
    const lblImersivo = document.getElementById('lblImersivo');
    const barraFerramentas = document.getElementById('barraFerramentas');
    const canvasContainer = document.getElementById('canvasContainer');
    const contextMenu = document.getElementById('contextMenuSelection');

    let modoImersivo = false;

    // Desativa menu de contexto padrão do navegador no canvas
    canvas.addEventListener('contextmenu', (e) => e.preventDefault());

    function redimensionarCanvas() {
        if (modoImersivo) {
            canvas.width = window.innerWidth;
            canvas.height = window.innerHeight;
        } else {
            const larguraArea = canvasContainer.clientWidth > 0 ? canvasContainer.clientWidth - 8 : (window.innerWidth - 32);
            canvas.width = larguraArea;
            canvas.height = Math.max(720, window.innerHeight - 220);
        }
        desenharCena();
    }

    btnToggleImersivo.addEventListener('click', () => {
        modoImersivo = !modoImersivo;
        document.body.classList.toggle('modo-imersivo-ativo', modoImersivo);

        if (modoImersivo) {
            if (navbarGlobal) navbarGlobal.classList.add('hidden');
            if (secaoCabecalho) secaoCabecalho.classList.add('hidden');

            canvasContainer.className = "fixed inset-0 w-screen h-screen z-30 m-0 p-0 rounded-none border-0 quadro-grid-full";
            barraFerramentas.className = "fixed top-4 left-4 right-4 z-50 card-md3 p-3 flex flex-wrap items-center justify-between gap-3 border border-gray-200 shadow-2xl bg-white/95 backdrop-blur-md rounded-2xl";

            lblImersivo.textContent = "Sair do Imersivo";
            showToast("Modo Imersivo! Foco total no quadro.");
        } else {
            if (navbarGlobal) navbarGlobal.classList.remove('hidden');
            if (secaoCabecalho) secaoCabecalho.classList.remove('hidden');

            canvasContainer.className = "w-full card-md3 p-1 border border-gray-200 overflow-hidden relative shadow-md rounded-2xl";
            barraFerramentas.className = "card-md3 p-3 mb-2 flex flex-wrap items-center justify-between gap-3 border border-gray-100 shadow-sm sticky top-16 z-40 bg-white/95 backdrop-blur-md rounded-2xl";

            lblImersivo.textContent = "Modo Imersivo";
        }

        setTimeout(redimensionarCanvas, 50);
    });

    window.addEventListener('resize', redimensionarCanvas);
    setTimeout(redimensionarCanvas, 50);

    let modo = 'selecionar';
    let itens = [];
    let itemSelecionado = null;
    let acaoAtual = null;
    let handleAtivo = null;
    let offsetX = 0, offsetY = 0;
    let tracoAtual = null;

    let corAtual = '#1C1B1F';
    let espessuraAtual = 4;

    let undoStack = [];
    let redoStack = [];

    const controlesPincel = document.getElementById('controlesPincel');
    const btnToolSelecionar = document.getElementById('btnToolSelecionar');
    const btnToolPincel = document.getElementById('btnToolPincel');

    function setModo(m) {
        modo = m;
        itemSelecionado = null;
        ocultarContextMenu();

        btnToolSelecionar.className = 'px-3 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 ' +
            (m === 'selecionar' ? 'bg-primary text-white shadow' : 'bg-gray-100 text-gray-700 hover:bg-gray-200');

        btnToolPincel.className = 'px-3 py-2 rounded-xl text-xs font-semibold transition flex items-center gap-1 ' +
            (m === 'pincel' ? 'bg-primary text-white shadow' : 'bg-gray-100 text-gray-700 hover:bg-gray-200');

        if (m === 'pincel') {
            controlesPincel.classList.remove('hidden');
            controlesPincel.classList.add('flex');
        } else {
            controlesPincel.classList.add('hidden');
            controlesPincel.classList.remove('flex');
        }
        desenharCena();
    }

    btnToolSelecionar.addEventListener('click', () => setModo('selecionar'));
    btnToolPincel.addEventListener('click', () => { setModo('pincel'); showToast("Pincel ativado."); });

    document.getElementById('btnToolTexto').addEventListener('click', () => {
        const texto = prompt("Digite o texto:");
        if (texto && texto.trim() !== '') {
            itens.push({
                id: Date.now(),
                tipo: 'texto',
                conteudo: texto,
                x: canvas.width / 2 - 75,
                y: canvas.height / 2 - 15,
                largura: 160,
                altura: 40,
                escala: 1,
                rotacao: 0,
                zIndex: itens.length
            });
            salvarEstado();
            setModo('selecionar');
        }
    });

    document.getElementById('btnToolImagem').addEventListener('click', () => {
        document.getElementById('inputImagem').click();
    });

    document.getElementById('btnToolSticker').addEventListener('click', () => {
        document.getElementById('modalStickers').classList.remove('hidden');
    });

    document.getElementById('btnFecharModalStickers').addEventListener('click', () => {
        document.getElementById('modalStickers').classList.add('hidden');
    });

    document.querySelectorAll('.btnCor').forEach(btn => {
        btn.addEventListener('click', () => {
            corAtual = btn.getAttribute('data-cor');
        });
    });

    document.getElementById('rangeEspessura').addEventListener('input', (e) => {
        espessuraAtual = e.target.value;
    });

    function salvarEstado() {
        undoStack.push(JSON.stringify(itens.map(it => {
            if (it.tipo === 'imagem') {
                return { ...it, imgElement: null, src: it.imgElement ? it.imgElement.src : '' };
            }
            return it;
        })));
        redoStack = [];
    }

    document.getElementById('btnUndo').addEventListener('click', () => {
        if (undoStack.length > 1) {
            redoStack.push(undoStack.pop());
            restaurarEstado(undoStack[undoStack.length - 1]);
        }
    });

    document.getElementById('btnRedo').addEventListener('click', () => {
        if (redoStack.length > 0) {
            const estado = redoStack.pop();
            undoStack.push(estado);
            restaurarEstado(estado);
        }
    });

    function restaurarEstado(jsonStr) {
        if (!jsonStr) return;
        const arr = JSON.parse(jsonStr);
        itens = arr.map(it => {
            if (it.tipo === 'imagem' && it.src) {
                const img = new Image();
                img.src = it.src;
                return { ...it, imgElement: img };
            }
            return it;
        });
        itemSelecionado = null;
        ocultarContextMenu();
        desenharCena();
    }

    function desenharCena() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        itens.sort((a, b) => a.zIndex - b.zIndex);

        itens.forEach(item => {
            ctx.save();
            ctx.translate(item.x + (item.largura * item.escala) / 2, item.y + (item.altura * item.escala) / 2);
            ctx.rotate((item.rotacao * Math.PI) / 180);
            ctx.scale(item.escala, item.escala);
            ctx.translate(-item.largura / 2, -item.altura / 2);

            if (item.tipo === 'pincel') {
                const pts = item.pontosRelativos || item.pontosBrutos;
                if (pts && pts.length > 0) {
                    ctx.beginPath();
                    ctx.strokeStyle = item.cor || '#1C1B1F';
                    ctx.fillStyle = item.cor || '#1C1B1F';
                    ctx.lineWidth = item.espessura;
                    ctx.lineCap = 'round';
                    ctx.lineJoin = 'round';

                    if (pts.length === 1) {
                        ctx.arc(pts[0].x, pts[0].y, item.espessura / 2, 0, Math.PI * 2);
                        ctx.fill();
                    } else {
                        ctx.moveTo(pts[0].x, pts[0].y);
                        for (let i = 1; i < pts.length; i++) {
                            ctx.lineTo(pts[i].x, pts[i].y);
                        }
                        ctx.stroke();
                    }
                }
            } else if (item.tipo === 'imagem' || item.tipo === 'sticker') {
                if (item.imgElement) {
                    ctx.drawImage(item.imgElement, 0, 0, item.largura, item.altura);
                } else if (item.tipo === 'sticker') {
                    ctx.font = '48px serif';
                    ctx.fillText(item.conteudo, 0, 40);
                }
            } else if (item.tipo === 'texto') {
                ctx.font = '22px Poppins, sans-serif';
                ctx.fillStyle = '#1C1B1F';
                ctx.fillText(item.conteudo, 0, 25);
            }
            ctx.restore();

            // Borda do Item Selecionado
            if (item === itemSelecionado) {
                ctx.save();
                ctx.translate(item.x + (item.largura * item.escala) / 2, item.y + (item.altura * item.escala) / 2);
                ctx.rotate((item.rotacao * Math.PI) / 180);

                const w = item.largura * item.escala;
                const h = item.altura * item.escala;

                ctx.strokeStyle = '#6750A4';
                ctx.lineWidth = 2;
                ctx.strokeRect(-w/2 - 4, -h/2 - 4, w + 8, h + 8);

                const handleSize = 10;
                ctx.fillStyle = '#6750A4';
                ctx.fillRect(-w/2 - 9, -h/2 - 9, handleSize, handleSize);
                ctx.fillRect(w/2 - 1, -h/2 - 9, handleSize, handleSize);
                ctx.fillRect(-w/2 - 9, h/2 - 1, handleSize, handleSize);
                ctx.fillRect(w/2 - 1, h/2 - 1, handleSize, handleSize);

                ctx.beginPath();
                ctx.moveTo(0, -h/2 - 4);
                ctx.lineTo(0, -h/2 - 25);
                ctx.strokeStyle = '#6750A4';
                ctx.lineWidth = 2;
                ctx.stroke();

                ctx.beginPath();
                ctx.arc(0, -h/2 - 28, 7, 0, 2 * Math.PI);
                ctx.fillStyle = '#6750A4';
                ctx.fill();

                ctx.restore();
            }
        });
    }

    function getPos(e) {
        const rect = canvas.getBoundingClientRect();
        const clientX = e.clientX || (e.touches && e.touches[0].clientX);
        const clientY = e.clientY || (e.touches && e.touches[0].clientY);
        return { x: clientX - rect.left, y: clientY - rect.top, pageX: e.pageX, pageY: e.pageY };
    }

    function testarHandles(pos, item) {
        const cx = item.x + (item.largura * item.escala) / 2;
        const cy = item.y + (item.altura * item.escala) / 2;

        const rad = (-item.rotacao * Math.PI) / 180;
        const cos = Math.cos(rad), sin = Math.sin(rad);
        const dx = pos.x - cx;
        const dy = pos.y - cy;
        const lx = dx * cos - dy * sin;
        const ly = dx * sin + dy * cos;

        const w = item.largura * item.escala;
        const h = item.altura * item.escala;

        if (Math.abs(lx - 0) < 15 && Math.abs(ly - (-h/2 - 28)) < 15) return 'rotacionar';
        if (Math.abs(lx - (-w/2)) < 15 && Math.abs(ly - (-h/2)) < 15) return 'redimensionar';
        if (Math.abs(lx - (w/2)) < 15 && Math.abs(ly - (-h/2)) < 15) return 'redimensionar';
        if (Math.abs(lx - (-w/2)) < 15 && Math.abs(ly - (h/2)) < 15) return 'redimensionar';
        if (Math.abs(lx - (w/2)) < 15 && Math.abs(ly - (h/2)) < 15) return 'redimensionar';

        if (lx >= -w/2 && lx <= w/2 && ly >= -h/2 && ly <= h/2) return 'mover';

        return null;
    }

    function exibirContextMenu(posX, posY) {
        contextMenu.style.left = posX + 'px';
        contextMenu.style.top = posY + 'px';
        contextMenu.classList.remove('hidden');
    }

    function ocultarContextMenu() {
        contextMenu.classList.add('hidden');
    }

    window.addEventListener('click', (e) => {
        if (!contextMenu.contains(e.target)) ocultarContextMenu();
    });

    // MOUSE DOWN: Trata Botão Esquerdo (0) e Botão Direito (2)
    canvas.addEventListener('mousedown', (e) => {
        const pos = getPos(e);
        const isRightClick = (e.button === 2);

        if (modo === 'pincel' && !isRightClick) {
            tracoAtual = {
                id: Date.now(),
                tipo: 'pincel',
                pontosBrutos: [pos],
                cor: corAtual,
                espessura: parseInt(espessuraAtual),
                zIndex: itens.length
            };
            itens.push(tracoAtual);
            acaoAtual = 'desenhar';
            ocultarContextMenu();
            desenharCena();
        } else {
            // Se for Clique com Botão DIREITO (2) -> Encontra o item sob o ponteiro e abre o Menu Contextual
            if (isRightClick) {
                e.preventDefault();
                let clicouEmAlgum = false;

                for (let i = itens.length - 1; i >= 0; i--) {
                    const it = itens[i];
                    if (pos.x >= it.x && pos.x <= it.x + it.largura * it.escala &&
                        pos.y >= it.y && pos.y <= it.y + it.altura * it.escala) {
                        itemSelecionado = it;
                        clicouEmAlgum = true;
                        exibirContextMenu(pos.pageX, pos.pageY);
                        desenharCena();
                        break;
                    }
                }

                if (!clicouEmAlgum) {
                    ocultarContextMenu();
                }
                return;
            }

            // Clique com Botão ESQUERDO (0) -> Arraste, Seleção e Transformação
            if (modo === 'selecionar') {
                ocultarContextMenu();

                if (itemSelecionado) {
                    handleAtivo = testarHandles(pos, itemSelecionado);
                    if (handleAtivo) {
                        acaoAtual = handleAtivo === 'redimensionar' ? 'redimensionar' : (handleAtivo === 'rotacionar' ? 'rotacionar' : 'mover');
                        offsetX = pos.x - itemSelecionado.x;
                        offsetY = pos.y - itemSelecionado.y;
                        return;
                    }
                }

                itemSelecionado = null;
                for (let i = itens.length - 1; i >= 0; i--) {
                    const it = itens[i];
                    if (pos.x >= it.x && pos.x <= it.x + it.largura * it.escala &&
                        pos.y >= it.y && pos.y <= it.y + it.altura * it.escala) {
                        itemSelecionado = it;
                        acaoAtual = 'mover';
                        offsetX = pos.x - it.x;
                        offsetY = pos.y - it.y;
                        break;
                    }
                }
                desenharCena();
            }
        }
    });

    canvas.addEventListener('mousemove', (e) => {
        if (!acaoAtual) return;
        const pos = getPos(e);

        if (acaoAtual === 'desenhar' && tracoAtual) {
            tracoAtual.pontosBrutos.push(pos);
            desenharCena();
        } else if (acaoAtual === 'mover' && itemSelecionado) {
            itemSelecionado.x = pos.x - offsetX;
            itemSelecionado.y = pos.y - offsetY;
            desenharCena();
        } else if (acaoAtual === 'redimensionar' && itemSelecionado) {
            const dx = pos.x - (itemSelecionado.x + (itemSelecionado.largura * itemSelecionado.escala)/2);
            itemSelecionado.escala = Math.max(0.3, Math.min(4.5, Math.abs(dx) / (itemSelecionado.largura / 2)));
            desenharCena();
        } else if (acaoAtual === 'rotacionar' && itemSelecionado) {
            const cx = itemSelecionado.x + (itemSelecionado.largura * itemSelecionado.escala) / 2;
            const cy = itemSelecionado.y + (itemSelecionado.altura * itemSelecionado.escala) / 2;
            const anguloRad = Math.atan2(pos.y - cy, pos.x - cx);
            itemSelecionado.rotacao = (anguloRad * 180) / Math.PI + 90;
            desenharCena();
        }
    });

    function finalizarTraco() {
        if (acaoAtual === 'desenhar' && tracoAtual && tracoAtual.pontosBrutos) {
            let minX = Infinity, maxX = -Infinity, minY = Infinity, maxY = -Infinity;
            tracoAtual.pontosBrutos.forEach(p => {
                if (p.x < minX) minX = p.x;
                if (p.x > maxX) maxX = p.x;
                if (p.y < minY) minY = p.y;
                if (p.y > maxY) maxY = p.y;
            });

            const pad = Math.max(6, tracoAtual.espessura);
            const x = minX - pad;
            const y = minY - pad;
            const w = Math.max(16, (maxX - minX) + pad * 2);
            const h = Math.max(16, (maxY - minY) + pad * 2);

            tracoAtual.x = x;
            tracoAtual.y = y;
            tracoAtual.largura = w;
            tracoAtual.altura = h;
            tracoAtual.escala = 1;
            tracoAtual.rotacao = 0;
            tracoAtual.pontosRelativos = tracoAtual.pontosBrutos.map(p => ({
                x: p.x - x,
                y: p.y - y
            }));

            salvarEstado();
            desenharCena();
        }
        acaoAtual = null;
        tracoAtual = null;
    }

    canvas.addEventListener('mouseup', finalizarTraco);
    canvas.addEventListener('mouseleave', finalizarTraco);

    // Opção do Menu Contextual (Botão Direito): Copiar / Duplicar
    document.getElementById('menuOptCopiar').addEventListener('click', () => {
        if (!itemSelecionado) return;
        const copia = JSON.parse(JSON.stringify(itemSelecionado));
        copia.id = Date.now();
        copia.x += 25;
        copia.y += 25;
        copia.zIndex = itens.length;

        if (itemSelecionado.tipo === 'imagem' && itemSelecionado.imgElement) {
            copia.imgElement = itemSelecionado.imgElement;
        }

        itens.push(copia);
        itemSelecionado = copia;
        salvarEstado();
        ocultarContextMenu();
        desenharCena();
        showToast("Item duplicado!");
    });

    // Opções do Menu Contextual: Camadas
    document.getElementById('menuOptFrente').addEventListener('click', () => {
        if (!itemSelecionado) return;
        itemSelecionado.zIndex = itens.length;
        salvarEstado();
        ocultarContextMenu();
        desenharCena();
    });

    document.getElementById('menuOptTras').addEventListener('click', () => {
        if (!itemSelecionado) return;
        itemSelecionado.zIndex = 0;
        salvarEstado();
        ocultarContextMenu();
        desenharCena();
    });

    // Opção do Menu Contextual: Excluir
    document.getElementById('menuOptExcluir').addEventListener('click', () => {
        if (!itemSelecionado) return;
        itens = itens.filter(it => it !== itemSelecionado);
        itemSelecionado = null;
        salvarEstado();
        ocultarContextMenu();
        desenharCena();
        showToast("Item excluído.");
    });

    document.getElementById('inputImagem').addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function(event) {
            const img = new Image();
            img.onload = function() {
                const w = 220;
                const h = (img.height * 220) / img.width;
                const novo = {
                    id: Date.now(),
                    tipo: 'imagem',
                    imgElement: img,
                    x: canvas.width / 2 - w / 2,
                    y: canvas.height / 2 - h / 2,
                    largura: w,
                    altura: h,
                    escala: 1,
                    rotacao: 0,
                    zIndex: itens.length
                };
                itens.push(novo);
                itemSelecionado = novo;
                setModo('selecionar');
                salvarEstado();
                showToast("Imagem inserida!");
                desenharCena();
            };
            img.src = event.target.result;
        };
        reader.readAsDataURL(file);
    });

    document.querySelectorAll('.btnStickerItem').forEach(btn => {
        btn.addEventListener('click', () => {
            const emoji = btn.getAttribute('data-emoji');
            const novo = {
                id: Date.now(),
                tipo: 'sticker',
                conteudo: emoji,
                x: canvas.width / 2 - 30,
                y: canvas.height / 2 - 30,
                largura: 60,
                altura: 60,
                escala: 1,
                rotacao: 0,
                zIndex: itens.length
            };
            itens.push(novo);
            itemSelecionado = novo;
            document.getElementById('modalStickers').classList.add('hidden');
            setModo('selecionar');
            salvarEstado();
            showToast("Sticker adicionado!");
            desenharCena();
        });
    });

    document.getElementById('btnClear').addEventListener('click', () => {
        if (confirm("Deseja limpar todo o quadro?")) {
            itens = [];
            itemSelecionado = null;
            salvarEstado();
            desenharCena();
        }
    });

    function carregarConteudoAnotacao(str) {
        if (!str || !str.trim()) return;
        const strTrim = str.trim();
        let imgSrc = null;

        if (strTrim.startsWith('data:image') || strTrim.startsWith('http')) {
            imgSrc = strTrim;
        } else if (strTrim.includes('src=')) {
            const match = strTrim.match(/src=["']([^"']+)["']/);
            if (match) imgSrc = match[1];
        } else if (strTrim.startsWith('{') || strTrim.startsWith('[')) {
            try {
                let arrayObjetos = [];
                if (strTrim.startsWith('{')) {
                    const root = JSON.parse(strTrim);
                    arrayObjetos = root.objetos || [];
                } else {
                    arrayObjetos = JSON.parse(strTrim);
                }

                arrayObjetos.forEach((obj, idx) => {
                    const tipo = obj.tipo || 'texto';
                    const x = parseFloat(obj.x) || 50;
                    const y = parseFloat(obj.y) || 50;
                    const scale = parseFloat(obj.scale) || 1;
                    const rot = parseFloat(obj.rotation) || 0;
                    const w = parseFloat(obj.w) || 200;
                    const h = parseFloat(obj.h) || 100;

                    if (tipo === 'texto') {
                        itens.push({
                            id: Date.now() + idx,
                            tipo: 'texto',
                            conteudo: obj.conteudo || 'Texto',
                            x: x, y: y, largura: w, altura: h,
                            escala: scale, rotacao: rot, zIndex: idx
                        });
                    } else if (tipo === 'imagem' || tipo === 'sticker') {
                        if (obj.conteudo) {
                            const img = new Image();
                            img.onload = () => {
                                itens.push({
                                    id: Date.now() + idx,
                                    tipo: 'imagem',
                                    imgElement: img,
                                    x: x, y: y, largura: w, altura: h,
                                    escala: scale, rotacao: rot, zIndex: idx
                                });
                                desenharCena();
                            };
                            img.src = obj.conteudo;
                        }
                    } else if (tipo === 'pincel' && obj.pontos) {
                        itens.push({
                            id: Date.now() + idx,
                            tipo: 'pincel',
                            pontosRelativos: obj.pontos,
                            cor: obj.cor || '#1C1B1F',
                            espessura: obj.espessura || 4,
                            x: x, y: y, largura: w, altura: h,
                            escala: scale, rotacao: rot, zIndex: idx
                        });
                    }
                });
                salvarEstado();
                desenharCena();
                return;
            } catch (e) {
                console.error("Erro ao analisar JSON da anotação:", e);
            }
        } else {
            itens.push({
                id: Date.now(),
                tipo: 'texto',
                conteudo: strTrim.replace(/<[^>]*>?/gm, ''),
                x: 100, y: 100, largura: 300, altura: 60,
                escala: 1, rotacao: 0, zIndex: 0
            });
            salvarEstado();
            desenharCena();
            return;
        }

        if (imgSrc) {
            const img = new Image();
            img.onload = () => {
                const imgWidth = img.naturalWidth || img.width || 600;
                const imgHeight = img.naturalHeight || img.height || 400;
                const targetW = Math.min(canvas.width - 40, imgWidth);
                const targetH = (imgHeight * targetW) / imgWidth;

                itens.push({
                    id: Date.now(),
                    tipo: 'imagem',
                    imgElement: img,
                    x: canvas.width / 2 - targetW / 2,
                    y: canvas.height / 2 - targetH / 2,
                    largura: targetW,
                    altura: targetH,
                    escala: 1,
                    rotacao: 0,
                    zIndex: 0
                });
                salvarEstado();
                desenharCena();
            };
            img.src = imgSrc;
        }
    }

    auth.onAuthStateChanged((user) => {
        if (!user) {
            window.location.href = 'login.php';
            return;
        }
        currentUserId = user.uid;

        // Carrega anotação por ID se fornecido na URL (?id=...)
        const urlParams = new URLSearchParams(window.location.search);
        const noteId = urlParams.get('id');
        if (noteId) {
            db.collection('users').doc(currentUserId).collection('anotacoes').doc(noteId).get()
                .then(doc => {
                    if (doc.exists) {
                        const data = doc.data();
                        if (data.conteudoHtml) {
                            carregarConteudoAnotacao(data.conteudoHtml);
                        }
                    }
                });
        }
    });

    document.getElementById('btnSalvarQuadro').addEventListener('click', () => {
        if (!currentUserId) return;
        const titulo = "Quadro Rápido " + new Date().toLocaleDateString('pt-BR');

        const backupSel = itemSelecionado;
        itemSelecionado = null;
        desenharCena();

        const dataUrl = canvas.toDataURL('image/png');
        itemSelecionado = backupSel;
        desenharCena();

        const ref = db.collection('users').doc(currentUserId).collection('anotacoes').doc();
        ref.set({
            id: ref.id,
            titulo: titulo,
            conteudoHtml: `<img src="${dataUrl}" alt="Quadro Interativo" />`,
            dataUltimaEdicao: Date.now()
        }).then(() => {
            showToast("Quadro salvo no Firebase!");
        });
    });

    salvarEstado();
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
