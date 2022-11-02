package app.server;

import java.io.IOException;
import java.util.Calendar;

import app.socket.multicast.MSocket;
import app.socket.multicast.MSocket.MSocketPayload;
import app.socket.unicast.USocket;

public class Server {
    static MSocket multicastSocket;
    static int multicastPort;
    static String multicastAddress;

    public static void main(String[] args) throws IOException {
        multicastAddress = "230.0.0.0"; // need to change to a config file =)
        multicastPort = 5000;

        int port = Integer.parseInt(args[0]);

        double chance = Double.parseDouble(args[1]);
        int events = Integer.parseInt(args[2]);

        int minDelay = Integer.parseInt(args[3]);
        int maxDelay = Integer.parseInt(args[4]);

        multicastSocket = new MSocket(multicastPort, multicastAddress);
        System.out.println("STARTED");
        unlock();

        new ServerEventSender(port, chance, events, minDelay, maxDelay).start();
    }

    public static void unlock() {
        while (true) {
            try {

                MSocketPayload socketPayload = multicastSocket.receivePacket();
                String vars[] = socketPayload.getContent().split("\\s");
                if (vars[0].equals("SETUP")) {
                    System.out.println("UNLOCKED");
                    System.out.println(String.format("%d:%d:%d.%d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE),
                            Calendar.getInstance().get(Calendar.SECOND),
                            Calendar.getInstance().get(Calendar.MILLISECOND)));
                    return;
                }
            } catch (Exception e) {
                System.out.print(".");
            }
        }
    }

}
