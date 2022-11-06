package app.server;

import java.io.IOException;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import app.socket.multicast.MSocket;

public class ServerManager {
    static final Logger logger = Logger.getLogger(ServerManager.class.getName());

    static MSocket socket;
    static int port;
    static String multicastAddress;

    public static void main(String[] args) throws IOException {
        int counter = 100;
        logger.log(Level.INFO, "Server manager starting");
        multicastAddress = args[0];
        port = Integer.parseInt(args[1]);
        socket = new MSocket(port, multicastAddress);
        logger.log(Level.INFO,
                String.format("Server manager info.\nMulticast Address is %s\nPort is %d",
                        multicastAddress, port));

        while (counter > 0) {
            logger.log(Level.INFO, "Sending packages to multicast group");
            socket.sendPacket("SETUP", InetAddress.getByName(multicastAddress), port);
            counter--;

        }
    }
}
