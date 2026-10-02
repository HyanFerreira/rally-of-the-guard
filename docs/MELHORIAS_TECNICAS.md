# Melhorias Técnicas

Este documento reúne pontos de melhoria identificados na implementação atual do **Rally of the Guard**. O foco é tornar o projeto mais reproduzível, seguro e previsível em cliente, servidor integrado e servidor dedicado.

## Prioridade alta

### 1. Tornar o build reproduzível

#### Problema

O `gradle.properties` define `org.gradle.java.home` com um caminho absoluto de uma instalação local do JDK:

```properties
org.gradle.java.home=C:/Program Files/Eclipse Adoptium/jdk-21.0.11.10-hotspot
```

Esse caminho não é portátil e pode impedir o Gradle de iniciar em outras máquinas e no runner Linux do GitHub Actions.

Além disso, a MidnightLib só é adicionada ao classpath quando um arquivo local, ignorado pelo Git, está presente. Em um clone limpo, a compilação falha em `RallyConfig` porque `MidnightConfig` e `@Entry` não são encontrados.

#### Melhoria recomendada

- Remover `org.gradle.java.home` do `gradle.properties` versionado.
- Usar o `JAVA_HOME` do ambiente ou configurar Java Toolchains no Gradle.
- Declarar a MidnightLib como dependência resolvível por repositório, sem depender de um arquivo local extraído.
- Manter arquivos locais apenas como fallback opcional para desenvolvimento offline.
- Validar o projeto em um clone limpo com `./gradlew build`.

#### Critério de conclusão

O build deve funcionar no Windows, Linux e GitHub Actions sem caminhos ou arquivos exclusivos da máquina do desenvolvedor.

### 2. Registrar o ScreenHandler no inicializador comum

#### Problema

O `HIRE_HANDLER` é registrado durante a inicialização estática de `ModScreenHandlers`, mas `ModScreenHandlers.init()` não é chamado em `RallyOfTheGuard.onInitialize()`.

No cliente, a classe é carregada ao registrar `HireGuardScreen`. Em um servidor dedicado, o primeiro acesso pode acontecer apenas quando um jogador interagir com um guarda, possivelmente depois que o registry já estiver congelado.

#### Melhoria recomendada

- Chamar `ModScreenHandlers.init()` no inicializador comum.
- Manter no inicializador do cliente apenas a associação entre o handler e a tela gráfica.
- Testar contratação em servidor dedicado, não somente em mundo single-player.

#### Critério de conclusão

O tipo de tela deve estar registrado durante a inicialização do mod nos dois ambientes, antes de qualquer jogador interagir com um guarda.

### 3. Endurecer a validação dos pacotes de rota

#### Problema

`GuardRouteUpdateC2SPayload` lê do pacote a quantidade de pontos enviada pelo cliente e aloca/processa essa quantidade antes de aplicar `routeMaxPoints` no servidor.

Um cliente modificado também pode enviar coordenadas arbitrárias. Como a recuperação de rota pode teletransportar um guarda que ficou preso, isso permite usar uma rota forjada para deslocar um guarda para locais que não foram registrados pela interface normal.

#### Melhoria recomendada

- Rejeitar o pacote durante o decode ou imediatamente no receiver quando a quantidade for negativa ou superior ao limite absoluto suportado.
- Limitar o tamanho do payload antes de criar a lista.
- Validar cada coordenada no servidor.
- Definir uma regra de distância entre o jogador e o ponto adicionado.
- Considerar enviar apenas uma ação de “adicionar posição atual”, deixando o servidor capturar `player.getBlockPos()`.
- Validar explicitamente os códigos de ação e rejeitar valores desconhecidos.
- Adicionar rate limiting ou cooldown para atualizações de rota.

#### Critério de conclusão

Um cliente modificado não deve conseguir enviar listas excessivas nem criar pontos que seriam impossíveis pela interface oficial.

## Prioridade média

### 4. Fazer o rali respeitar a postura “Aguardar”

#### Problema

A seleção de guardas do Pergaminho do Rali ignora guardas patrulhando, mas não ignora guardas marcados com `GuardOrders.isWaiting()`.

Ao entrar no rali, `rallyGuardToPlayer()` executa `GuardOrders.setWaiting(guard, false)`. Portanto, um guarda em postura “Aguardar” pode ser convocado e perder sua ordem.

#### Melhoria recomendada

- Excluir guardas em espera ao montar a lista de participantes do rali.
- Centralizar a regra de elegibilidade em um método, por exemplo `canJoinRally(guard)`.
- Usar a mesma regra na formação, nas ordens de combate e na convocação inicial.

#### Critério de conclusão

Guardas em “Aguardar”, patrulha fixa ou rota devem manter sua ordem quando outro grupo for convocado.

### 5. Encerrar o rali somente para guardas participantes

#### Problema

Ao desligar o rali, o código percorre todos os guardas do jogador dentro de `combatGuardSearchRadius` e desativa `Following`, mesmo que determinado guarda não tenha participado daquele rali.

Isso pode interromper um guarda que estava seguindo normalmente por uma ordem independente.

#### Melhoria recomendada

- Filtrar a lista usando `GuardOrders.isRallied(guard)`.
- Limpar `Following` e a tag de rali apenas dos participantes.
- Considerar armazenar a associação entre guarda e comandante do rali quando houver cenários com troca de dono ou múltiplos estados.

