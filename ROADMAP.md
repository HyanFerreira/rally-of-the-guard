# Rally of the Guard — Roadmap

> Este roadmap reúne as principais ideias planejadas para a evolução do **Rally of the Guard**.
>
> O objetivo geral é fazer o mod evoluir de uma camada de comandos para o Guard Villagers para um sistema completo e leve de **comando, progressão, organização e identidade de tropas**, sem substituir ou descaracterizar a IA original do Guard Villagers.

---

## Estado atual

A versão atual do Rally of the Guard já estabelece a base do sistema de comando:

- contratação de guardas;
- vínculo de guardas ao jogador;
- Commander's Ledger;
- ordens de seguir, aguardar e patrulhar;
- rotas de patrulha;
- Rally / Scroll of Rallying;
- formação de escolta;
- ordens de combate;
- distinção entre infantaria e arqueiros;
- proteção contra friendly fire em contexto de Rally;
- configuração por arquivo / interface compatível.

A partir daqui, a evolução do mod deve priorizar duas frentes:

1. **dar identidade individual aos guardas**;
2. **transformar vários guardas em uma tropa realmente organizada e comandável**.

---

# Próxima grande atualização — Guard Profiles & Progression

Esta será a **próxima grande atualização do Rally of the Guard** e terá prioridade sobre as demais expansões de gameplay.

A proposta é fazer com que cada guarda deixe de ser apenas mais uma entidade idêntica às outras.

Cada guarda passará a possuir um perfil persistente próprio, com:

- nível;
- experiência;
- patente;
- atributos individuais;
- qualidade natural;
- estatísticas de combate;
- valor de contratação;
- possível função especial;
- histórico de progressão.

A intenção é criar apego aos guardas e transformar contratação, exploração e combate em sistemas de progressão de longo prazo.

---

## 1. Perfil individual persistente

Quando o Rally of the Guard encontrar um Guard Villager ainda não inicializado pelo mod, será criado um perfil individual para ele.

Exemplo conceitual:

```text
Thomas
Level 4
Guard

Health: 22
Attack: 10.8
Armor: 11
Movement Speed: 0.30

Kills: 18
Assists: 11
XP: 42 / 60
```

Esse perfil deverá ser salvo persistentemente no próprio guarda ou em uma estrutura persistente apropriada.

### Objetivos

- dois guardas nunca precisam ser exatamente iguais;
- guardas existentes em mundos antigos também podem receber perfil;
- não depender exclusivamente da geração de novas vilas;
- preservar os dados ao recarregar o mundo;
- evitar substituir a IA original do Guard Villagers.

### Inicialização

Conceitualmente:

```text
Guard Villager carregado
        |
        v
Possui perfil do Rally?
   |             |
  não           sim
   |             |
   v             v
Criar perfil   manter dados
```

Um campo de controle deverá impedir que atributos naturais sejam sorteados novamente.

Exemplo conceitual:

```text
rally:
{
    initialized: true,
    level: 3,
    xp: 18,
    naturalQuality: 0.72,
    healthRoll: 0.08,
    attackRoll: -0.02,
    speedRoll: 0.01,
    rank: "GUARD",
    kills: 14,
    assists: 9
}
```

A estrutura final ainda será definida durante a implementação.

---

# 2. Níveis

Os guardas terão progressão por níveis.

A ideia inicial é utilizar:

```text
Level 1 → Level 10
```

O nível representa a experiência acumulada em combate.

Quanto maior o nível:

- maior a experiência necessária para evoluir;
- melhores podem ser os atributos do guarda;
- maior sua patente;
- maior seu valor;
- maior seu histórico de combate.

### Curva inicial de XP

Valores ainda sujeitos a balanceamento:

| Evolução | XP necessário |
|---|---:|
| Lv. 1 → 2 | 20 |
| Lv. 2 → 3 | 30 |
| Lv. 3 → 4 | 45 |
| Lv. 4 → 5 | 60 |
| Lv. 5 → 6 | 80 |
| Lv. 6 → 7 | 105 |
| Lv. 7 → 8 | 135 |
| Lv. 8 → 9 | 170 |
| Lv. 9 → 10 | 210 |

A progressão final deverá ser lenta o suficiente para que um guarda de nível máximo represente tempo real de serviço.

O objetivo é evitar que uma única raid transforme rapidamente todos os guardas em unidades máximas.

---

# 3. Patentes

**Nível e patente não serão exatamente a mesma coisa.**

A patente funcionará como uma representação legível da experiência do guarda.

