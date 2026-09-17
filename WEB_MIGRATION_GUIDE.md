# Guia de Desenvolvimento: StudyFlow Versão Web

Este documento serve como um mapa de arquitetura e roteiro para iniciar a criação da versão web do StudyFlow, garantindo sincronização em tempo real com o aplicativo Android através do Firebase Firestore.

---

## 🛠️ 1. Ambiente de Desenvolvimento (IDE)

Para o desenvolvimento da aplicação Web, a recomendação principal é o uso de ferramentas leves e focadas no ecossistema JavaScript/TypeScript.

*   **Recomendação:** **Visual Studio Code (VS Code)**
    *   *Vantagens:* Gratuito, extremamente leve, possui a maior comunidade e a melhor biblioteca de extensões para React e Firebase.
*   **Alternativa JetBrains:** **WebStorm**
    *   *Vantagens:* Se você prefere manter a mesma experiência visual, atalhos e ferramentas integradas do Android Studio, o WebStorm é o equivalente da JetBrains para Web (porém, é pago).

---

## 🚀 2. Stack Tecnológica Recomendada

Para criar uma interface rica, reativa e compatível com as regras de negócio já estabelecidas no Android, a melhor combinação é:

1.  **Framework Principal:** **React.js** (utilizando o empacotador **Vite**)
    *   *Por que?* O sistema de componentes do React se assemelha muito à lógica de Fragmentos/Views que você já domina no Android. O Vite oferece inicialização e recarregamento de tela instantâneos.
2.  **Biblioteca de Interface (UI):** **Material UI (MUI)**
    *   *Por que?* Como o aplicativo Android utiliza componentes Material Design (MaterialCardView, ChipGroup, FloatingActionButton), o MUI para React permitirá recriar o visual exato do aplicativo móvel na web de forma nativa e responsiva.
3.  **Utilitário de Estilização:** **Tailwind CSS** (Opcional, para ajustes rápidos de layout e espaçamentos).
4.  **Banco de Dados:** **Firebase SDK Web (versão 10+)** para conexão direta com o Firestore e Authentication.

---

## 📋 3. Passo a Passo Inicial

