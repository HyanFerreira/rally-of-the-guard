# Rally of the Guard

**Rally of the Guard** e uma expansao tática leve para o
[Guard Villagers (Fabric/Quilt)](https://www.curseforge.com/minecraft/mc-mods/guard-villagers-fabric).

O objetivo do mod e transformar guardas contratados em um esquadrao comandavel, sem descaracterizar a IA original do
Guard Villagers. O jogador pode contratar guardas, convocar unidades, definir posturas, criar patrulhas fixas e montar
rotas de patrulha em loop usando o Livro de Comando.

---

## Versao Atual

### 1.3.0

Principais mudancas:

- Adicionadas posturas no Livro de Comando: seguir, aguardar, patrulhar e rota.
- Adicionada tela de rota de patrulha.
- Rotas suportam ate 5 pontos.
- Cada ponto usa a posicao atual do jogador como referencia.
- Guardas em rota aguardam um tempo configuravel antes de seguir para o proximo ponto.
- Rotas usam o comportamento nativo de patrulha do Guard Villagers.
- Dados da rota ficam salvos no proprio guarda e sobrevivem ao reload do mundo.
- O Pergaminho do Rali respeita guardas em patrulha ou aguardando.

---

## Funcionalidades

### Contratacao de Guardas

- Clique com o botao direito em um guarda sem dono para abrir a tela de contratacao.
- O custo padrao e de **3 esmeraldas**.
- Guardas contratados ficam vinculados ao UUID do jogador.
- Guardas contratados recebem nome dourado e mensagem de apresentacao.
- Guardas contratados podem seguir o jogador sem depender do efeito Hero of the Village.

### Livro de Comando

O **Livro de Comando** abre um painel portatil para gerenciar guardas contratados.

Pelo painel, o jogador pode:

- Ver guardas contratados carregados no mundo atual.
- Ver o estado atual de cada guarda.
- Convocar um guarda para perto do jogador.
- Mandar um guarda seguir.
- Mandar um guarda aguardar.
- Definir uma patrulha fixa na posicao atual.
- Parar uma patrulha.
- Criar, iniciar, pausar, limpar e editar rotas de patrulha.

Estados exibidos:

- `Ocioso`
- `Seguindo`
- `Aguardando`
- `Em patrulha`
- `Em rota`

### Rotas de Patrulha

Rotas de patrulha permitem que um guarda percorra varios pontos em loop.

Regras atuais:

- Maximo de 5 pontos por rota.
- Minimo de 2 pontos para iniciar.
- Cada ponto pode ser definido usando a posicao atual do jogador.
- O tempo de espera por ponto pode ser ajustado na tela da rota.
- Ao chegar em um ponto, o guarda aguarda o tempo configurado e depois recebe o proximo ponto como `patrolPos`.
- A rota continua em loop ate ser pausada ou limpa.
- Outras ordens, como seguir ou aguardar, pausam a rota sem apagar os pontos.
- Somente o botao `Limpar` apaga os pontos da rota.

### Pergaminho do Rali

O **Pergaminho do Rali** convoca guardas contratados proximos para perto do jogador.

- `Shift + botao direito` ativa ou encerra o rali.
- Guardas convocados sao teleportados para perto do jogador e passam a seguir.
- Guardas em patrulha nao sao puxados.
- Guardas aguardando nao sao puxados.
- Durante o rali, guardas contratados ficam protegidos contra dano causado pelo proprio comandante.

### Patrulha Fixa

A patrulha fixa usa o comportamento nativo do Guard Villagers:

- O jogador define a posicao atual como ponto de patrulha.
- O guarda caminha ate o ponto e guarda aquela posicao.
- Se encontrar mobs hostis, a IA normal do guarda continua funcionando.

### Friendly Fire e Neutralidade

- Guardas em rali ficam protegidos contra ataques do jogador dono.
- Ao atacar um guarda que nao pertence ao jogador, os guardas contratados do jogador permanecem neutros.

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

- **Minecraft:** 1.21.11
- **Loader:** Fabric
- **Java:** 21 ou superior
- **Fabric API**
- **Guard Villagers (Fabric/Quilt)**

---

## Observacoes Tecnicas

- Comandos funcionam apenas no mundo/dimensao onde o guarda esta carregado.
- Guardas em chunks descarregados nao processam rota ate serem carregados novamente.
- Rotas sao salvas no guarda usando tags persistentes.
- O sistema de rota atual nao substitui a IA do Guard Villagers; ele apenas atualiza dinamicamente o `patrolPos`.
- A protecao contra friendly fire se aplica ao contexto de rali.

---

## Licenca

Este mod esta disponivel sob a licenca **CC0-1.0**.

---

## Links

- GitHub: https://github.com/HyanFerreira/rally-of-the-guard
- Perfil do autor: https://github.com/HyanFerreira