Estrutura inicial sugerida:

| Nível | Patente |
|---|---|
| 1–2 | Recruit |
| 3–4 | Guard |
| 5–6 | Soldier |
| 7–8 | Veteran |
| 9–10 | Elite |

Os nomes e intervalos poderão ser ajustados posteriormente.

## Capitão não será simplesmente uma patente de nível

A função **Captain** deverá ser tratada separadamente.

Isso evita uma situação em que todos os guardas veteranos eventualmente se tornam capitães.

Um guarda poderá ser, por exemplo:

```text
William
Level 8
Veteran

Role: Captain
```

No futuro, capitães poderão liderar squads e participar de sistemas de comando específicos.

---

# 4. Experiência de combate

Guardas ganharão experiência ao participar da morte de entidades hostis.

## Regra principal

**XP não será concedido por hit.**

O XP será processado quando o alvo morrer.

Isso evita farms em que um guarda poderia atacar repetidamente uma entidade resistente sem matá-la.

### Participação em combate

Exemplo:

```text
Zombie

Guard A causou dano.
Guard B causou dano.
Guard C não participou.

Guard A deu o último hit.

Resultado:

Guard A → XP de Kill
Guard B → XP de Assist
Guard C → 0 XP
```

### Valores conceituais

Exemplo inicial:

```text
Assist: +2 XP
Final Blow: +5 XP
```

Os valores deverão ser configuráveis.

---

# 5. Apenas alvos válidos concederão XP

O sistema deve impedir progressão por matar entidades passivas.

Não deverá ser possível evoluir guardas matando repetidamente:

- sheep;
- cows;
- pigs;
- chickens;
- villagers;
- outros mobs passivos comuns.

A progressão deve ocorrer através de combate real.

Exemplos de alvos naturalmente válidos:

- zombies;
- skeletons;
- creepers;
- hostile spiders;
- pillagers;
- vindicators;
- evokers;
- ravagers;
- outros mobs hostis.

## Tags

Para manter compatibilidade com modpacks, o sistema deverá preferencialmente utilizar tags.

Exemplo:

```text
#rallyoftheguard:xp_targets
```

Também poderá existir uma categoria especial:

```text
#rallyoftheguard:xp_bosses
```

Bosses poderão oferecer multiplicadores de experiência.

Isso permitirá que outros mods adicionem seus próprios mobs ao sistema sem exigir compatibilidade hardcoded.

---

# 6. Estatísticas de combate

O mesmo sistema usado para determinar kills e assists permitirá registrar o histórico de cada guarda.

Informações inicialmente planejadas:

- kills;
- assists;
- nível;
- XP atual;
- patente;
- possivelmente raids sobrevividas;
- possivelmente tempo de serviço;
- futuramente outras estatísticas.

Exemplo:

```text
Sir William
Level 10
Elite

Kills: 237
Assists: 109
Raids Survived: 14
```

Essas estatísticas são importantes para criar identidade e apego.

Um guarda veterano deve parecer um personagem que construiu uma história no mundo.

---

# 7. Atributos naturais

Guardas poderão nascer com pequenas diferenças naturais de atributos.

A intenção não é criar diferenças absurdas, mas fazer com que dois guardas da mesma patente não sejam necessariamente idênticos.

Exemplos de atributos que podem receber variação:

- Max Health;
- Attack Damage;
- Armor;
- Movement Speed.

## Regra importante

O Rally of the Guard **não deverá substituir valores base do Guard Villagers por números hardcoded**.

Em vez disso, o mod deverá ler o valor atual do atributo fornecido pelo Guard Villagers e aplicar modificadores próprios.

Conceitualmente:

```text
Guard Villagers fornece:
Base Attribute = X

Rally aplica:
Natural Modifier
+
Level Modifier
+
Rank Modifier

Resultado:
Final Attribute
```

Isso mantém compatibilidade caso o Guard Villagers altere seu balanceamento em versões futuras.

Também facilita ports entre versões.

---

# 8. Variação controlada

A aleatoriedade deverá ser pequena.

Não queremos:

- guardas extremamente lentos;
- guardas rápidos demais para o pathfinding;
- unidades de nível baixo melhores que unidades máximas em todos os atributos;
- diferenças grandes o suficiente para quebrar o balanceamento do Guard Villagers.

Exemplo conceitual de faixas:

### Recruit

```text
Health:       -10% → +5%
Attack:       -10% → +5%
Armor:        -1   → +1
Movement:     -3%  → +3%
```

