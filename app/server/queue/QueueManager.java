package app.server.queue;

public class QueueManager implements IQueueManager {

    int[] queue;
    static QueueManager queueManager;

    QueueManager(int queueSize) {
        queue = new int[queueSize];
    }

    public static QueueManager getInstance(int queueSize) {
        if (queueManager == null) {
            queueManager = new QueueManager(queueSize);
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
}
