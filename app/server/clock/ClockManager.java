package app.server.clock;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ClockManager implements IClockManager {

    int[] queue;
    static ClockManager queueManager;

    ClockManager(int queueSize) {
        queue = new int[queueSize];
    }

    public static ClockManager getInstance(int queueSize) {
        if (queueManager == null) {
            queueManager = new ClockManager(queueSize);
        }
        return queueManager;
    }

    public void update(int queuePosition) {
        synchronized (queue) {
            queue[queuePosition]++;
        }
    }

    public void update(int queuePosition, int[] toCompareQueue) {
        synchronized (queue) {
            for (int i = 0; i < queue.length; i++)
                queue[i] = Math.max(queue[i], toCompareQueue[i]);
        }
        this.update(queuePosition);
    }

    @Override
    public String toString() {
        return Arrays.toString(queue);
    }

    public String serialize() {
        return Arrays.stream(queue).mapToObj(String::valueOf).collect(Collectors.joining(","));
    }

    public static int[] deserialize(String content) {
        return Arrays.stream(content.split(",")).mapToInt(Integer::valueOf).toArray();
    }

}