### Guard

```text
Health:       -5% → +10%
Attack:       -5% → +10%
Armor:         0  → +2
Movement:     -2% → +3%
```

### Soldier

```text
Health:        0% → +15%
Attack:        0% → +15%
Armor:         0  → +3
```

### Veteran

```text
Health:       +10% → +25%
Attack:        +5% → +20%
Armor:         +1  → +4
```

Os números acima são apenas referências iniciais para discussão e balanceamento.

---

# 9. Talento natural + progressão

Os atributos finais do guarda deverão ser compostos por duas camadas principais.

## Talento natural

Gerado uma única vez quando o perfil do guarda é criado.

Representa a qualidade original daquele indivíduo.

Não muda durante sua vida.

## Progressão

Recebida ao ganhar níveis.

Permite que até um guarda inicialmente fraco se torne uma unidade valiosa depois de sobreviver e lutar por bastante tempo.

Exemplo:

```text
Level 1
20 Health
10 Attack

↓

Level 5
23 Health
11 Attack

↓

Level 10
28 Health
13 Attack
```

A progressão deverá ser moderada.

Um guarda de nível máximo deve ser claramente melhor que um recruta, mas não deve se transformar em um boss.

---

# 10. Guardas naturais de nível e patente superiores

Nem todos os guardas encontrados em vilas precisam começar no Level 1.

Alguns poderão receber perfis naturalmente superiores.

Exemplo de uma vila com aproximadamente 20 guardas:

```text
7 Recruits
8 Guards
3 Soldiers
1 Veteran
1 Captain
```

Essa distribuição não precisa ser garantida.

Ela representa o tipo de resultado que queremos tornar possível.

A intenção é criar momentos em que explorar uma vila revele um guarda raro e valioso.

Exemplo:

```text
Captain Aldric
Level 8
Veteran

Health: 30
Attack: 13.5
Armor: 15
```

Encontrar um guarda assim deverá ser um pequeno evento para o jogador.

---

# 11. Capitães naturais

Capitães deverão ser raros.

A geração deverá evitar excesso de capitães em uma mesma região.

Uma possibilidade futura:

```text
Nenhum Captain próximo:
chance maior

1 Captain próximo:
chance reduzida

2 Captains próximos:
chance zero
```

Também poderá existir um limite baseado em raio, vila ou outra forma de agrupamento.

A implementação exata ainda deverá ser definida.

---

# 12. Custo de contratação variável

A contratação deixará de ter necessariamente um valor idêntico para todos os guardas.

O preço poderá considerar:

- configuração base;
- nível;
- patente;
- qualidade natural;
- atributos;
- função especial;
- Captain modifier.

Exemplo de objetivo de balanceamento:

| Guarda | Faixa de preço aproximada |
|---|---:|
| Recruit fraco | 1 |
| Recruit bom | 2–3 |
| Guard | 3–4 |
| Soldier | 4–6 |
| Veteran | 6–9 |
| Elite | 8–12 |
| Captain raro | 10–15+ |

Esses valores deverão respeitar o item de contratação configurado pelo jogador/modpack.

Ou seja, o sistema deve continuar compatível com:

```text
economy.hireItem
```

e não assumir que a moeda será sempre Emerald.

---

# 13. Interface de perfil

As informações de progressão **não deverão poluir o Commander's Ledger**.

O Ledger continuará sendo principalmente uma ferramenta de comando.

Uma nova interface será criada especificamente para consultar informações de guardas.

Possíveis nomes para o item:

- Guard Registry;
- Muster Roll;
- Guard Roster;
- outro nome a ser definido.

O item poderá abrir o perfil do guarda selecionado.

Exemplo conceitual:

```text
┌─────────────────────────────────────┐
│              THOMAS                 │
│         Veteran • Level 7           │
│                                     │
│            [Guard Model]            │
│                                     │
│ EXPERIENCE                          │
│ █████████████░░░░   84 / 135        │
│                                     │
│ ATTRIBUTES                          │
│ Health                   27 / 27    │
│ Attack                    12.3      │
│ Armor                     13        │
│ Movement                  0.31      │
│                                     │
│ COMBAT                              │
│ Kills                     43        │
│ Assists                   28        │
│                                     │
│ Squad                     Alpha     │
│ Role                      Infantry  │
│                                     │
│ Service                   12 days   │
└─────────────────────────────────────┘
```

Nem todos os campos acima precisam existir já na primeira versão.

A interface deverá crescer junto com os sistemas futuros.

---

