# GUI Usability Overhaul — Design

## Objetivo

Reduzir a poluição visual e tornar comando, organização e identificação de
guardas compreensíveis conforme o mod cresce, sem criar um item para cada
subsistema e sem alterar a autoridade das regras de servidor.

## Ferramentas do jogador

- Commander's Ledger: comando e organização do exército;
- Muster Roll: inspeção e progressão individual;
- Scroll of Rallying: Rally e formações;
- tecla `G`: ordens emergenciais para todos os guardas carregados.

Nenhum novo item será adicionado nesta revisão.

## Commander's Ledger

A tela deixa de repetir cinco botões em cada linha. A lista de guardas ocupa a
coluna esquerda e mostra nome, patente, estado e Pelotão. Um guarda selecionado
recebe um único painel contextual de ações na coluna direita.

O escopo continua selecionável no topo. A interface diferencia explicitamente
ações individuais das coletivas para evitar ordens em massa acidentais.

As ações são agrupadas em:

- movimento: Convocar, Seguir, Aguardar e Patrulhar;
- tático: Combate, Mover para cá e Defender área;
- planejamento: editar rota do guarda selecionado.

## Pelotões e Oficiais

A gestão usa duas colunas inspiradas na tela vanilla de Resource Packs:

```text
Disponíveis e outros Pelotões  →  Membros do Pelotão selecionado
```

Cada entrada mostra:

- nome do guarda;
- patente e indicadores Captain/Officer;
- Pelotão atual, inclusive `Sem Pelotão`.

Mover um guarda de outro Pelotão exige confirmação. Remover devolve o guarda ao
estado sem Pelotão. A definição de Officer fica disponível apenas para Captains
que já estejam na coluna de membros.

## Comando rápido

O painel da tecla `G` continua não pausante, mas recebe fundo escurecido. Um
único painel central opaco cobre a mira, mostra claramente o escopo
`Todos os guardas` e reúne as seis ações em uma grade compacta `2 × 3`.

## Identidade e Villager Names

Villager Names passa a ser dependência necessária. A interface continua usando
o nome autoritativo da entidade e combina esse nome com patente, estado e
Pelotão, para que a identificação não dependa exclusivamente da unicidade do
nome.

## Segurança e compatibilidade

- nenhuma GUI decide propriedade ou elegibilidade;
- payloads e serviços autoritativos existentes são reutilizados;
- a associação atual de cada guarda já é fornecida pelo snapshot do Ledger;
- telas continuam não pausantes e acessíveis por teclado/narração;
- a revisão deve preservar escalas pequenas de GUI e traduções EN/PT-BR.

## Entrega

1. especificação e dependência de identidade;
2. modelos puros de seleção e transferência;
3. Ledger contextual;
4. Pelotões em duas colunas;
5. overlay do Comando Rápido;
6. recursos, testes automatizados e smoke test em jogo.
