package app.server.queue;

public interface IQueueManager {
    public void update(int queuePosition, int[] toCompareQueue);

    public void update(int queuePosition);
}