#### Critério de conclusão

Encerrar o rali não deve alterar guardas em posturas que não foram criadas por ele.

### 6. Sincronizar o estado do efeito, do item e dos guardas

#### Problema

O estado visual do pergaminho fica no componente `ACTIVE`, enquanto o estado do jogador é representado por `RALLY_COMMANDER` e o estado dos guardas por command tags.

Essas três fontes podem divergir em situações como:

- morte ou desconexão do jogador;
- perda, troca ou duplicidade de pergaminhos;
- remoção do efeito por outra mecânica;
- reload do servidor;
- desativação da formação nas configurações durante um rali.

#### Melhoria recomendada

- Definir uma única fonte autoritativa para determinar se o rali está ativo.
- Tratar login, logout, morte, troca de dimensão e remoção inesperada do efeito.
- Fazer o brilho do item refletir o estado real, ou documentar que ele representa apenas o último uso daquele item.
- Criar uma rotina centralizada de início e encerramento do rali.

#### Critério de conclusão

Efeito, brilho e tags dos guardas devem convergir para o mesmo estado após qualquer transição relevante.

### 7. Validar distância na contratação

#### Problema

`HireGuardScreenHandler.canUse()` sempre retorna `true`. Depois que a tela é aberta, o jogador pode se afastar antes de confirmar a contratação.

#### Melhoria recomendada

- Verificar se o guarda ainda existe, está vivo, continua sem dono e permanece próximo ao jogador.
- Aplicar a mesma validação no clique do botão, pois o cliente não é uma fonte confiável.
- Fechar a tela quando a entidade deixar de ser válida.

#### Critério de conclusão

Uma contratação só deve ser concluída enquanto jogador e guarda estiverem em uma situação válida de interação.

## Manutenção e qualidade

### 8. Corrigir o metadata do mod

O `fabric.mod.json` ainda contém valores herdados do projeto de exemplo:

- homepage do Fabric;
- repositório `FabricMC/fabric-example-mod`;
- licença MIT, enquanto `LICENSE` e a documentação usam CC0-1.0;
- sugestão genérica de `another-mod`.

Recomenda-se atualizar os links, unificar a licença e remover entradas de exemplo.

### 9. Remover resíduos do template e código obsoleto

Revisar e remover, caso realmente não sejam usados:

- `ExampleMixin`;
- `ExampleClientMixin`;
- arquivos de configuração desses mixins;
- `ModScreensClient`, se o registro já é feito diretamente no inicializador do cliente;
- `ACTION_ROUTE_PLACEHOLDER`;
- traduções que ainda dizem que rotas serão adicionadas “em breve”;
- constantes de identificadores duplicadas que não são consumidas.

Isso reduz ruído e evita que código de exemplo seja interpretado como parte necessária da arquitetura.

### 10. Adicionar testes automatizados

O projeto não possui testes unitários ou GameTests. Os cenários mais valiosos para automatizar são:

- contratação com custo e item configuráveis;
- tentativa de contratar guarda já contratado;
- transições entre ocioso, seguindo, aguardando, patrulhando e em rota;
- persistência e parsing de rotas;
- limite de pontos e payloads inválidos;
- avanço, pausa, limpeza e loop de rotas;
- entrada e saída do rali;
- preservação de guardas aguardando ou patrulhando;
- proteção contra friendly fire;
- seleção de infantaria e arqueiros;
- registro e contratação em servidor dedicado.

Testes unitários podem cobrir serialização e regras de estado. GameTests ou testes de integração são mais adequados para entidades, navegação, registries e rede.

### 11. Melhorar observabilidade

Adicionar logs de nível `debug` para eventos importantes pode facilitar diagnóstico sem poluir o log normal:

- início e fim de rali;
- quantidade de guardas convocados ou ignorados;
- criação e mudança de ponto de rota;
- teleporte de recuperação;
- rejeição de payload inválido;
- falha ao localizar guarda ou proprietário.

Evitar registrar dados a cada tick; logs devem se concentrar em transições de estado e situações anormais.

## Ordem sugerida de execução

1. Corrigir JDK e dependências do build.
2. Garantir o registro comum do `ScreenHandler`.
3. Validar e limitar os payloads de rota.
4. Corrigir as regras de entrada e saída do rali.
5. Centralizar a máquina de estados do rali.
6. Validar contratação por distância e estado da entidade.
7. Limpar metadata, templates e código obsoleto.
8. Adicionar testes de regras e integração.

## Checklist de entrega

- [ ] Clone limpo compila somente com JDK 21 e acesso aos repositórios declarados.
- [ ] GitHub Actions executa o build sem caminhos locais.
- [ ] Contratação funciona em servidor dedicado.
- [ ] Payloads de rota possuem limites e validação no servidor.
- [ ] Guardas aguardando ou patrulhando não entram no rali.
- [ ] Encerrar o rali afeta somente guardas participantes.
- [ ] Estado do pergaminho, jogador e guardas permanece consistente.
- [ ] Contratação exige proximidade e entidade válida.
- [ ] Metadata e licença estão consistentes.
- [ ] Código de exemplo e placeholders foram removidos.
- [ ] Fluxos críticos possuem cobertura automatizada.
