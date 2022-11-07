package app.socket.unicast;

import java.io.IOException;
import java.io.InputStream;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

/**
 * Represents a unicast socket to ease development
 */
public class USocket {
    /**
     * Main static logger for class
     */
    static final Logger logger = Logger.getLogger(USocket.class.getName());
    static {
        try {
            InputStream stream = USocket.class.getClassLoader()
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
     * The main socket to perform
     */
    DatagramSocket datagramSocket;

    /**
     * Main constructor for UnicastSocket
     * It creates a DatagramSocket with defined TIMEOUT
     * 
     * @param port
     */
    public USocket(int port) {
        try {
            this.datagramSocket = new DatagramSocket(port);
            logger.log(Level.CONFIG, String.format("Socket port is %d", port));
            datagramSocket.setSoTimeout(TIMEOUT);
            logger.log(Level.CONFIG, String.format("Unicast Socket timeout is %d milisseconds", TIMEOUT));
            logger.log(Level.CONFIG, "Created a Unicast Socket.");
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
     * @param addr    the Address to be sent. In this assignment, LOCALHOST is being
     *                used
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
            // this.close();
        }
    }

    /**
     * Returns the socket port
     * 
     * @return int
     */
    public int getLocalPort() {
        return this.datagramSocket.getLocalPort();
    }

    /**
     * Receives a packet, using defined TIMEOUT.
     * In case timeout is reached, throws an exception.
     * 
     * @return USocketPayload the captured packet sent by any remote host
     * @throws Exception
     */
    public USocketPayload receivePacket() throws Exception {
        try {
            byte[] buffer = new byte[KILOBYTE];

            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            datagramSocket.receive(datagramPacket);

            String content = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

            USocketPayload response = new USocketPayload(datagramPacket.getAddress(), datagramPacket.getPort(),
                    content);

            logger.log(Level.FINEST, String.format("Received a package. \nOrigin port is %d\nPackage content is %s",
                    response.getPort(), response.getContent()));

            return response;

        } catch (IOException e) {
            throw new Exception();
        }
    }

    public void close() {
        this.datagramSocket.close();

    }

    /**
     * Represent the packet sent and received by the socket.
     * Contains the address, port and the content
     */
    public class USocketPayload {
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

        protected USocketPayload(InetAddress address, int port, String content) {
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