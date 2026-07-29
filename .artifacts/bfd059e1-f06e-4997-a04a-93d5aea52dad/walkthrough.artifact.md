# Exibição de Metas Dinâmicas na Home

Atualizamos o scroll de metas na tela inicial para exibir dados reais do banco de dados, com foco na longevidade de cada objetivo.

## O que mudou

### 1. Scroll Dinâmico (RecyclerView)
- Substituímos o componente estático por um `RecyclerView` horizontal. Agora, todas as metas que você cria aparecem automaticamente na tela inicial sem precisar de ajustes manuais.

### 2. Ordenação por Tempo de Atividade
- As metas são exibidas em ordem **decrescente de tempo**: os objetivos que você mantém há mais tempo aparecem primeiro (da esquerda para a direita). Isso destaca sua persistência nos hábitos de longa data.

### 3. Visual Simplificado
- Para manter a Home limpa e focada, removemos o cronômetro detalhado (segundos/minutos) e passamos a exibir apenas a contagem de **dias ativos**.
- Se uma meta foi criada hoje, o card exibe "Começou hoje".

## Detalhes Técnicos
- **item_meta_home.xml**: Novo layout de card compacto para a Home.
- **MetaHomeAdapter.java**: Gerencia o cálculo simplificado de dias e a exibição dos títulos.
- **HomeFragment.java**: Agora carrega a lista de metas do Room e realiza a ordenação cronológica antes de exibir.

## Como Testar
1. Crie uma nova meta.
2. Volte para a Home e veja-a aparecer no final da lista (se for a mais recente).
3. Verifique se as metas que você criou anteriormente continuam nas primeiras posições, exibindo a quantidade correta de dias.
