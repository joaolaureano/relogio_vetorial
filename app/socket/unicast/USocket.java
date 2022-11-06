package app.socket.unicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is a custom SOCKET Implementation to ease the development
 * It contains the following variables/constant:
 * TIMEOUT, to define the timeout maximum time
 * KILOBYTE, to define the size of a byte
 * datagramSocket, to define a socket that will be used throught the whole
 * software
 * It also contains the following methods:
 * sendPacket -> It will send a packet, with a specific content, to some host at
 * some port
 * receivePacket -> It will lock the software to listen to a specific port until
 * some resourse is sent to this port
 * close -> It will close the socket
 * It also define a internal class SocketPayload.
 * This class is used to ease the echange of packets between host and peers
 * This class contains the address, the port and the content of the packet
 * 
 */
public class USocket {
    static final Logger logger = Logger.getLogger(USocket.class.getName());

    int TIMEOUT = 1000;
    int KILOBYTE = 1024;

    DatagramSocket datagramSocket;

    public USocket(int port) {
        try {
            this.datagramSocket = new DatagramSocket(port);
            logger.log(Level.INFO,"Created a Unicast Socket.");
            logger.log(Level.INFO,String.format("Socket port is %d", port));
            datagramSocket.setSoTimeout(TIMEOUT);
            logger.log(Level.INFO,String.format("Unicast Socket timeout is %d milisseconds", TIMEOUT));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(String content, InetAddress addr, int port) {
        try {
            byte[] contentBytes = content.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(contentBytes, content.length(), addr, port);
            logger.log(Level.INFO,String.format("Sent a package. \n Destiny port is %d\nPackage content is %s", port, content));
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            // this.close();
        }
    }

    public int getLocalPort() {
        return this.datagramSocket.getLocalPort();
    }

    public USocketPayload receivePacket() throws Exception {
        try {
            byte[] buffer = new byte[KILOBYTE];

            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            datagramSocket.receive(datagramPacket);

            String content = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

            USocketPayload response = new USocketPayload(datagramPacket.getAddress(), datagramPacket.getPort(),
                    content);

            logger.log(Level.INFO,String.format("Received a package. \n Origin port is %d\nPackage content is %s",
                    response.getPort(), response.getContent()));

            return response;

        } catch (IOException e) {
            throw new Exception();
        }
    }

    public void close() {
        this.datagramSocket.close();

    }

    public class USocketPayload {
        private InetAddress address;
        private int port;
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