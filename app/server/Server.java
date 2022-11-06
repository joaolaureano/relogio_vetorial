package app.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import app.server.ServerSender.ServerSenderBuilder;
import app.server.ServerListener.ServerListenerBuilder;
import app.server.event.EventManager;
import app.socket.multicast.MSocket;
import app.socket.multicast.MSocket.MSocketPayload;
import app.socket.unicast.USocket;
import app.server.event.EventManager.EventManagerBuilder;
import java.util.logging.*;

public class Server {
    static MSocket multicastSocket;
    static int multicastPort;
    static String multicastAddress;
    static final Logger logger = Logger.getGlobal(); 

    public static void main(String[] _args) throws IOException {
        
        logger.log(Level.INFO, "Initializing server...");
        multicastAddress = "230.0.0.0"; // need to change to a config file =)
        multicastPort = 5000;
        String[] args = { "1", "1", "5001", "25", "100", "100", "200", "1235" };

        int id = Integer.parseInt(args[0]);
        logger.log(Level.INFO, String.format("Id is %d", id));

        int position = Integer.parseInt(args[1]);
        logger.log(Level.INFO, String.format("Position is %d", position));

        int port = Integer.parseInt(args[2]);
        logger.log(Level.INFO, String.format("Server port is %d", port));

        double chance = Double.parseDouble(args[3]);
        logger.log(Level.INFO, String.format("Chance for a remote event is %f", chance));

        int events = Integer.parseInt(args[4]);
        logger.log(Level.INFO, String.format("Amount of events to be fired is %d", events));

        int minDelay = Integer.parseInt(args[5]);
        int maxDelay = Integer.parseInt(args[6]);
        logger.log(Level.INFO, String.format("Range for delay values is %d to %d", minDelay, maxDelay));

        List<Integer> serverList;
        serverList = new ArrayList<Integer>();
        Stream.of(args[7].split(",")).map(Integer::valueOf).forEach(serverList::add);
        logger.log(Level.INFO, String.format("Process Neighbors are %s", Arrays.toString(serverList.toArray())));

        multicastSocket = new MSocket(multicastPort, multicastAddress);
        logger.warning("Server is LOCKED...");
        unlock();
        USocket socket = new USocket(port);
        USocket socketAck = new USocket(port + 1);
        EventManager clock = (new EventManagerBuilder()
                .setClockPosition(position)
                .setClockSize(serverList.size() + 1)
                .setSocket(socket)
                .setAckSocket(socketAck)
                .build());

        (new ServerListenerBuilder()
                .setEventManager(clock)
                .setUnicastSocket(socket)
                .build())
                .start();

        (new ServerSenderBuilder()
                .setChance(chance)
                .setEventManager(clock)
                .setEvents(events)
                .setMinDelay(minDelay)
                .setMaxDelay(maxDelay)
                .setServerList(serverList)
                .build())
                .start();

    }

    public static void unlock() {
        while (true) {
            try {

                MSocketPayload socketPayload = multicastSocket.receivePacket();
                String vars[] = socketPayload.getContent().split("\\s");
                if (vars[0].equals("SETUP")) {
                    logger.log(Level.INFO, "Server is UNLOCKED");
                    return;
                }
            } catch (Exception e) {
                System.out.print(".");
            }
        }
    }

}