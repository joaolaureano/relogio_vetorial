package app.server.event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.server.clock.ClockManager;
import app.socket.unicast.USocket;

public class EventManager {
    static final Logger logger = Logger.getGlobal();

    ClockManager clock;
    int clockPosition;
    USocket unicastSocket;
    USocket ackSocket;

    private EventManager(EventManagerBuilder builder) {
        this.clock = builder.clock;
        this.clockPosition = builder.clockPosition;
        this.unicastSocket = builder.unicastSocket;
        this.ackSocket = builder.ackSocket;
    }

    public boolean local() {
        logger.log(Level.FINE, String.format("Local event triggered"));
        this.clock.update(this.clockPosition);
        logger.log(Level.FINE, String.format("New clock status is %s ",
                this.clock.toString()));

        return true;
    }

    public boolean remote(int port) {
        logger.log(Level.FINE, String.format("Remote event triggered"));
        try {
            logger.log(Level.FINE, String.format("Triggering Local event..."));
            this.local();

            String array = this.clock.serialize();

            String content = "EVENT - " + array;

            logger.log(Level.FINE, String.format("Sending remote package.\nContent is %s\nPort is %d", content, port));

            unicastSocket.sendPacket(content, InetAddress.getByName("localhost"), port);

            unicastSocket.receivePacket();

            logger.log(Level.FINE, String.format("New clock status is %s ",
                    this.clock.toString()));
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            logger.warning(String.format("Operation time-out."));
        }
        return false;
    }

    public boolean receive(int[] remoteClock) {
        logger.log(Level.FINE,
                String.format("Updating local clock with remote clock.\nRemote clock is %s\nLocal clock is %s",
                        Arrays.toString(remoteClock), this.clock.toString()));
        this.clock.update(this.clockPosition, remoteClock);
        logger.log(Level.FINE, String.format("Successfull operation."));
        return true;
    }

    @Override
    public String toString() {
        return this.clock.toString();
    }

    public static class EventManagerBuilder {
        static final Logger logger = Logger.getGlobal();

        ClockManager clock;
        int clockPosition;
        USocket unicastSocket;
        USocket ackSocket;

        public EventManagerBuilder setClockSize(int clockSize) {
            logger.log(Level.FINE, String.format("Clock size setted.\nSize is %d ",
                    clockSize));
            this.clock = ClockManager.getInstance(clockSize);
            return this;
        }

        public EventManagerBuilder setClockPosition(int clockPosition) {
            logger.log(Level.FINE, String.format("Clock Index position setted.\nIndex is %d ",
                    clockPosition));
            this.clockPosition = clockPosition;
            return this;
        }

        public EventManagerBuilder setSocket(USocket socket) {
            logger.log(Level.FINE, String.format("Unicast Socket added to build ServerListener.\nPort is %d ",
                    socket.getLocalPort()));
            this.unicastSocket = socket;
            return this;
        }

        public EventManagerBuilder setAckSocket(USocket socket) {
            logger.log(Level.FINE, String.format("ACK Socket added to build ServerListener.\nPort is %d ",
                    socket.getLocalPort()));
            this.ackSocket = socket;
            return this;
        }

        public EventManager build() {
            logger.log(Level.FINE, String.format("Building a new EventManager."));
            return new EventManager(this);
        }
    }
}
