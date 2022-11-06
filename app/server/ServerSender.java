package app.server;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import app.server.event.EventManager;
import app.server.sleeper.Sleeper;

public class ServerSender extends Thread {
    static final Logger logger = Logger.getLogger(ServerSender.class.getName());
    static {
        try {
            InputStream stream = ServerSender.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected EventManager eventManager;
    protected int minDelay, maxDelay;
    protected double chance;
    protected int events;
    protected List<Integer> serverList;
    protected List<Integer> idList;

    ServerSender(ServerSenderBuilder builder) {
        this.chance = builder.chance;
        this.events = builder.events;
        this.minDelay = builder.minDelay;
        this.maxDelay = builder.maxDelay;
        this.serverList = builder.serverList;
        this.eventManager = builder.eventManager;
        this.idList = builder.idList;
    }

    public void run() {

        while (true) {
            try {
                Sleeper.performSleep(minDelay, maxDelay);
                boolean eventResponse = nextEvent();
                if (events == 0) {
                    logger.log(Level.INFO, String.format("Number of Events is 0."));
                    logger.log(Level.INFO, String.format("Final clock status is %s", this.eventManager.toString()));
                    logger.log(Level.INFO, String.format("Ending process..."));
                    System.exit(0);
                    return;
                }
                if (!eventResponse) {
                    logger.log(Level.INFO, String.format("Time-out event."));
                    logger.log(Level.INFO, String.format("Final clock status is %s", this.eventManager.toString()));
                    logger.log(Level.INFO, String.format("Ending process..."));
                    System.exit(0);
                }
            } catch (Exception e) {

                // System.out.print(".");

            }
        }
    }

    public boolean nextEvent() {

        double nextChance = Math.random();
        boolean success = false;
        logger.log(Level.FINEST, String.format("Chance value calculated. Chance value is %f", nextChance));

        if (nextChance <= chance) {
            logger.log(Level.FINEST, String.format("Remote Event triggered."));
            int randomNum = (new Random()).nextInt(this.serverList.size());
            int port = this.serverList.get(randomNum);
            int id = this.idList.get(randomNum);
            logger.log(Level.FINEST, String.format("Remote port is %d\nRemote ID is %d", port, id));
            success = this.eventManager.remote(port, id);

        } else {
            logger.log(Level.FINEST, "Local Event triggered.");
            success = this.eventManager.local();

        }
        if (success) {
            logger.log(Level.FINEST, String.format("Event decreased. Event number is %d", events));
            events--;
            return true;
        } else {
            return false;
        }

    }

    public static class ServerSenderBuilder {
        static final Logger logger = Logger.getLogger(ServerSenderBuilder.class.getName());
        static {
            try {
                InputStream stream = ServerSenderBuilder.class.getClassLoader()
                        .getResourceAsStream("app/logging.properties");
                LogManager.getLogManager().readConfiguration(stream);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        EventManager eventManager;
        int minDelay, maxDelay;
        double chance;
        int events;
        List<Integer> serverList;
        List<Integer> idList;

        public ServerSenderBuilder setEventManager(EventManager eManager) {
            this.eventManager = eManager;
            logger.log(Level.CONFIG, String.format("Event Manager added to build ServerListener.\nClock status is %s.",
                    eventManager.toString()));
            return this;
        }

        public ServerSenderBuilder setMinDelay(int minDelay) {
            this.minDelay = minDelay;
            logger.log(Level.CONFIG, String.format("Minimum Delay value is %d.",
                    minDelay));
            return this;
        }

        public ServerSenderBuilder setMaxDelay(int maxDelay) {
            this.maxDelay = maxDelay;
            logger.log(Level.CONFIG, String.format("Maximum Delay value is %d.",
                    maxDelay));
            return this;
        }

        public ServerSenderBuilder setChance(double chance) {
            this.chance = chance / 100;
            logger.log(Level.CONFIG, String.format("Remote chance value %f.",
                    chance));
            return this;
        }

        public ServerSenderBuilder setEvents(int events) {
            this.events = events;
            logger.log(Level.CONFIG, String.format("Number of Events is %d.",
                    events));
            return this;
        }

        public ServerSenderBuilder setServerList(List<Integer> serverList) {
            this.serverList = serverList;
            logger.log(Level.CONFIG, String.format("Server List is %s.",
                    Arrays.toString(serverList.toArray())));

            return this;
        }

        public ServerSenderBuilder setIdList(List<Integer> idList) {
            this.idList = idList;
            logger.log(Level.CONFIG, String.format("ID List is %s.",
                    Arrays.toString(idList.toArray())));

            return this;
        }

        public ServerSender build() {
            logger.log(Level.INFO, String.format("Building a new ServerSender."));
            return new ServerSender(this);
        }
    }
}
