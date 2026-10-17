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

# Atualização concluída — Guard Profiles & Progression

Este marco está concluído e estabelece a base necessária para a próxima frente:
**Captains como líderes e Squads**.

A proposta é fazer com que cada guarda deixe de ser apenas mais uma entidade idêntica às outras. Cada guarda passará a possuir um perfil persistente próprio, com nível, experiência, patente, atributos naturais, qualidade natural, progressão de atributos, estatísticas de combate, valor de contratação, possível função especial e histórico de progressão.

A intenção é criar apego aos guardas e transformar contratação, exploração e combate em sistemas de progressão de longo prazo.

---

## 1. Perfil individual persistente

Quando o Rally of the Guard encontrar um Guard Villager ainda não inicializado pelo mod, será criado um perfil individual para ele.

Exemplo conceitual:

```text
Thomas
Level 4
Guardsman

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
    armorRoll: 1,
    speedRoll: 0.01,
    rank: "GUARDSMAN",
    captain: false,
    kills: 14,
    assists: 9
}
```

A estrutura final ainda será definida durante a implementação.

---

## 2. Níveis

Os guardas terão progressão de **Level 1 até Level 10**.

O nível representa a experiência acumulada em combate. Quanto maior o nível, maior a experiência necessária para evoluir, melhores os bônus de progressão e maior o valor histórico daquele guarda.

### Curva inicial de XP

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

Total aproximado para alcançar o nível máximo: **855 XP**.

A intenção é que:

- Lv. 1–3 seja relativamente comum;
- Lv. 4–6 represente experiência real;
- Lv. 7–8 seja claramente veterano;
- Lv. 9–10 seja raro e memorável.

Um guarda Lv.10 deve representar bastante tempo de serviço e ser alguém que o jogador reconhece.

---

## 3. Patentes

**Nível, patente e cargo são conceitos separados.**

A patente representa a experiência militar do guarda.

| Nível | Patente |
|---|---|
| 1–2 | Recruit |
| 3–4 | Guardsman |
| 5–6 | Soldier |
| 7–8 | Veteran |
| 9–10 | Elite |

`Guardsman` é preferível a `Guard` para evitar confusão com o próprio tipo de entidade.

---

## 4. Captain é cargo, não patente

`Captain` não será simplesmente a última patente da progressão. Isso evita que todos os guardas veteranos eventualmente se tornem capitães.

Um guarda poderá ser, por exemplo:

```text
William
Level 8
Veteran
Captain
```

No futuro, Captains deverão conversar diretamente com Squads, Officers, formações e sistemas de liderança.

Um Captain natural poderá nascer já com nível elevado, mas o cargo continuará separado da patente.

---

## 5. Experiência de combate

Guardas ganharão experiência ao participar da morte de entidades hostis válidas.

**XP não será concedido por hit.** O cálculo ocorrerá quando o alvo morrer, evitando farms baseadas em atacar repetidamente entidades resistentes sem matá-las.

---

## 6. XP por contribuição de dano

O sistema não deverá usar apenas valores fixos de `Assist XP` e `Kill XP`, pois isso premiaria excessivamente quem apenas deu o último hit.

Cada inimigo possuirá um **orçamento total de XP**. Esse XP será dividido conforme a contribuição de dano de cada guarda, com um pequeno bônus para quem der o golpe final.

Referência inicial:

```text
80% do XP total → participação no dano
20% do XP total → Final Blow
```

Exemplo:

```text
Zombie = 6 XP total

Guard A causou 75% do dano.
Guard B causou 25% do dano.
Guard B deu o último hit.
```

Resultado aproximado:

```text
Guard A:
4.8 × 75%
≈ 3.6 XP

Guard B:
4.8 × 25%
+ 1.2 Final Blow
≈ 2.4 XP
```

Dessa forma:

- o último hit continua importante;
- quem realmente participou do combate é recompensado;
- kill stealing deixa de ser extremamente vantajoso;
- o XP total gerado por um mob permanece controlado mesmo com muitos guardas envolvidos.

