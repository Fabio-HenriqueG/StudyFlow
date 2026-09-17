# Guia de Migração: SQLite (Room) para Firebase Firestore

Este documento descreve o progresso da migração do banco de dados local para a nuvem e os próximos passos necessários.

## ✅ O que já foi feito
1.  **Modelo `Tarefa` e `Meta`**: Atualizados para suportar IDs em `String` (Firestore) e compatibilidade com notificações/check-ins.
2.  **`FirestoreService`**: Criado e expandido para centralizar as operações de Tarefas e Metas.
3.  **CRUD de Tarefas**: Totalmente migrado no `TarefasFragment`, `CriaTarefaFragment`, `TarefaAdapter` e `HistoricoTarefasFragment`.
4.  **CRUD de Metas**: Totalmente migrado no `MetasFragment`, `CriaMetaFragment` e `MetaAdapter`.
5.  **Lógica de Sistema**: `MainActivity` e `NotificacaoScheduler` ajustados para o novo fluxo.

## 🚀 Próximos Passos (Obrigatórios)

### 1. Migração das Entidades Restantes
Seguir o mesmo padrão de criação de serviço e atualização de UI para:
*   [x] **Metas** (Concluído)
*   [ ] **Anotações**
*   [ ] **Checklists**
*   [ ] **Materias**
*   [ ] **Flashcards**

### 2. Autenticação (Firebase Auth)
*   Ativar o login (Google ou E-mail) no console do Firebase.
*   Adicionar o campo `usuarioId` em todas as entidades para garantir que os dados sejam privados e sincronizados.

### 3. Índices no Firestore
*   Ao rodar consultas complexas, clique nos links gerados no Logcat para criar os índices necessários no console do Firebase.

### 4. Limpeza de Código
*   Após migrar todas as entidades, remover as dependências do Room e a classe `AppDatabase`.

---

## ⚠️ O App Roda Agora?
**Sim, o app deve compilar e rodar.** 

Entretanto, observe:
1.  **Dados Locais vs Nuvem**: As tarefas que você tinha no celular (Room) não aparecerão na nova lista, pois o app agora está lendo apenas do Firebase.
2.  **Internet**: O app agora exige conexão com a internet para carregar/salvar as tarefas (embora o Firestore tenha cache offline, o primeiro carregamento precisa da rede).
3.  **Notificações**: Tarefas criadas antes da migração podem ter problemas para serem canceladas via ID antigo, mas as novas funcionarão perfeitamente.
