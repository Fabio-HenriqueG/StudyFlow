# Implementação de Opções de Tarefas (Editar/Excluir)

As funcionalidades de edição e exclusão de tarefas foram implementadas com sucesso. Agora, cada item da lista possui um menu de opções acessível por um botão de três pontos.

## Mudanças Realizadas

### 1. Interface do Usuário
- **Novo Ícone**: Adicionado `ic_more_vert.xml` para representar o menu de opções.
- **Layout de Item**: Modificado `item_tarefa.xml` para incluir o botão de opções posicionado à direita do título e data.

### 2. Lógica de Negócio e Dados
- **TarefaDao**: Adicionados métodos `@Update` e `@Delete` para permitir a manipulação de tarefas existentes.
- **Entidade Tarefa**: Implementada a interface `Serializable` para permitir que objetos de tarefa sejam passados como argumentos entre fragmentos.
- **TarefaAdapter**:
    - Implementada a exibição de um `PopupMenu` ao clicar no botão de opções.
    - Implementada a exclusão direta com atualização imediata da lista.
    - Implementada a navegação para a tela de edição.

### 3. Fluxo de Edição
- **CriaTarefaFragment**:
    - Agora detecta se recebeu uma tarefa para edição através de `arguments`.
    - Preenche os campos automaticamente se estiver em modo de edição.
    - O botão muda o texto para "Atualizar".
    - A lógica de salvamento diferencia entre inserir uma nova tarefa ou atualizar uma existente.

## Como Testar
1. Vá para a tela de **Tarefas**.
2. Clique no ícone de três pontos (`⋮`) em qualquer tarefa.
3. Escolha **Excluir** para remover a tarefa da lista.
4. Escolha **Editar** para abrir a tela de criação com os dados da tarefa preenchidos.
5. Altere o título ou descrição e clique em **Atualizar** para salvar as mudanças.

![Screenshot do item com o botão de opções](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_tarefa.xml)
