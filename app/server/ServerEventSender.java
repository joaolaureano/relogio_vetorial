package app.server;

import java.io.IOException;
import java.util.Random;

import app.server.queue.QueueManager;
import app.socket.unicast.USocket;

public class ServerEventSender {
    static USocket unicastSocket;
    static QueueManager queueManager;
    static int minDelay, maxDelay;
    static double chance;
    static int events;
    static Random random;

    public static void main(String[] args) throws IOException {

        chance = Double.parseDouble(args[0]);
        events = Integer.parseInt(args[1]);

        minDelay = Integer.parseInt(args[2]);
        maxDelay = Integer.parseInt(args[3]);

        //int delay = new Random();
        System.out.println("STARTED");
        while (true) {
            try {

            } catch (Exception e) {
            }
        }
    }

}
