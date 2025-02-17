package cc.blynk.server.dao;

import cc.blynk.common.utils.Config;
import cc.blynk.server.model.auth.User;
import cc.blynk.server.utils.Finder;
import cc.blynk.server.utils.JsonParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class responsible for saving/reading user data to/from disk.
 *
 * User: ddumanskiy
 * Date: 8/11/13
 * Time: 6:53 PM
 */
public class FileManager {

    private static final Logger log = LogManager.getLogger(FileManager.class);

    /**
     * Folder where all user profiles are stored locally.
     */
    private Path dataDir;

    public FileManager(String dataFolder) {
        if (dataFolder == null || "".equals(dataFolder)) {
            dataFolder = Paths.get(System.getProperty("java.io.tmpdir"), "blynk").toString();
        }
        try {
            this.dataDir = createDatadir(dataFolder);
        } catch (RuntimeException e) {
            this.dataDir = createDatadir(Paths.get(System.getProperty("java.io.tmpdir"), "blynk"));
        }

        log.info("Using data dir '{}'", dataDir);
    }

    private static Path createDatadir(String dataFolder) {
        Path dataDir = Paths.get(dataFolder);
        return createDatadir(dataDir);
    }

    private static Path createDatadir(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException ioe) {
            log.error("Error creating data folder '{}'", dataDir);
            throw new RuntimeException("Error creating data folder '" + dataDir + "'");
        }
        return dataDir;
    }

    private static User readUserFromFile(Path path) {
        try {
            return JsonParser.parseUserFromFile(path.toFile());
        } catch (IOException ioe) {
            log.error("Error parsing file '{}'.", path);
        }

        return null;
    }

    public Path getDataDir() {
        return dataDir;
    }

    public Path generateFileName(String userName) {
        return Paths.get(dataDir.toString(), "u_" + userName + ".user");
    }

    public void overrideUserFile(User user) throws IOException {
        Path file = generateFileName(user.getName());
        try (BufferedWriter writer = Files.newBufferedWriter(file, Config.DEFAULT_CHARSET)) {
            String userString = user.toString();

            writer.write(userString);
        }
    }

    /**
     * Loads all user profiles one by one from disk using dataDir as starting point.
     *
     * @return mapping between username and it's profile.
     */
    public Map<String, User> deserialize() {
        log.debug("Starting reading user DB.");
        Finder finder = new Finder();


        try {
            Files.walkFileTree(dataDir, finder);
        } catch (IOException e) {
            log.error("Error reading tmp files.", e);
        }

        Map<String, User> users = new ConcurrentHashMap<>();
        for (Path path : finder.getFoundFiles()) {
            User user = readUserFromFile(path);
            if (user != null) {
                users.putIfAbsent(user.getName(), user);
            }
        }

        log.debug("Reading user DB finished.");

        return users;
    }

}
