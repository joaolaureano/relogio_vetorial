package app.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import app.socket.multicast.MSocket;

/**
 * This class will unlock all servers using packet in multicast group
 */
public class ServerManager {
    /**
     * Main static logger for class
     */
    static final Logger logger = Logger.getLogger(ServerManager.class.getName());
    static {
        try {
            InputStream stream = ServerManager.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Main multicast socket
     */
    static MSocket socket;
    /**
     * Port to bind socket
     */
    static int port;
    /**
     * Multicast Address to bind socket
     */
    static String multicastAddress;

    /**
     * Main ServerManager
     * Must receive required configuration values. At position args[0]
     * multicastAddress must be sent. At position args[1] port must be sent
     * 
     * Flow is at its follow:
     * Build multicast socket based in args value
     * While counter value is bigger than 0, multicast socket will send a packet to
     * unlock all connected servers
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        int counter = 10;
        logger.log(Level.INFO, "Server manager starting");
        multicastAddress = args[0];
        port = Integer.parseInt(args[1]);
        socket = new MSocket(port, multicastAddress);
        logger.log(Level.FINER,
                String.format("Server manager info.\tMulticast Address is %s\tPort is %d",
                        multicastAddress, port));

        while (counter > 0) {
            logger.log(Level.INFO, "Sending packages to multicast group");
            socket.sendPacket("SETUP", InetAddress.getByName(multicastAddress), port);
            counter--;

        }
    }
}
