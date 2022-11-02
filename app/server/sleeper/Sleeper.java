package app.server.sleeper;

import java.util.Random;

public class Sleeper {    

    public static void performSleep(int minDelay, int maxDelay) throws InterruptedException{
        int delay = new Random().nextInt(maxDelay + 1 - minDelay) + minDelay;
        System.out.println(String.format("Sleeping for %d time...", delay));
        Thread.sleep(delay);

    }
}
