# Command Post — Design

## Papel

O `Command Post` é o complemento territorial do Commander's Ledger. O Ledger
continua sendo a ferramenta móvel; o posto persiste uma ordem defensiva em um
ponto do mundo e a reaplica aos guardas atribuídos enquanto estiver ativo.

## Bloco e propriedade

- novo bloco e item `rallyguard:command_post`;
- o jogador que posiciona o bloco torna-se seu proprietário;
- somente o proprietário pode editar, ativar, desativar ou remover atribuições;
- quebrar o bloco não preserva configuração sensível no item derrubado;
- cada posto persiste schema, proprietário, nome, atividade, raio, formação e
  UUIDs dos guardas atribuídos em sua block entity;
- dados de schema futuro ficam inativos e somente leitura.

## Configuração inicial

O primeiro comportamento é `Defender`, reaproveitando a ordem territorial já
existente. O proprietário configura:

- nome do posto;
- guardas carregados atribuídos explicitamente;
- formação territorial (`Escort`, `Column`, `Line`, `Wedge`, `Square`,
  `Circle` ou `Shield Wall`);
- raio de defesa;
- estado ativo ou inativo.

Pelotões aparecem como informação na seleção e poderão ganhar atribuição em
lote depois; a fonte de verdade desta fase é a lista explícita de UUIDs. Isso
evita que uma mudança administrativa de Pelotão altere silenciosamente a defesa
de uma base.

## Ativação territorial

Ao ativar, o servidor resolve posições seguras ao redor do bloco usando o layout
da formação selecionada e aplica `Defend Area` a cada guarda carregado,
pertencente ao dono, na mesma dimensão e atribuído ao posto. O centro defensivo
é o Command Post, e cada guarda recebe um posto próprio dentro da formação.

Um ticker leve verifica periodicamente os membros carregados:

- reaplica a ordem quando um membro atribuído reaparece ou perde a ordem;
- nunca força chunks;
- nunca teleporta apenas para ingressar no posto;
- ignora guardas de outro dono e remove UUIDs inválidos somente por ação
  explícita do proprietário;
- desativar o posto pausa somente ordens `Defend Area` originadas por ele.

Para distinguir a origem, o estado de `Defend Area` ganha um UUID opcional do
Command Post. Ordens manuais existentes permanecem sem origem e não são
canceladas por outro posto.

## Interface

Clique direito abre uma tela não pausante com resumo no topo e duas colunas:

```text
Guardas disponíveis  →  Defensores atribuídos
```

Cada linha mostra nome, patente e Pelotão. A tela oferece nome, formação, raio,
ativação, paginação independente e confirmação antes de transferir um guarda
que já esteja vinculado a outro posto carregado.

## Limites e configuração

- máximo de defensores por posto;
- raio mínimo e máximo de defesa;
- intervalo do ticker;
- distância máxima para abrir e mutar a tela;
- nomes Unicode normalizados e limitados.

## Etapas

1. bloco, item, recursos e persistência versionada da block entity;
2. origem opcional no estado de Defend Area e política territorial;
3. payloads, sessão, validação de propriedade e ticker;
4. tela responsiva de configuração e atribuição;
5. documentação, smoke test, receitas e build completo.
