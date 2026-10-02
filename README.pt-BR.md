# Relógio Vetorial — V1 (versão original)

**[Read this in English / Leia em inglês](README.md)**

Um **relógio vetorial** distribuído em Java. Cada processo (servidor) roda na
sua própria porta: um socket **multicast** (`MSocket`) libera todos os
servidores ao mesmo tempo, e um socket **unicast** (`USocket`) leva mensagens
`EVENT` e `ACK` entre vizinhos. Cada servidor dispara eventos locais
(incrementa a própria posição) ou remotos (envia o relógio a um vizinho, que
faz o merge componente a componente e incrementa a própria posição) até
atingir seu limite de eventos, e os logs mostram como o relógio vetorial
captura as relações de "aconteceu antes".

Desenvolvido como trabalho da disciplina de **Sistemas Distribuídos** na
faculdade.

> **Esta é a versão original arquivada**, exatamente como foi entregue. A
> única mudança sobre ela é este README. A versão corrigida está na branch
> [`main`](../../tree/main).

## Executando

Requer um JDK, Python 3 e `gnome-terminal`.

```bash
make                                         # compila as classes
python3 setup.py scenarios/scenario_01.txt   # um terminal por servidor, depois os libera
```

Cada linha de um arquivo em `scenarios/` descreve um servidor:

```
<id> <host> <porta> <chance de evento remoto (%)> <número de eventos> <atraso mín> <atraso máx>
```

## Problemas conhecidos desta versão

Foram encontrados depois e estão corrigidos na `main`:

1. Um evento remoto recebido é descartado sem `ACK` quando o limite de
   eventos de quem recebe já acabou, o que quebra a causalidade e faz o
   remetente estourar o timeout e encerrar também.
2. `MSocket.close()` fecha o socket antes de sair do grupo multicast, então o
   grupo nunca é deixado de forma limpa.
3. Arquivos `.class` compilados e o Javadoc gerado (`outputdir/`) estão
   versionados.
