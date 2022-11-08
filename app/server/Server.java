package app.server;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
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

/**
 * Main class used to instantiate {@link ServerListener} and
 * {@link ServerSender} threads
 */
public class Server {
    /**
     * Main static logger for class
     */
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
    /**
     * Multicast Socket to join group
     */
    static MSocket multicastSocket;
    /**
     * Multicast port to bind socket
     */
    static int multicastPort;
    /**
     * Multicast address to bind socket
     */
    static String multicastAddress;

    /**
     * @param args
     * @throws IOException
     */
    /**
     * Main method for {@link Server}.
     * This method will instantiate the {@link ServerListener} and
     * {@link ServerSender} based in the args values. args must contains the
     * following format
     * args[0] is server id
     * args[1] is server clock position (Used by {@link EventManager} clock, so it
     * will knows the local position)
     * args[2] is server port
     * args[3] is Chance value
     * args[4] is Events value
     * args[5] is Minimum delay valye
     * args[6] is Maximum delay value
     * args[7] is List of neighbors port value
     * args[8] is List of neighbors id values
     * 
     * 
     * Flow is:
     * It will capture all values sent throught args value.
     * It will stay in LOCKED position, and wait for a Multicast packet so it can be
     * unlocked.
     * It will configure internal sockets
     * It will finally instantiate {@link ServerListener} and {@link ServerSender}
     * threads
     * 
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {

        logger.log(Level.INFO, "Initializing server...");
        multicastAddress = "230.0.0.0"; // need to change to a config file =)
        multicastPort = 5000;

        int id = Integer.parseInt(args[0]);
        logger.log(Level.INFO, String.format("Id is %d", id));

        String address = args[1];
        int position = Integer.parseInt(args[2]);
        logger.log(Level.INFO, String.format("Clock Position is %d", position));

        int port = Integer.parseInt(args[3]);
        logger.log(Level.CONFIG, String.format("Server port is %d", port));

        double chance = Double.parseDouble(args[4]);
        logger.log(Level.CONFIG, String.format("Chance for a remote event is %f", chance));

        int events = Integer.parseInt(args[5]);
        logger.log(Level.CONFIG, String.format("Amount of events to be fired is %d", events));

        int minDelay = Integer.parseInt(args[6]);
        int maxDelay = Integer.parseInt(args[7]);
        logger.log(Level.CONFIG, String.format("Range for delay values is %d to %d", minDelay, maxDelay));

        List<Integer> serverList;
        serverList = new ArrayList<Integer>();
        Stream.of(args[8].split(",")).map(Integer::valueOf).forEach(serverList::add);
        logger.log(Level.CONFIG,
                String.format("Process Neighbors ports are %s", Arrays.toString(serverList.toArray())));

        List<Integer> idList;
        idList = new ArrayList<Integer>();
        Stream.of(args[9].split(",")).map(Integer::valueOf).forEach(idList::add);
        logger.log(Level.CONFIG, String.format("Process Neighbors ID's are %s", Arrays.toString(idList.toArray())));

        List<String> addressList;
        addressList = new ArrayList<String>();
        Stream.of(args[10].split(",")).map(String::valueOf).forEach(addressList::add);
        logger.log(Level.CONFIG,
                String.format("Process Neighbors addresses are %s", Arrays.toString(addressList.toArray())));

        waitForUnlock();

        USocket socket = new USocket(port, InetAddress.getByName(address));
        USocket socketAck = new USocket(port + 1);

        logger.log(Level.CONFIG, "Initializing EventManager");
        EventManager eventManager = (new EventManagerBuilder()
                .setProcessId(id)
                .setClockPosition(position)
                .setEvents(events)
                .setClockSize(serverList.size() + 1)
                .setSocket(socket)
                .setAckSocket(socketAck)
                .build());

        ServerListener listener = (new ServerListenerBuilder()
                .setEventManager(eventManager)
                .setUnicastSocket(socket)
                .setAddress(address)
                .build());

        ServerSender sender = (new ServerSenderBuilder()
                .setChance(chance)
                .setEventManager(eventManager)
                .setMinDelay(minDelay)
                .setMaxDelay(maxDelay)
                .setServerList(serverList)
                .setIdList(idList)
                .setAddressesList(addressList)
                .build());

        logger.log(Level.INFO, "Initializing ServerListener Thread");
        listener.start();
        // listener.join();
        logger.log(Level.INFO, "Initializing ServerSender Thread");
        sender.start();
        // sender.join();

        // logger.log(Level.INFO, String.format("Number of Events is 0."));
        // logger.log(Level.INFO, String.format("Final clock status is %s",
        // eventManager.toString()));
        // logger.log(Level.INFO, String.format("Ending process..."));
        // System.exit(0);
    }

    /**
     * Method to listen Multicast socket until a "SETUP" package is sent.
     * While "SETUP" package is not sent, it will timeout, and will kept the
     * "LOCKED" status
     */
    public static void waitForUnlock() {
        multicastSocket = new MSocket(multicastPort, multicastAddress);
        logger.log(Level.WARNING, "Server is LOCKED...");

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

    /**
     * Main {@link Server} builder
     */
    public static class ServerBuilder {
        private String id;
        private String port;
        private String position;
        private String chance;
        private String events;
        private String minDelay;
        private String maxDelay;
        private String address;

        private String serverList;
        private String idList;
        private String addressList;

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

        public ServerBuilder setAddressList(String addressList) {
            this.addressList = addressList;
            return this;
        }

        public ServerBuilder setAddress(String address) {
            this.address = address;
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

        public void build() throws IOException, InterruptedException {
            String[] arguments = { this.id, this.address, this.position, this.port, this.chance, this.events,
                    this.minDelay,
                    this.maxDelay, this.serverList, this.idList, this.addressList };
            Server.main(arguments);
        }

    }
}