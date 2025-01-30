package eventplanner;

import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import eventplanner.services.AvailableEventsService;
import eventplanner.services.UserService;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;

import eventplanner.services.DatabaseConnectionService;
import eventplanner.services.EncryptionServices;

public class Main {
    private static String serverUsername = null;
    private static String serverPassword = null;
    private static DatabaseConnectionService dbService = null;
    private static EncryptionServices es = new EncryptionServices();

    public static void main(String[] args) {
        Properties props = loadProperties();
        serverUsername = props.getProperty("serverUsername");
        serverPassword = props.getProperty("serverPassword");

        dbService = new DatabaseConnectionService(props.getProperty("serverName"), props.getProperty("databaseName"));

        if (!dbService.connect(serverUsername, serverPassword)) {
            System.err.println("Connection to database failed.");
            System.exit(1);
        }


        try {
            // TODO: test login and register on webpage (API required)
            UserService userService = new UserService(dbService);

            // test AvailableEventsService in command line
            // TODO: test AvailableEventsService on webpage (API required)
            AvailableEventsService availableEventsService = new AvailableEventsService(dbService);
            List<String> availableEvents = availableEventsService.getAvailableEvents();

            System.out.println("\n=== Available Public Events ===");
            if (availableEvents.isEmpty()) {
                System.out.println("No public events available.");
            } else {
                for (String event : availableEvents) {
                    System.out.println(event);
                }
            }

        } catch (Exception e) {
            System.err.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        } finally {
            dbService.closeConnection();
            System.out.println("\nDatabase connection closed.");
        }
    }

    public static Properties loadProperties() {
        String configPath = System.getProperty("user.dir") + "/EventPlannerApp.properties";
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        encryptor.setPassword(es.getEncryptionPassword());
        FileInputStream fis = null;
        EncryptableProperties props = new EncryptableProperties(encryptor);
        try {
            fis = new FileInputStream(configPath);
            props.load(fis);
        } catch (FileNotFoundException e) {
            System.out.println("EventPlannerApp.properties file not found.");
            e.printStackTrace();
            System.exit(1);
        } catch (IOException e) {
            System.out.println("EventPlannerApp.properties file could not be opened.");
            e.printStackTrace();
            System.exit(1);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    System.out.println("Input Stream could not be closed.");
                    e.printStackTrace();
                }
            }
        }
        return props;
    }
}
