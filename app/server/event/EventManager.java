package app.server.event;

import java.net.InetAddress;
import java.net.UnknownHostException;

import app.server.clock.ClockManager;
import app.socket.unicast.USocket;
import app.socket.unicast.USocket.USocketPayload;

public class EventManager {

    ClockManager clock;
    int clockPosition;
    USocket unicastSocket;


    private EventManager(EventManagerBuilder builder) {
        this.clock = builder.clock;
        this.clockPosition = builder.clockPosition;
        this.unicastSocket = builder.unicastSocket;
    }

    public boolean local() {
        this.clock.update(this.clockPosition);
        return true;
    }

    public boolean remote(int port) {
        try {
            this.local();
            String array = this.clock.serialize();

            String content = "EVENT - " + array;
            unicastSocket.sendPacket(content, InetAddress.getByName("localhost"), port);
            
            USocketPayload ackPackage = unicastSocket.receivePacket();
            System.out.println("RECEIVED ACK");

            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("DID NOT RECEIVED ACK");
            // e.printStackTrace();
        }
        return false;

    }

    public boolean receive(int[] remoteClock){
        this.clock.update(this.clockPosition, remoteClock);
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

        public EventManagerBuilder setClockPosition(int clockPosition) {
            this.clockPosition = clockPosition;
            return this;
        }

        public EventManagerBuilder setSocket(USocket socket) {
            this.unicastSocket = socket;
            return this;
        }

        public EventManager build() {
            return new EventManager(this);
        }
    }
}
