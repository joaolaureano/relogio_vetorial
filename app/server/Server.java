package app.server;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import app.server.ServerSender.ServerSenderBuilder;
import app.server.ServerListener;
import app.server.ServerListener.ServerListenerBuilder;
import app.server.clock.ClockManager;
import app.server.event.EventManager;
import app.socket.multicast.MSocket;
import app.socket.multicast.MSocket.MSocketPayload;
import app.socket.unicast.USocket;
import app.server.event.EventManager.EventManagerBuilder;

public class Server {
    static MSocket multicastSocket;
    static int multicastPort;
    static String multicastAddress;

    public static void main(String[] _args) throws IOException {
        multicastAddress = "230.0.0.0"; // need to change to a config file =)
        multicastPort = 5000;
        String[] args = { "1", "1", "5001", "25", "100" ,"100", "200", "1235" };

        int id = Integer.parseInt(args[0]);

        int position = Integer.parseInt(args[1]);

        int port = Integer.parseInt(args[2]);

        double chance = Double.parseDouble(args[3]);
        int events = Integer.parseInt(args[4]);

        int minDelay = Integer.parseInt(args[5]);
        int maxDelay = Integer.parseInt(args[6]);

        List<Integer> serverList;
        serverList = new ArrayList<Integer>();
        Stream.of(args[7].split(",")).map(Integer::valueOf).forEach(serverList::add);

        multicastSocket = new MSocket(multicastPort, multicastAddress);
        System.out.println("LOCKED..." + port);
        unlock();
        USocket socket = new USocket(port);
        USocket socketAck = new USocket(port + 1);
        EventManager clock = (new EventManagerBuilder()
                .setClockPosition(position)
                .setClockSize(serverList.size() + 1)
                .setSocket(socketAck)
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
                    System.out.println("UNLOCKED :)");
                    System.out.println(String.format("%d:%d:%d.%d", Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                            Calendar.getInstance().get(Calendar.MINUTE),
                            Calendar.getInstance().get(Calendar.SECOND),
                            Calendar.getInstance().get(Calendar.MILLISECOND)));
                    return;
                }
            } catch (Exception e) {
                System.out.print(".");
            }
        }
    }

}