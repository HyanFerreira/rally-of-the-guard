# Melhorias Técnicas

Este documento registra o estado das recomendações técnicas do **Rally of the
Guard 1.4.0**. Ele separa correções já implementadas de validações manuais e
melhorias que continuam abertas.

## Itens resolvidos

### Build e inicialização comum

- O projeto usa Java 25, sem `org.gradle.java.home` versionado.
- MidnightLib é resolvida pelo repositório declarado no Gradle; não depende de
  um JAR local ignorado pelo Git.
- O workflow Linux configura Java 25, valida o wrapper, executa `build` e publica
  os artefatos.
- `ModScreenHandlers.init()` é chamado no inicializador comum, antes de qualquer
  interação de contratação em cliente ou servidor dedicado.

Evidência local: `./gradlew.bat build` concluiu com sucesso antes deste ciclo de
hardening. A execução remota do GitHub Actions continua sendo evidência externa,
não substituída pelo teste local.

### Segurança das rotas individuais

- O codec rejeita contagens negativas ou acima do limite absoluto de 10 antes
  de alocar a lista.
- O receiver aceita apenas ações conhecidas e rejeita a requisição inteira sem
  alteração parcial.
- O limite configurado também é validado; não há truncamento silencioso.
- Pontos precisam estar dentro da altura de construção, da borda do mundo e a
  no máximo 8 blocos do jogador quando são novos.
- Pontos já armazenados podem ser preservados, renomeados ou removidos mesmo
  quando estão distantes.

Cobertura: `GuardRouteUpdatePayloadTest`, `GuardRouteUpdatePolicyTest`,
`GuardRoutesTest` e contratos de recursos.

### Participação e ciclo de vida do Rally

- Guardas em `Aguardar`, rota ativa ou patrulha fixa comum não entram no Rally.
- Defensores territoriais continuam elegíveis à substituição temporária já
  prevista pela regra de Defend Area.
- Encerrar o Rally libera somente guardas do comandante marcados como
  participantes; seguidores independentes são preservados.
- Uma sessão transitória e limitada no servidor coordena efeito, âncora,
  participantes e apresentação `ACTIVE` do pergaminho.
- Toggle normal, perda do efeito, morte, troca de dimensão, desconexão e login
  convergem pela mesma limpeza idempotente, sem forçar chunks.

Cobertura: políticas puras de participação/limpeza e GameTest de isolamento
entre participante e seguidor independente.

### Contratação, metadata e resíduos

- A tela e o serviço de contratação revalidam entidade, vida, propriedade,
  dimensão e distância máxima de 8 blocos antes do pagamento.
- Homepage, fontes e issues apontam para o projeto real.
- `fabric.mod.json`, `LICENSE` e README concordam em `CC0-1.0`.
- O placeholder `ACTION_ROUTE_PLACEHOLDER` e as traduções `route_soon` foram
  removidos e protegidos por teste de contrato.
- O projeto possui uma suíte JUnit, GameTests de servidor e GameTests de cliente,
  reunidos pela tarefa `automatedSmokeTest`.

## Itens ainda abertos

### Validação manual da versão 1.4.0

Os testes automatizados não aprovam UX, navegação prolongada, multiplayer,
raids completas nem persistência após reabrir um mundo. O roteiro consolidado
continua com resultado geral `PENDENTE` e deve ser concluído pelo testador:

- `docs/testing/26.1-1.4.0-remaining-smoke-test.md`;
- `docs/testing/26.1-1.4.0-smoke-test-observations.md`.

### Compatibilidade da JVM de desenvolvimento

O build exige Java 25, mas o runtime de teste atualmente informa que o subsistema
Mixin ativo declara suporte até Java 22. Os testes seguem passando, porém o aviso
deve ser acompanhado nas atualizações de Fabric Loader/Loom/Mixin.

### Limpeza residual e observabilidade

- Remover ou passar a usar `ModScreensClient`, que duplica o registro feito no
  inicializador do cliente.
- Revisar constantes duplicadas somente depois de confirmar consumidores de
  integração.
- Adicionar logs `debug` de transições e rejeições relevantes, sem emitir dados a
  cada tick.
- Considerar rate limiting de atualizações de rota se abuso de pacotes for
  observado em servidores públicos; limites de tamanho, ação e coordenadas já
  são aplicados.

### Cobertura complementar

Ainda são úteis cenários automatizados mais profundos para navegação real,
reconexão, troca de dimensão com jogador real, multiplayer e servidor dedicado.
Esses testes complementam, mas não substituem, o smoke test manual.

## Checklist de entrega

- [x] Build sem caminho absoluto de JDK ou dependência local obrigatória.
- [x] Workflow Linux configurado para Java 25.
- [x] `ScreenHandler` registrado no inicializador comum.
- [x] Payloads de rota limitados e validados no servidor.
- [x] Guardas aguardando, em rota ou patrulha fixa comum não entram no Rally.
- [x] Encerrar o Rally afeta somente participantes marcados.
- [x] Sessão, efeito, âncora, tags e brilho convergem por limpeza centralizada.
- [x] Contratação exige proximidade e entidade válida.
- [x] Metadata e licença estão consistentes.
- [x] Placeholder de rota obsoleto foi removido.
- [x] Fluxos críticos possuem cobertura automatizada.
- [ ] `automatedSmokeTest` final executado após todas as mudanças deste ciclo.
- [ ] Smoke test manual consolidado concluído e resultado geral aprovado.
- [ ] Aviso Java 25/Mixin eliminado ou aceito com versão compatível documentada.
