# Modernização Visual e Responsividade com Material 3

Este plano detalha a reformulação completa da interface do StudyFlow para seguir os princípios do Material Design 3, garantindo um visual limpo, profissional e que se adapta automaticamente ao tema do sistema (Claro/Escuro).

## User Review Required

> [!IMPORTANT]
> A mudança para Material 3 alterará significativamente o visual de botões e campos de texto para um estilo mais moderno e arredondado.

## Proposed Changes

### [Componente] Temas e Estilos

#### [MODIFY] [themes.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/values/themes.xml)
- Definir cores semânticas usando atributos do Material 3 (`colorPrimary`, `colorSurface`, etc.).
- Configurar estilos globais para botões e campos de texto.

#### [MODIFY] [themes.xml (night)](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/values-night/themes.xml)
- Configurar as mesmas cores semânticas, mas com tons otimizados para o modo escuro.

### [Componente] Layouts Principais

#### [MODIFY] [activity_main.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/activity_main.xml)
- Remover cores hardcoded do `BottomNavigationView`.
- Ajustar para que ele use as cores do tema.

#### [MODIFY] [fragment_tarefas.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_tarefas.xml)
- Refinar espaçamentos.
- Usar cores de texto dinâmicas.

#### [MODIFY] [fragment_cria_tarefa.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_cria_tarefa.xml)
- Substituir `EditText` simples por `TextInputLayout` com `MaterialTextInputEditText`.
- Usar `MaterialButton`.
- Melhorar a hierarquia visual.

### [Componente] Itens de Lista

#### [MODIFY] [item_tarefa.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_tarefa.xml)
- Usar `MaterialCardView`.
- Ajustar cores de texto para `?attr/colorOnSurface` e `?attr/colorOnSurfaceVariant`.

## Verification Plan

### Manual Verification
1. **Tema Claro/Escuro**: Alternar o tema do sistema do celular e verificar se o app muda as cores corretamente.
2. **Responsividade**: Testar em diferentes tamanhos de tela (se possível no emulador) para garantir que os elementos não fiquem cortados.
3. **Usabilidade**: Verificar se os campos de texto e botões têm estados visuais claros (foco, clique).
