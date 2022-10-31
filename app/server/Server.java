package app.server;

import java.io.IOException;
import java.util.Calendar;

import app.socket.multicast.MSocket;
import app.socket.multicast.MSocket.MSocketPayload;
import app.socket.unicast.USocket;

public class Server {
    static MSocket multicastSocket;
    static USocket unicastSocket;
    static int port;
    static String multicastAddress;

    public static void main(String[] args) throws IOException {

        port = Integer.parseInt(args[0]);
        multicastAddress = args[1];

        multicastSocket = new MSocket(port, multicastAddress);
        System.out.println("STARTED");
        unlock();
        while (true) {
            try {
                MSocketPayload socketPayload = multicastSocket.receivePacket();
                String vars[] = socketPayload.getContent().split("\\s");
                int port = socketPayload.getPort();
                System.out.println(vars[0]);
                System.out.println(port);

                if (vars[0].equals("SETUP"))
                    return;

            } catch (Exception e) {
            }
        }
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