---

## 7. XP por ameaça

Nem todo mob hostil deverá valer a mesma quantidade de XP.

| Categoria | Exemplos | XP base |
|---|---|---:|
| Common | Zombie, Skeleton, Spider | 5 |
| Dangerous | Creeper, Witch | 7 |
| Elite | Piglin Brute, Guardian | 10 |
| Raid | Pillager, Vindicator | 6–10 |
| Raid Elite | Evoker, Ravager | 15–20 |
| Boss | Wither e equivalentes | 30+ |

Os valores exatos ainda deverão ser testados e balanceados.

---

## 8. Apenas alvos válidos concederão XP

O sistema deve impedir progressão por matar entidades passivas. Não deverá ser possível evoluir guardas matando repetidamente sheep, cows, pigs, chickens, villagers ou outros mobs passivos comuns.

A progressão deve ocorrer através de combate real.

Para manter compatibilidade com modpacks, o sistema deverá preferencialmente utilizar tags:

```text
#rallyguard:xp_targets
#rallyguard:no_xp
#rallyguard:xp/common
#rallyguard:xp/dangerous
#rallyguard:xp/raid
#rallyguard:xp/elite
#rallyguard:xp/raid_elite
#rallyguard:xp/boss
```

Isso permitirá que outros mods adicionem seus próprios mobs ao sistema sem exigir compatibilidade hardcoded.

---

## 9. Estatísticas de combate

O mesmo sistema usado para determinar participação em combate permitirá registrar o histórico de cada guarda.

Inicialmente serão persistidos pelo menos:

- kills;
- assists;
- nível;
- XP atual;
- patente.

Possibilidades posteriores:

- raids sobrevividas;
- tempo de serviço;
- outras estatísticas históricas.

Exemplo:

```text
Sir William
Level 10
Elite

Kills: 237
Assists: 109
Raids Survived: 14
```

Um guarda veterano deve parecer um personagem que construiu uma história no mundo.

---

## 10. Progressão de atributos por nível

A progressão será deliberadamente moderada. Um Elite deve ser claramente superior a um Recruit sem se transformar em um boss.

Referência inicial:

| Nível | HP adicional | Attack adicional | Armor adicional |
|---:|---:|---:|---:|
| 1 | +0 | +0 | +0 |
| 2 | +1 | +0 | +0 |
| 3 | +1 | +0.25 | +0 |
| 4 | +2 | +0.25 | +1 |
| 5 | +2 | +0.50 | +1 |
| 6 | +3 | +0.75 | +1 |
| 7 | +4 | +1.00 | +2 |
| 8 | +5 | +1.25 | +2 |
| 9 | +6 | +1.50 | +3 |
| 10 | +8 | +2.00 | +3 |

Os números ainda deverão ser testados.

### Movement Speed

**Movement Speed não aumentará por nível.**

A velocidade poderá possuir pequena variação natural, mas não deverá escalar com progressão para preservar pathfinding, formações, combate e acompanhamento do jogador.

---

## 11. Não hardcodar atributos do Guard Villagers

O Rally of the Guard não deverá substituir os valores base do Guard Villagers por números fixos próprios.

Em vez disso:

```text
Guard Villagers fornece:
Base Attribute = X

Rally aplica:
Natural Modifier
+
Level Modifier

Resultado:
Final Attribute
```

Isso mantém compatibilidade caso o Guard Villagers altere seu balanceamento em versões futuras e facilita ports entre versões/loaders.

---

## 12. Natural Quality

Cada guarda terá uma **Natural Quality** gerada uma única vez quando seu perfil for criado.

Conceitualmente:

```text
0.0 → muito abaixo da média
0.5 → normal
1.0 → excepcional
```

A Natural Quality influenciará a tendência geral dos atributos, mas não obrigará o guarda a ser melhor em tudo.

Exemplos:

```text
Robert
Natural Quality: 0.35

Health: +3%
Attack: -4%
Armor: 0
Speed: +1%
```

