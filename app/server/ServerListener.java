package app.server;

import java.net.InetAddress;
import java.util.Arrays;

import app.socket.unicast.USocket;
import app.socket.unicast.USocket.USocketPayload;
import app.server.clock.ClockManager;
import app.server.event.EventManager;

public class ServerListener extends Thread {
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
                String vars = socketPayload.getContent();

                if (vars.startsWith("EVENT")) {
                    int[] clock = ClockManager.deserialize(vars.split("\\s-\\s")[1]);

                    System.out.println("Received: " + Arrays.toString(clock));

                    String ackMessage = "ACK";
                    System.out.println("Sending ACK...");
                    unicastSocket.sendPacket(ackMessage, InetAddress.getByName("localhost"), socketPayload.getPort());
                    System.out.println("Sent ACK");
                }
                if(vars.startsWith("ACK")){
                    String ackMessage = "ACK";
                    unicastSocket.sendPacket(ackMessage, InetAddress.getByName("localhost"), this.unicastSocket.getLocalPort() + 1);
                }

            } catch (Exception e) {
                System.out.print(".");
            }
        }
    }

    public static class ServerListenerBuilder {

        USocket unicastSocket;
        EventManager eventManager;

        public ServerListenerBuilder setUnicastSocket(USocket unicastSocket) {
            this.unicastSocket = unicastSocket;
            return this;
        }

        public ServerListenerBuilder setEventManager(EventManager eventManager) {
            this.eventManager = eventManager;
            return this;
        }

        public ServerListener build() {
            return new ServerListener(this);
        }
    }

}
