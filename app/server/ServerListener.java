package app.server;

import java.net.InetAddress;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.socket.unicast.USocket;
import app.socket.unicast.USocket.USocketPayload;
import app.server.clock.ClockManager;
import app.server.event.EventManager;

public class ServerListener extends Thread {
    static final Logger logger = Logger.getLogger(ServerListener.class.getName());

    USocket unicastSocket;
    EventManager eventManager;

    public ServerListener(USocket unicastSocket) {
        this.unicastSocket = unicastSocket;
    }

    ServerListener(ServerListenerBuilder builder) {
        this.unicastSocket = builder.unicastSocket;
        this.eventManager = builder.eventManager;
    }

    public void run() {
        while (true) {
            try {
                USocketPayload socketPayload = unicastSocket.receivePacket();
                int port = socketPayload.getPort();
                String vars = socketPayload.getContent();
                logger.log(Level.INFO,
                        String.format(
                                "Received an package.\nContent is %s\n Port is %d",
                                socketPayload.getContent(),
                                port));

                if (vars.startsWith("EVENT")) {
                    logger.log(Level.INFO, "Received an EVENT package");

                    int[] clock = ClockManager.deserialize(vars.split("\\s-\\s")[1]);
                    logger.log(Level.FINE, String.format("Origin Clock status is %s ",
                            Arrays.toString(clock)));

                    String ackMessage = "ACK";
                    unicastSocket.sendPacket(ackMessage, InetAddress.getByName("localhost"), port);
                    logger.log(Level.INFO, String.format("Sent ACK to port %d", port));

                } else if (vars.startsWith("ACK")) {
                    logger.log(Level.INFO, String.format("Received an ACK package from %d", port));

                    logger.log(Level.INFO, String.format("Moving ACK to internal socket.\n Internal Port is %d",
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

    public static class ServerListenerBuilder {
        static final Logger logger = Logger.getLogger(ServerListenerBuilder.class.getName());

        USocket unicastSocket;
        EventManager eventManager;

        public ServerListenerBuilder setUnicastSocket(USocket unicastSocket) {
            this.unicastSocket = unicastSocket;
            logger.log(Level.FINE, String.format("Unicast Socket added to build ServerListener.\nPort is %d ",
                    unicastSocket.getLocalPort()));
            return this;
        }

        public ServerListenerBuilder setEventManager(EventManager eventManager) {
            this.eventManager = eventManager;
            logger.log(Level.FINE, String.format("Event Manager added to build ServerListener.\nClock status is %s.",
                    eventManager.toString()));
            return this;
        }

        public ServerListener build() {
            logger.log(Level.INFO, String.format("Building a new ServerListener."));
            return new ServerListener(this);
        }
    }

}
