package app.server;

import java.io.IOException;
import java.net.InetAddress;

import app.socket.multicast.MSocket;

public class ServerSender {
    static MSocket socket;
    static int port;
    static String multicastAddress;

    public static void main(String[] args) throws IOException {
        
        port = Integer.parseInt(args[0]);
        multicastAddress = args[1];

        socket = new MSocket(port, multicastAddress);
while(true)
        socket.sendPacket("SETUP", InetAddress.getByName(multicastAddress), 5000);
    }
}
