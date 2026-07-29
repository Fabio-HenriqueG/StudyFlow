# Exibição de Metas na Home com Ordenação por Tempo

Este plano detalha a substituição do scroll estático de metas na tela inicial por uma lista dinâmica (RecyclerView) que exibe as metas reais do usuário, ordenadas pelas que estão ativas há mais tempo.

## User Review Required

> [!NOTE]
> O visual das metas na Home será simplificado, exibindo apenas o título e a quantidade de dias ativos (ex: "5 dias"), sem o cronômetro de segundos para manter a tela limpa.

## Proposed Changes

### [Componente] Layouts

#### [NEW] [item_meta_home.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_meta_home.xml)
- Layout de card horizontal pequeno para a Home.
- Inclui Título e um campo simplificado de "Dias Ativos".

#### [MODIFY] [fragment_home.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_home.xml)
- Substituir o `HorizontalScrollView` (metas principais) por um `androidx.recyclerview.widget.RecyclerView`.
- Configurar o `layoutManager` como `LinearLayoutManager` horizontal.

### [Componente] Lógica e Dados

#### [NEW] [MetaHomeAdapter.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/MetaHomeAdapter.java)
- Adapter especializado para os cards pequenos da Home.
- Lógica para calcular apenas a diferença em dias.

#### [MODIFY] [HomeFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/HomeFragment.java)
- Buscar metas no banco de dados.
- Ordenar a lista: as metas com `dataCriacao` mais antiga (mais tempo ativas) aparecem primeiro.
- Inicializar o novo RecyclerView.

## Verification Plan

### Manual Verification
1. Criar várias metas com nomes diferentes.
2. Ir para a tela inicial e verificar se elas aparecem no scroll horizontal.
3. Verificar se a meta criada primeiro (que tem mais tempo) aparece na primeira posição da esquerda.
4. Garantir que o texto exibe corretamente "X dias" ou "Hoje" caso tenha menos de 24h.
