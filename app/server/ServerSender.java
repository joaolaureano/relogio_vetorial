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

/**
 * Main thread class to send the events to the neighbors servers.
 */
public class ServerSender extends Thread {
    /**
     * Main static logger for class
     */
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
    /**
     * Main {@link EventManager} EventManager shared along whole application
     */
    protected EventManager eventManager;
    /**
     * Minimum value for Thread sleep
     */
    protected int minDelay;
    /**
     * Maximum value for Thread sleep
     */
    protected int maxDelay;
    /**
     * Chance to trigger a remote event
     */
    protected double chance;
    /**
     * List of all neighbor servers port
     */
    protected List<Integer> serverList;
    /**
     * List of all neighbor servers ID
     */
    protected List<Integer> idList;

    /**
     * Main builder for ServerSender.
     * It is used by {@link ServerSenderBuilder} builder only
     * 
     * @param builder
     */
    private ServerSender(ServerSenderBuilder builder) {
        this.chance = builder.chance;
        this.minDelay = builder.minDelay;
        this.maxDelay = builder.maxDelay;
        this.serverList = builder.serverList;
        this.eventManager = builder.eventManager;
        this.idList = builder.idList;
    }

    /**
     * Main method for ServerSender thread
     * 
     * Execution flow is at is follow:
     * It will run a while-true loop, and first operation is a sleep
     * After sleep, it will decide whether a local or remote event will be triggered
     * by EventManagers
     * In case the number of events is 0, therefore no more events will need be
     * triggered, and execution will be finalized.
     * In case the event operation returns a false value, then it reached a timeout,
     * therefore execution will be finalized.
     * Loop will restart.
     */
    public void run() {

        while (true) {
            try {
                Sleeper.performSleep(minDelay, maxDelay);
                boolean eventResponse = nextEvent();
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

    /**
     * This is the main method to decide the remote or local event
     * It will calculate a random number and compare with
     * {@link ServerSender#chance} field.
     * In case it is smaller or equal, it will trigger a remote event. Otherwise,
     * trigger a local event.
     * 
     * @return boolean A value that represents whether the event was success or not.
     *         Should only returns false in case of remote timeout.
     */
    public boolean nextEvent() {

        double nextChance = Math.random();
        boolean success = false;
        logger.log(Level.FINEST, String.format("Chance value calculated. Chance value is %f", nextChance));
        boolean isEventAvailable = this.eventManager.decreaseEvent();
        if (!isEventAvailable) {
            logger.log(Level.INFO, String.format("Number of Events is 0."));
            logger.log(Level.INFO, String.format("Final clock status is %s", this.eventManager.toString()));
            logger.log(Level.INFO, String.format("Ending process..."));
            System.exit(0);
        }
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
        return success;

    }

    /**
     * Builder for {@link ServerSender}
     */
    public static class ServerSenderBuilder {
        /**
         * Main static logger for class
         */
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

        /**
         * Main {@link EventManager} shared along whole application
         */
        private EventManager eventManager;
        /**
         * Minimum value for Thread sleep
         */
        private int minDelay;
        /**
         * Maximum value for Thread sleep
         */
        private int maxDelay;
        /**
         * Chance to trigger a remote event
         */
        private double chance;
        /**
         * List of all neighbor servers port
         */
        private List<Integer> serverList;
        /**
         * List of all neighbor servers ID
         */
        private List<Integer> idList;

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
