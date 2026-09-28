    </main>

    <!-- Rodapé -->
    <footer class="bg-gray-100 border-t border-gray-200 py-6 mt-8">
        <div class="max-w-7xl mx-auto px-4 text-center text-sm text-gray-500">
            <p>&copy; <?= date('Y') ?> StudyFlow Web - Organizador de Estudos. Todos os direitos reservados.</p>
        </div>
    </footer>

    <!-- Firebase SDK (v10 compat/modular via scripts cdn) -->
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-app-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-auth-compat.js"></script>
    <script src="https://www.gstatic.com/firebasejs/10.8.0/firebase-firestore-compat.js"></script>

    <!-- Configuração e Lógica do Firebase -->
    <script src="assets/js/firebase-init.js"></script>
    <script src="assets/js/app.js"></script>
</body>
</html>
