package app.server.clock;

import java.util.Arrays;

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
            for (int i = 0; i < queue.length; i++) {
                if (queue[i] < toCompareQueue[i])
                    queue[i] = toCompareQueue[i];
            }
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(queue);
    }
}
