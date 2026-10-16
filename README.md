# Rally of the Guard

**Rally of the Guard** é uma expansão tática leve para o
[Guard Villagers (Fabric/Quilt)](https://www.curseforge.com/minecraft/mc-mods/guard-villagers-fabric).

O objetivo do mod é transformar guardas contratados em um esquadrão comandável, sem descaracterizar a IA original do
Guard Villagers. O jogador pode contratar guardas, convocar unidades, definir posturas, criar patrulhas fixas e montar
rotas de patrulha em loop usando o Livro de Comando.

---

## Versão Atual

### 1.4.0

Principais mudanças:

- Pelotões persistentes com nomes personalizados, filtros e ordens coletivas no Livro de Comando.
- Um Capitão existente pode atuar como Oficial de cada Pelotão.
- Criação, renomeação, exclusão e atribuição de guardas pela nova tela de gerenciamento.
- Perfis, progressão, Capitães naturais, custo dinâmico e Registro de Tropa.
- Promoção de guardas veteranos a Capitão e dispensa segura pelo Registro de Tropa.
- Formações Escolta, Coluna, Linha, Cunha, Quadrado, Círculo e Muralha de Escudos selecionáveis no Pergaminho do Rali.

- Adicionadas posturas no Livro de Comando: seguir, aguardar, patrulhar e rota.
- Adicionada tela de rota de patrulha.
- Rotas suportam limite configurável de pontos.
- Adicionada configuração via MidnightLib, acessível pelo Mod Menu/Configured quando disponível.
- Cada ponto usa a posição atual do jogador como referência.
- Guardas em rota aguardam um tempo configurável antes de seguir para o próximo ponto.
- Rotas usam o comportamento nativo de patrulha do Guard Villagers.
- Dados da rota ficam salvos no próprio guarda e sobrevivem ao reload do mundo.
- O Pergaminho do Rali respeita guardas em patrulha ou aguardando.
- O Rali permite escolher entre sete formações persistentes no próprio pergaminho.
- Adicionada tela de Ordens de Combate para atacar o alvo mirado com todos, infantaria ou arqueiros.

---

## Funcionalidades

### Contratação de Guardas

- Clique com o botão direito em um guarda sem dono para abrir a tela de contratação.
- O custo padrão é de **3 esmeraldas**, mas custo e item podem ser alterados na configuração.
- Quando o custo dinâmico está ativo, nível, patente, qualidade natural e o cargo de Capitão influenciam a oferta.
- Guardas contratados ficam vinculados ao UUID do jogador.
- Guardas contratados recebem nome dourado e mensagem de apresentação.
- Guardas contratados podem seguir o jogador sem depender do efeito Hero of the Village.

### Perfis, progressão e Capitães naturais

- Cada guarda possui um perfil persistente com nível, patente, qualidade, variações naturais, abates e assistências.
- Guardas naturais podem surgir em níveis avançados e, quando habilitado, receber o cargo separado de **Capitão**.
- A chance padrão de Capitão é de 5%, com nível mínimo 5.
- O controle regional usa os guardas carregados em um raio configurável: sem Capitão próximo a chance é normal, com um ela é reduzida pela metade e, no limite regional, cai para zero.
- Perfis antigos são migrados sem transformar guardas existentes em Capitães nem sortear novamente seus atributos.
- O **Registro de Tropa (Muster Roll)** exibe o cargo de Capitão e aplica o bônus configurável ao preço de contratação.

### Promoção e dispensa de Capitães

- Um guarda contratado pode ser promovido pelo Registro de Tropa ao alcançar **Level 7 / Veteran** por padrão.
- A promoção custa **4 unidades** de `economy.hireItem` por padrão; item, custo, nível mínimo e habilitação são configuráveis.
- O servidor repete propriedade, disponibilidade, perfil, nível e pagamento no momento da ação.
- Se a persistência falhar, o pagamento completo é devolvido.
- Dispensar um Capitão não devolve pagamento e preserva XP, patente, atributos, estatísticas e Pelotão.
- Um Capitão que seja Officer precisa deixar essa função antes de ser dispensado.
- Desativar novas promoções não impede a dispensa de Capitães existentes.

### Livro de Comando

O **Livro de Comando** abre um painel portátil para gerenciar guardas contratados.

Pelo painel, o jogador pode:

- Ver guardas contratados carregados no mundo atual.
- Selecionar um guarda na lista à esquerda e usar um único painel contextual de ordens individuais à direita.
- Ver o estado atual de cada guarda.
- Convocar um guarda para perto do jogador.
- Mandar um guarda seguir.
- Mandar um guarda aguardar.
- Definir uma patrulha fixa na posição atual.
- Parar uma patrulha.
- Criar, iniciar, pausar, limpar e editar rotas de patrulha.
- Filtrar a lista por todos os guardas, guardas sem Pelotão ou um Pelotão específico.
- Enviar ordens coletivas de convocar, seguir, aguardar e patrulhar ao escopo selecionado.
- Distinguir visualmente as ordens individuais das ordens coletivas do escopo atual.
- Enviar `Mover para cá` ao escopo selecionado, filtrando todos, infantaria ou arqueiros, e indicar o destino com o próximo clique direito em bloco.
- Enviar `Defender área` ao escopo selecionado, com o mesmo filtro de unidades, e escolher o centro territorial no mundo.
- Restringir ordens de combate ao escopo selecionado.
- Aplicar posturas `Defensiva`, `Agressiva`, `Passiva`, `Manter Posição` ou `Proteger Comandante` ao escopo selecionado.

### Pelotões e Oficiais

- O jogador pode criar, renomear e excluir Pelotões com nomes personalizados.
- A tela usa duas colunas: guardas disponíveis ou de outros Pelotões à esquerda e membros do Pelotão selecionado à direita.
- Cada linha mostra o nome fornecido pelo Villager Names, a patente e o Pelotão atual; transferências entre Pelotões pedem confirmação.
- Cada guarda contratado pode pertencer a no máximo um Pelotão.
- Um Capitão que já pertença ao Pelotão pode ser definido como seu único Oficial.
- Trocar ou remover o Oficial não retira o cargo persistente de Capitão do guarda.
- Excluir um Pelotão mantém seus guardas e suas ordens atuais; eles passam a aparecer como `Sem Pelotão`.
- O gerenciamento e os comandos consideram apenas guardas carregados na dimensão atual.
- Associações de guardas descarregados são preservadas; referências a Pelotões excluídos são reparadas quando o guarda volta a ser carregado.

Estados exibidos:

- `Ocioso`
- `Seguindo`
- `Aguardando`
- `Em patrulha`
- `Em rota`
- `Em movimento`
- `Defendendo`

### Mover para cá

No Livro de Comando, escolha o escopo atual e pressione `Mover para cá`. Selecione
`Todos`, `Infantaria` ou `Arqueiros` e clique com o botão direito em uma face de
bloco dentro de oito blocos. O servidor distribui os guardas carregados em slots
seguros próximos, sem teleporte corretivo. Combate pausa a marcha, que é retomada
depois; ao chegar, cada guarda permanece em `Aguardando`.

O modo de escolha dura 15 segundos. `Esc`, troca do Livro de mão, morte ou
desconexão cancelam a seleção sem emitir a ordem.

### Defender área

No Livro de Comando, escolha `Defender área`, selecione `Todos`, `Infantaria` ou
`Arqueiros` e indique o centro com o próximo clique direito em bloco. O servidor
atribui postos seguros e persiste centro, posto e raio para cada guarda. O raio
padrão é 16 blocos e pode ser alterado na configuração.

Dentro do território, patrulha, percepção e combate continuam sob controle da IA
nativa do Guard Villagers. Alvos que deixam o raio são abandonados; guardas que
passam da margem territorial retornam ao posto usando pathfinding, sem teleporte.
Outra ordem explícita cancela a defesa.

### Posturas de combate

Na tela de Ordens de Combate, pressione `Postura de Combate...` e escolha uma
postura para todos os guardas carregados do escopo atual:

- `Defensiva`: mantém a reação nativa do Guard Villagers;
- `Agressiva`: procura monstros hostis dentro do raio configurado;
- `Passiva`: abandona alvos e rejeita ordens explícitas de ataque;
- `Manter Posição`: combate por perto, abandona perseguições longas e retorna à
  posição em que a postura foi recebida;
- `Proteger Comandante`: prioriza criaturas que estejam atacando o dono.

A postura persiste no próprio guarda e não substitui ordens de seguir, mover,
patrulhar, rota ou defender área. IDs inválidos, Pelotões removidos e guardas que
deixaram de pertencer ao comandante são rejeitados novamente pelo servidor.

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
- `Botão direito` alterna entre Escolta, Coluna, Linha, Cunha, Quadrado, Círculo e Muralha de Escudos.
- A formação escolhida fica salva no pergaminho; pergaminhos antigos usam Escolta.
- Trocar a formação pelo pergaminho ativo reorganiza imediatamente os guardas do rali.
- Guardas convocados são posicionados na formação selecionada e passam a seguir.
- Enquanto o comandante se move, os guardas navegam continuamente para slots levemente adiantados, evitando o ciclo de andar e parar.
- Corrida e corrida com salto recebem recuperação moderada; viagens montadas não recebem aceleração especial.
- Guardas em patrulha fixa não são puxados; uma ordem `Defender área` pode ser
  substituída explicitamente pelo Rally.
- Guardas aguardando não são puxados.
- Enquanto o rali está ativo, guardas tentam manter seus slots de formação quando não estão em combate.
- Se o comandante se afastar mais de 24 blocos durante um combate, os guardas abandonam o alvo e retornam por padrão.
- Durante o rali, guardas contratados ficam protegidos contra dano causado pelo próprio comandante.
- A ordem normal de seguir preserva a IA do Guard Villagers e recebe apenas uma assistência moderada quando o comandante corre e o guarda começa a ficar para trás.

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

### Comando Rápido

Pressione `G` para abrir um painel não pausante com seis ordens frequentes para
todos os guardas carregados: `Seguir`, `Aguardar`, `Patrulhar`, `Atacar`,
`Mover para cá` e `Defender área`. A tecla pode ser remapeada nos controles.
O fundo é escurecido e as seis ações ficam reunidas em uma grade `2 × 3`
dentro de um único painel central opaco, que também cobre a mira.

As três ações imediatas reutilizam o feedback normal do servidor. `Atacar`,
`Mover para cá` e `Defender área` encaminham para as mesmas telas e modos de
seleção usados pelo Livro de Comando. Pelotões, guardas individuais, rotas,
formações e Rally permanecem nas interfaces completas.

### Patrulha Fixa

A patrulha fixa usa o comportamento nativo do Guard Villagers:

- O jogador define a posição atual como ponto de patrulha.
- O guarda caminha até o ponto e guarda aquela posição.
- Se encontrar mobs hostis, a IA normal do guarda continua funcionando.

### Rotas Nomeadas

- O botão `Rota` do Ledger permite escolher entre a rota individual do guarda e o acervo de Rotas Salvas.
- Uma rota individual com pelo menos dois pontos pode ser salva com nome, dimensão e tempo de espera próprios.
- O mesmo modelo pode ser atribuído e iniciado em vários guardas carregados do jogador.
- A gestão usa duas colunas para distinguir guardas disponíveis dos já atribuídos à rota selecionada.
- Transferir um guarda que já usa outra Rota Salva exige confirmação.
- Atualizar o modelo sincroniza seus guardas carregados; excluir pausa e desvincula os membros carregados sem apagar a cópia individual dos pontos.
- Rotas antigas continuam válidas como rotas individuais e são migradas automaticamente para o novo formato.

### Friendly Fire e Neutralidade

- Guardas em rali ficam protegidos contra ataques do jogador dono.
- Guardas contratados nunca atacam nem causam dano a animais, montarias ou outros guardas pertencentes ao mesmo comandante.
- Entidades sem propriedade persistente, como o Happy Ghast, ficam protegidas enquanto forem montadas ou diretamente conduzidas pelo comandante.
- Ordens de ataque contra uma entidade protegida são rejeitadas pelo servidor.
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

## Comandos administrativos de progressão

Com cheats habilitados ou permissão de operador, os comandos abaixo ajudam a testar níveis sem exigir combate repetitivo:

```text
/rallyguard xp get
/rallyguard xp get <alvo>
/rallyguard xp add <quantidade>
/rallyguard xp add <alvos> <quantidade>
```

Sem um alvo explícito, o comando usa o guarda vivo mais próximo em até 8 blocos. `<alvo>` aceita o UUID de uma entidade carregada, e `<alvos>` também aceita seletores do Minecraft. A quantidade usa os mesmos pontos mostrados no Registro de Tropa; por exemplo, `20` leva um recruta sem XP ao nível 2.

Exemplos:

```text
/rallyguard xp add 20
/rallyguard xp add @e[type=guardvillagers:guard,sort=nearest,limit=1] 30
/rallyguard xp get @e[type=guardvillagers:guard,sort=nearest,limit=1]
```

Os comandos usam o serviço normal de progressão, portanto respeitam a configuração, eventos de XP, mudança de patente e atualização de atributos.

---

## Requisitos

- **Minecraft:** 26.1
- **Loader:** Fabric
- **Java:** 25 ou superior
- **Fabric API**
- **Guard Villagers (Fabric/Quilt)**
- **MidnightLib**
- **Collective**
- **Villager Names** — fornece identidade nominal aos guardas e villagers

---

## Observações Técnicas

- Comandos funcionam apenas no mundo/dimensão onde o guarda está carregado.
- Guardas em chunks descarregados não processam rota até serem carregados novamente.
- Rotas são salvas no guarda usando tags persistentes.
- O sistema de rota atual não substitui a IA do Guard Villagers; ele apenas atualiza dinamicamente o `patrolPos`.
- A formação do rali não usa `patrolling`; ela usa slots relativos ao jogador e navegação.
- Ordens de combate atuam sobre guardas em rali/seguindo no mundo atual.
- Pelotões são definidos no jogador, enquanto cada guarda persiste apenas o UUID de sua associação.
- Listagem e ordens de Pelotão atuam apenas sobre membros carregados na dimensão do comandante.
- A proteção contra friendly fire se aplica ao contexto de rali.

---

## Configuração

O mod registra suas opções usando MidnightLib. Com **Mod Menu** e uma tela de configuração compatível instalada, o botão
de configuração aparece na lista de mods e abre uma interface para editar as opções.

As opções também ficam salvas no arquivo de configuração gerado pela MidnightLib para o mod.

Principais opções:

- `economy.hireCost`: custo para contratar um guarda.
- `economy.hireItem`: item usado na contratação, em formato de ID, por exemplo `minecraft:emerald`.
- `economy.dynamicHireCostEnabled`: ativa o preço calculado a partir do perfil do guarda.
- `economy.dynamicHireCostMultiplier`: multiplica o valor calculado antes dos limites finais.
- `economy.dynamicHireCostMinimum` / `economy.dynamicHireCostMaximum`: limites do preço dinâmico.
- `economy.captainHireCostBonus`: adicional fixo para Capitães, padrão de 4 itens.
- `progression.captainSpawnEnabled`: permite o surgimento de Capitães naturais.
- `progression.captainSpawnChance`: chance base por guarda natural, padrão de `0.05` (5%).
- `progression.captainMinimumLevel`: nível mínimo de um Capitão natural, padrão 5.
- `progression.captainRegionalRadius`: raio usado para contar Capitães carregados, padrão 128 blocos.
- `progression.captainRegionalLimit`: quantidade que zera a chance regional, padrão 2.
- `rally.radius`: raio usado para buscar guardas ao iniciar o rali.
- `rally.maxGuards`: limite de guardas chamados pelo rali.
- `rally.teleportEnabled`: permite ou bloqueia teleportes do rali/formação.
- `rally.teleportMinDistance`: distância mínima para teleportar um guarda ao iniciar o rali.
- `rally.formationEnabled`: liga ou desliga o posicionamento em formação.
- `rally.protectRalliedGuardsFromOwner`: impede o comandante de acertar seus guardas durante o rali.
- `rally.retreatEnabled`: controla a retirada automática de guardas em rali (`rallyRetreatEnabled` no arquivo MidnightLib).
- `rally.retreatDistance`: distância para abandonar o combate e retornar, padrão de 24 blocos (`rallyRetreatDistance` no arquivo MidnightLib).
- `formation.columnSpacing`: distância lateral usada pelas formações.
- `formation.rowSpacing`: distância entre fileiras.
- `formation.circleRadius`: raio mínimo de proteção da formação Círculo, padrão 3,25 blocos.
- `formation.headingMargin`: desvio lateral tolerado antes de a formação mudar de direção gradualmente, padrão 1,25 blocos.
- `formation.returnSpeed`: velocidade usada para voltar ao slot.
- `formation.teleportDistance`: distância em que a formação teleporta um guarda muito longe.
- `combat.targetRange`: alcance máximo da mira para ordens de ataque.
- `combat.guardSearchRadius`: raio dos guardas que recebem ordens de combate.
- `combat.allowPassiveTargets`: permite ordenar ataque contra mobs passivos.
- `combat.allowPlayerTargets`: permite ordenar ataque contra jogadores.
- `combat.combatStanceAggressiveRadius`: raio de aquisição da postura Agressiva, padrão 16 blocos.
- `combat.combatStanceProtectRadius`: raio de busca da postura Proteger Comandante, padrão 24 blocos.
- `combat.combatStanceHoldRadius`: limite de perseguição da postura Manter Posição, padrão 8 blocos.
- `route.maxPoints`: máximo de pontos por rota, limitado a 10 pela interface atual.
- `route.defaultWaitSeconds`: espera padrão em cada ponto.
- `route.moveSpeed`: velocidade ao mover entre pontos.
- `route.teleportIfStuck`: permite teleportar guardas de rota que ficaram presos/longe.
- `route.teleportDistance`: distância mínima para considerar teleporte de rota.
- `squads.enabled`: ativa o gerenciamento de Pelotões e as ordens coletivas sem apagar dados quando desativado.
- `squads.maxSquadsPerPlayer`: máximo de Pelotões personalizados por jogador, padrão 16.
- `squads.maxMembersPerSquad`: máximo de membros carregados por Pelotão, padrão 32.
- `route.maxNamedRoutesPerPlayer`: máximo de Rotas Salvas por jogador, padrão 32.
- `route.maxNamedRouteNameLength`: máximo de caracteres Unicode no nome de uma Rota Salva, padrão 32.
- `squads.maxSquadNameLength`: máximo de caracteres Unicode no nome, padrão 24.

---

## Licença

Este mod está disponível sob a licença **CC0-1.0**.

---

## Links

- GitHub: https://github.com/HyanFerreira/rally-of-the-guard
- Perfil do autor: https://github.com/HyanFerreira
