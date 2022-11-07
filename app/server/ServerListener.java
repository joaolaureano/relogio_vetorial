package app.server;

import java.io.InputStream;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import app.socket.unicast.USocket;
import app.socket.unicast.USocket.USocketPayload;
import app.server.clock.ClockManager;
import app.server.event.EventManager;

/**
 * Main thread class to listen the events from neighbors servers.
 */
public class ServerListener extends Thread {
    /**
     * Main static logger for class
     */
    static final Logger logger = Logger.getLogger(ServerListener.class.getName());
    static {
        try {
            InputStream stream = ServerListener.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Main {@link USocket} unicast socket shared along whole application
     */
    USocket unicastSocket;
    /**
     * Main {@link EventManager} EventManager shared along whole application
     */
    EventManager eventManager;

    /**
     * Main builder for ServerListener.
     * It is used by {@link ServerListenerBuilder } builder only
     * 
     * @param builder
     */
    ServerListener(ServerListenerBuilder builder) {
        this.unicastSocket = builder.unicastSocket;
        this.eventManager = builder.eventManager;
    }

    /**
     * Main method for ServerListener thread
     * 
     * Execution flow is at is follow:
     * It executes a while-true loop
     * It will listen to any package that might be becoming, given Socket timeout.
     * In case the number of events is 0, therefore no more events will need be
     * triggered, and execution will be finalized.
     * In case packet is EVENT type, it will trigger
     * {@link EventManager#receive(int[], int)} to update clock.
     * It will also send a ACK packet to origin host
     * In case packet is ACK type, it will send the packet to internal
     * {@link EventManager#clock}, in order to satisfy socket timeout
     */
    public void run() {
        while (true) {
            try {
                USocketPayload socketPayload = unicastSocket.receivePacket();
                int port = socketPayload.getPort();
                String vars = socketPayload.getContent();
                // logger.log(Level.OFF,
                // String.format(
                // "Received an package.\nContent is %s\nPort is %d",
                // socketPayload.getContent(),
                // port));

                if (vars.startsWith("EVENT")) {
                    logger.log(Level.FINE, "Received an EVENT package");
                    int idSender = Integer.parseInt(vars.split("\\s-\\s")[1]);
                    int[] clock = ClockManager.deserialize(vars.split("\\s-\\s")[2]);

                    this.eventManager.receive(clock, idSender);
                    logger.log(Level.FINEST, String.format("Origin Clock status is %s ",
                            Arrays.toString(clock)));

                    String ackMessage = "ACK";
                    unicastSocket.sendPacket(ackMessage, InetAddress.getByName("localhost"), port);
                    logger.log(Level.FINE, String.format("Sent ACK to port %d", port));

                } else if (vars.startsWith("ACK")) {
                    logger.log(Level.FINE, String.format("Received an ACK package from %d", port));

                    logger.log(Level.FINEST, String.format("Moving ACK to internal socket.\nInternal Port is %d",
                            this.unicastSocket.getLocalPort() + 1));
                    String ackMessage = "ACK";
                    unicastSocket.sendPacket(ackMessage, InetAddress.getByName("localhost"),
                            this.unicastSocket.getLocalPort() + 1);
                }

            } catch (Exception e) {
                System.out.print(".");
            }
        }
    }

    /**
     * Builder for {@link ServerListener}
     */
    public static class ServerListenerBuilder {
        static final Logger logger = Logger.getLogger(ServerListenerBuilder.class.getName());
        static {
            try {
                InputStream stream = ServerListenerBuilder.class.getClassLoader()
                        .getResourceAsStream("app/logging.properties");
                LogManager.getLogManager().readConfiguration(stream);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        USocket unicastSocket;
        EventManager eventManager;

        public ServerListenerBuilder setUnicastSocket(USocket unicastSocket) {
            this.unicastSocket = unicastSocket;
            logger.log(Level.CONFIG, String.format("Unicast Socket added to build ServerListener.\nPort is %d ",
                    unicastSocket.getLocalPort()));
            return this;
        }

        public ServerListenerBuilder setEventManager(EventManager eventManager) {
            this.eventManager = eventManager;
            logger.log(Level.CONFIG, String.format("Event Manager added to build ServerListener.\nClock status is %s.",
                    eventManager.toString()));
            return this;
        }

        public ServerListener build() {
            logger.log(Level.CONFIG, String.format("Building a new ServerListener."));
            return new ServerListener(this);
        }
    }

}