# 14. Qualidade do guarda

Além dos números individuais, poderá existir uma representação resumida da qualidade do guarda.

Exemplo:

```text
Quality: ★★★★☆
```

Essa classificação não precisa alterar atributos por si só.

Ela pode simplesmente resumir a qualidade natural gerada para facilitar a leitura pelo jogador.

Ainda deverá ser decidido se essa informação será:

- completamente visível;
- parcialmente visível;
- descoberta após contratação;
- exibida somente por meio do item de perfil.

---

# 15. Configuração

A progressão deverá ser altamente configurável para servidores e modpacks.

Possíveis opções:

```text
progression.enabled
progression.maxLevel

progression.assistXp
progression.killXp
progression.bossMultiplier

progression.statGrowthEnabled
progression.naturalVariationEnabled

progression.advancedNaturalGuardsEnabled
progression.captainSpawnEnabled

economy.dynamicHireCostEnabled
```

Também poderão existir configurações específicas de distribuição de níveis e patentes.

---

# 16. Compatibilidade com mundos existentes

A atualização deverá ser projetada para funcionar em saves já existentes.

Ao carregar um guarda antigo:

```text
Guard Villager existente
        |
        v
Sem perfil Rally
        |
        v
Perfil criado uma única vez
```

Isso significa que o jogador não precisará criar um mundo novo para utilizar a atualização.

---

# 17. Possível evolução futura — Traits

Traits não fazem parte obrigatoriamente da primeira implementação de Guard Profiles, mas o sistema deverá ser pensado para permitir sua adição posteriormente.

Exemplos positivos:

```text
Strong
+ Attack

Tough
+ Health

Swift
+ Movement Speed

Marksman
+ Ranged effectiveness

Defender
+ Armor
```

Possíveis traits negativos:

```text
Weak
Fragile
Slow
```

Traits deverão ser sutis.

O objetivo é adicionar personalidade, não transformar a escolha de guarda em um sistema excessivamente complexo de min-maxing.

---

# Atualizações futuras

As versões abaixo são direções planejadas.

Nomes, números de versão, conteúdo e ordem ainda podem mudar.

---

# Squads & Officers

Depois que cada guarda possuir identidade própria, o próximo passo natural será organizar esses indivíduos em unidades.

## Squads

O jogador poderá criar grupos personalizados.

Exemplos:

```text
Alpha Squad
Bravo Squad
Archers
Gate Guard
Village Patrol
Royal Guard
```

Cada guarda poderá ser atribuído a um squad.

O Commander's Ledger deverá permitir selecionar:

```text
All Guards

Alpha Squad
Bravo Squad
Archers
```

e então enviar ordens para aquele grupo.

## Officers

Captains poderão assumir papel real no sistema.

Exemplo:

```text
Alpha Squad

Captain William — Lv. 9
Soldier Henry — Lv. 5
Guard Robert — Lv. 4
Recruit Arthur — Lv. 2
```

Possíveis regras futuras:

- exigência de nível mínimo para promoção;
- somente um Captain por squad;
- Capitains naturais já podem possuir experiência elevada;
- Capitains podem funcionar como referência para determinadas formações;
- pequenos bônus de liderança podem ser avaliados posteriormente.

---

# Tactical Orders

O sistema de ordens poderá ser expandido para transformar o Rally em uma camada tática mais completa.

## Move Here

Permitir mandar:

- todos os guardas;
- um squad;
- uma categoria;

até a posição indicada pelo jogador.

Exemplo:

```text
Move Squad Here
```

## Defend Area

Criar uma ordem territorial.

Exemplo:

```text
Defend Area
Radius: 16 blocks
```

A unidade deverá proteger uma região sem necessariamente ficar presa a um único ponto.

Essa ordem será diferente da patrulha fixa.

---

# Combat Stances

Guardas e squads poderão receber comportamentos táticos.

Possibilidades:

### Defensive

Reage principalmente quando:

- o comandante é atacado;
- outro guarda aliado é atacado;
- uma ameaça entra em uma área protegida.

### Aggressive

Procura e enfrenta hostis dentro de determinado alcance.

### Passive

Não inicia combate.

### Hold Position

Pode combater, mas evita perseguições longas.

### Protect Commander

Prioriza ameaças que estejam atacando o jogador comandante.

---

# Formations

O sistema de Rally poderá ganhar várias formações.

Possibilidades:

### Escort

Formação atual.

### Column

```text
X
X
X
X
```

### Line

```text
X X X X X X
```

### Wedge

