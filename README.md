# Vector Clock (Relógio Vetorial) — V1 (original version)

**[Leia em português / Read this in Portuguese](README.pt-BR.md)**

A distributed **vector clock** in Java. Each process (server) runs on its own
port: a **multicast** socket (`MSocket`) unlocks all servers at once, and a
**unicast** socket (`USocket`) carries `EVENT` and `ACK` messages between
neighbours. Each server fires local events (increments its own position) or
remote events (sends its clock to a neighbour, which merges component-wise
and increments its own position) until it reaches its event budget, and the
logs show how the vector clock captures "happened-before" relations.

Built as an assignment for a college **Distributed Systems** course.

> **This is the archived original version**, exactly as it was submitted.
> The only change on top of it is this README. The fixed version lives on the
> [`main`](../../tree/main) branch.

## Running

Requires a JDK, Python 3 and `gnome-terminal`.

```bash
make                                         # compiles the classes
python3 setup.py scenarios/scenario_01.txt   # one terminal per server, then unlocks them
```

Each line in a `scenarios/` file describes one server:

```
<id> <host> <port> <remote event chance (%)> <number of events> <min delay> <max delay>
```

## Known issues in this version

These were found later and are fixed on `main`:

1. A received remote event is dropped without an `ACK` when the receiver's
   event budget has run out, which breaks causality and makes the sender
   time out and exit as well.
2. `MSocket.close()` closes the socket before leaving the multicast group, so
   the group is never left cleanly.
3. Compiled `.class` files and the generated Javadoc (`outputdir/`) are
   committed.