### Passo 1: Configuração no Console do Firebase
1. Acesse o [Console do Firebase](https://console.firebase.google.com/).
2. Clique no ícone de engrenagem (**Configurações do Projeto**).
3. Na aba *Geral*, role até a seção *Seus aplicativos* e clique no ícone Web (`</>`).
4. Registre o app com o nome `StudyFlow Web`.
5. O console exibirá um objeto chamado `firebaseConfig`. Copie essas credenciais.

### Passo 2: Criando o Projeto Localmente
Certifique-se de ter o **Node.js** instalado. No terminal do seu computador, execute:

```bash
# 1. Cria o projeto React com suporte ao Vite
npm create vite@latest studyflow-web -- --template react

# 2. Entra na pasta do projeto
cd studyflow-web

# 3. Instala as dependências padrão
npm install

# 4. Instala o SDK oficial do Firebase
npm install firebase
```

### Passo 3: Conectando ao Banco de Dados
Abra a pasta no VS Code e, dentro da pasta `src/`, crie um arquivo chamado `firebase.js`:

```javascript
import { initializeApp } from "firebase/app";
import { getFirestore } from "firebase/firestore";

// Credenciais geradas no Console do Firebase
const firebaseConfig = {
  apiKey: "SUA_API_KEY",
  authDomain: "SEU_AUTH_DOMAIN",
  projectId: "SEU_PROJECT_ID",
  storageBucket: "SEU_STORAGE_BUCKET",
  messagingSenderId: "SEU_MESSAGING_SENDER_ID",
  appId: "SEU_APP_ID"
};

// Inicializa o Firebase
const app = initializeApp(firebaseConfig);

// Exporta a instância do Firestore para ser usada nas telas
export const db = getFirestore(app);
```

Para rodar o servidor de desenvolvimento e começar a programar, execute no terminal do VS Code:
```bash
npm run dev
```

---

## ⚠️ 4. Regra de Ouro para Sincronização

O Firestore é um banco de dados NoSQL baseado em documentos. Para que o site e o aplicativo Android mostrem os mesmos dados, a estrutura dos campos precisa ser **idêntica**.

### Dicionário de Campos (Coleção `tarefas`):
| Campo no Android (Java) | Campo no Firestore (Web/JSON) | Tipo de Dado |
| :--- | :--- | :--- |
| `id` | ID do Documento | `String` |
| `titulo` | `titulo` | `String` |
| `descricao` | `descricao` | `String` |
| `dataLimite` | `dataLimite` | `Number` (Timestamp em milissegundos) |
| `prioridade` | `prioridade` | `Number` (0=Baixa, 1=Média, 2=Alta) |
| `insistencia` | `insistencia` | `Number` (0=Focada, 1=Padrão, 2=Intensa) |
| `concluida` | `concluida` | `Boolean` |
| `dataConclusao` | `dataConclusao` | `Number` (Timestamp em milissegundos) |

### Dicionário de Campos (Coleção `metas`):
| Campo no Android (Java) | Campo no Firestore (Web/JSON) | Tipo de Dado |
| :--- | :--- | :--- |
| `id` | ID do Documento | `String` |
| `titulo` | `titulo` | `String` |
| `dataCriacao` | `dataCriacao` | `Number` (Timestamp em milissegundos) |
| `ultimoCheckin` | `ultimoCheckin` | `Number` (Timestamp em milissegundos) |
| `ultimoAlerta` | `ultimoAlerta` | `Number` (Timestamp em milissegundos) |

---

## 🌐 5. Hospedagem (Hosting)

Como você já está utilizando o Firebase para o banco de dados (Firestore), a escolha mais natural, eficiente e barata é o **Firebase Hosting**.

### Vantagens do Firebase Hosting:
1.  **Plano Gratuito Generoso:** Totalmente gratuito para projetos pequenos e de estudantes (inclui SSL/HTTPS grátis e suporte a domínios personalizados).
2.  **Integração Nativa:** Você gerencia o banco e a hospedagem no mesmo painel, usando a mesma conta.
3.  **Deploy em 1 Comando:** Você publica o site na internet digitando apenas `firebase deploy` no terminal.
4.  **CDN Global:** O site carrega muito rápido pois o Firebase distribui os arquivos em servidores ao redor do mundo.

### Como configurar a Hospedagem (Quando o site estiver pronto):
No terminal do seu projeto da Web (`studyflow-web`), você executará os seguintes passos:

1.  **Instalar as ferramentas do Firebase no computador globalmente:**
    ```bash
    npm install -g firebase-tools
    ```
2.  **Fazer login na sua conta do Firebase pelo terminal:**
    ```bash
    firebase login
    ```
3.  **Iniciar a configuração do Hosting no projeto:**
    ```bash
    firebase init hosting
    ```
    *   *Perguntas do assistente:*
        *   Selecione seu projeto existente do StudyFlow.
        *   Qual pasta usar como diretório público? Digite **`dist`** (que é a pasta que o Vite gera ao compilar).
        *   Configurar como um aplicativo de página única (SPA)? Digite **`Yes`** (essencial para React/Vue).
        *   Configurar builds automáticos com o GitHub? Digite `No` (pode ativar isso depois se quiser).
4.  **Gerar a versão final do site e publicar:**
    ```bash
    # Compila o projeto React
    npm run build
    
    # Envia para a internet
    firebase deploy
    ```

Após o comando, o terminal gerará um link público (ex: `https://seu-projeto.web.app`) e o seu site já estará no ar!

### 🌍 Outras Alternativas Gratuitas:
Se futuramente você não quiser usar o Firebase Hosting por algum motivo, existem outras excelentes opções focadas em front-end que possuem planos gratuitos vitalícios:
*   **Vercel:** A melhor experiência de deploy do mercado (conecta no seu GitHub e atualiza o site a cada `git push`).
*   **Netlify:** Muito similar à Vercel, excelente para projetos React.
*   **GitHub Pages:** Bom para sites estáticos simples, mas exige configurações adicionais para rotas do React.

