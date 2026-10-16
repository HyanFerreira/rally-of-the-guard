# Combat Stances — Design

## Objetivo

Adicionar posturas persistentes que modulam o combate dos guardas sem substituir
ordens de movimento, patrulha, rota ou defesa territorial. A postura é aplicada
a todos os guardas carregados do escopo atual do Commander's Ledger.

## Posturas

Os IDs de rede e persistência são estáveis:

- `DEFENSIVE = 0`: comportamento nativo do Guard Villagers, reagindo às ameaças
  percebidas e às ordens já ativas;
- `AGGRESSIVE = 1`: procura monstros hostis próximos quando não possui alvo;
- `PASSIVE = 2`: abandona qualquer alvo e nunca inicia combate;
- `HOLD_POSITION = 3`: pode combater dentro de um raio curto, mas abandona
  perseguições além da âncora capturada ao receber a postura;
- `PROTECT_COMMANDER = 4`: prioriza criaturas que estejam atacando o comandante.

Defensiva é o padrão para guardas sem dados persistidos, preservando o
comportamento atual do mod e do Guard Villagers.

## Estado persistente

Cada guarda pode receber um attachment versionado com a postura e uma âncora.
A âncora é obrigatória somente para `HOLD_POSITION` e é capturada na posição do
guarda no servidor no instante da ordem.

Dados ausentes ou malformados são tratados como `DEFENSIVE`. Uma versão futura é
preservada e impede somente a substituição da postura, evitando perda silenciosa
de dados de versões mais novas.

## Comportamento em servidor

Um ticker de baixa frequência processa somente guardas carregados e com dono:

- `DEFENSIVE` não interfere na IA nativa;
- `PASSIVE` limpa alvo, agressividade e uso de item;
- `AGGRESSIVE` adquire o monstro válido mais próximo dentro do raio configurado;
- `HOLD_POSITION` abandona alvos fora do raio e retorna à âncora sem teleporte;
- `PROTECT_COMMANDER` seleciona primeiro o agressor vivo mais próximo do dono.

A aquisição proativa respeita a política de aliados protegidos. A postura
agressiva limita sua busca a `Monster`; proteger comandante considera `Mob` cujo
alvo atual seja o dono. A ordem explícita de ataque não inclui guardas passivos.

Os raios de busca agressiva, proteção e perseguição de `HOLD_POSITION` são
configuráveis no servidor. A avaliação é determinística por distância e UUID.

## Serviço e rede

`GuardCommandService` recebe `setCombatStance(...)`, exposto como
`SET_COMBAT_STANCE`, passando pelas políticas de elegibilidade e pelos eventos
BEFORE/AFTER existentes. A API sobe para a versão 8.

O cliente envia apenas escopo e ID da postura. O servidor valida o ID, o escopo,
a propriedade e a elegibilidade de cada guarda. O resultado agregado reutiliza
a mensagem de sucesso, rejeição e indisponibilidade dos demais comandos em lote.

## Interface

A tela de combate ganha acesso à seleção de postura. Uma tela compacta mostra as
cinco opções e uma descrição curta; escolher uma opção envia o lote para o
escopo atual e retorna ao Ledger. Traduções são fornecidas em inglês e português.

## Testes

- IDs, codec, schema futuro e fallback defensivo;
- decisões de passivo e limite de perseguição;
- configuração e contrato público da API;
- payload, validação e lote determinístico;
- exclusão de passivos da ordem explícita de ataque;
- recursos e traduções;
- suíte completa, build limpo e roteiro de smoke test.

## Fora de escopo

- sincronizar a postura de cada guarda de volta ao cliente;
- exibir uma postura mista ou individual na lista do Ledger;
- atacar jogadores automaticamente;
- force-load, teleporte ou busca em outras dimensões;
- editor de prioridade de alvos por jogador.
