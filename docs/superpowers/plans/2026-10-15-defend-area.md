# Defend Area — Implementation Plan

**Goal:** entregar uma ordem territorial persistente e autoritativa que reutilize
a patrulha e o combate nativos do Guard Villagers.

**Constraints:** TDD, commits pequenos, sem force-load e sem teleporte. Preservar
IDs existentes e dados de schemas futuros.

## Task 1: Estado, configuração e política territorial

- Criar `order/defend` com estado, codec, attachment, facade e política pura.
- Adicionar raio configurável padrão 16 e margem de retorno de 2 blocos.
- Testar round trip, dados malformados/futuros, contenção de alvo e retorno.
- Commit: `feat(defend): add persistent territorial order state`.

## Task 2: Execução nativa e status

- Criar ticker de dez ticks sobre guardas carregados.
- Manter patrulha/posto, abandonar alvo fora da área e retornar sem teleporte.
- Adicionar status `DEFENDING = 6` e precedência sobre patrol/wait/follow.
- Testar decisões puras e IDs estáveis.
- Commit: `feat(defend): enforce native defense boundaries`.

## Task 3: Serviço público e exclusividade

- Expor `defendArea` e `DEFEND_AREA`; subir API para 7.
- Aplicar elegibilidade e eventos existentes.
- Limpar Defend Area em todas as ordens mutuamente exclusivas e vice-versa.
- Documentar o contrato da API.
- Commit: `feat(api): expose defend area command`.

## Task 4: Rede autoritativa e lote

- Adicionar payload C2S e receiver.
- Reutilizar validação de posição, categorias, escopos, slots e ordenação UUID.
- Persistir centro, posto e raio aceitos pelo servidor.
- Testar payload, validação sem mutação e falhas parciais.
- Commit: `feat(defend): handle territorial defense requests`.

## Task 5: Ledger e targeting compartilhado

- Adicionar botão e tela de categoria para Defend Area.
- Fazer Move e Defend compartilharem uma única sessão de próximo clique.
- Cobrir substituição de modo, consumo único, cancelamento, expiração e mão.
- Adicionar traduções e status nos dois idiomas.
- Commit: `feat(client): add defend area targeting mode`.

## Task 6: Documentação e verificação

- Atualizar README, CHANGELOG e ROADMAP.
- Criar roteiro de smoke test em jogo.
- Rodar testes focados, suíte completa e `clean build`.
- Revisar diff, recursos empacotados e worktree.
- Commit: `docs(roadmap): document defend area order`.