```text
      X
    X   X
  X       X
```

### Square

```text
X X X
X X X
X X X
```

### Circle

```text
  X X
X   P   X
  X X
```

### Shield Wall

```text
X X X X X
```

As formações deverão respeitar combate e pathfinding, evitando que o sistema force posições quando a IA precisar reagir.

---

# Quick Command / Radial Menu

O Commander's Ledger continuará sendo a interface completa.

Entretanto, uma interface rápida poderá facilitar ordens durante combate.

Possível menu radial:

```text
             FOLLOW

      DEFEND        ATTACK

   FORMATION          MOVE

       WAIT          RALLY

             PATROL
```

A ideia é permitir que o jogador dê ordens táticas sem precisar navegar pelo Ledger em situações urgentes.

---

# Named Routes

O sistema atual de rotas poderá evoluir para rotas reutilizáveis.

Exemplos:

```text
Village Walls
Gate Patrol
Farm Route
Castle Interior
Northern Road
```

Em vez de a rota existir exclusivamente no guarda, ela poderá ser salva como uma entidade lógica reutilizável.

Então vários guardas poderão receber a mesma rota.

Exemplo:

```text
Village Walls

Assigned Guards:
✓ Robert
✓ Henry
✓ William
□ Arthur
```

Isso tornará patrulhas grandes muito mais fáceis de configurar.

---

# Rally Point / Command Post

Uma futura atualização poderá adicionar um bloco territorial de comando.

Possíveis nomes:

- Rally Point;
- Command Post;
- Commander's Table.

Função conceitual:

```text
Rally Point

Assigned Guards: 8

Squad:
Village Guard

Formation:
Square

Behavior:
Defend

Radius:
32
```

A ideia é criar dois tipos complementares de comando:

```text
Commander's Ledger
→ comando móvel

Rally Point
→ comando territorial
```

Isso seria especialmente útil para:

- vilas;
- castelos;
- muralhas;
- portões;
- bases de jogadores.

---

# Guard Registry global

No futuro, o sistema de perfis poderá crescer para um registro persistente dos guardas contratados.

Isso permitiria ao jogador consultar unidades mesmo quando não estiverem carregadas.

Exemplo:

```text
Robert
Level 6
Soldier

Dimension: Overworld
Order: Patrol
Distance: 842 m
Status: Chunk unloaded
```

Outro exemplo:

```text
Arthur
Level 4
Guard

Dimension: Nether
Last known position:
350, 67, -122
```

O objetivo é fazer o sistema deixar de ser apenas uma interface para entidades próximas e se tornar um verdadeiro registro do exército do jogador.

---

# Village Defense & Raids

Raids combinam naturalmente com a proposta do Rally of the Guard.

Uma futura atualização poderá adicionar integração especial.

Exemplo:

```text
RAID DETECTED

Village under attack!

[ Rally All Guards ]
[ Defend Village ]
[ Ignore ]
```

Possíveis comportamentos configuráveis:

```text
Raid Behavior:

Normal
Rally to Village
Rally to Commander
```

## Raid Report

Depois da raid, poderá existir um relatório.

Exemplo:

```text
RAID REPORT

Enemies defeated: 38
Guards deployed: 12
Guards lost: 2

MVP:
Robert — 9 kills
```

Esse sistema conversa diretamente com:

- níveis;
- kills;
- assists;
- histórico;
- squads;
- capitães.

---

# Multiplayer & Shared Command

Uma expansão futura poderá melhorar suporte para servidores e grupos de jogadores.

Possibilidades:

- compartilhar squads;
- autorizar comandantes adicionais;
- integrar com vanilla teams;
- impedir conflitos entre guardas de jogadores aliados;
- criar tropas de guildas ou facções.

Exemplo:

```text
Squad Access

✓ Hyan
✓ Maria
```

O ownership continuará sendo importante, mas poderá existir uma camada opcional de permissões.

---

# Weapon & Mod Compatibility

A classificação atual de unidades poderá evoluir para um sistema baseado em tags.

Exemplo:

```text
#rallyoftheguard:ranged_weapons
#rallyoftheguard:melee_weapons
```

Isso permitiria compatibilidade mais fácil com:

- novos bows;
- crossbows;
- muskets;
- firearms;
- magic weapons;
- equipamentos de outros mods.

Em vez de programar cada integração manualmente, outros mods e modpacks poderão adicionar itens às tags.

---

# Expansão de versões e mod loaders

O crescimento de gameplay deverá acontecer em paralelo com o aumento de compatibilidade.