```text
William
Natural Quality: 0.81

Health: +8%
Attack: +5%
Armor: +1
Speed: -1%
```

```text
Henry

Health: +10%
Attack: -3%
Armor: +2
Speed: -2%
```

Isso cria indivíduos naturalmente mais resistentes, ofensivos ou equilibrados sem exigir classes formais.

---

## 13. Representação por estrelas

A Natural Quality poderá ser apresentada de forma resumida por estrelas:

```text
Quality: ★★★★☆
```

As estrelas representam **talento natural**, não nível.

Portanto, pode existir:

```text
Level 2 Recruit
★★★★★
```

assim como:

```text
Level 7 Veteran
★★☆☆☆
```

Princípio central:

> talento natural não é a mesma coisa que experiência.

Ainda deverá ser decidido se a qualidade ficará totalmente visível, parcialmente visível, acessível apenas pelo Muster Roll ou escondida até a contratação.

---

## 14. Variações naturais de atributos

As variações naturais deverão ser pequenas.

Faixas iniciais:

```text
Health:
-8% → +10%

Attack:
-8% → +10%

Armor:
-1 → +2

Movement Speed:
-2% → +2%
```

A aleatoriedade deve permanecer controlada para evitar guardas extremamente lentos, rápidos demais para o pathfinding ou unidades de nível baixo absurdamente superiores às unidades máximas.

Os atributos finais resultarão de:

```text
Guard Villagers base stats
+
Natural Stats
+
Level Progression
```

---

## 15. Talento natural + experiência

Os atributos finais terão duas camadas principais.

### Talento natural

Gerado uma única vez quando o perfil é criado. Nunca muda.

### Progressão

Recebida ao ganhar níveis. Permite que até um guarda inicialmente fraco se torne valioso depois de sobreviver e lutar por bastante tempo.

Exemplo conceitual:

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
12 Attack
```

A experiência terá peso sem apagar completamente a individualidade natural do guarda.

---

## 16. Nível inicial de guardas naturais

Nem todos os guardas encontrados naturalmente precisam começar no Level 1.

Distribuição inicial para testes:

| Level | Chance |
|---:|---:|
| 1 | 45% |
| 2 | 25% |
| 3 | 14% |
| 4 | 8% |
| 5 | 4% |
| 6 | 2% |
| 7 | 1% |
| 8 | 0.6% |
| 9 | 0.3% |
| 10 | 0.1% |

A intenção é que a maioria fique entre Lv.1 e Lv.4, encontrar Lv.5–6 seja interessante, Lv.7+ seja raro e um Lv.10 natural seja quase lendário.

Isso cria valor real em explorar novas vilas.

---

## 17. Captains naturais

Captain terá seu próprio roll.

Referência inicial:

```text
Natural Captain Chance: 5%
Minimum Captain Level: 5
```

Caso um guarda sorteado como Captain tenha recebido nível inferior, o sistema poderá promovê-lo para uma faixa apropriada.

Exemplo:

```text
Captain Aldric
Level 7
Veteran
```

Também deverá existir controle regional para impedir excesso de Captains.

Objetivo aproximado:

```text
Vila com ~20 guardas:
normalmente 0–1 Captain
ocasionalmente 2
raramente mais que isso
```

Regra implementada:

```text
0 Captains próximos → chance normal
1 Captain próximo  → chance reduzida
2 Captains próximos → chance zero
```

Por padrão, o controle considera Capitães vivos e carregados em um raio de 128
blocos. A chance base é de 5%, cai pela metade com um Capitão próximo e chega a
zero ao atingir o limite regional de dois. Chance, raio, limite e nível mínimo
são configuráveis.

---

## 18. Custo de contratação variável

A contratação deixará de ter necessariamente um valor idêntico para todos os guardas.

O preço deverá resultar principalmente de:

```text
Patente
+
Natural Quality
+
Cargo
```

Objetivo inicial de balanceamento:

| Tipo | Faixa aproximada |
|---|---:|
| Recruit fraco | 1 |
| Recruit bom | 2–3 |
| Guardsman | 2–4 |
| Soldier | 4–6 |
| Veteran | 6–9 |
| Elite | 9–13 |
| Captain | +3 a +5 sobre o valor normal |

Exemplo:

```text
Thomas
Recruit · Level 1
★☆☆☆☆

