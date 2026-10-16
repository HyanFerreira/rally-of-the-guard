# Quick Command — Design

## Objetivo

Adicionar uma interface rápida, não pausante e acessível por tecla para as ordens
mais usadas em combate, sem substituir o Commander's Ledger nem duplicar regras
autoritativas do servidor.

## Primeira versão

Uma nova tecla configurável abre um painel compacto em disposição radial usando
controles nativos do Minecraft. A primeira versão atua em `Todos os Guardas` e
oferece:

- Seguir;
- Aguardar;
- Patrulhar na posição atual do comandante;
- Atacar, abrindo a seleção de categoria/alvo existente;
- Mover para cá;
- Defender área.

O Ledger continua responsável por Pelotões, guardas individuais, rotas e gestão.
Formação e Rally continuam no Pergaminho do Rali, pois dependem da mão, do item e
de estado próprio; o menu rápido não simula o uso desse item.

## Interação

- tecla padrão `G`, remapeável nos controles;
- mundo não pausa;
- clique em uma ação confirma; `Esc` cancela;
- ações imediatas fecham o painel e exibem o feedback normal do servidor;
- Ataque abre a tela de combate; Mover e Defender abrem a seleção de categoria e
  reutilizam a sessão única de próximo clique já existente.

O layout usa seis botões nativos ao redor de um centro vazio, garantindo foco,
narração e compatibilidade com mouse sem implementar hitboxes polares frágeis.

## Segurança

Follow, Wait e Patrol reutilizam `ScopedGuardActionC2SPayload` com escopo `ALL`.
Ataque, Move Here e Defend Area reutilizam as telas e payloads existentes. Toda
propriedade, elegibilidade, alcance, destino e estado são revalidados no servidor.

## Testes

- disposição determinística dos seis slots em diferentes tamanhos de tela;
- tecla, traduções e recursos nos dois idiomas;
- mapeamento de cada ação ao fluxo existente;
- suíte completa, build limpo e smoke test em jogo.

## Fora de escopo

- seleção de Pelotão no menu rápido;
- gesto de segurar/soltar a tecla ou seleção puramente angular;
- acionar ou trocar a formação do Pergaminho do Rali;
- novas regras ou novos payloads de ordem no servidor.
