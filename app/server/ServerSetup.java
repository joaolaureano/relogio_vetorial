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
import java.util.stream.Stream;

import app.server.Server.ServerBuilder;

public class ServerSetup {
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
        logger.log(Level.CONFIG, String.format("ServerSetup is up."));

        List<String> lineList = Files.readAllLines(Paths.get(args[0]));
        String[] configLine = lineList.get(Integer.parseInt(args[1])).split("\\s");

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
