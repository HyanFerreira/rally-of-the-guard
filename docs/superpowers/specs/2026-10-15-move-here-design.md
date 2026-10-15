# Move Here — Design

## Objetivo

Adicionar uma ordem tática pontual que permita mandar guardas carregados até uma
posição indicada no mundo. A ordem deve funcionar para o escopo atual do
Commander's Ledger (`All Guards`, `Unassigned` ou um Squad) e para as categorias
`All`, `Infantry` e `Ranged`.

Ao alcançar seu destino, cada guarda deve encerrar a marcha e permanecer em
`Waiting`. `Move Here` não substitui patrulha, rotas, Rally ou o futuro
`Defend Area`.

## Fluxo do jogador

1. O jogador escolhe o escopo no Commander's Ledger.
2. Pressiona `Move Here` entre as ações coletivas.
3. Uma tela compacta permite escolher `All`, `Infantry` ou `Ranged`.
4. A tela fecha e o cliente entra em modo de posicionamento.
5. O próximo clique direito em uma face de bloco envia a posição ao servidor.
6. O servidor valida a solicitação, distribui os guardas em slots próximos e
   responde com o resultado agregado.

O modo de posicionamento é cancelado sem emitir ordem quando o jogador pressiona
`Esc`, troca o Ledger da mão usada para iniciar a ação, morre, desconecta ou
deixa o prazo expirar. A primeira versão usa um prazo de 15 segundos.

## Escopo e categoria

O escopo e a categoria são filtros combináveis:

- escopo: todos, sem Squad ou um Squad específico;
- categoria: todos, infantaria ou arqueiros.

Isso permite, por exemplo, mover somente os arqueiros de um Squad. Apenas
guardas vivos, carregados, na mesma dimensão e pertencentes ao comandante são
candidatos.

A classificação de infantaria e arqueiros deve ser extraída da regra atualmente
usada pelas ordens de combate. O projeto terá uma única política compartilhada;
`Move Here` não criará uma segunda lista de armas.

## Estado persistente

Cada guarda com ordem ativa recebe um `MoveOrderState` em um attachment
persistente e versionado. O estado contém:

- versão do schema;
- posição de destino atribuída ao guarda;
- indicador de ordem ativa.

Dados ausentes ou malformados são tratados como ordem inativa. Dados de versões
futuras são preservados e bloqueiam somente a emissão de outro `Move Here`; uma
ordem mutuamente exclusiva continua podendo removê-los como cancelamento
explícito do jogador. Como o destino pertence à entidade, a dimensão é a
dimensão atual do guarda e não precisa ser duplicada no estado.

O attachment permite que a ordem sobreviva a save, reload e descarregamento de
chunk. Quando o guarda voltar a ser carregado, a marcha continua.

## Serviço de comando e API

`GuardCommandService` recebe uma operação `moveTo(ServerPlayer, GuardEntity,
BlockPos)`, e `GuardCommandType` recebe `MOVE_TO`. O contrato público passa para
API version 6 e a documentação registra a nova operação.

O serviço continua sendo a única camada que altera ordens do guarda. Ele aplica
as políticas BEFORE/AFTER e a elegibilidade já existentes, incluindo a posição
solicitada no contexto.

Ao aceitar `Move Here`, o serviço:

- desativa follow, wait, patrol, rota e Rally;
- limpa alvo e agressividade atuais apenas no momento da emissão;
- interrompe a navegação anterior;
- persiste o novo destino.

Toda outra ordem mutuamente exclusiva (`Summon`, `Follow`, `Wait`, `Patrol`,
rota ou entrada em Rally) limpa um `Move Here` ativo. Emitir outro `Move Here`
substitui atomicamente o destino anterior.

## Navegação e conclusão

Um ticker comum dedicado processa guardas carregados com `MoveOrderState` ativo.
Ele executa em intervalo limitado, reutiliza a velocidade configurada para rotas
e deixa o pathfinding do Guard Villagers conduzir a entidade.

Regras:

