package app.server.clock;

import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Main class for vectorial clock.
 * This class is a SINGLETON
 */
public class ClockManager implements IClockManager {
    /**
     * Main static logger for class
     */
    static final Logger logger = Logger.getLogger(ClockManager.class.getName());
    static {
        try {
            InputStream stream = ClockManager.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Main clock content
     */
    int[] clock;
    /**
     * {@link ClockManager} singleton instance
     */
    static ClockManager clockManager;

    /**
     * Constructor for ClockManager.
     * 
     * @param clockSize the vectorial clock size
     */
    private ClockManager(int clockSize) {
        logger.log(Level.CONFIG, String.format("New Clock created. Clock size is %d", clockSize));
        clock = new int[clockSize];
    }

    /**
     * Create a Singleton instance if required.
     * Returns the clock singleton instance
     * 
     * @param clockSize
     * @return ClockManager
     */
    public static ClockManager getInstance(int clockSize) {
        if (clockManager == null) {
            clockManager = new ClockManager(clockSize);
        }
        return clockManager;
    }

    /**
     * Updates the clock locally.
     * It is a critical method.s
     * 
     * @param queuePosition Defines the Server local position at vectorial position.
     * 
     */
    public void update(int queuePosition) {

        synchronized (clock) {
            clock[queuePosition]++;
        }
        logger.log(Level.FINER, String.format("Updated Clock is %s", Arrays.toString(clock)));
    }

    /**
     * Compares the local clock with remote clock and update local values.
     * Also update local position by increment.
     * Defines a critical section to compare the clock and to update local position.
     * 
     * @param queuePosition Server local position
     * @param remoteClock   Remote clock to compare with
     */
    public void update(int queuePosition, int[] remoteClock) {
        logger.log(Level.FINER, String.format("Comparing Clock to remote Clock.\tRemote clock is %s\tLocal clock is %s",
                Arrays.toString(remoteClock), Arrays.toString(clock)));
        synchronized (clock) {
            for (int i = 0; i < clock.length; i++)
                clock[i] = Math.max(clock[i], remoteClock[i]);

            clock[queuePosition]++;
        }
        logger.log(Level.FINER, String.format("Updated Clock is %s", Arrays.toString(clock)));
    }

    /**
     * @return String
     */
    @Override
    public String toString() {
        return Arrays.toString(clock);
    }

    /**
     * Serializes the clock to a String
     * 
     * @return String
     */
    public String serialize() {

        String response = Arrays.stream(clock).mapToObj(String::valueOf).collect(Collectors.joining(","));
        logger.log(Level.FINER, String.format("Serialized Clock is %s", response));
        return response;
    }

    /**
     * Deserialize the clock to a int[]
     * 
     * @param content
     * @return int[]
     */
    public static int[] deserialize(String content) {
        int[] response = Arrays.stream(content.split(",")).mapToInt(Integer::valueOf).toArray();
        logger.log(Level.FINER, String.format("Deserialized Clock is %s", Arrays.toString(response)));
        return response;
    }

}