Princípio principal:

> Sempre que possível, Rally of the Guard deverá acompanhar as versões que o Guard Villagers suporta.

Prioridades gerais:

- Fabric;
- Forge;
- NeoForge;
- versões modernas com maior uso;
- versões populares em modpacks;
- manutenção de versões antigas quando fizer sentido.

A arquitetura deverá evitar lógica duplicada excessiva entre loaders.

Sempre que possível:

```text
Common logic
    |
    +-- Fabric
    |
    +-- Forge / NeoForge
```

A estratégia exata dependerá das diferenças entre versões do Minecraft e das APIs disponíveis.

---

# Distribuição

Além do desenvolvimento técnico, o projeto poderá expandir sua distribuição.

Objetivos:

- manter CurseForge atualizado;
- publicar e manter versões no Modrinth;
- documentar claramente loaders e versões;
- manter changelogs completos;
- melhorar screenshots e apresentação;
- facilitar inclusão em modpacks.

A ideia é tornar o Rally of the Guard fácil de encontrar para qualquer jogador que já utilize Guard Villagers.

---

# Princípios de design

Todas as futuras atualizações devem respeitar alguns princípios.

## 1. Rally complementa Guard Villagers

O Rally of the Guard não deve substituir Guard Villagers.

Ele deve adicionar:

- comando;
- organização;
- progressão;
- informação;
- personalização;
- estratégia.

A IA base deverá continuar sendo aproveitada sempre que possível.

## 2. Vanilla-friendly

Mesmo adicionando sistemas táticos e RPG, o mod deve continuar parecendo compatível com Minecraft.

Evitar:

- interfaces excessivamente modernas;
- números gigantes;
- buffs absurdos;
- sistemas complexos sem necessidade;
- transformar guardas em unidades de RTS completamente desconectadas do jogo.

## 3. Configurabilidade

Sistemas importantes devem poder ser ajustados.

Especialmente:

- economia;
- progressão;
- experiência;
- atributos;
- níveis;
- chances de guardas raros;
- limites;
- comportamento tático.

## 4. Compatibilidade com modpacks

Sempre que possível, utilizar:

- tags;
- configurações;
- valores derivados dos atributos existentes;
- integração não hardcoded.

## 5. Identidade individual

Um objetivo central daqui em diante é permitir que o jogador se importe com seus guardas.

O guarda ideal não deve ser apenas:

```text
Guard #7
```

Ele deve poder se tornar:

```text
William
Level 10
Elite
Captain of Alpha Squad

237 Kills
109 Assists
14 Raids Survived
```

Quando isso acontecer, perder um guarda veterano terá peso real.

---

# Direção geral do projeto

A evolução planejada pode ser resumida assim:

```text
Guard Villagers
      |
      v
Individual Guards
      |
      v
Profiles & Progression
      |
      v
Ranks & Captains
      |
      v
Squads
      |
      v
Formations & Tactical Orders
      |
      v
Patrol Infrastructure
      |
      v
Village Defense
      |
      v
Multiplayer Command
```

O objetivo final é que Rally of the Guard se torne o complemento natural para jogadores que querem fazer mais com os guardas do Guard Villagers.

> Guard Villagers cria os guardas.
>
> Rally of the Guard permite conhecê-los, desenvolvê-los, organizá-los e comandá-los.

---

## Status do roadmap

- [x] Core Rally system
- [x] Guard hiring
- [x] Commander's Ledger
- [x] Follow / Wait
- [x] Fixed patrol
- [x] Patrol routes
- [x] Escort formation
- [x] Combat orders
- [ ] **Guard Profiles & Progression — próxima grande atualização**
- [ ] Guard levels
- [ ] Guard ranks
- [ ] XP from kills and assists
- [ ] Combat statistics
- [ ] Natural attribute variation
- [ ] Advanced natural guards
- [ ] Dynamic hiring cost
- [ ] Guard Profile GUI
- [ ] Captains
- [ ] Squads
- [ ] Additional formations
- [ ] Move Here
- [ ] Defend Area
- [ ] Combat stances
- [ ] Quick Command / Radial Menu
- [ ] Named Routes
- [ ] Rally Point / Command Post
- [ ] Global Guard Registry
- [ ] Raid integration
- [ ] Multiplayer shared command
- [ ] Extended weapon compatibility
- [ ] Additional loaders and Minecraft versions

---

*This roadmap is intentionally flexible. Features, names, balancing values, version numbers and implementation order may change during development.*
