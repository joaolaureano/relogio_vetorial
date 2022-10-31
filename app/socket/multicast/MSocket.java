package app.socket.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;

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
public class MSocket {
    int TIMEOUT = 500;
    int KILOBYTE = 1024;
    MulticastSocket datagramSocket;
    String multicastAddress;

    public MSocket(int port, String multicastAddress) {
        try {
            this.datagramSocket = new MulticastSocket(port);
            this.multicastAddress = multicastAddress;
            this.joinGroup(multicastAddress);
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

    public MSocketPayload receivePacket() {
        try {
            byte[] buffer = new byte[KILOBYTE];

            DatagramPacket datagramPacket = new DatagramPacket(buffer, buffer.length);

            datagramSocket.receive(datagramPacket);

            String content = new String(datagramPacket.getData(), 0, datagramPacket.getLength());

            return new MSocketPayload(datagramPacket.getAddress(), datagramPacket.getPort(), content);

        } catch (IOException e) {

        }

        return null;
    }

    public boolean joinGroup(String multicastAddress) {
        InetAddress group;
        try {
            group = InetAddress.getByName(multicastAddress);
            if (Inet4Address.getByAddress(group.getAddress()).isMulticastAddress()) {

                datagramSocket.joinGroup(group);

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

    public class MSocketPayload {
        private InetAddress address;
        private int port;
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