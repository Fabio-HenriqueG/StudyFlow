# StudyFlow

TCC — Desenvolvimento de Sistemas 2026 (ETEC Prof. José Carlos Seno Júnior) - Desenvolvido pelos alunos Andrey, Akin, Bruno e Fábio

StudyFlow é um aplicativo Android escrito em Java que ajuda estudantes a organizar estudos usando funcionalidades como anotações, flashcards, checklists, metas e tarefas com calendário e notificações. Este repositório contém o código-fonte do app (gradle/Android), recursos e classes responsáveis pela interface e lógica principal.

## Principais funcionalidades
- Anotações (editor e visualização)
- Flashcards (criação, revisão e estatísticas)
- Checklists (criação de listas com itens)
- Tarefas e calendário de tarefas (criação, histórico, notificações)
- Metas (criação/visualização)
- Editor de texto e desenho (desenho livre em anotações)
- Notificações planejadas e imediatas (workers/helpers)
- Área de configurações e menu de opções

## Estrutura do projeto (resumo)
- app/ — módulo Android principal
  - app/src/main/AndroidManifest.xml
  - app/src/main/java/com/example/studyflow/ — pacotes e classes Java
    - MainActivity.java, HomeFragment.java
    - Vários Fragments para telas: FlashcardsFragment, TarefasFragment, MetasFragment, AnotacoesFragment, etc.
    - Adapters e Helpers: FlashcardAdapter, TarefaAdapter, MateriaAdapter, NotificacaoHelper, etc.
    - Visual/UX: DesenhoView.java, Draw utilities (GridDrawable, ArrowDrawable, ShapeDrawableHelper)
    - Workers: NotificacaoWorker.java, NotificacaoImediataWorker.java
  - app/src/main/res/ — recursos (imagens, layouts, strings)
  - app/build.gradle.kts — dependências e configurações do módulo
- build.gradle.kts, settings.gradle.kts, gradle.properties, scripts gradle (wrapper)

Observação: a pasta de código principal está em app/src/main/java/com/example/studyflow. Para navegar nos arquivos do pacote use:
https://github.com/Fabio-HenriqueG/StudyFlow/tree/master/app/src/main/java/com/example/studyflow

Há mais arquivos/classe no pacote (adapters, fragments e utilitários) que detalham as telas e o comportamento — consulte o link acima para ver a lista completa.

## Tecnologias
- Linguagem: Java
- Plataforma: Android (SDK)
- Sistema de build: Gradle (wrapper incluso)
- Arquitetura: tradicional de Activities/Fragments com Adapters para listas

## Pré-requisitos
- Android Studio (recomendado)
- JDK compatível com a versão do Gradle usada no projeto
- Dispositivo ou emulador Android (API compatível; ver compileSdk/targetSdk em app/build.gradle.kts)

## Como compilar e executar
1. Clone o repositório:
   git clone https://github.com/Fabio-HenriqueG/StudyFlow.git
2. Abra o projeto no Android Studio (File → Open → selecione a pasta do projeto).
3. Deixe o Android Studio sincronizar o Gradle e baixar dependências.
4. Conecte um dispositivo ou inicie um emulador.
5. Execute o app (Run → Run 'app').

Alternativamente pelo terminal:
- No diretório do projeto execute: 
  - Linux/macOS: ./gradlew assembleDebug
  - Windows: gradlew.bat assembleDebug

Para instalar o APK gerado: adb install -r app/build/outputs/apk/debug/app-debug.apk

## Permissões e configurações comuns
- Notificações: o app utiliza workers para programar notificações; ver AndroidManifest.xml e classes NotificacaoHelper/NotificacaoWorker.
- Armazenamento/arquivos: se o app salva anotações ou imagens localmente, as permissões relevantes serão declaradas no manifest.

## Boas práticas para contribuir
- Teste em emulador e dispositivo real quando alterar código relacionado a UI, armazenamento ou notificações.

## Sugestões de melhorias (ideias)
- Migrar para Kotlin (opcional) e arquitetura MVVM + Jetpack (ViewModel, LiveData/StateFlow)
- Adicionar persistência com Room para gerenciar anotações, tarefas e flashcards
- Implementar testes instrumentados e unitários
- Suporte a temas (claro/escuro) e acessibilidade (tamanhos de fonte, contentDescription)

## Contato / Autor
- Repositório: https://github.com/Fabio-HenriqueG/StudyFlow
- Autor: Fabio-HenriqueG


