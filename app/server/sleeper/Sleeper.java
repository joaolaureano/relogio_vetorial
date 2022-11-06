package app.server.sleeper;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Sleeper {    
    
    static final Logger logger = Logger.getLogger(Sleeper.class.getName());

    public static void performSleep(int minDelay, int maxDelay) throws InterruptedException {
        logger.log(Level.FINE, String.format("Min delay is %d. Max delay is %d.", minDelay, maxDelay));
        int delay = new Random().nextInt(maxDelay + 1 - minDelay) + minDelay;
        logger.info(String.format("Thread sleeping for %d seconds", delay));
        Thread.sleep(delay);
    }
}