Hire: 1 Emerald
```

```text
Aldric
Veteran · Level 8
★★★★☆
Captain

Hire: 12 Emeralds
```

Isso cria uma decisão real entre contratar vários recrutas baratos ou investir em uma unidade excepcional.

O sistema deverá continuar respeitando `economy.hireItem`; a moeda não deverá ser hardcoded como Emerald.

---

## 19. Muster Roll

As informações de progressão **não deverão poluir o Commander's Ledger**.

O Ledger continuará sendo principalmente uma ferramenta de comando.

O novo item/interface de perfil será chamado inicialmente de **Muster Roll**, funcionando como um registro militar.

Descrição conceitual:

```text
A record of the guards under your command.
```

Na primeira versão, seu uso principal será inspecionar um guarda. No futuro, poderá evoluir para um registro completo das tropas.

Exemplo conceitual de interface:

```text
╔════════════════════════════════╗
        WILLIAM
       ★★★★☆

   Veteran · Level 7
       Captain

   ████████████░░
      94 / 135 XP

        ATTRIBUTES

   ♥ Health       27
   ⚔ Attack       11.8
   🛡 Armor        13
   ➤ Speed         0.30

         SERVICE

   Kills           47
   Assists         31

   Squad           Alpha
   Role             Melee

   Hiring Value     9
╚════════════════════════════════╝
```

Campos ligados a sistemas ainda não implementados, como `Squad`, poderão permanecer ocultos até suas respectivas atualizações.

---

## 20. Traits ficam para depois

Traits continuam sendo uma possibilidade futura, mas **não fazem parte da primeira implementação de Guard Profiles & Progression**.

Possibilidades futuras:

```text
Strong
Tough
Swift
Marksman
Defender
```

E eventualmente alguns negativos:

```text
Weak
Fragile
Slow
```

A arquitetura poderá ser preparada para suportá-los, mas eles não deverão atrasar a implementação principal.

---

## 21. Configuração

A progressão deverá ser altamente configurável para servidores e modpacks.

Possíveis opções:

```text
progression.enabled
progression.maxLevel

progression.damageContributionShare
progression.finalBlowShare

progression.statGrowthEnabled
progression.naturalVariationEnabled
progression.naturalQualityEnabled

progression.advancedNaturalGuardsEnabled

progression.captainSpawnEnabled
progression.captainSpawnChance
progression.captainMinimumLevel
progression.captainRegionalRadius
progression.captainRegionalLimit

economy.dynamicHireCostEnabled
economy.captainHireCostBonus
```

Também poderão existir configurações específicas para distribuição de níveis naturais, XP por categoria de inimigo, multiplicadores de bosses, limites regionais de Captains e crescimento por nível.

---

## 22. Compatibilidade com mundos existentes

A atualização deverá funcionar em saves já existentes.

```text
Guard Villager existente
        |
        v
Sem perfil Rally
        |
        v
