package app.server;

import java.io.IOException;
import java.util.Random;

import app.server.queue.QueueManager;
import app.socket.unicast.USocket;

public class ServerEventSender extends Thread {
    protected USocket unicastSocket;
    protected QueueManager queueManager;
    protected int minDelay, maxDelay;
    protected double chance;
    protected int events;
    protected Random random;

    ServerEventSender(int port, double chance, int events, int minDelay, int maxDelay) {
        unicastSocket = new USocket(port);
        this.chance = chance;
        this.events = events;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;
        // queueManager = QueueManager.getInstance(maxDelay)
    }

    public void run() {

        System.out.println("STARTED");
        while (true) {
            try {
                // int delay = new Random();

            } catch (Exception e) {
            }
        }
    }

}
