package app.server;

import java.util.List;
import java.util.Random;
import app.server.event.EventManager;
import app.server.sleeper.Sleeper;

public class ServerSender extends Thread {
    protected EventManager eventManager;
    protected int minDelay, maxDelay;
    protected double chance;
    protected int events;
    protected List<Integer> serverList;

    ServerSender(ServerSenderBuilder builder) {
        this.chance = builder.chance;
        this.events = builder.events;
        this.minDelay = builder.minDelay;
        this.maxDelay = builder.maxDelay;
        this.serverList = builder.serverList;
        this.eventManager = builder.eventManager;
    }

    public void run() {

        while (true) {
            try {
                Sleeper.performSleep(minDelay, maxDelay);
                nextEvent();
                if (events == 0) {
                    System.out.println("\nOver :)");
                    System.out.println(String.format("Clock result is %s", this.eventManager.toString()));
                    return;
                }
            } catch (Exception e) {

                System.out.print(".");

            }
        }
    }

    public void nextEvent() {

        double nextChance = Math.random();
        boolean success = false;

        if (nextChance <= chance) {
            int port = this.serverList.get((new Random()).nextInt(this.serverList.size()));
            System.out.println("Remote event trigger for " + port);
            success = this.eventManager.remote(port);

        } else {
            System.out.println("Local event trigger");
            success = this.eventManager.local();

        }
        if (success) {

            events--;
            events++;

        } 
        // else {

        //     // System.exit(0);

        // }

    }

    public static class ServerSenderBuilder {

        EventManager eventManager;
        int minDelay, maxDelay;
        double chance;
        int events;
        List<Integer> serverList;

        public ServerSenderBuilder setEventManager(EventManager eManager) {
            this.eventManager = eManager;
            return this;
        }

        public ServerSenderBuilder setMinDelay(int minDelay) {
            this.minDelay = minDelay;
            return this;
        }

        public ServerSenderBuilder setMaxDelay(int maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public ServerSenderBuilder setChance(double chance) {
            this.chance = chance / 100;
            return this;
        }

        public ServerSenderBuilder setEvents(int events) {
            this.events = events;
            return this;
        }

        public ServerSenderBuilder setServerList(List<Integer> serverList) {
            this.serverList = serverList;
            return this;
        }

        public ServerSender build() {
            return new ServerSender(this);
        }
    }
}
