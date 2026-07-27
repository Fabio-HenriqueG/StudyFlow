# Implementar Opções de Editar e Excluir nas Tarefas

Este plano detalha a adição de um botão de opções em cada item da lista de tarefas, permitindo que o usuário edite ou exclua uma tarefa diretamente do `RecyclerView`.

## User Review Required

> [!IMPORTANT]
> A funcionalidade de edição exigirá modificações no `CriaTarefaFragment` para que ele possa receber uma tarefa existente e preencher os campos automaticamente.

## Proposed Changes

### [Componente] Layout e Recursos

#### [MODIFY] [item_tarefa.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_tarefa.xml)
- Adicionar um `ImageButton` com um ícone de "mais opções" (três pontos verticais).
- Ajustar o `RelativeLayout` para que o botão fique posicionado à direita, ao lado da data ou do título.

### [Componente] Lógica do Adapter

#### [MODIFY] [TarefaAdapter.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/TarefaAdapter.java)
- Atualizar o `TarefaViewHolder` para incluir o novo `ImageButton`.
- No `onBindViewHolder`, configurar o clique do botão para exibir um `PopupMenu`.
- Implementar as ações do `PopupMenu`:
    - **Editar**: Navegar para o `CriaTarefaFragment` passando a tarefa selecionada.
    - **Excluir**: Chamar o `TarefaDao` para remover a tarefa do banco de dados e atualizar a lista no Adapter.

### [Componente] Criação/Edição de Tarefas

#### [MODIFY] [CriaTarefaFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/CriaTarefaFragment.java)
- Adicionar suporte para receber um objeto `Tarefa` via `Arguments`.
- Se uma tarefa for recebida, preencher os campos `EditText` e mudar o texto do botão para "Atualizar".
- Alterar a lógica de salvamento para decidir entre `inserir` ou `atualizar` no banco de dados.

#### [MODIFY] [TarefaDao.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/dao/TarefaDao.java)
- Adicionar os métodos `@Update void atualizar(Tarefa tarefa)` e `@Delete void excluir(Tarefa tarefa)`.

## Verification Plan

### Automated Tests
- Não se aplica (verificação manual via interface).

### Manual Verification
1. Abrir a tela de tarefas.
2. Verificar se o botão de opções aparece em cada item.
3. Clicar no botão e verificar se o menu com "Editar" e "Excluir" é exibido.
4. Testar a exclusão: a tarefa deve sumir da lista imediatamente.
5. Testar a edição: deve abrir a tela de criação com os dados preenchidos, e ao salvar, a tarefa original deve ser atualizada.