Perfil criado uma única vez
```

O jogador não precisará criar um mundo novo para utilizar a atualização.

---

## 23. Especificação atual consolidada

A direção desta atualização foi definida assim:

1. **Level 1–10**.
2. **Recruit → Guardsman → Soldier → Veteran → Elite**.
3. **Captain separado da patente**, como cargo.
4. XP somente por inimigos hostis válidos.
5. XP concedido somente quando o alvo morrer.
6. XP dividido por contribuição de dano.
7. Pequeno bônus para Final Blow.
8. Aproximadamente **855 XP** até Lv.10.
9. Progressão moderada de HP, Attack e Armor.
10. Movement Speed não cresce por nível.
11. Atributos sempre derivados dos valores atuais do Guard Villagers.
12. Cada guarda recebe pequenas variações naturais permanentes.
13. Cada guarda possui **Natural Quality**.
14. Natural Quality poderá ser exibida em **1–5 estrelas**.
15. Guardas naturais podem nascer acima do Lv.1.
16. Níveis altos naturais serão raros.
17. Captain terá roll próprio e controle regional.
18. Preço de contratação dependerá de patente, qualidade e cargo.
19. O novo item/interface de perfil será o **Muster Roll**.
20. Inicialmente serão persistidos pelo menos **kills e assists**.
21. Traits ficam para uma atualização posterior.

Essas definições formam a base de design para a implementação da atualização.

---

# Atualização concluída — Squads & Officers

Com a identidade individual estabelecida, os guardas agora podem ser organizados em unidades persistentes e comandados coletivamente.

## Squads

O jogador pode criar, renomear e excluir grupos personalizados.

Exemplos:

```text
Alpha Squad
Bravo Squad
Archers
Gate Guard
Village Patrol
Royal Guard
```

Cada guarda carregado pode ser atribuído a um squad, com reparo seguro de associações antigas ou excluídas.

O Commander's Ledger permite selecionar:

```text
All Guards

Alpha Squad
Bravo Squad
Archers
```

e então enviar ordens para aquele grupo.

## Officers

Captains podem assumir o papel de Officer de um squad. O servidor exige que o Officer esteja vivo, carregado, pertença ao jogador, seja membro do squad e possua o cargo de Captain.

Exemplo:

```text
Alpha Squad

Captain William — Lv. 9
Soldier Henry — Lv. 5
Guard Robert — Lv. 4
Recruit Arthur — Lv. 2
```

Possíveis evoluções futuras:

- Captains podem funcionar como referência para determinadas formações;
- pequenos bônus de liderança podem ser avaliados posteriormente.

---

# Atualização concluída — Captain Promotion & Demotion

O jogador agora pode transformar um guarda veterano em Captain por meio de uma
promoção deliberada no Muster Roll. Isso permite que um guarda que construiu sua
história em serviço assuma um cargo de liderança, sem depender
exclusivamente da chance de encontrar um Captain natural.

Os dois caminhos continuam valiosos:

```text
Captain natural
→ raro, experiente desde a geração e mais caro para contratar

