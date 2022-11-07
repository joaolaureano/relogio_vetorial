package app.server.event;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import app.server.clock.ClockManager;
import app.socket.unicast.USocket;

/**
 * Main class for Events management
 */
public class EventManager {
    /**
     * Main static logger for class
     */
    static final Logger logger = Logger.getLogger(EventManager.class.getName());
    static {
        try {
            InputStream stream = EventManager.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Main server vectorial clock
     */
    private ClockManager clock;
    /**
     * Value to identify the server position in vectorial clock
     */
    private int clockPosition;
    /**
     * Main {@link USocket} unicast socket shared along whole application
     */
    private USocket unicastSocket;
    /**
     * Additional {@link USocket} unicast socket to listen ACK packages.
     * Used for remote procedures only.
     */
    private USocket ackSocket;
    /**
     * Server ID
     */
    private int processId;

    /**
     * Main builder for EventManager.
     * It is used by {@link EventManagerBuilder } builder only
     * 
     * @param builder
     */
    private EventManager(EventManagerBuilder builder) {
        this.clock = builder.clock;
        this.clockPosition = builder.clockPosition;
        this.unicastSocket = builder.unicastSocket;
        this.ackSocket = builder.ackSocket;
        this.processId = builder.processId;
    }

    /**
     * Executes a local event in clock and returns true
     * 
     * @return boolean
     */
    public boolean local() {
        logger.log(Level.FINER, String.format("Local event triggered"));
        this.clock.update(this.clockPosition);
        logger.log(Level.FINEST, String.format("New clock status is %s ",
                this.clock.toString()));

        logger.log(Level.INFO, String.format("%d %s L", processId, this.clock.toString()));
        return true;
    }

    /**
     * Executes a local update and send package to remote server.
     * It will wait for remote ACK package using {@link EventManager#ackSocket}
     * socket
     * In case package is received, return true. Return false otherwise
     * 
     * @param port
     * @param id
     * @return boolean
     */
    public boolean remote(int port, int id) {
        logger.log(Level.FINER, String.format("Remote event triggered"));
        try {
            logger.log(Level.FINER, String.format("Triggering Local event..."));

            this.clock.update(this.clockPosition);

            String array = this.clock.serialize();

            String content = String.format("EVENT - %d - %s", processId, array);

            logger.log(Level.FINEST,
                    String.format("Sending remote package.\nContent is %s\nPort is %d.\nId is %d", content, port, id));

            unicastSocket.sendPacket(content, InetAddress.getByName("localhost"), port);

            logger.log(Level.FINEST, String.format("New clock status is %s ",
                    this.clock.toString()));

            logger.log(Level.INFO, String.format("%d %s S %d", processId, this.clock.toString(), id));
            ackSocket.receivePacket();
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (Exception e) {
            logger.log(Level.WARNING, String.format("Remote operation timed-out."));
        }
        return false;
    }

    /**
     * Receives a remote vectorial clock and update with local clock.
     * 
     * @param remoteClock
     * @param idSender
     * @return boolean
     */
    public boolean receive(int[] remoteClock, int idSender) {
        logger.log(Level.FINE,
                String.format("Updating local clock with remote clock.\nRemote clock is %s\nLocal clock is %s",
                        Arrays.toString(remoteClock), this.clock.toString()));
        this.clock.update(this.clockPosition, remoteClock);
        logger.log(Level.FINE, String.format("Successfull operation."));

        logger.log(Level.INFO,
                String.format("%d %s R %d %s", processId, this.clock.toString(), idSender,
                        Arrays.toString(remoteClock)));
        return true;
    }

    /**
     * @return String
     */
    @Override
    public String toString() {
        return this.clock.toString();
    }

    /**
     * Builder for {@link EventManagerBuilder}
     */
    public static class EventManagerBuilder {
        static final Logger logger = Logger.getGlobal();

        ClockManager clock;
        int clockPosition;
        int processId;
        USocket unicastSocket;
        USocket ackSocket;

        public EventManagerBuilder setClockSize(int clockSize) {
            logger.log(Level.FINE, String.format("Clock size setted.\nSize is %d ",
                    clockSize));
            this.clock = ClockManager.getInstance(clockSize);
            return this;
        }

        public EventManagerBuilder setProcessId(int processId) {
            logger.log(Level.FINE, String.format("Process ID setted.\nSize is %d ",
                    processId));
            this.processId = processId;
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
