package app.socket.unicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
    int TIMEOUT = 500;
    int KILOBYTE = 1024;
    DatagramSocket datagramSocket;
    String multicastAddress;

    public USocket(int port, String multicastAddress) {
        try {
            this.datagramSocket = new DatagramSocket(port);
            this.multicastAddress = multicastAddress;
            datagramSocket.setSoTimeout(TIMEOUT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendPacket(String content, InetAddress addr, int port) {
        try {
            byte[] contentBytes = content.getBytes();
            DatagramPacket datagramPacket = new DatagramPacket(contentBytes, content.length(), addr, port);
            datagramSocket.send(datagramPacket);
        } catch (IOException e) {
            // this.close();
        }
    }

    public USocketPayload receivePacket() {
        try {
            byte[] buffer = new byte[KILOBYTE];

            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            datagramSocket.receive(datagramPacket);

            String content = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

            return new USocketPayload(datagramPacket.getAddress(), datagramPacket.getPort(), content);

        } catch (IOException e) {

        }

        return null;
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