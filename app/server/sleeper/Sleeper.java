package app.server.sleeper;

import java.io.InputStream;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Sleeper {
  /**
   * Main static logger for class
   */
  static final Logger logger = Logger.getLogger(Sleeper.class.getName());
  static {
    try {
      InputStream stream = Sleeper.class.getClassLoader()
          .getResourceAsStream("app/logging.properties");
      LogManager.getLogManager().readConfiguration(stream);
    } catch (Exception ex) {
      ex.printStackTrace();
    }
  }

  /**
   * Will generate a random value between minDelay and maxDelay, and perform
   * {@link Thread#sleep(long)} based in the random value
   * 
   * @param minDelay Minimum delay value
   * @param maxDelay Maximum delay value
   * @throws InterruptedException
   */
  public static void performSleep(int minDelay, int maxDelay) throws InterruptedException {
    logger.log(Level.FINER, String.format("Min delay is %d. Max delay is %d.", minDelay, maxDelay));
    int delay = new Random().nextInt(maxDelay + 1 - minDelay) + minDelay;
    logger.log(Level.FINER, String.format("Thread sleeping for %d seconds", delay));
    Thread.sleep(delay);
  }
}
