# Quick Command — Implementation Plan

**Goal:** oferecer seis ordens frequentes por uma interface rápida, nativa e não
pausante, reutilizando toda a autoridade já existente.

## Task 1: Layout e modelo de ação

- Criar enum de seis ações e política pura de posições radiais responsivas.
- Testar IDs, posições e limites de tela.
- Commit: `feat(quick-command): define radial action layout`.

## Task 2: Tela e tecla

- Registrar tecla `G` na categoria atual.
- Criar tela com botões nativos, cancelamento e narração.
- Encaminhar ações imediatas e telas de Attack, Move e Defend.
- Commit: `feat(client): add quick command menu`.

## Task 3: Recursos e verificação

- Adicionar traduções, README, CHANGELOG e ROADMAP.
- Criar smoke test e cobrir recursos.
- Rodar suíte completa e `clean build`.
- Commit: `docs(roadmap): document quick command menu`.
