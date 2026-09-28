# Vector Clock (Relógio Vetorial)

**[Leia em português / Read this in Portuguese](README.md)**

## About the project

This project implements a distributed **vector clock** in Java, used to determine the causal ordering of events in a distributed system without relying on a synchronized global clock.

Each process (server) runs on its own port and communicates with the others over UDP:

- A **multicast socket** (`MSocket`) is used to "unlock" all servers at the same time, via `ServerManager`.
- A **unicast socket** (`USocket`) is used to exchange `EVENT` and `ACK` messages between neighboring processes.
- Each server periodically fires either a **local** event (increments only its own position in the vector) or a **remote** event (sends its clock to a neighbor, which merges the received vector with its own — taking the component-wise maximum — and increments its own position).
- Each process has a configured maximum number of events; once reached, the process logs the final clock state and terminates.

The goal is to observe, through the generated logs, how the vector clock correctly captures the "happened-before" causal relationships between distributed events.

## Context

This is an **academic assignment**, developed as part of a **Distributed Systems** college course. The code was written to learn the concepts of logical clock synchronization (Lamport clocks / vector clocks), socket-based communication (multicast/unicast), and concurrency (threads) in Java — it is not intended for production use.

## Project structure

```
app/
  server/
    Server.java          # Main process: initializes listener and sender
    ServerListener.java  # Thread that listens for incoming events/ACKs
    ServerSender.java    # Thread that periodically fires local/remote events
    ServerManager.java   # Sends the multicast "SETUP" packet to unlock the servers
    ServerSetup.java     # Reads a scenario file and initializes a server with its data
    clock/
      ClockManager.java  # Vector clock implementation (singleton per process)
    event/
      EventManager.java  # Orchestrates local, remote, and received events
    sleeper/
      Sleeper.java        # Random delay utility between events
  socket/
    multicast/MSocket.java  # MulticastSocket wrapper
    unicast/USocket.java    # DatagramSocket wrapper
scenarios/                # Test scenario configuration files
setup.py                  # Helper script to spawn one terminal per server
Makefile                  # Build for the main classes
```

Each line in a `scenarios/` file represents one server:

```
<id> <host> <port> <remote event chance (%)> <number of events> <min delay> <max delay>
```

## How to run

Requires a JDK (tested with Java 21).

```bash
# Compile all classes
find app -name "*.java" | xargs javac

# Start the processes described in a scenario (uses gnome-terminal)
python3 setup.py scenarios/scenario_01.txt
```

`setup.py` opens one terminal per scenario line running `ServerSetup`, waits a few seconds, then triggers `ServerManager`, which sends the multicast packet that unlocks all servers simultaneously.

## Versions in this repository

- **[`original-v1.0`](../../tree/original-v1.0)** — snapshot of the project exactly as it was originally submitted/developed for the college course, with no fixes applied. Preserved as a branch for historical reference.
- **Current version (this branch)** — starting from the original, a few concurrency/protocol bugs were identified and fixed (see below). Use this version for more correct protocol behavior; use the `original-v1.0` branch to see the code exactly as it was submitted.

## Identified and fixed bugs

The following issues were found while reviewing the original code:

### 1. Received remote event was dropped without an ACK once the local event budget ran out (critical)

In `ServerListener.run()`, when an `EVENT` packet was received, the code called `eventManager.decreaseEvent()` **before** processing the event and **before** sending the `ACK`. If the receiving process's event counter had already reached zero, the process would immediately exit (`System.exit(0)`):

- without applying the vector-clock merge for the received event (breaking the causality guarantee the vector clock is supposed to provide);
- without replying with an `ACK` to the sending process.

This made the sender (`EventManager.remote`) block until the socket timeout, report a false "time-out" failure, and in turn terminate the sending process incorrectly — a cascade of spurious shutdowns even though no real network failure had occurred.

**Fix:** the received event is now always processed (clock merge) and acknowledged (`ACK`) before checking whether the local process's event budget has run out. The process only exits after correctly handling the received event.

### 2. `MSocket.close()` closed the socket before leaving the multicast group

The method called `datagramSocket.close()` and only then `datagramSocket.leaveGroup(...)`. Since the socket was already closed, the `leaveGroup` call threw an exception (caught and just logged), so the multicast group was never actually left cleanly.

**Fix:** the order was swapped — the socket now leaves the multicast group (`leaveGroup`) first, then closes, using a `finally` block to guarantee the socket is closed even if `leaveGroup` fails.

### 3. Compiled build artifacts (`*.class`) and Javadoc output were committed

The original repository had compiled `.class` files and the generated Javadoc output (`outputdir/`) committed, which drifted out of sync with the source on every change. A `.gitignore` was added and the build artifacts were removed from version control — they should be generated locally with `javac` / `javadoc` when needed.

## Disclaimer

This is an academic/educational project. The protocol assumes communication over `localhost` and was not designed for production environments (no security handling, no packet-loss handling beyond a simple timeout, no dynamic topology reconfiguration).
