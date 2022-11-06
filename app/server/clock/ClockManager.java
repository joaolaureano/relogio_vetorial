package app.server.clock;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class ClockManager implements IClockManager {

    static final Logger logger = Logger.getLogger(ClockManager.class.getName());

    int[] clock;
    static ClockManager clockManager;

    ClockManager(int clockSize) {
        logger.info(String.format("New Clock created. Clock size is %d", clock));
        clock = new int[clockSize];
    }

    public static ClockManager getInstance(int clockSize) {
        if (clockManager == null) {
            clockManager = new ClockManager(clockSize);
        }
        return clockManager;
    }

    public void update(int queuePosition) {

        synchronized (clock) {
            clock[queuePosition]++;
        }
        logger.log(Level.FINE, String.format("Updated Clock is %s", Arrays.toString(clock)));
    }

    public void update(int queuePosition, int[] toCompareQueue) {
        logger.log(Level.FINE, String.format("Comparing Clock to remote Clock.\nRemote clock is %s\nLocal clock is %s",
                Arrays.toString(toCompareQueue), Arrays.toString(clock)));
        synchronized (clock) {
            for (int i = 0; i < clock.length; i++)
                clock[i] = Math.max(clock[i], toCompareQueue[i]);
        }
        logger.log(Level.FINE, String.format("Updated Clock is %s", Arrays.toString(clock)));

        logger.log(Level.FINE, String.format("Triggered Local update."));
        this.update(queuePosition);
    }

    @Override
    public String toString() {
        return Arrays.toString(clock);
    }

    public String serialize() {

        String response = Arrays.stream(clock).mapToObj(String::valueOf).collect(Collectors.joining(","));
        logger.log(Level.FINE, String.format("Serialized Clock is %s", response));
        return response;
    }

    public static int[] deserialize(String content) {
        int[] response = Arrays.stream(content.split(",")).mapToInt(Integer::valueOf).toArray();
        logger.log(Level.FINE, String.format("Deserialized Clock is %s", Arrays.toString(response)));
        return response;
    }

}
