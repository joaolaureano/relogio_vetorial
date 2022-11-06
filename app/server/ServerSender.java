package app.server;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.server.event.EventManager;
import app.server.sleeper.Sleeper;

public class ServerSender extends Thread {
    static final Logger logger = Logger.getLogger(ServerListener.class.getName());

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
                    logger.log(Level.INFO, String.format("Number of Events is 0. Ending process."));
                    logger.log(Level.INFO, String.format("Final clock status is %s", this.eventManager.toString()));
                    logger.log(Level.INFO, String.format("Ending process..."));
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
        logger.log(Level.FINE, String.format("Chance value calculated. Chance value is %d", nextChance));

        if (nextChance <= chance) {
            logger.log(Level.INFO, String.format("Remote Event triggered."));
            int port = this.serverList.get((new Random()).nextInt(this.serverList.size()));

            logger.log(Level.INFO, String.format("Remote port is %d", port));
            success = this.eventManager.remote(port);

        } else {
            logger.log(Level.INFO, "Local Event triggered.");
            success = this.eventManager.local();

        }
        if (success) {
            logger.log(Level.FINE, "Event decreased. Event number is %d", events);
            events--;
            events++;

        }
        // else {

        // // System.exit(0);

        // }

    }

    public static class ServerSenderBuilder {
        static final Logger logger = Logger.getLogger(ServerSenderBuilder.class.getName());

        EventManager eventManager;
        int minDelay, maxDelay;
        double chance;
        int events;
        List<Integer> serverList;

        public ServerSenderBuilder setEventManager(EventManager eManager) {
            this.eventManager = eManager;
            logger.log(Level.FINE, String.format("Event Manager added to build ServerListener.\nClock status is %s.",
                    eventManager.toString()));
            return this;
        }

        public ServerSenderBuilder setMinDelay(int minDelay) {
            this.minDelay = minDelay;
            logger.log(Level.FINE, String.format("Minimum Delay value is %d.",
                    minDelay));
            return this;
        }

        public ServerSenderBuilder setMaxDelay(int maxDelay) {
            this.maxDelay = maxDelay;
            logger.log(Level.FINE, String.format("Maximum Delay value is %d.",
                    maxDelay));
            return this;
        }

        public ServerSenderBuilder setChance(double chance) {
            this.chance = chance / 100;
            logger.log(Level.FINE, String.format("Remote chance value %f.",
                    chance));
            return this;
        }

        public ServerSenderBuilder setEvents(int events) {
            this.events = events;
            logger.log(Level.FINE, String.format("Number of Events is %d.",
                    events));
            return this;
        }

        public ServerSenderBuilder setServerList(List<Integer> serverList) {
            this.serverList = serverList;
            logger.log(Level.FINE, String.format("Server List is %s.",
                    Arrays.toString(serverList.toArray())));

            return this;
        }

        public ServerSender build() {
            logger.log(Level.INFO, String.format("Building a new ServerSender."));
            return new ServerSender(this);
        }
    }
}
