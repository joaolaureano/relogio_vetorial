package app.socket.multicast;

import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Represents a multicast socket to ease development
 */
public class MSocket {
    /**
     * Main static logger for class
     */
    static final Logger logger = Logger.getLogger(MSocket.class.getName());
    static {
        try {
            InputStream stream = MSocket.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    /**
     * Constant time out for listening operation
     * Standard value is 1000
     */
    final int TIMEOUT = 1000;
    /**
     * Representation of Kilobyte in bits
     * Used to ease packet manipulation
     * Standard value is 1024
     */
    final int KILOBYTE = 1024;
    /**
     * The main socket to perform operation
     */
    MulticastSocket datagramSocket;
    /**
     * The multicast address to connect with.
     * In this assignment, all host will connect with only one address,
     * therefore, make senses to use a simple String variable.
     */
    String multicastAddress;

    /**
     * Main constructor for UnicastSocket
     * It creates a DatagramSocket with defined TIMEOUT
     * 
     * @param port             port to bind with
     * @param multicastAddress multicast address to connect with
     */
    public MSocket(int port, String multicastAddress) {
        try {
            this.datagramSocket = new MulticastSocket(port);
            logger.log(Level.CONFIG, String.format("Socket port is %d", port));
            this.multicastAddress = multicastAddress;
            this.joinGroup(multicastAddress);
            datagramSocket.setSoTimeout(TIMEOUT);
            logger.log(Level.CONFIG, String.format("Multicast Socket timeout is %d milisseconds", TIMEOUT));

            logger.log(Level.CONFIG, "Created a Multicast Socket.");
        } catch (BindException e) {
            logger.log(Level.SEVERE, String.format("Port is already in use. Terminating execution"));
            System.exit(0);
        } catch (IOException e) {
        }
    }

    /**
     * Sends a packet with String content to specific address and port
     * 
     * @param content the content to be sent
     * @param addr    the Address to be sent.
     * @param port    the remote port to communicate with
     */
    public void sendPacket(String content, InetAddress addr, int port) {
        try {
            byte[] contentBytes = content.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(contentBytes, content.length(), addr, port);
            logger.log(Level.FINEST,
                    String.format("Sent a package. \nDestiny port is %d\nPackage content is %s", port, content));
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
        }
    }

    /**
     * Receives a packet, using defined TIMEOUT.
     * In case timeout is reached, throws an exception.
     * 
     * @return USocketPayload the captured packet sent by any remote host
     * @throws Exception
     */
    public MSocketPayload receivePacket() {
        try {
            byte[] buffer = new byte[KILOBYTE];

            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            datagramSocket.receive(datagramPacket);

            String content = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

            MSocketPayload response = new MSocketPayload(datagramPacket.getAddress(), datagramPacket.getPort(),
                    content);

            logger.log(Level.FINE, String.format("Received a package. \nOrigin port is %d\nPackage content is %s",
                    response.getPort(), response.getContent()));

            return response;

        } catch (IOException e) {

        }

        return null;
    }

    /**
     * Will join socket to a multicastAddress
     * 
     * @param multicastAddress
     * @return whether it was success or not
     */
    public boolean joinGroup(String multicastAddress) {
        InetAddress group;
        try {
            group = InetAddress.getByName(multicastAddress);
            if (Inet4Address.getByAddress(group.getAddress()).isMulticastAddress()) {

                datagramSocket.joinGroup(group);
                logger.log(Level.CONFIG, String.format("Joined Multicast group\nIP is %s.", multicastAddress));
                return true;
            }
            return false;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return false;
    }

    public void close() {
        try {
            this.datagramSocket.close();
            datagramSocket.leaveGroup(InetAddress.getByName(multicastAddress));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    /**
     * Represent the packet sent and received by the socket.
     * Contains the address, port and the content
     */
    public class MSocketPayload {

        /**
         * The address that will receive/send the packet
         */
        private InetAddress address;
        /**
         * The port that will receive/send the packet
         */
        private int port;
        /**
         * The content sent/received in the packet
         */
        private String content;

        protected MSocketPayload(InetAddress address, int port, String content) {
            this.address = address;
            this.port = port;
            this.content = content;
        }

        public InetAddress getAddress() {
            return address;
        }

        public int getPort() {
            return port;
        }

        public String getContent() {
            return content;
        }
    }
}