# Rally of the Guard

**Rally of the Guard** é uma expansão tática leve para o
[Guard Villagers (Fabric/Quilt)](https://www.curseforge.com/minecraft/mc-mods/guard-villagers-fabric).

O objetivo do mod é transformar guardas contratados em um esquadrão comandável, sem descaracterizar a IA original do
Guard Villagers. O jogador pode contratar guardas, convocar unidades, definir posturas, criar patrulhas fixas e montar
rotas de patrulha em loop usando o Livro de Comando.

---

## Versão Atual

### 1.3.0

Principais mudanças:

- Adicionadas posturas no Livro de Comando: seguir, aguardar, patrulhar e rota.
- Adicionada tela de rota de patrulha.
- Rotas suportam limite configurável de pontos.
- Adicionada configuração via MidnightLib, acessível pelo Mod Menu/Configured quando disponível.
- Cada ponto usa a posição atual do jogador como referência.
- Guardas em rota aguardam um tempo configurável antes de seguir para o próximo ponto.
- Rotas usam o comportamento nativo de patrulha do Guard Villagers.
- Dados da rota ficam salvos no próprio guarda e sobrevivem ao reload do mundo.
- O Pergaminho do Rali respeita guardas em patrulha ou aguardando.
- O Rali usa formação Escolta para posicionar guardas em duas colunas atrás/laterais do jogador.
- Adicionada tela de Ordens de Combate para atacar o alvo mirado com todos, infantaria ou arqueiros.

---

## Funcionalidades

### Contratação de Guardas

- Clique com o botão direito em um guarda sem dono para abrir a tela de contratação.
- O custo padrão é de **3 esmeraldas**, mas custo e item podem ser alterados na configuração.
- Guardas contratados ficam vinculados ao UUID do jogador.
- Guardas contratados recebem nome dourado e mensagem de apresentação.
- Guardas contratados podem seguir o jogador sem depender do efeito Hero of the Village.

### Livro de Comando

O **Livro de Comando** abre um painel portátil para gerenciar guardas contratados.

Pelo painel, o jogador pode:

- Ver guardas contratados carregados no mundo atual.
- Ver o estado atual de cada guarda.
- Convocar um guarda para perto do jogador.
- Mandar um guarda seguir.
- Mandar um guarda aguardar.
- Definir uma patrulha fixa na posição atual.
- Parar uma patrulha.
- Criar, iniciar, pausar, limpar e editar rotas de patrulha.

Estados exibidos:

- `Ocioso`
- `Seguindo`
- `Aguardando`
- `Em patrulha`
- `Em rota`

### Rotas de Patrulha

Rotas de patrulha permitem que um guarda percorra vários pontos em loop.

Regras atuais:

- Máximo configurável de pontos por rota.
- Mínimo de 2 pontos para iniciar.
- Cada ponto pode ser definido usando a posição atual do jogador.
- O tempo de espera por ponto pode ser ajustado na tela da rota.
- Ao chegar em um ponto, o guarda aguarda o tempo configurado e depois recebe o próximo ponto como `patrolPos`.
- A rota continua em loop até ser pausada ou limpa.
- Outras ordens, como seguir ou aguardar, pausam a rota sem apagar os pontos.
- Somente o botão `Limpar` apaga os pontos da rota.

### Pergaminho do Rali

O **Pergaminho do Rali** convoca guardas contratados próximos para perto do jogador.

- `Shift + botão direito` ativa ou encerra o rali.
- Guardas convocados são posicionados em formação Escolta e passam a seguir.
- Guardas em patrulha não são puxados.
- Guardas aguardando não são puxados.
- Enquanto o rali está ativo, guardas tentam manter seus slots de formação quando não estão em combate.
- Durante o rali, guardas contratados ficam protegidos contra dano causado pelo próprio comandante.

### Ordens de Combate

O painel de combate permite mandar guardas em rali atacarem o alvo mirado pelo jogador.

Atalho padrão:

- `R`: abre a tela de Ordens de Combate.
- A tecla pode ser alterada na tela de controles do Minecraft.

Modos disponíveis:

- `Todos`
- `Infantaria`
- `Arqueiros`

Regras atuais:

- A ordem considera guardas contratados que estão seguindo o jogador.
- Guardas em patrulha, aguardando ou em rota não recebem a ordem.
- Arqueiros são identificados por `Bow` ou `Crossbow` na mão principal ou secundária.
- Infantaria corresponde aos guardas sem arco ou besta equipados.

### Patrulha Fixa

A patrulha fixa usa o comportamento nativo do Guard Villagers:

- O jogador define a posição atual como ponto de patrulha.
- O guarda caminha até o ponto e guarda aquela posição.
- Se encontrar mobs hostis, a IA normal do guarda continua funcionando.

### Friendly Fire e Neutralidade

- Guardas em rali ficam protegidos contra ataques do jogador dono.
- Ao atacar um guarda que não pertence ao jogador, os guardas contratados do jogador permanecem neutros.

---

## Itens

### Pergaminho do Rali

Receita:

```text
Spruce Slab | Spruce Planks | Spruce Slab
Black Wool  | Iron Sword    | Black Wool
Empty       | Spruce Slab   | Empty
```

### Livro de Comando

Receita:

```text
Book    | Scroll of Rallying | Empty
Emerald | Empty              | Empty
Empty   | Empty              | Empty
```

---

## Requisitos

- **Minecraft:** 1.21.1
- **Loader:** Fabric
- **Java:** 21 ou superior
- **Fabric API**
- **Guard Villagers (Fabric/Quilt)**

---

## Observações Técnicas

- Comandos funcionam apenas no mundo/dimensão onde o guarda está carregado.
- Guardas em chunks descarregados não processam rota até serem carregados novamente.
- Rotas são salvas no guarda usando tags persistentes.
- O sistema de rota atual não substitui a IA do Guard Villagers; ele apenas atualiza dinamicamente o `patrolPos`.
- A formação do rali não usa `patrolling`; ela usa slots relativos ao jogador e navegação.
- Ordens de combate atuam sobre guardas em rali/seguindo no mundo atual.
- A proteção contra friendly fire se aplica ao contexto de rali.

---

## Configuração

O mod registra suas opções usando MidnightLib. Com **Mod Menu** e uma tela de configuração compatível instalada, o botão
de configuração aparece na lista de mods e abre uma interface para editar as opções.

As opções também ficam salvas no arquivo de configuração gerado pela MidnightLib para o mod.

Principais opções:

- `economy.hireCost`: custo para contratar um guarda.
- `economy.hireItem`: item usado na contratação, em formato de ID, por exemplo `minecraft:emerald`.
- `rally.radius`: raio usado para buscar guardas ao iniciar o rali.
- `rally.maxGuards`: limite de guardas chamados pelo rali.
- `rally.teleportEnabled`: permite ou bloqueia teleportes do rali/formação.
- `rally.teleportMinDistance`: distância mínima para teleportar um guarda ao iniciar o rali.
- `rally.formationEnabled`: liga ou desliga a formação Escolta.
- `rally.protectRalliedGuardsFromOwner`: impede o comandante de acertar seus guardas durante o rali.
- `formation.columnSpacing`: distância entre as duas colunas da formação.
- `formation.rowSpacing`: distância entre fileiras.
- `formation.returnSpeed`: velocidade usada para voltar ao slot.
- `formation.teleportDistance`: distância em que a formação teleporta um guarda muito longe.
- `combat.targetRange`: alcance máximo da mira para ordens de ataque.
- `combat.guardSearchRadius`: raio dos guardas que recebem ordens de combate.
- `combat.allowPassiveTargets`: permite ordenar ataque contra mobs passivos.
- `combat.allowPlayerTargets`: permite ordenar ataque contra jogadores.
- `route.maxPoints`: máximo de pontos por rota, limitado a 10 pela interface atual.
- `route.defaultWaitSeconds`: espera padrão em cada ponto.
- `route.moveSpeed`: velocidade ao mover entre pontos.
- `route.teleportIfStuck`: permite teleportar guardas de rota que ficaram presos/longe.
- `route.teleportDistance`: distância mínima para considerar teleporte de rota.

---

## Licença

Este mod está disponível sob a licença **CC0-1.0**.

---

## Links

- GitHub: https://github.com/HyanFerreira/rally-of-the-guard
- Perfil do autor: https://github.com/HyanFerreira
