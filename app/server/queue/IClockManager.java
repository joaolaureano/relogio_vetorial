package app.server.queue;

public interface IClockManager {
    public void update(int queuePosition, int[] toCompareQueue);

    public void update(int queuePosition);
}
