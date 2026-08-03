# Guia de Continuidade do Projeto: StudyFlow

Este documento serve como um "manual de instruções" para qualquer IA ou desenvolvedor que precise dar continuidade a este projeto no Android Studio.

## 📌 Visão Geral do Projeto
O **StudyFlow** é um app de produtividade e organização pessoal desenvolvido com Java e Room Database, seguindo o padrão Material Design 3.

---

## 🛠️ Status das Funcionalidades

### 1. Sistema de Tarefas (Concluído)
- Listagem em Cards, Criação, Edição e Exclusão.
- **Notificações Inteligentes**: Usando `WorkManager`. Frequência dinâmica:
    - Mesmo dia: alerta a cada 30min e um inicial em 10s.
    - Prazos longos: frequência reduzida (6h, 12h, 24h).
- **Ajuste Técnico**: Prazos de tarefas diárias são salvos como `23:59:59` para evitar expiração imediata.

### 2. Sistema de Metas (Concluído)
- Contador em tempo real (dias, horas, minutos, segundos).
- **Check-in Diário**: Botão que desabilita após o uso e reinicia no dia seguinte.
- Ordenação por tempo de atividade (mais antigas primeiro).

### 3. Sistema de Checklist (Concluído)
- Estrutura hierárquica (Lista -> Tópicos).
- Tópicos marcáveis com efeito visual (riscado).
- Exclusão em cascata (deletar lista apaga os tópicos).

### 4. Caderno Digital (FASE ATUAL: Fase 2)
O objetivo é um editor de notas "estilo caderno" com formatação livre.
- **Fase 1 (Concluída)**: Banco de Dados (Room v5) implementado salvando conteúdo em **HTML** para preservar estilos. Listagem de notas funcional.
- **Fase 2 (Iniciando)**: Implementação de ferramentas de formatação (Negrito e Itálico) usando `Spannables`.
- **Fase 3 (Planejada)**: Seletores de Cor e Tamanho de Fonte.
- **Fase 4 (Planejada)**: Inserção de imagens da galeria.

---

## 🏗️ Estrutura do Banco de Dados (Room)
- **Versão Atual**: 5
- **Tabelas**: `Tarefas`, `Metas`, `Checklists`, `ChecklistItems`, `Anotacoes`.
- **Dica para a IA**: Ao modificar entidades, lembre-se de incrementar a versão no `AppDatabase.java` e usar `.fallbackToDestructiveMigration()` para testes rápidos.

---

## 🚀 Próximos Passos para o Desenvolvedor/IA
1.  Continuar a **Fase 2 do Caderno Digital** no arquivo `EditorAnotacaoFragment.java`.
2.  Implementar a lógica de `SpannableStringBuilder` para aplicar `StyleSpan` (Bold/Italic) ao texto selecionado no `editConteudo`.
3.  Garantir que a conversão `Html.toHtml` e `Html.fromHtml` continue preservando esses novos estilos.

---
*Documento gerado em 03/08/2026 para facilitar a portabilidade do projeto.*
