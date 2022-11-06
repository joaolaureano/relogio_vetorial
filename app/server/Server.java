package app.server;

import java.io.IOException;
import java.io.InputStream;
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
    static final Logger logger = Logger.getLogger(Server.class.getName());
    static {
        try {
            InputStream stream = Server.class.getClassLoader()
                    .getResourceAsStream("app/logging.properties");
            LogManager.getLogManager().readConfiguration(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {

        logger.log(Level.INFO, "Initializing server...");
        multicastAddress = "230.0.0.0"; // need to change to a config file =)
        multicastPort = 5000;
        
        int id = Integer.parseInt(args[0]);
        logger.log(Level.INFO, String.format("Id is %d", id));

        int position = Integer.parseInt(args[1]);
        logger.log(Level.INFO, String.format("Clock Position is %d", position));

        int port = Integer.parseInt(args[2]);
        logger.log(Level.CONFIG, String.format("Server port is %d", port));

        double chance = Double.parseDouble(args[3]);
        logger.log(Level.CONFIG, String.format("Chance for a remote event is %f", chance));

        int events = Integer.parseInt(args[4]);
        logger.log(Level.CONFIG, String.format("Amount of events to be fired is %d", events));

        int minDelay = Integer.parseInt(args[5]);
        int maxDelay = Integer.parseInt(args[6]);
        logger.log(Level.CONFIG, String.format("Range for delay values is %d to %d", minDelay, maxDelay));

        List<Integer> serverList;
        serverList = new ArrayList<Integer>();
        Stream.of(args[7].split(",")).map(Integer::valueOf).forEach(serverList::add);
        logger.log(Level.CONFIG,
                String.format("Process Neighbors ports are %s", Arrays.toString(serverList.toArray())));

        List<Integer> idList;
        idList = new ArrayList<Integer>();
        Stream.of(args[8].split(",")).map(Integer::valueOf).forEach(idList::add);
        logger.log(Level.CONFIG, String.format("Process Neighbors ID's are %s", Arrays.toString(idList.toArray())));

        multicastSocket = new MSocket(multicastPort, multicastAddress);

        logger.log(Level.WARNING, "Server is LOCKED...");

        unlock();

        USocket socket = new USocket(port);
        USocket socketAck = new USocket(port + 1);

        logger.log(Level.CONFIG, "Initializing EventManager");
        EventManager clock = (new EventManagerBuilder()
                .setProcessId(id)
                .setClockPosition(position)
                .setClockSize(serverList.size() + 1)
                .setSocket(socket)
                .setAckSocket(socketAck)
                .build());

        logger.log(Level.INFO, "Initializing ServerListener Thread");
        (new ServerListenerBuilder()
                .setEventManager(clock)
                .setUnicastSocket(socket)
                .build())
                .start();

        logger.log(Level.INFO, "Initializing ServerSender Thread");
        (new ServerSenderBuilder()
                .setChance(chance)
                .setEventManager(clock)
                .setEvents(events)
                .setMinDelay(minDelay)
                .setMaxDelay(maxDelay)
                .setServerList(serverList)
                .setIdList(idList)
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

    public static class ServerBuilder {
        String id;
        String port;
        String position;
        String chance;
        String events;
        String minDelay;
        String maxDelay;

        String serverList;
        String idList;

        public ServerBuilder setId(String id) {
            this.id = id;
            return this;
        }

        public ServerBuilder setServerList(String serverList) {
            this.serverList = serverList;
            return this;
        }

        public ServerBuilder setIdList(String idList) {
            this.idList = idList;
            return this;
        }


        public ServerBuilder setPosition(String position) {
            this.position = position;
            return this;
        }

        public ServerBuilder setPort(String port) {
            this.port = port;
            return this;
        }

        public ServerBuilder setChance(String chance) {
            this.chance = chance;
            return this;
        }

        public ServerBuilder setEvents(String events) {
            this.events = events;
            return this;
        }

        public ServerBuilder setMinDelay(String minDelay) {
            this.minDelay = minDelay;
            return this;
        }

        public ServerBuilder setMaxDelay(String maxDelay) {
            this.maxDelay = maxDelay;
            return this;
        }

        public void build() throws IOException {
            String[] arguments = { this.id, this.position, this.port, this.chance, this.events, this.minDelay,
                    this.maxDelay, this.serverList, this.idList };
            Server.main(arguments);
        }

    }
}