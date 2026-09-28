<?php
$pageTitle = "Quadro Interativo de Anotações";
require_once __DIR__ . '/includes/header.php';
?>

<style>
.quadro-grid {
    background-color: #FFFFFF;
    background-image:
        linear-gradient(to right, #D1C4E9 1.5px, transparent 1.5px),
        linear-gradient(to bottom, #D1C4E9 1.5px, transparent 1.5px);
    background-size: 40px 40px;
}
</style>

<!-- Container Principal com Largura Expandida -->
<div class="max-w-full mx-auto px-2 sm:px-4 py-4">

    <!-- Cabeçalho -->
    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-4">
        <div>
            <h1 class="text-2xl font-bold text-gray-800">Quadro de Anotações</h1>
            <p class="text-sm text-gray-500">Desenhe, insira imagens, stickers e organize suas ideias.</p>
        </div>
        <button id="btnSalvarQuadro" class="bg-primary hover:bg-primaryDark text-white px-6 py-2.5 rounded-xl font-medium text-sm transition shadow-md flex items-center gap-2">
            <span class="material-icons-outlined text-lg">save</span> Salvar no Firebase
        </button>
    </div>

    <!-- Barra de Ferramentas -->
    <div class="card-md3 p-4 mb-4 flex flex-wrap items-center justify-between gap-4 border border-gray-100 shadow-sm">
        <div class="flex items-center gap-3 flex-wrap">
            <!-- Botão Selecionar -->
            <button onclick="setModo('selecionar')" id="tool-selecionar" class="px-4 py-2.5 rounded-xl text-sm font-medium transition flex items-center gap-2 bg-primary text-white shadow">
                <span class="material-icons-outlined text-base">near_me</span> Selecionar
            </button>

            <!-- Menu Dropdown de Ferramentas -->
            <div class="relative inline-block text-left">
                <button id="btnMenuFerramentas" class="bg-gray-100 hover:bg-gray-200 text-gray-800 px-4 py-2.5 rounded-xl text-sm font-medium transition flex items-center gap-2 shadow-sm">
                    <span class="material-icons-outlined text-base">construction</span> Ferramentas <span class="material-icons-outlined text-sm">arrow_drop_down</span>
                </button>
                <div id="dropdownFerramentas" class="hidden absolute left-0 mt-2 w-48 rounded-xl bg-white shadow-lg ring-1 ring-black ring-opacity-5 z-50 py-1">
                    <button onclick="escolherFerramenta('pincel')" class="w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-purple-50 hover:text-primary flex items-center gap-2">
                        <span class="material-icons-outlined text-sm">brush</span> Pincel / Desenho
                    </button>
                    <button onclick="escolherFerramenta('texto')" class="w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-purple-50 hover:text-primary flex items-center gap-2">
                        <span class="material-icons-outlined text-sm">text_fields</span> Caixa de Texto
                    </button>
                    <button onclick="document.getElementById('inputImagem').click()" class="w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-purple-50 hover:text-primary flex items-center gap-2">
                        <span class="material-icons-outlined text-sm">image</span> Inserir Imagem
                    </button>
                    <button onclick="document.getElementById('modalStickers').classList.remove('hidden')" class="w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-purple-50 hover:text-primary flex items-center gap-2">
                        <span class="material-icons-outlined text-sm">emoji_emotions</span> Inserir Sticker
                    </button>
                </div>
            </div>
            <input type="file" id="inputImagem" accept="image/*" class="hidden">

            <!-- Cores e Espessura (Ativas para Pincel) -->
            <div id="controlesPincel" class="hidden flex items-center space-x-3 border-l pl-3 border-gray-200">
                <div class="flex items-center space-x-1">
                    <button onclick="mudarCor('#1C1B1F')" class="w-6 h-6 rounded-full bg-gray-900 border-2 border-white shadow hover:scale-110 transition"></button>
                    <button onclick="mudarCor('#6750A4')" class="w-6 h-6 rounded-full bg-primary border-2 border-white shadow hover:scale-110 transition"></button>
                    <button onclick="mudarCor('#B3261E')" class="w-6 h-6 rounded-full bg-red-600 border-2 border-white shadow hover:scale-110 transition"></button>
                    <button onclick="mudarCor('#1F8A70')" class="w-6 h-6 rounded-full bg-emerald-600 border-2 border-white shadow hover:scale-110 transition"></button>
                </div>
                <input type="range" id="rangeEspessura" min="2" max="25" value="4" class="accent-primary cursor-pointer w-20">
            </div>
        </div>

        <!-- Ações Rápidas do Item Selecionado -->
        <div id="painelItemSelecionado" class="hidden flex items-center gap-2 bg-purple-50 px-3 py-1.5 rounded-xl border border-purple-100">
            <span class="text-xs font-bold text-primary mr-1">Item Ativo:</span>
            <button onclick="mudarCamada('frente')" class="p-1.5 bg-white hover:bg-purple-100 text-primary rounded-lg transition text-xs shadow-sm" title="Trazer para Frente">
                <span class="material-icons-outlined text-sm">flip_to_front</span>
            </button>
            <button onclick="mudarCamada('tras')" class="p-1.5 bg-white hover:bg-purple-100 text-primary rounded-lg transition text-xs shadow-sm" title="Enviar para Trás">
                <span class="material-icons-outlined text-sm">flip_to_back</span>
            </button>
            <button onclick="excluirItemSelecionado()" class="p-1.5 bg-red-50 hover:bg-red-100 text-red-600 rounded-lg transition text-xs shadow-sm" title="Excluir">
                <span class="material-icons-outlined text-sm">delete</span>
            </button>
        </div>

        <!-- Limpar Quadro -->
        <div>
            <button id="btnClear" class="bg-red-50 hover:bg-red-100 text-red-600 px-3 py-2 rounded-xl text-sm font-medium transition flex items-center gap-1">
                <span class="material-icons-outlined text-sm">delete_sweep</span> Limpar Quadro
            </button>
        </div>
    </div>

    <!-- Modal de Stickers -->
    <div id="modalStickers" class="fixed inset-0 bg-black/40 z-50 flex items-center justify-center p-4 hidden">
        <div class="bg-white rounded-2xl max-w-sm w-full p-6 shadow-xl relative">
            <button onclick="document.getElementById('modalStickers').classList.add('hidden')" class="absolute top-4 right-4 text-gray-400 hover:text-gray-600">
                <span class="material-icons-outlined">close</span>
            </button>
            <h2 class="text-lg font-bold text-gray-800 mb-4">Escolher Sticker</h2>
            <div class="grid grid-cols-4 gap-4 text-3xl text-center">
                <button onclick="adicionarSticker('📚')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">📚</button>
                <button onclick="adicionarSticker('💡')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">💡</button>
                <button onclick="adicionarSticker('⭐')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">⭐</button>
                <button onclick="adicionarSticker('🔥')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">🔥</button>
                <button onclick="adicionarSticker('✅')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">✅</button>
                <button onclick="adicionarSticker('📌')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">📌</button>
                <button onclick="adicionarSticker('🚀')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">🚀</button>
                <button onclick="adicionarSticker('🎓')" class="p-3 bg-gray-50 hover:bg-purple-50 rounded-xl transition">🎓</button>
            </div>
        </div>
    </div>

    <!-- Container com Grid CSS Nativo (Largura Total e Altura Imersiva) -->
    <div class="card-md3 p-2 border border-gray-200 overflow-hidden relative shadow-md quadro-grid rounded-xl w-full">
        <canvas id="drawingCanvas" style="width: 100%; height: 750px;" class="bg-transparent cursor-default touch-none"></canvas>
    </div>

</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    let currentUserId = null;
    const canvas = document.getElementById('drawingCanvas');
    const ctx = canvas.getContext('2d');

    canvas.width = canvas.parentElement.clientWidth > 0 ? canvas.parentElement.clientWidth - 16 : 1200;
    canvas.height = 750;

    let modo = 'selecionar';
    let itens = [];
    let itemSelecionado = null;
    let acaoAtual = null;
    let handleAtivo = null;
    let offsetX = 0, offsetY = 0;
    let tracoAtual = null;

    let corAtual = '#1C1B1F';
    let espessuraAtual = 4;

    const btnMenu = document.getElementById('btnMenuFerramentas');
    const dropdown = document.getElementById('dropdownFerramentas');
    const controlesPincel = document.getElementById('controlesPincel');

    btnMenu.addEventListener('click', (e) => {
        e.stopPropagation();
        dropdown.classList.toggle('hidden');
    });
    window.addEventListener('click', () => {
        if (!dropdown.classList.contains('hidden')) dropdown.classList.add('hidden');
    });

    window.setModo = function(m) {
        modo = m;
        itemSelecionado = null;
        atualizarPainelItem();

        document.getElementById('tool-selecionar').className = m === 'selecionar'
            ? 'px-4 py-2.5 rounded-xl text-sm font-medium transition flex items-center gap-2 bg-primary text-white shadow'
            : 'px-4 py-2.5 rounded-xl text-sm font-medium transition flex items-center gap-2 bg-gray-100 text-gray-700 hover:bg-gray-200';

        if (m === 'pincel') {
            controlesPincel.classList.remove('hidden');
            controlesPincel.classList.add('flex');
        } else {
            controlesPincel.classList.add('hidden');
            controlesPincel.classList.remove('flex');
        }
        desenharCena();
    };

    window.escolherFerramenta = function(f) {
        dropdown.classList.add('hidden');
        if (f === 'pincel') {
            setModo('pincel');
            showToast("Modo Pincel ativado. Desenhe livremente no quadro.");
        } else if (f === 'texto') {
            modo = 'texto';
            controlesPincel.classList.add('hidden');
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
                setModo('selecionar');
            }
        }
    };

    window.mudarCor = function(cor) {
        corAtual = cor;
    };

    document.getElementById('rangeEspessura').addEventListener('input', (e) => {
        espessuraAtual = e.target.value;
    });

    function desenharCena() {
        ctx.clearRect(0, 0, canvas.width, canvas.height);
        itens.sort((a, b) => a.zIndex - b.zIndex);

        itens.forEach(item => {
            ctx.save();
            if (item.tipo === 'pincel') {
                if (item.pontos && item.pontos.length > 1) {
                    ctx.beginPath();
                    ctx.strokeStyle = item.cor;
                    ctx.lineWidth = item.espessura;
                    ctx.lineCap = 'round';
                    ctx.lineJoin = 'round';
                    ctx.moveTo(item.pontos[0].x, item.pontos[0].y);
                    for (let i = 1; i < item.pontos.length; i++) {
                        ctx.lineTo(item.pontos[i].x, item.pontos[i].y);
                    }
                    ctx.stroke();
                }
            } else {
                ctx.translate(item.x + (item.largura * item.escala) / 2, item.y + (item.altura * item.escala) / 2);
                ctx.rotate((item.rotacao * Math.PI) / 180);
                ctx.scale(item.escala, item.escala);
                ctx.translate(-item.largura / 2, -item.altura / 2);

                if (item.tipo === 'imagem' || item.tipo === 'sticker') {
                    if (item.imgElement) {
                        ctx.drawImage(item.imgElement, 0, 0, item.largura, item.altura);
                    } else if (item.tipo === 'sticker') {
                        ctx.font = '48px serif';
                        ctx.fillText(item.conteudo, 0, 40);
                    }
                } else if (item.tipo === 'texto') {
                    ctx.font = '22px Roboto, sans-serif';
                    ctx.fillStyle = '#1C1B1F';
                    ctx.fillText(item.conteudo, 0, 25);
                }
            }
            ctx.restore();

            if (item === itemSelecionado && item.tipo !== 'pincel') {
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
        return { x: clientX - rect.left, y: clientY - rect.top };
    }

    function testarHandles(pos, item) {
        if (item.tipo === 'pincel') return null;
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

    canvas.addEventListener('mousedown', (e) => {
        const pos = getPos(e);

        if (modo === 'pincel') {
            tracoAtual = {
                tipo: 'pincel',
                pontos: [pos],
                cor: corAtual,
                espessura: parseInt(espessuraAtual),
                zIndex: itens.length
            };
            itens.push(tracoAtual);
            acaoAtual = 'desenhar';
        } else if (modo === 'selecionar') {
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
                if (it.tipo === 'pincel') continue;
                if (pos.x >= it.x && pos.x <= it.x + it.largura * it.escala &&
                    pos.y >= it.y && pos.y <= it.y + it.altura * it.escala) {
                    itemSelecionado = it;
                    acaoAtual = 'mover';
                    offsetX = pos.x - it.x;
                    offsetY = pos.y - it.y;
                    break;
                }
            }
            atualizarPainelItem();
            desenharCena();
        }
    });

    canvas.addEventListener('mousemove', (e) => {
        if (!acaoAtual) return;
        const pos = getPos(e);

        if (acaoAtual === 'desenhar' && tracoAtual) {
            tracoAtual.pontos.push(pos);
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

    canvas.addEventListener('mouseup', () => { acaoAtual = null; tracoAtual = null; });
    canvas.addEventListener('mouseleave', () => { acaoAtual = null; tracoAtual = null; });

    function atualizarPainelItem() {
        const painel = document.getElementById('painelItemSelecionado');
        if (itemSelecionado) {
            painel.classList.remove('hidden');
            painel.classList.add('flex');
        } else {
            painel.classList.add('hidden');
            painel.classList.remove('flex');
        }
    }

    window.mudarCamada = function(dir) {
        if (!itemSelecionado) return;
        itemSelecionado.zIndex = (dir === 'frente') ? itens.length : 0;
        desenharCena();
    };

    window.excluirItemSelecionado = function() {
        if (!itemSelecionado) return;
        itens = itens.filter(it => it !== itemSelecionado);
        itemSelecionado = null;
        atualizarPainelItem();
        desenharCena();
        showToast("Item excluído.");
    };

    document.getElementById('inputImagem').addEventListener('change', (e) => {
        const file = e.target.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = function(event) {
            const img = new Image();
            img.onload = function() {
                const w = 200;
                const h = (img.height * 200) / img.width;
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
                showToast("Imagem inserida!");
                desenharCena();
            };
            img.src = event.target.result;
        };
        reader.readAsDataURL(file);
    });

    window.adicionarSticker = function(emoji) {
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
        showToast("Sticker adicionado!");
        desenharCena();
    };

    document.getElementById('btnClear').addEventListener('click', () => {
        if (confirm("Deseja limpar todo o quadro?")) {
            itens = [];
            itemSelecionado = null;
            atualizarPainelItem();
            desenharCena();
        }
    });

    auth.onAuthStateChanged((user) => {
        if (!user) {
            window.location.href = 'login.php';
            return;
        }
        currentUserId = user.uid;
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

    desenharCena();
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
