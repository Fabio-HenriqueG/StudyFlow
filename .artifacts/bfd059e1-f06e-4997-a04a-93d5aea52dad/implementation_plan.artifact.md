# Implementação da Tela de Configurações

Este plano detalha a criação da tela de configurações, permitindo a personalização do perfil, tema e notificações.

## User Review Required

> [!NOTE]
> As configurações de nome de usuário e tema são salvas localmente usando SharedPreferences.

## Proposed Changes

### [Componente] Interface do Usuário

#### [NEW] [fragment_configuracoes.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_configuracoes.xml)
- Layout com seções para Perfil, Aparência e Notificações.
- Inclui seletor de tema (Claro/Escuro/Sistema) e campo para nome.

#### [MODIFY] [fragment_home.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_home.xml)
- Adicionado ícone de engrenagem no topo para acessar as configurações.

### [Componente] Lógica e Persistência

#### [NEW] [ConfiguracoesFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/ConfiguracoesFragment.java)
- Gerencia o salvamento das preferências no SharedPreferences.
- Implementa a troca dinâmica de tema usando `AppCompatDelegate`.

#### [MODIFY] [HomeFragment.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/HomeFragment.java)
- Carrega o nome do usuário para personalizar a saudação.

#### [MODIFY] [MainActivity.java](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/java/com/example/studyflow/MainActivity.java)
- Aplica o tema salvo durante a inicialização do app.

## Verification Plan

### Manual Verification
1. Abrir a Home e clicar na engrenagem.
2. Alterar o nome e salvar. Verificar se a saudação na Home mudou.
3. Alternar entre tema Claro e Escuro. O app deve mudar de cor instantaneamente.
4. Reiniciar o app e verificar se as configurações foram mantidas.
