package app.server;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

import app.server.queue.ClockManager;
import app.server.sleeper.Sleeper;
import app.socket.unicast.USocket;
import app.socket.unicast.USocket.USocketPayload;

public class ServerEventSender extends Thread {
    protected USocket unicastSocket;
    protected ClockManager queueManager;
    protected int minDelay, maxDelay;
    protected double chance;
    protected int events;
    protected List<Integer> serverList;

    ServerEventSender(int port, double chance, int events, int minDelay, int maxDelay, String serverList) {
        unicastSocket = new USocket(port);
        this.chance = chance;
        this.events = events;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;

        queueManager = ClockManager.getInstance(maxDelay);

        this.serverList = new ArrayList<Integer>();
        Stream.of(serverList.split(",")).map(Integer::valueOf).forEach(this.serverList::add);

    }

    public void run() {

        while (true) {
            try {
                Sleeper.performSleep(minDelay, maxDelay);
                nextEvent();

            } catch (Exception e) {
                System.out.print(".");

            }
        }
    }

    public void nextEvent() {
        double nextChance = Math.random();
        if (nextChance <= chance) {
            // remote_event()
        } else {
            // local_event()
        }
        events--;
    }

}
