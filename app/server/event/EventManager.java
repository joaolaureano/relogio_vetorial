package app.server.event;

import app.server.clock.ClockManager;
import app.socket.unicast.USocket;

public class EventManager {

    ClockManager clock;
    int clockPosition;
    USocket unicastSocket;

    private EventManager(EventManagerBuilder builder) {
        this.clock = builder.clock;
        this.clockPosition = builder.clockPosition;
        
    }

    public boolean local(){

        this.clock.update(this.clockPosition);
        return true;
    }

    public boolean remote(int[] remoteClock){
        this.clock.update(clockPosition, remoteClock);

        return true;
    }

    @Override
    public String toString() {
        return this.clock.toString();
    }

    public static class EventManagerBuilder {

        ClockManager clock;
        int clockPosition;
        USocket unicastSocket;

        public EventManagerBuilder setClockSize(int clockSize) {
            this.clock = ClockManager.getInstance(clockSize);
            return this;
        }
        public EventManagerBuilder setClockPosition(int clockPosition){
            this.clockPosition = clockPosition;
            return this;
        }
        public EventManagerBuilder setPort(int port){
            this.unicastSocket = new USocket(port);
            return this;
        }
        public EventManager build() {
            return new EventManager(this);
        }
    }
}
