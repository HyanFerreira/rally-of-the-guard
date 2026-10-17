# Defesa de Vilas e Raids — Design

## Papel

A integração de raids conecta as unidades já contratadas a uma emergência
territorial vanilla. Ela não substitui a IA da raid nem transforma todo início
de raid em uma ordem obrigatória: o jogador escolhe uma reação, ou configura um
comportamento automático explícito.

## Detecção

- o servidor observa raids vanilla ativas sem criar uma segunda definição de
  raid;
- uma oportunidade é identificada por dimensão, identidade da raid e centro;
- cada jogador recebe no máximo uma notificação por raid, com cooldown contra
  reabertura repetida;
- só jogadores próximos o suficiente e com pelo menos um guarda contratado
  carregado recebem a oportunidade;
- chunks não são forçados e guardas do Registro Global que estejam
  descarregados aparecem apenas como indisponíveis.

## Fluxo de reação

A notificação oferece três escolhas:

```text
Vila sob ataque

[ Reunir guardas ] [ Defender vila ] [ Ignorar ]
```

- `Reunir guardas` reutiliza a convocação existente para guardas carregados do
  jogador e não teleporta unidades descarregadas;
- `Defender vila` reutiliza `Defend Area`, com centro e raio derivados da raid,
  distribuindo postos seguros e determinísticos;
- `Ignorar` encerra a oportunidade apenas para aquele jogador;
- o servidor valida novamente raid, dimensão, distância, propriedade e sessão
  antes de executar qualquer ação.

O escopo inicial é `Todos os guardas carregados`. Pelotões poderão ser escolhidos
em uma evolução posterior sem alterar o protocolo básico da sessão.

## Comportamento automático

Uma configuração por jogador poderá usar:

- `NORMAL`: somente notificação;
- `RALLY_TO_VILLAGE`: aplica `Defender vila`;
- `RALLY_TO_COMMANDER`: aplica a convocação existente.

O padrão é `NORMAL`. Automação nunca inclui guardas de outro dono, outra
dimensão ou chunk descarregado.

## Relatório

Uma sessão de relatório acompanha apenas fatos observados enquanto a raid e as
entidades estão carregadas:

- guardas mobilizados;
- inimigos da raid derrotados com contribuição de guardas do jogador;
- guardas mortos durante a sessão;
- MVP por abates confirmados.

O relatório é encerrado quando a raid termina, é interrompida ou expira. Dados
incompletos por unload são apresentados como tal; o sistema não estima mortes.

## Multiplayer

Cada jogador possui decisão e relatório próprios. A mesma raid pode ser
defendida por vários comandantes, mas propriedade, contagem de contribuição e
ordens permanecem isoladas. Uma escolha de um jogador não fecha a oportunidade
dos demais.

## Etapas

1. identidade de raid, política de elegibilidade, sessão e deduplicação;
2. detecção server-side e payload de oportunidade com validação de resposta;
3. integração com convocação e `Defend Area` usando serviços existentes;
4. GUI compacta de alerta e configuração de comportamento;
5. rastreamento e tela de relatório;
6. documentação, smoke test, balanceamento e build completo.
