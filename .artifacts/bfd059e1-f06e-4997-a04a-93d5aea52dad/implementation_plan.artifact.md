# Implementação do Sistema de Checklist Completo

Este plano detalha a criação de um sistema de checklists hierárquico, onde o usuário pode gerenciar múltiplas listas, cada uma com seus próprios tópicos editáveis e marcáveis.

## User Review Required

> [!IMPORTANT]
> A implementação exigirá duas novas tabelas no banco de dados. Como estamos mudando a estrutura, a versão do banco de dados será incrementada para 4.

## Proposed Changes

### [Componente] Banco de Dados e Modelos

#### [NEW] [Checklist.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/Checklist.java)
- Entidade representando a lista principal (ex: "Viagem", "Supermercado").
- Campos: `id`, `titulo`.

#### [NEW] [ChecklistItem.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/ChecklistItem.java)
- Entidade representando os tópicos dentro de uma checklist.
- Campos: `id`, `checklistId` (vinculado à lista pai), `texto`, `isChecked`.

#### [NEW] [ChecklistDao.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/dao/ChecklistDao.java)
- Gerencia as listas principais e seus itens relacionados (CRUD completo).

#### [MODIFY] [AppDatabase.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/AppDatabase.java)
- Registrar novas entidades e DAO.
- Incrementar versão para 4.

### [Componente] Interface da Lista de Checklists

#### [MODIFY] [fragment_check_list.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_check_list.xml)
- Adicionar `RecyclerView` para as listas.

#### [NEW] [item_checklist.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_checklist.xml)
- Layout de card para a lista principal (estilo tarefas).

#### [NEW] [ChecklistAdapter.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/ChecklistAdapter.java)
- Adapter para gerenciar o clique na lista e navegação para os detalhes.

### [Componente] Detalhes do Checklist (Tópicos)

#### [NEW] [fragment_checklist_detalhes.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_checklist_detalhes.xml)
- Tela que exibe o título da lista e os tópicos.
- Botão flutuante ou campo para adicionar novos tópicos.

#### [NEW] [item_checklist_topico.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_checklist_topico.xml)
- Layout de cada item: `CheckBox`, `TextView` (texto do tópico), e botões de editar/excluir.

#### [NEW] [ChecklistDetalhesFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/ChecklistDetalhesFragment.java)
- Lógica para gerenciar os itens de uma lista específica.

### [Componente] Criação e Integração

#### [NEW] [CriaChecklistFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/CriaChecklistFragment.java)
- Tela para criar/editar o título de uma checklist.

#### [MODIFY] [MenuMaisBottomSheet.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/MenuMaisBottomSheet.java)
- Vincular o botão "Novo Checklist".

## Verification Plan

### Manual Verification
1. Criar uma nova Checklist ("Testes").
2. Abrir a Checklist criada.
3. Adicionar 3 tópicos.
4. Marcar um tópico como concluído e verificar se ele persiste.
5. Editar o texto de um tópico.
6. Excluir um tópico.
7. Voltar e verificar se a contagem (opcional) ou o título da lista na Home/Lista Geral está correto.
