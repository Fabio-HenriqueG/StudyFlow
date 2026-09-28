/**
 * Lógica Global do StudyFlow Web
 */

document.addEventListener('DOMContentLoaded', () => {
    // Monitora o estado de autenticação do usuário
    auth.onAuthStateChanged((user) => {
        const userProfile = document.getElementById('userProfile');
        const userName = document.getElementById('userName');
        const btnLoginNav = document.getElementById('btnLoginNav');

        if (user) {
            if (userProfile) userProfile.classList.remove('hidden');
            if (userName) userName.textContent = user.displayName || user.email.split('@')[0];
            if (btnLoginNav) btnLoginNav.classList.add('hidden');
        } else {
            if (userProfile) userProfile.classList.add('hidden');
            if (btnLoginNav) btnLoginNav.classList.remove('hidden');
        }
    });

    // Botão de Logout
    const btnLogout = document.getElementById('btnLogout');
    if (btnLogout) {
        btnLogout.addEventListener('click', () => {
            auth.signOut().then(() => {
                window.location.href = 'login.php';
            }).catch((error) => {
                console.error("Erro ao fazer logout:", error);
            });
        });
    }
});

/**
 * Função utilitária para exibir alertas ou toasts simples
 */
function showToast(message, isError = false) {
    const toast = document.createElement('div');
    toast.className = `fixed bottom-5 right-5 px-4 py-3 rounded-lg text-white shadow-lg z-50 transition transform duration-300 ${isError ? 'bg-red-600' : 'bg-emerald-600'}`;
    toast.innerText = message;
    document.body.appendChild(toast);

    setTimeout(() => {
        toast.remove();
    }, 3500);
}
