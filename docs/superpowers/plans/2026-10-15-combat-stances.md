# Combat Stances — Implementation Plan

**Goal:** entregar cinco posturas persistentes e autoritativas que modulam a IA
nativa sem substituir as ordens táticas existentes.

**Constraints:** TDD, commits pequenos, sem force-load e sem teleporte. Preservar
IDs existentes e dados de schemas futuros.

## Task 1: Estado persistente e políticas puras

- Criar `combat/stance` com enum, estado, codec, attachment e facade.
- Persistir âncora somente quando necessária e usar Defensive como fallback.
- Testar IDs, round trip, dados malformados/futuros e decisões de perseguição.
- Commit: `feat(stance): add persistent combat stance state`.

## Task 2: Execução e configuração

- Adicionar raios configuráveis para agressividade, proteção e hold position.
- Criar ticker sobre guardas carregados e implementar as cinco posturas.
- Reutilizar segurança de aliados e desempate determinístico.
- Testar políticas de aquisição e retorno.
- Commit: `feat(stance): enforce combat stance behavior`.

## Task 3: Serviço público e ataque explícito

- Expor `setCombatStance` e `SET_COMBAT_STANCE`; subir API para 8.
- Aplicar elegibilidade e eventos existentes, capturando a âncora no servidor.
- Excluir guardas passivos da ordem explícita de ataque.
- Commit: `feat(api): expose combat stance command`.

## Task 4: Rede autoritativa e lote

- Adicionar payload C2S e receiver.
- Validar ID, escopo, propriedade e elegibilidade no servidor.
- Executar em ordem determinística e reportar resultado agregado.
- Commit: `feat(stance): handle scoped stance requests`.

## Task 5: Interface e recursos

- Adicionar acesso às posturas na tela de combate.
- Criar tela compacta com cinco escolhas e descrições.
- Adicionar traduções em inglês e português e testes de recursos.
- Commit: `feat(client): add combat stance selection`.

## Task 6: Documentação e verificação

- Atualizar README, CHANGELOG e ROADMAP.
- Criar roteiro de smoke test em jogo.
- Rodar testes focados, suíte completa e `clean build`.
- Revisar diff, recursos empacotados e worktree.
- Commit: `docs(roadmap): document combat stances`.
