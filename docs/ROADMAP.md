# Roadmap - Rally of the Guard

Este arquivo registra o estado atual do projeto e as proximas ideias combinadas, para facilitar a retomada do
desenvolvimento sem depender do historico do chat.

## Estado atual

Versao planejada para publicacao: **1.3.0**

Ja implementado:

- Posturas no Livro de Comando:
  - Seguir
  - Aguardar
  - Patrulhar
  - Rota
- Estado visivel no Ledger:
  - Ocioso
  - Seguindo
  - Aguardando
  - Em patrulha
  - Em rota
- Scroll of Rallying respeita guardas em patrulha e aguardando.
- Rotas de patrulha:
  - ate 5 pontos
  - adicionar ponto usando a posicao atual
  - substituir um ponto pela posicao atual
  - iniciar, pausar, limpar e voltar
  - espera configuravel por ponto
  - usa o `patrolPos` nativo do Guard Villagers
  - dados persistem no proprio guarda
  - outras ordens pausam a rota sem apagar os pontos
- Tactical Rally:
  - formacao Escolta no Rali
  - slots relativos ao jogador
  - ticker de formacao sem `patrolling`
  - quebra temporaria de formacao quando o guarda tem alvo
  - ordem de ataque ao alvo mirado
  - filtros de ataque: Todos, Infantaria e Arqueiros
- `README.md` tecnico em portugues.
- `CURSEFORGE.md` promocional em ingles.

## Tactical Rally implementado

### 1. Formacao Escolta no Rally

Formacao inicial desejada:

```text
  P
x   x
x   x
x   x
```

`P` = jogador

`x` = guardas

Regras pensadas:

- Nao usar `patrolling` para formacao.
- Usar slots relativos ao jogador.
- A formacao acompanha a direcao do player.
- Guardas tentam manter seus slots usando navegacao.
- Se o guarda entra em combate, ele sai temporariamente da formacao.
- Quando o combate acaba, ele tenta voltar para o slot.
- Comecar com uma unica formacao: Escolta.
- Nao tentar formacao perfeita em todo terreno.

Implementacao atual:

- Ao ativar o Rally, guardas participantes sao posicionados em slots de escolta.
- A cada alguns ticks, calcular a posicao ideal do slot com base na posicao e direcao horizontal do jogador.
- Se o guarda estiver longe do slot e sem alvo, usar `guard.getNavigation().startMovingTo(...)`.
- Se estiver muito longe, usar teleporte corretivo como fallback.
- Se `guard.getTarget() != null`, nao forcar slot.

### 2. Ordem de ataque ao alvo mirado

Implementacao atual:

- O jogador mira em um mob.
- Abre a tela de Ordens de Combate no Livro de Comando.
- Pode mandar atacarem:
  - Todos
  - Infantaria
  - Arqueiros

Critério provavel:

- Arqueiro = guarda com `Bow` ou `Crossbow`.
- Infantaria = guarda sem `Bow`/`Crossbow`, normalmente com arma corpo-a-corpo.

Comportamento:

- Servidor valida o alvo mirado.
- Guardas selecionados recebem o alvo.
- Usar `setTarget`, `setAttacking` e navegacao se necessario.
- Nao implementar controle manual de escudo por enquanto.

## Ideias deixadas de lado por enquanto

### Controle manual de escudo

Motivo:

- Guard Villagers ja usa escudo naturalmente.
- Forcar escudo manualmente pode brigar com a IA existente.
- O ganho de gameplay parece menor que formacao e ordens de ataque.

## Pendencias de apresentacao

- Tirar screenshots para os placeholders do `CURSEFORGE.md`:
  - `docs/media/rally-of-the-guard-banner.png`
  - `docs/media/commanders-ledger.png`
  - `docs/media/combat-orders.png`
  - `docs/media/patrol-route-editor.png`
  - `docs/media/scroll-of-rallying.png`
  - `docs/media/guard-patrol-post.png`
  - `docs/media/rally-formation.png`
  - `docs/media/patrol-in-action.png`
  - `docs/media/village-defense.png`
- Substituir `ISSUES_LINK_HERE` no `CURSEFORGE.md` pelo link correto de issues.
