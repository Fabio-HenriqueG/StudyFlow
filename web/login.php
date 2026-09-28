<?php
$pageTitle = "Login";
require_once __DIR__ . '/includes/header.php';
?>

<div class="max-w-md mx-auto mt-10 card-md3 p-8 border border-gray-100">
    <!-- Tab Headers -->
    <div class="flex border-b border-gray-200 mb-6">
        <button id="tabLogin" class="flex-1 py-3 text-center font-semibold text-primary border-b-2 border-primary transition">
            Entrar
        </button>
        <button id="tabRegister" class="flex-1 py-3 text-center font-medium text-gray-500 hover:text-primary transition">
            Cadastrar
        </button>
    </div>

    <!-- Formulário de Login -->
    <form id="formLogin" class="space-y-4">
        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">E-mail</label>
            <input type="email" id="loginEmail" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition" placeholder="seu@email.com">
        </div>

        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Senha</label>
            <input type="password" id="loginPassword" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition" placeholder="••••••••">
        </div>

        <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white font-medium py-2.5 rounded-lg transition shadow">
            Entrar no StudyFlow
        </button>
    </form>

    <!-- Formulário de Cadastro (Oculto por padrão) -->
    <form id="formRegister" class="space-y-4 hidden">
        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Nome Completo</label>
            <input type="text" id="regName" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition" placeholder="Seu Nome">
        </div>

        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">E-mail</label>
            <input type="email" id="regEmail" required class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition" placeholder="seu@email.com">
        </div>

        <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Senha</label>
            <input type="password" id="regPassword" required minlength="6" class="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-primary focus:border-transparent outline-none transition" placeholder="Mínimo 6 caracteres">
        </div>

        <button type="submit" class="w-full bg-primary hover:bg-primaryDark text-white font-medium py-2.5 rounded-lg transition shadow">
            Criar Conta
        </button>
    </form>

    <!-- Divisor -->
    <div class="relative my-6">
        <div class="absolute inset-0 flex items-center"><div class="w-full border-t border-gray-200"></div></div>
        <div class="relative flex justify-center text-xs uppercase"><span class="bg-white px-2 text-gray-400">Ou entre com</span></div>
    </div>

    <!-- Login com Google -->
    <button id="btnGoogleLogin" class="w-full flex items-center justify-center gap-3 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 font-medium py-2.5 rounded-lg transition shadow-sm">
        <svg class="w-5 h-5" viewBox="0 0 24 24">
            <path fill="#4285F4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
            <path fill="#34A853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
            <path fill="#FBBC05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.06H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.94l2.85-2.22.81-.63z"/>
            <path fill="#EA4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.06l3.66 2.84c.87-2.6 3.3-4.52 6.16-4.52z"/>
        </svg>
        Entrar com o Google
    </button>
</div>

<script>
document.addEventListener('DOMContentLoaded', () => {
    const tabLogin = document.getElementById('tabLogin');
    const tabRegister = document.getElementById('tabRegister');
    const formLogin = document.getElementById('formLogin');
    const formRegister = document.getElementById('formRegister');

    // Troca de Abas
    tabLogin.addEventListener('click', () => {
        tabLogin.classList.add('text-primary', 'border-b-2', 'border-primary', 'font-semibold');
        tabLogin.classList.remove('text-gray-500');
        tabRegister.classList.remove('text-primary', 'border-b-2', 'border-primary', 'font-semibold');
        tabRegister.classList.add('text-gray-500');

        formLogin.classList.remove('hidden');
        formRegister.classList.add('hidden');
    });

    tabRegister.addEventListener('click', () => {
        tabRegister.classList.add('text-primary', 'border-b-2', 'border-primary', 'font-semibold');
        tabRegister.classList.remove('text-gray-500');
        tabLogin.classList.remove('text-primary', 'border-b-2', 'border-primary', 'font-semibold');
        tabLogin.classList.add('text-gray-500');

        formRegister.classList.remove('hidden');
        formLogin.classList.add('hidden');
    });

    // Submeter Login
    formLogin.addEventListener('submit', (e) => {
        e.preventDefault();
        const email = document.getElementById('loginEmail').value;
        const pass = document.getElementById('loginPassword').value;

        auth.signInWithEmailAndPassword(email, pass)
            .then(() => {
                showToast("Login realizado com sucesso!");
                window.location.href = 'index.php';
            })
            .catch((error) => {
                showToast("Erro no login: " + error.message, true);
            });
    });

    // Submeter Cadastro
    formRegister.addEventListener('submit', (e) => {
        e.preventDefault();
        const name = document.getElementById('regName').value;
        const email = document.getElementById('regEmail').value;
        const pass = document.getElementById('regPassword').value;

        auth.createUserWithEmailAndPassword(email, pass)
            .then((userCredential) => {
                return userCredential.user.updateProfile({ displayName: name });
            })
            .then(() => {
                showToast("Conta criada com sucesso!");
                window.location.href = 'index.php';
            })
            .catch((error) => {
                showToast("Erro no cadastro: " + error.message, true);
            });
    });

    // Login Google
    document.getElementById('btnGoogleLogin').addEventListener('click', () => {
        auth.signInWithPopup(googleProvider)
            .then(() => {
                showToast("Login com Google efetuado!");
                window.location.href = 'index.php';
            })
            .catch((error) => {
                showToast("Erro ao entrar com Google: " + error.message, true);
            });
    });
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
