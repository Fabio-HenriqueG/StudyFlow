# Plano de Implementação: Caderno Inteligente (Rich Text Editor)

Este plano detalha a criação de um editor de anotações avançado que permite formatação de texto (tamanho, cor, estilo) e inserção de imagens, transformando a aba de anotações em um caderno digital versátil.

## User Review Required

> [!WARNING]
> Para salvar formatação de texto (cores, negrito, etc.) no banco de dados, utilizaremos o formato **HTML**. Isso permite que o estilo seja preservado entre as sessões.
> A inserção de imagens exigirá permissões de leitura de mídia no dispositivo.

## Proposed Changes

### [Componente] Banco de Dados

#### [NEW] [Anotacao.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/Anotacao.java)
- Entidade Room com campos: `id`, `titulo`, `conteudoHtml` (onde a formatação fica salva) e `dataUltimaEdicao`.

#### [NEW] [AnotacaoDao.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/dao/AnotacaoDao.java)
- CRUD básico para as anotações.

#### [MODIFY] [AppDatabase.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/data/AppDatabase.java)
- Adicionar `Anotacao.class` e incrementar a versão para 5.

### [Componente] Interface do Editor

#### [MODIFY] [fragment_anotacoes.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_anotacoes.xml)
- RecyclerView para listar as notas existentes.

#### [NEW] [fragment_editor_anotacao.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_editor_anotacao.xml)
- **Barra de Ferramentas**: Botões para Negrito, Itálico, Cor, Tamanho de Fonte e Inserir Imagem.
- **Área de Edição**: Um `EditText` especializado ou uma WebView que suporte edição de texto rico.

### [Componente] Lógica de Edição

#### [NEW] [EditorAnotacaoFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/EditorAnotacaoFragment.java)
- Implementar a lógica de aplicar estilos ao texto selecionado usando `SpannableStringBuilder`.
- Lógica de abertura de galeria para selecionar imagens.

#### [NEW] [AnotacaoAdapter.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/AnotacaoAdapter.java)
- Exibir prévia das notas na lista principal.

## Estratégia de Desenvolvimento

1.  **Fase 1: Estrutura**: Criar o banco de dados e a tela de listagem de anotações.
2.  **Fase 2: O Editor**: Criar a tela de escrita com a barra de ferramentas básica (Negrito/Itálico).
3.  **Fase 3: Estilização Avançada**: Adicionar seletor de cores e tamanhos.
4.  **Fase 4: Imagens**: Implementar a inserção de fotos no corpo da nota.

## Verification Plan

### Manual Verification
1. Criar uma nota.
2. Escrever um texto, selecionar uma parte e aplicar **Negrito**.
3. Mudar a cor de uma palavra.
4. Salvar, fechar o app, abrir novamente e verificar se a formatação continua lá.
5. Tentar inserir uma imagem da galeria.
