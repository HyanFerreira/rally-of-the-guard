# Registro Global de Guardas — Design

## Papel

O Registro Global transforma o Livro de Comando em uma fonte persistente sobre
todo o exército contratado, inclusive quando uma entidade está descarregada. Ele
não substitui o guarda como autoridade de combate: conserva uma projeção de
consulta e só permite ações que sejam seguras para entidades carregadas.

## Fonte de verdade e propriedade

- cada jogador possui um registro persistente anexado ao próprio jogador;
- cada entrada é identificada pelo UUID estável do guarda;
- um guarda carregado e pertencente ao jogador atualiza sua entrada;
- o registro nunca cria, teleporta ou carrega uma entidade para satisfazer a UI;
- troca ou remoção de proprietário remove a entrada do antigo proprietário na
  próxima reconciliação verificável;
- entradas descarregadas são preservadas e exibidas como última informação
  conhecida, não como estado em tempo real.

## Snapshot persistido

Cada entrada armazena apenas dados necessários para localizar e identificar a
unidade:

- UUID e nome exibido;
- dimensão e última posição conhecida;
- instante lógico da última observação;
- estado carregado apenas no snapshot enviado ao cliente;
- nível, patente, Capitão, Pelotão e ordem são resolvidos novamente quando o
  guarda está carregado;
- para guardas descarregados, esses campos usam a última projeção persistida e
  são explicitamente marcados como desatualizados.

O formato possui versão de schema, limites de entradas e strings, reparo de
dados malformados e bloqueio somente leitura para schemas futuros.

## Atualização

Um reconciliador leve atualiza guardas carregados em eventos de ciclo de vida e
em intervalo configurável. Descarregar uma entidade grava a última posição antes
de ela desaparecer. O sistema não varre chunks nem mundos completos.

Entradas não são apagadas apenas por ficarem antigas: morte confirmada, dispensa
confirmada ou comando explícito de limpeza são eventos distintos. Isso evita
perder unidades que permaneceram meses em chunks descarregados.

## Interface do Livro de Comando

O filtro principal ganha duas visões:

```text
Carregados | Registro global
```

Uma linha global mostra nome, patente, Pelotão, dimensão, distância aproximada,
última posição e disponibilidade. Ações que precisam da entidade ficam
desativadas com explicação quando ela está descarregada. A seleção não envia
ordens a um UUID sem entidade validada pelo servidor.

## Rede e segurança

- o servidor monta snapshots paginados e limitados;
- o cliente nunca informa dimensão, posição, proprietário ou disponibilidade;
- pedidos de atualização validam jogador, schema e limites;
- ações existentes continuam usando entity ID e as validações atuais quando a
  unidade está carregada;
- nenhuma consulta força chunk, troca dimensão ou revela guardas de outro dono.

## Etapas

1. modelo, codec, attachment por jogador e proteção de schema futuro;
2. reconciliador de guardas carregados, unload, morte e troca de propriedade;
3. snapshot global paginado e payloads somente de consulta;
4. aba global responsiva no Livro de Comando, com estados indisponíveis claros;
5. comandos administrativos de diagnóstico, documentação, smoke test e build.