- durante combate, a ordem permanece persistida, mas a navegação tática pausa;
- quando o combate termina, a marcha recomeça;
- dentro de 2,5 blocos do destino, a navegação para, o estado é removido e o
  guarda entra em `Waiting`;
- não há teleporte corretivo na primeira versão;
- um caminho temporariamente bloqueado continua sendo tentado;
- outra ordem explícita é sempre uma forma válida de cancelamento.

Essa escolha preserva terreno, muralhas e a IA nativa. Recuperação configurável
para guardas presos poderá ser avaliada após testes em jogo.

## Distribuição dos slots

O ponto clicado é transformado no espaço adjacente à face atingida. Esse ponto é
a âncora do grupo.

Os candidatos são ordenados por UUID e recebem offsets determinísticos em anéis
compactos ao redor da âncora. Cada offset passa por uma busca vertical curta por
uma posição com piso sólido e dois blocos livres. O resolvedor nunca carrega
chunks e nunca escolhe uma posição fora da borda do mundo.

Se um slot não possuir posição segura, apenas aquele guarda é rejeitado. Os
demais recebem suas ordens normalmente. A distribuição não representa uma
formação persistente: depois da chegada, cada guarda apenas aguarda em seu slot.

## Cliente

A tela de categoria reutiliza os padrões visuais das telas compactas existentes,
não pausa o mundo e oferece foco por teclado, narração e traduções em inglês e
português brasileiro.

Um controlador cliente mantém temporariamente:

- escopo selecionado;
- categoria selecionada;
- mão que continha o Ledger;
- instante de expiração.

O controlador intercepta apenas o próximo uso em bloco enquanto estiver armado.
Depois de enviar, cancelar ou expirar, todo o estado temporário é removido. Uma
mensagem na action bar informa que o jogador deve escolher um destino e outra
confirma cancelamento ou expiração.

## Rede e validação do servidor

O payload C2S contém:

- escopo;
- categoria;
- posição do bloco clicado;
- face atingida.

O servidor valida antes de consultar ou alterar guardas:

- categoria e face conhecidas;
- escopo válido e pertencente ao jogador;
- posição na dimensão atual;
- distância máxima de 8 blocos do jogador;
- chunk já carregado;
- posição dentro da borda do mundo;
- âncora com ao menos uma posição segura.

Coordenadas, escopo e categoria enviados pelo cliente nunca são considerados
autoritativos. Payloads inválidos são rejeitados sem mudanças parciais.

Depois da validação global, cada candidato passa pelas regras de propriedade,
elegibilidade e políticas do serviço. O servidor retorna na action bar um resumo
com quantidades de sucesso, rejeição e indisponibilidade.

## Status e apresentação

Enquanto a ordem estiver ativa, o Ledger exibe o novo status `Moving`. Na
conclusão, o snapshot passa a exibir `Waiting`. O status da ordem de movimento
tem precedência sobre estados ociosos, mas combate não precisa criar um novo
status persistente.

As traduções incluem o botão, a tela de categoria, instruções de posicionamento,
cancelamento, expiração, resultado agregado e o status `Moving`.

## Testes

O desenvolvimento seguirá TDD e cobrirá:

- codec, defaults seguros e rejeição de schema futuro;
- persistência e limpeza do attachment;
- contrato do payload e IDs desconhecidos;
- combinação de escopo e categoria;
- política compartilhada de infantaria/arqueiros;
- offsets determinísticos e busca de slots seguros;
- validação global do destino;
- transições entre `Move Here` e outras ordens;
- pausa em combate, retomada e conclusão em `Waiting`;
- cancelamento, expiração e troca de item no estado cliente;
- status do Ledger e traduções;
- suíte completa e build limpo.

Um roteiro de smoke test em jogo verificará clique no mundo, múltiplos escopos e
categorias, distribuição, combate durante a marcha, reload de mundo, cancelamento
e conclusão em `Waiting`.

## Fora de escopo

- `Defend Area`;
- teleporte corretivo ou recuperação avançada de pathfinding;
- destinos em chunks descarregados ou outras dimensões;
- formações persistentes durante `Move Here`;
- Guard Registry global;
- radial menu ou atalhos rápidos para emitir a ordem.
