# Named Routes — Design

## Objetivo

Transformar uma rota de patrulha individual em um modelo nomeado e reutilizável
do jogador. Uma rota como `Muralhas da Vila` poderá ser criada uma vez, editada
e atribuída a vários guardas contratados, sem remover o editor individual já
existente.

## Modelo de dados

Cada jogador terá um registro persistente e versionado de rotas nomeadas. Cada
definição contém:

- UUID estável;
- nome Unicode validado;
- dimensão em que a rota existe;
- tempo de espera por ponto;
- lista ordenada de pontos, cada um com nome opcional.

O registro usa attachment persistente com cópia após a morte, seguindo o mesmo
padrão do registro de Pelotões. Dados inválidos conhecidos são reparados e
canonicalizados; uma versão de schema futura fica somente leitura.

O estado individual do guarda continua armazenando seu progresso, atividade e
uma cópia dos pontos necessários para executar a patrulha. Quando a origem for
uma rota nomeada, o estado também guarda o UUID dessa definição. Assim, a rota
continua segura caso o jogador esteja offline, e a associação pode ser exibida
e atualizada quando o comandante estiver presente.

## Autoridade e validação

Toda criação, renomeação, edição, exclusão e atribuição acontece no servidor.
O servidor valida:

- propriedade do registro e dos guardas;
- nome normalizado, duplicado e limites configurados;
- dimensão atual da definição e do guarda;
- quantidade e coordenadas dos pontos;
- existência da rota antes de atribuir ou iniciar;
- limites de rotas por jogador e guardas carregados por operação.

Excluir uma rota nomeada pausa e desvincula os guardas carregados que a usam,
mas mantém uma cópia individual dos pontos. Guardas descarregados são
desvinculados de forma preguiçosa na próxima sincronização com o proprietário.

## Fluxo de interface

O botão `Rota` do Ledger abre um hub que primeiro informa se o guarda não possui
rota, usa uma rota individual ou está atribuído a uma rota salva. Em seguida,
oferece duas opções claras:

1. `Rota deste guarda`, que preserva o editor atual;
2. `Rotas salvas`, que abre o gerenciador de modelos reutilizáveis.

O gerenciador terá uma lista de rotas à esquerda e, à direita, nome, dimensão,
quantidade de pontos, espera e guardas carregados atribuídos. A atribuição usará
duas colunas, no mesmo padrão visual dos Pelotões: disponíveis à esquerda e
atribuídos à direita. Aplicar uma rota inicia a patrulha; remover a associação
pausa a rota no guarda sem apagar o modelo.

Criar uma rota salva a partir da rota individual do guarda selecionado também
atribui o próprio guarda ao novo modelo e inicia a patrulha. A operação é
atômica: se a atribuição falhar, o modelo recém-criado é removido.

Uma tela dedicada permite ver, nomear e reordenar os pontos, substituir uma
coordenada pela posição atual, adicionar/remover pontos e alterar a espera. Os
nomes são opcionais; pontos sem nome recebem apenas um rótulo localizado na GUI.
Copiar novamente os pontos da rota individual exige confirmação e só altera o
modelo depois que o jogador salva.

## Compatibilidade

- rotas individuais existentes continuam válidas e sem associação nomeada;
- o formato individual será migrado de `v1` para `v2` sem perder pontos;
- o registro de rotas salvas migra de `v1` para `v2`, preservando coordenadas e
  inicializando os nomes opcionais como vazios;
- Named Routes não força chunks nem modifica guardas de outro jogador;
- somente guardas carregados na dimensão do comandante aparecem para atribuição;
- nomes e feedback terão traduções em inglês e português brasileiro.

## Etapas de implementação

1. registro persistente, codec, políticas e testes de domínio;
2. schema `v2` do estado individual com origem nomeada opcional;
3. payloads, snapshots e mutações server-authoritative;
4. hub e gerenciador de Rotas Salvas;
5. integração com o Ledger, documentação, smoke test e build completo.
