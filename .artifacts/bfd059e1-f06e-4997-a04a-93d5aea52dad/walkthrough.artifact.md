# Sistema de Checklist Hierárquico Completo

Implementamos um sistema robusto de checklists que permite organizar suas tarefas em listas temáticas, com controle total sobre cada tópico.

## Novas Funcionalidades

### 1. Gerenciamento de Listas (Checklists)
- **Criação Dinâmica**: Através do menu **+**, você pode criar novas listas (ex: "Compras", "Viagem").
- **Organização em Cards**: Na aba de Checklists, suas listas aparecem em cartões limpos e modernos, seguindo o padrão das Tarefas e Metas.
- **Edição e Exclusão**: Cada card possui o menu de três pontos (`⋮`) para renomear ou remover a lista completa.

### 2. Detalhes e Tópicos
- **Tela Interna**: Ao clicar em uma lista, você entra em uma tela dedicada para gerenciar os itens daquela checklist.
- **Adição Rápida**: Um campo na parte inferior permite adicionar novos tópicos instantaneamente.
- **Interação Completa**: Cada tópico possui:
    - **Check**: Marque ou desmarque itens. Itens marcados ganham um efeito visual de riscado.
    - **Edição**: Corrija o texto de um tópico clicando no ícone de lápis.
    - **Exclusão**: Remova tópicos individuais clicando no ícone de lixeira.

### 3. Persistência de Dados
- **Banco de Dados Room**: Todas as listas e itens são salvos localmente. Mesmo que o app seja fechado, suas marcações e tópicos estarão lá quando você voltar.
- **Cascata**: Ao excluir uma lista principal, todos os seus tópicos são removidos automaticamente do banco.

## Detalhes Técnicos
- **Checklist.java & ChecklistItem.java**: Novas entidades com relacionamento de Chave Estrangeira.
- **ChecklistDao.java**: Gerencia as operações complexas de busca e atualização das duas tabelas.
- **Adapters Especializados**: `ChecklistAdapter` para as listas e `ChecklistItemsAdapter` para a lógica interna dos tópicos.

## Como Testar
1. Vá no menu **+** e selecione **Novo Checklist**.
2. Dê um nome para a lista e salve.
3. Na aba **Checklist**, clique no card que você acabou de criar.
4. Adicione alguns tópicos no campo inferior (ex: "Comprar pão", "Comprar leite").
5. Marque um item e veja o efeito visual.
6. Edite o texto de um item usando o ícone lateral.
