# Relógio Vetorial

**[Read this in English / Leia em inglês](README.md)**

## Sobre o projeto

Este projeto implementa um **relógio vetorial (vector clock)** distribuído em Java, usado para determinar a ordem causal de eventos em um sistema distribuído sem depender de um relógio global sincronizado.

Cada processo (servidor) roda em uma porta própria e se comunica com os demais via UDP:

- Um **socket multicast** (`MSocket`) é usado para "destravar" (unlock) todos os servidores ao mesmo tempo, através do `ServerManager`.
- Um **socket unicast** (`USocket`) é usado para trocar mensagens de evento (`EVENT`) e confirmação (`ACK`) entre processos vizinhos.
- Cada servidor dispara, periodicamente, um evento **local** (incrementa apenas sua própria posição no vetor) ou um evento **remoto** (envia seu relógio para um vizinho, que mescla o vetor recebido com o seu — tomando o máximo componente a componente — e incrementa sua própria posição).
- Cada processo tem um número máximo de eventos configurado; ao atingi-lo, o processo registra o estado final do relógio e encerra.

O objetivo é observar, através dos logs gerados, como o relógio vetorial captura corretamente as relações de causa e efeito ("happened-before") entre eventos distribuídos.

## Contexto

Este é um **trabalho acadêmico**, desenvolvido como parte da disciplina de **Sistemas Distribuídos** na faculdade. O código foi escrito com foco em aprendizado dos conceitos de sincronização de relógios lógicos (relógios de Lamport / relógios vetoriais), comunicação via sockets (multicast/unicast) e concorrência (threads) em Java — não em uso em produção.

## Estrutura do projeto

```
app/
  server/
    Server.java          # Processo principal: inicializa listener e sender
    ServerListener.java  # Thread que escuta eventos/ACKs recebidos
    ServerSender.java    # Thread que dispara eventos locais/remotos periodicamente
    ServerManager.java   # Envia o pacote multicast "SETUP" para destravar os servidores
    ServerSetup.java     # Lê um arquivo de cenário e inicializa um servidor com seus dados
    clock/
      ClockManager.java  # Implementação do relógio vetorial (singleton por processo)
    event/
      EventManager.java  # Orquestra eventos locais, remotos e recebidos
    sleeper/
      Sleeper.java        # Utilitário de delay aleatório entre eventos
  socket/
    multicast/MSocket.java  # Wrapper de MulticastSocket
    unicast/USocket.java    # Wrapper de DatagramSocket
scenarios/                # Arquivos de configuração de cenários de teste
setup.sh                  # Script auxiliar para abrir um terminal por servidor
Makefile                  # Compilação das classes principais
```

Cada linha de um arquivo em `scenarios/` representa um servidor:

```
<id> <host> <porta> <chance de evento remoto (%)> <nº de eventos> <delay mínimo> <delay máximo>
```

## Como rodar

Requer JDK (testado com Java 21) instalado.

```bash
# Compilar todas as classes
find app -name "*.java" | xargs javac

# Iniciar os processos descritos em um cenário (usa gnome-terminal)
./setup.sh scenarios/scenario_01.txt
```

O `setup.sh` abre um terminal por linha do cenário executando `ServerSetup`, aguarda alguns segundos e então dispara o `ServerManager`, que envia o pacote multicast que destrava todos os servidores simultaneamente.

## Versões deste repositório

- **[`V1`](../../tree/V1)** — snapshot do projeto exatamente como foi entregue/desenvolvido originalmente na faculdade, sem nenhuma correção. Preservado como tag para referência histórica.
- **Versão atual (branch `main`)** — a partir do original, foram identificados e corrigidos alguns bugs de concorrência/protocolo (ver seção abaixo). Use esta versão se quiser um comportamento mais correto do protocolo; use a tag `V1` se quiser ver o código exatamente como foi entregue.

## Bugs identificados e corrigidos

Durante a análise do código original foram encontrados os seguintes problemas:

### 1. Evento remoto recebido era descartado sem ACK quando o orçamento local de eventos acabava (crítico)

Em `ServerListener.run()`, ao receber um pacote `EVENT`, o código chamava `eventManager.decreaseEvent()` **antes** de processar o evento e **antes** de enviar o `ACK`. Se o contador de eventos do processo receptor já estivesse zerado, o processo encerrava (`System.exit(0)`) imediatamente:

- sem aplicar o merge do relógio vetorial recebido (quebrando a garantia de causalidade que o relógio vetorial deveria preservar);
- sem responder com `ACK` ao processo remetente.

Isso fazia o remetente (`EventManager.remote`) travar até o timeout do socket, retornar falha por "time-out" e, por sua vez, encerrar o processo remetente de forma incorreta — um efeito cascata de encerramentos indevidos, mesmo quando não havia nenhuma falha real de rede.

**Correção:** o evento recebido agora é sempre processado (merge do relógio) e confirmado (`ACK`) antes de verificar se o orçamento de eventos do processo local acabou. O processo só encerra depois de ter tratado corretamente o evento recebido.

### 2. `MSocket.close()` fechava o socket antes de sair do grupo multicast

O método chamava `datagramSocket.close()` e, em seguida, `datagramSocket.leaveGroup(...)`. Como o socket já estava fechado, a chamada a `leaveGroup` lançava uma exceção (capturada e apenas logada), então o grupo multicast nunca era efetivamente abandonado de forma limpa.

**Correção:** a ordem foi invertida — primeiro sai do grupo multicast (`leaveGroup`), depois fecha o socket, usando um bloco `finally` para garantir o fechamento mesmo se `leaveGroup` falhar.

### 3. Artefatos de build (`*.class`) e saída do Javadoc versionados

O repositório original tinha arquivos `.class` compilados e a pasta `outputdir/` (Javadoc gerado) commitados, e que ficavam dessincronizados do código-fonte a cada alteração. Foi adicionado um `.gitignore` e os artefatos de build foram removidos do controle de versão — eles devem ser gerados localmente com `javac` / `javadoc` quando necessário.

## Aviso

Este é um projeto acadêmico/educacional. O protocolo assume comunicação em `localhost` e não foi desenhado para ambientes de produção (não há tratamento de segurança, perda de pacotes além do timeout simples, ou reconfiguração dinâmica de topologia).
