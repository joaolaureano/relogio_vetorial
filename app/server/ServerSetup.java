package app.server;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import app.server.Server.ServerBuilder;

/**
 * This class is used as a helper to instantiate all servers based in the config
 * file
 */
public class ServerSetup {
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
     * Main method for setup
     * 
     * @param args This MUST contains the arguments to run properly. In position
     *             args[0], must be sent name of the config text file. In position
     *             args[1], must send the line number of config file to be
     *             instantiate the server
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        logger.log(Level.CONFIG, String.format("ServerSetup is up."));

        List<String> lineList = Files.readAllLines(Paths.get(args[0]));
        String[] configLine = lineList.get(Integer.parseInt(args[1])).split("\\s");

        /*
         * Below block will pass all the servers neighbors(ID-PORT) to communicate with
         */
        List<String> idList = new ArrayList<>();
        List<String> portList = new ArrayList<>();
        for (String line : lineList) {
            String[] splitLine = line.split("\\s");
            if (splitLine[2].equals(configLine[2]))
                continue;
            idList.add(splitLine[0]);
            portList.add(splitLine[2]);
        }

        (new ServerBuilder()
                .setId(configLine[0])
                .setPosition(args[1])
                .setPort(configLine[2])
                .setChance(configLine[3])
                .setEvents(configLine[4])
                .setMinDelay(configLine[5])
                .setMaxDelay(configLine[6])
                .setServerList(String.join(",", portList))
                .setIdList(String.join(",", idList)))
                .build();
    }
}
