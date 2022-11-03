package app.server;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import app.server.event.EventManager;
import app.server.event.EventManager.EventManagerBuilder;
import app.server.sleeper.Sleeper;

public class ServerEventSender extends Thread {
    protected EventManager eventManager;
    protected int minDelay, maxDelay;
    protected double chance;
    protected int events;
    protected List<Integer> serverList;

    ServerEventSender(int port, double chance, int events, int minDelay, int maxDelay, String serverList) {
        this.chance = chance / 100;
        this.events = events;
        this.minDelay = minDelay;
        this.maxDelay = maxDelay;

        this.serverList = new ArrayList<Integer>();
        Stream.of(serverList.split(",")).map(Integer::valueOf).forEach(this.serverList::add);

        this.eventManager = new EventManagerBuilder()
                .setClockSize(this.serverList.size())
                .setClockPosition(0)
                .setPort(port)
                .build();
    }

    public void run() {

        while (true) {
            try {
                Sleeper.performSleep(minDelay, maxDelay);
                nextEvent();
                if (events == 0) {
                    System.out.println("\nOver :)");
                    System.out.println(String.format("Clock result is %s", this.eventManager.toString()));
                    return;
                }

            } catch (Exception e) {
                System.out.print(".");

            }
        }
    }

    public void nextEvent() {
        double nextChance = Math.random();
        if (nextChance <= chance) {
            // remote_event()
            System.out.print("|");
        } else {
            // local_event()
            this.eventManager.local();
        }
        events--;
    }

}
