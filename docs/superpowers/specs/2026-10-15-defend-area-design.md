# Defend Area — Design

## Objetivo

Adicionar uma ordem territorial persistente para que guardas protejam uma região
sem ficarem presos a um único bloco. A ordem reutiliza o escopo atual do
Commander's Ledger e as categorias `All`, `Infantry` e `Ranged`.

## Fluxo do jogador

1. O jogador abre o Ledger e escolhe `Defend Area`.
2. Escolhe a categoria de unidades.
3. O próximo clique direito em um bloco define o centro da área.
4. O servidor valida o pedido e distribui postos seguros ao redor do centro.
5. Cada guarda passa a patrulhar seu posto com a IA nativa do Guard Villagers.

O raio será configurável no servidor, com padrão de 16 blocos. A primeira
versão não adicionará uma etapa de escolha de raio no cliente.

## Estado persistente

Cada guarda recebe um attachment versionado contendo:

- centro da área;
- posto individual seguro;
- raio aceito pelo servidor;
- indicador de ordem ativa.

Dados ausentes ou malformados são tratados como ordem inativa. Uma versão futura
é preservada e bloqueia somente outra emissão de `Defend Area`; ordens explícitas
mutuamente exclusivas podem removê-la.

## Comportamento nativo

Ao aceitar a ordem, o serviço desativa follow, wait, rota, Rally e Move Here,
limpa o combate atual e ativa a patrulha nativa no posto atribuído.

A cada dez ticks, somente para guardas carregados:

- alvo vivo dentro do raio continua sob controle da IA de combate nativa;
- alvo fora do raio é abandonado;
- guarda fora do raio e sem alvo válido navega de volta ao posto;
- guarda dentro da área continua usando a patrulha e aquisição de alvos do Guard
  Villagers;
- nenhum guarda é teleportado.

A área é uma esfera em coordenadas de mundo. A margem de retorno evita oscilações
na borda: o alvo deve permanecer dentro do raio e o guarda retorna quando passa
do raio acrescido de dois blocos.

## Serviço, rede e segurança

`GuardCommandService` recebe `defendArea(...)`, exposto como `DEFEND_AREA`. O
serviço executa as políticas de elegibilidade e os eventos BEFORE/AFTER atuais.

O cliente envia somente escopo, categoria e face clicada. O servidor valida:

- escopo ainda existente;
- categoria e face conhecidas;
- alcance máximo de interação;
- chunks carregados e world border;
- centro e postos seguros;
- propriedade e elegibilidade de cada guarda.

Ordenação por UUID e falha parcial de slots seguem as regras de `Move Here`.

## Exclusividade e apresentação

Summon, Follow, Wait, Patrol, rota, Rally e Move Here cancelam Defend Area.
Defend Area cancela essas ordens antes de ativar a patrulha territorial.

O Ledger mostra `Defending` com novo ID de status estável. O modo de clique é
único e compartilhado entre Move Here e Defend Area, impedindo dois pacotes para
um mesmo clique.

## Testes

- codec, schema futuro e limpeza explícita;
- contenção, abandono de alvo e retorno com margem;
- estabilidade e precedência do status;
- contrato de API e eventos;
- payload, validação e lote determinístico;
- sessão cliente compartilhada entre Move e Defend;
- recursos, traduções e configuração;
- suíte completa, build limpo e roteiro de smoke test.

## Fora de escopo

- seleção visual ou desenho do raio no mundo;
- teleporte de recuperação;
- busca global de ameaças além da percepção nativa;
- combat stances;
- áreas compartilhadas entre dimensões ou chunks descarregados.