Captain promovido
→ desenvolvido pelo jogador e reconhecido por seu tempo de serviço
```

Captain continua sendo um **cargo persistente**, separado da patente e da
função de Officer. Promover um guarda não o torna automaticamente Officer de
um squad; essa continua sendo uma escolha separada.

## Requisitos de promoção

Regras implementadas:

- o guarda deve pertencer ao jogador;
- o guarda deve estar vivo, carregado e possuir um perfil válido;
- o guarda ainda não pode possuir o cargo de Captain;
- o guarda deve ter pelo menos **Level 7 / Veteran** por padrão;
- o jogador deve pagar o custo configurado de promoção;
- toda validação e mutação deve ocorrer no servidor.

O nível mínimo é configurável. A promoção não concede XP,
níveis, patente ou atributos adicionais por si mesma.

## Custo de promoção

A promoção utiliza `economy.hireItem`, preservando compatibilidade
com servidores e modpacks que substituem Emerald por outra moeda.

O pagamento deve ser atômico:

1. validar propriedade, perfil, nível, cargo atual e saldo;
2. consumir o custo configurado;
3. persistir o cargo de Captain;
4. sincronizar as interfaces afetadas.

Uma falha em qualquer validação não deverá consumir itens nem alterar o perfil.

## Demissão do cargo

O jogador também poderá retirar o cargo de Captain de um guarda sob seu
comando.

- a demissão não devolverá o custo da promoção ou da contratação;
- nível, patente, XP, atributos e estatísticas permanecem inalterados;
- um Captain que seja Officer precisa ser removido dessa função antes da demissão;
- um guarda demitido pode ser promovido novamente se cumprir os requisitos e
  pagar um novo custo.

Essa regra evita alterações silenciosas na estrutura do squad e mantém Officer
como uma responsabilidade explícita do jogador.

## Persistência e compatibilidade

O sistema reutiliza o campo persistente de Captain já existente no
perfil, sem reroll de identidade e sem promover retroativamente guardas de saves
existentes.

A escrita passa por um serviço dedicado de promoção, sem expor edição
arbitrária do perfil para integrações. Eventos isolados de promoção e demissão
permitem integrações futuras sem acoplar outros mods à persistência
interna.

## Interface

A ação fica associada ao perfil individual do guarda no Muster Roll, com custo
e impedimentos derivados do snapshot autoritativo do servidor. O controle atual
é funcional e deliberadamente mínimo; o fluxo visual definitivo será definido
na revisão mais ampla da GUI, sem alterar as regras de servidor descritas acima.

## Configuração inicial

Opções implementadas:

```text
progression.captainPromotionEnabled
progression.captainPromotionMinimumLevel
economy.captainPromotionCost
```

O custo padrão é de **4 unidades** de `economy.hireItem`; o nível mínimo padrão
é **Level 7**. A promoção pode ser desativada sem bloquear a demissão segura de
Capitães existentes.

---

# Atualização concluída — Additional Formations

O Pergaminho do Rali agora permite escolher a geometria usada pelos guardas
convocados, preservando Escolta como padrão compatível com itens antigos.

Formações implementadas:

```text
Escort
Column
Line
Wedge
Square
Circle
Shield Wall
```

O clique direito alterna a seleção e `Shift + clique direito` continua
ativando ou encerrando o Rally. A escolha fica persistida em cada pergaminho e
pode reorganizar um Rally ativo imediatamente.

Todas as geometrias reutilizam as configurações de espaçamento existentes e o
mesmo fluxo de posição segura, navegação, retorno e teleporte corretivo. O
combate continua tendo prioridade enquanto o guarda estiver dentro das regras
de engajamento; ao reagrupar, ele retorna ao slot da formação selecionada.

Após validação em jogo, o Quadrado foi refinado como uma grade rígida e o
Círculo recebeu raio mínimo configurável de 3,25 blocos, mantendo sua unidade
frontal adiante do comandante. O deslocamento das formações passou a usar
navegação contínua e uma pequena antecipação enquanto o comandante anda, com
recuperação moderada para corrida e corrida com salto. A ordem normal de seguir
mantém a IA nativa e recebe assistência apenas quando o comandante corre e o
guarda fica para trás; montarias não ativam essa assistência.

Pequenos desvios laterais do comandante agora são absorvidos por uma margem
configurável de 1,25 blocos. Ao ultrapassá-la, a formação realinha sua direção
gradualmente em vez de girar todos os slots de forma brusca.

Durante a marcha, guardas próximos combinam a velocidade do comandante com uma
correção posicional pequena e contínua. Guardas separados ainda usam o
pathfinding para retornar, preservando terreno, obstáculos e a geometria da
formação sem projetar os primeiros slots sobre o jogador.

Esta etapa não adiciona bônus de combate ou comportamento especial por
formação. Ela estabelece uma base determinística e testável para futuras ordens
táticas e para a revisão posterior da interface.

---

# Implementação concluída para 1.4.0 — Formation Selection Hub

O ciclo sequencial do Pergaminho do Rali será substituído por um seletor visual
compacto, permitindo acessar qualquer formação diretamente sem atravessar todas
as opções intermediárias.

## Interação

- clique direito abre o seletor sem pausar o mundo;
- `Shift + clique direito` continua ativando ou encerrando o Rally;
- uma faixa central exibe Escolta, Coluna, Linha, Cunha, Quadrado, Círculo e
  Muralha de Escudos;
- a formação atual recebe destaque dourado e a opção sob o cursor exibe seu
  nome traduzido;
- clique esquerdo confirma e fecha; `Esc` fecha sem alterar a seleção;
- selecionar durante um Rally ativo reorganiza os guardas imediatamente.

## Segurança e compatibilidade

A tela será apenas a camada de interação. A escolha continuará autoritativa no
servidor, que deverá validar o jogador, a mão, o Pergaminho do Rali e o
identificador da formação antes de persistir a alteração. Pergaminhos antigos
ou sem componente continuarão usando Escolta como padrão.

O seletor deverá se adaptar a escalas menores de GUI, oferecer nomes traduzidos
e preservar navegação por teclado e narração sempre que os componentes nativos
permitirem.

## Etapas

- [x] conjunto original de sete ícones 21×21 com fundo transparente;
- [x] gerador determinístico e validação dos recursos;
- [x] tela nativa e layout responsivo;
- [x] abertura pelo clique direito e preservação do atalho com `Shift`;
- [x] pacote de seleção e validação autoritativa no servidor;
- [x] atualização ao vivo de um Rally ativo;
- [x] traduções, acessibilidade e testes automatizados;
- [ ] smoke test em jogo (roteiro documentado).

---

# Atualizações futuras

As versões abaixo são direções planejadas.

Nomes, números de versão, conteúdo e ordem ainda podem mudar.

---

# Tactical Orders

O sistema de ordens poderá ser expandido para transformar o Rally em uma camada tática mais completa.

## Move Here

Implementação concluída; smoke test em jogo pendente.

Permite mandar:

- todos os guardas;
- um squad;
- uma categoria;

até a posição indicada pelo jogador. O Livro de Comando combina o escopo atual
com `Todos`, `Infantaria` ou `Arqueiros`; o próximo clique direito escolhe a
âncora e o servidor distribui slots seguros determinísticos. A ordem persiste
entre reloads, pausa durante combate e termina em `Aguardando`.

Exemplo:

```text
Move Squad Here
```

## Defend Area

Implementação concluída; smoke test em jogo pendente.

Exemplo:

```text
Defend Area
Radius: 16 blocks
```

O Livro de Comando combina o escopo atual com `Todos`, `Infantaria` ou
`Arqueiros`; o próximo clique escolhe o centro e o servidor distribui postos
seguros determinísticos. O raio é configurável, com padrão de 16 blocos.

Patrulha, percepção e combate permanecem nativos do Guard Villagers. Alvos fora
do território são abandonados e guardas que ultrapassam a margem retornam ao
posto sem teleporte. A ordem persiste entre reloads e é diferente da patrulha
fixa porque protege uma região limitada e pode ser substituída pelo Rally.

---

# Combat Stances

Implementação e testes automatizados concluídos; smoke test em jogo pendente.

O painel de combate aplica a postura a todos os guardas carregados do escopo
atual. A escolha é persistida individualmente e continua ativa junto das ordens
de movimento, patrulha, rota e defesa territorial.

### Defensive

É o padrão compatível: não interfere na percepção e reação nativas do Guard
Villagers.

### Aggressive

Procura e enfrenta monstros hostis dentro de um raio configurável.

### Passive

Abandona o alvo atual, não inicia combate e não recebe ordens explícitas de
ataque.

### Hold Position

Pode combater, mas abandona perseguições longas e retorna por pathfinding à
posição em que recebeu a postura, sem teleporte.

### Protect Commander

Prioriza criaturas que estejam atacando o jogador comandante dentro de um raio
configurável.

O servidor revalida escopo, propriedade e elegibilidade por guarda. Aquisição
proativa nunca escolhe aliados protegidos, usa distância e UUID para desempate e
processa somente entidades carregadas na dimensão atual. O contrato público está
na API 8 com `setCombatStance` e `SET_COMBAT_STANCE`.

---

# Quick Command / Radial Menu

Implementação e testes automatizados concluídos; smoke test em jogo pendente.

O Commander's Ledger continua sendo a interface completa.

Uma tecla `G` remapeável abre uma interface rápida para facilitar ordens durante
combate. A primeira versão atua em todos os guardas carregados e reutiliza os
fluxos autoritativos existentes.

Menu radial implementado:

```text
             FOLLOW

      PATROL        ATTACK

      DEFEND          MOVE

              WAIT
