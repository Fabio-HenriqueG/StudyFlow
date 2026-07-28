# Modernização Visual com Material 3

Transformamos a interface do StudyFlow para um padrão moderno, limpo e totalmente adaptável ao tema do sistema (Claro e Escuro).

## O que foi melhorado

### 1. Sistema de Cores Dinâmico
- **Material 3**: Implementamos o sistema de cores do Material Design 3.
- **Modo Escuro**: O app agora muda todas as cores automaticamente conforme a configuração do celular do usuário.
- **Cores Semânticas**: Substituímos cores fixas (como `#FFFFFF`) por atributos dinâmicos (como `?attr/colorSurface`), garantindo que o texto sempre seja legível em qualquer fundo.

### 2. Componentes Modernizados
- **Campos de Texto**: Na tela de criação, agora usamos o estilo "Outlined" do Material 3, que é muito mais elegante e funcional.
- **Cartões de Tarefas**: Os itens da lista agora são `MaterialCardView`, com bordas arredondadas e um design mais leve.
- **Botões**: Todos os botões foram atualizados para o padrão Material 3, com feedback visual aprimorado ao clicar.

### 3. Layout Responsivo e Limpo
- **Espaçamentos**: Ajustamos margens e paddings em todas as telas para dar mais "ar" ao design.
- **Hierarquia Visual**: Títulos maiores e cores de destaque ajudam o usuário a focar no que é importante.
- **Menu Inferior**: O menu de navegação foi simplificado para seguir o novo padrão visual.

## Arquivos Principais

- [themes.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/values/themes.xml): Configuração central do novo estilo.
- [fragment_cria_tarefa.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/fragment_cria_tarefa.xml): Exemplo da nova interface de formulário.
- [item_tarefa.xml](file:///C:/Users/GABINETE-04/StudioProjects/StudyFlow/app/src/main/res/layout/item_tarefa.xml): O novo visual dos cards de tarefas.

## Como Testar
1. Abra o app no **Tema Claro**.
2. Vá nas configurações do seu Android e mude para o **Tema Escuro**.
3. Volte para o StudyFlow e veja como ele se adaptou instantaneamente.
4. Navegue pelas abas e note como os títulos e botões estão mais harmônicos.