```

Seguir, Aguardar e Patrulhar enviam a ordem coletiva imediatamente. Atacar abre
a seleção de combate existente; Mover e Defender abrem a seleção de categoria
e reutilizam a sessão de próximo clique no mundo. O painel não pausa o jogo e
usa botões nativos para foco, teclado e narração.

---

# Named Routes

O sistema de rotas evoluiu para modelos reutilizáveis persistentes por jogador.

Exemplos:

```text
Village Walls
Gate Patrol
Farm Route
Castle Interior
Northern Road
```

Em vez de a rota existir exclusivamente no guarda, ela pode ser salva como uma
entidade lógica reutilizável com UUID, nome, dimensão, espera e pontos.

Vários guardas carregados podem receber e iniciar a mesma rota. Atualizar o
modelo sincroniza as cópias carregadas; excluir pausa e desvincula guardas
carregados, e referências descarregadas são reparadas de forma preguiçosa.

Exemplo:

```text
Village Walls

Assigned Guards:
✓ Robert
✓ Henry
✓ William
□ Arthur
```

O Ledger oferece um hub entre a rota individual e as Rotas Salvas. O gerenciador
reutiliza o padrão de duas colunas, mostra a associação atual e confirma trocas
entre modelos. Rotas individuais `v1` continuam compatíveis e migram para `v2`
sem associação inventada.

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
Squads & Officers
      |
      v
Captain Promotion & Demotion
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
- [x] **Guard Profiles & Progression**
- [x] Profile Core — persistência versionada, migração e API somente leitura
- [x] Natural Identity — qualidade, estrelas, rolls e origem de spawn
- [x] Guard levels
- [x] Guard ranks (Recruit → Guardsman → Soldier → Veteran → Elite)
- [x] XP by damage contribution + Final Blow bonus
- [x] Combat statistics (kills / assists)
- [x] Natural Quality + star rating
- [x] Natural attribute variation
- [x] Advanced natural guards / rare natural levels
- [x] Attribute progression — modificadores idempotentes e configuráveis
- [x] Natural Captains
- [x] Dynamic hiring cost
- [x] Muster Roll profile GUI
- [x] Captains como Officers de Squad
- [x] **Squads & Officers**
- [x] **Captain Promotion & Demotion**
- [x] **Additional formations**
- [ ] **Formation Selection Hub (1.4.0)** — implementação concluída; smoke test em jogo pendente
- [ ] **Move Here (1.4.0)** — implementação e testes automatizados concluídos; smoke test em jogo pendente
- [ ] **Defend Area (1.4.0)** — implementação e testes automatizados concluídos; smoke test em jogo pendente
- [ ] **Combat Stances (1.4.0)** — implementação e testes automatizados concluídos; smoke test em jogo pendente
- [ ] **Quick Command / Radial Menu (1.4.0)** — implementação e testes automatizados concluídos; smoke test em jogo pendente
- [ ] **GUI Usability Overhaul (1.4.0)** — Ledger contextual, Pelotões em duas colunas, Villager Names obrigatório e Comando Rápido compacto em grade concluídos; smoke test em jogo pendente
- [ ] **Named Routes (1.4.0)** — implementação e testes automatizados concluídos; smoke test em jogo pendente
- [ ] **Rally Point / Command Post (1.4.0)** — block, ownership, persistent settings, territorial formations, two-column assignment GUI and automated tests complete; in-game smoke test pending
- [ ] **Global Guard Registry (1.4.0)** — persistent last-known records, lifecycle reconciliation, paged Ledger view, diagnostics and automated tests complete; in-game smoke test pending
- [ ] Raid integration
- [ ] Multiplayer shared command
- [ ] Extended weapon compatibility
- [ ] Additional loaders and Minecraft versions

---

*This roadmap is intentionally flexible. Features, names, balancing values, version numbers and implementation order may change during development.*
