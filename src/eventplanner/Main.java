package eventplanner;

import java.util.List;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

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
            // TODO: test login and register on webpage (API needed)
            // TODO: show available events on webpage (API needed)

            // test register, login, show available events in command-line
            Scanner scanner = new Scanner(System.in);
            UserService userService = new UserService(dbService);
            AvailableEventsService availableEventsService = new AvailableEventsService(dbService);
            
            while(true) {
            	System.out.println("Login or Register? l to login, r to register,s to show all available events, c to close,\nand lre to leave a review for event");
                String choice = scanner.nextLine().trim();
                System.out.println(choice);
	            if(choice.equals("l")) {
	            	login(scanner, userService);
	            } else if(choice.equals("r")) {
	            	register(scanner,userService); 
	            } else if(choice.equals("lr")) {
	            	leaveReview(scanner,userService);
	            } else if(choice.equals("s")) {
                    showAvailableEvents(scanner, availableEventsService);
                } else if(choice.equals("c")) {
	            	break;
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

    /*private static void leaveEventReview(Scanner scanner, UserService userService) {
    	System.out.println("\n=== LEAVE REVIEW ===");
    	System.out.print("Enter Email: ");
    	String email = scanner.nextLine();

    	System.out.print("Enter Event Name: ");
    	String eventName = scanner.nextLine();

        System.out.print("Enter Event Start Time (YYYY-MM-DD HH:MM): ");
        String eventStartDate = scanner.nextLine();

        System.out.print("Enter Venue Name: ");
        String venueName = scanner.nextLine();

    	System.out.print("Enter Title (Can be empty): ");
    	String title = scanner.nextLine();

    	System.out.print("Enter Rating (1-5): ");
    	int rating = scanner.nextInt();
    	scanner.nextLine();  

    	System.out.print("Enter Description (Can be empty): ");
    	String desc = scanner.nextLine();

        boolean registered = userService.leaveEventReview(email, eventStartDate, eventStartDate, venueName, title, rating, desc);
        if (registered) {
            System.out.println("Review successful!");
        } else {
            System.out.println("Review failed.");
        }
	}*/

    private static void showAvailableEvents(Scanner scanner, AvailableEventsService availableEventsService) {
        System.out.println("\n=== Available Public Events ===");
        List<String> availableEvents = availableEventsService.getAvailableEvents();
        for (String event : availableEvents) {
            System.out.println(event);
        }
    }

    private static void leaveReview(Scanner scanner, UserService userService) {
        System.out.println("\n=== LEAVE REVIEW ===");
        System.out.print("Enter PersonID: ");
        int personID = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter VenueID: ");
        Integer venueID = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter EventID: ");
        Integer eventID = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Title (Can be empty): ");
        String title = scanner.nextLine();

        System.out.print("Enter Rating (1-5): ");
        int rating = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter Description (Can be empty): ");
        String desc = scanner.nextLine();

        System.out.print("Enter PostedOn (MM-DD-YYYY HH:MM:SS.DDD): ");
        String postedOn = scanner.nextLine();

        boolean registered = userService.leaveReview(personID, venueID, eventID, title, rating, desc, postedOn);
        if (registered) {
            System.out.println("Review successful!");
        } else {
            System.out.println("Review failed.");
        }

    }

	private static void login(Scanner scanner, UserService userService) {
    	 System.out.println("\n=== USER LOGIN ===");
         System.out.print("Enter Email: ");
         String loginEmail = scanner.nextLine();
         System.out.print("Enter Password: ");
         String loginPassword = scanner.nextLine();

         boolean loginSuccess = userService.loginUser(loginEmail, loginPassword);
         if (loginSuccess) {
             System.out.println("Login successful!");
         } else {
             System.out.println("Login failed. Invalid email or password.");
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
    
    private static void register(Scanner scanner, UserService userService) {
    	System.out.println("\n=== USER REGISTRATION ===");
        System.out.print("Enter Email: ");
        String email = scanner.nextLine();
        System.out.print("Enter Phone Number: ");
        String phoneNo = scanner.nextLine();
        System.out.print("Enter First Name: ");
        String firstName = scanner.nextLine();
        System.out.print("Enter Middle Initial (or press Enter to skip): ");
        String middleInit = scanner.nextLine();
        System.out.print("Enter Last Name: ");
        String lastName = scanner.nextLine();
        System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
        String dob = scanner.nextLine();
        System.out.print("Enter Credit Card Number (or press Enter to skip): ");
        String ccNum = scanner.nextLine();
        System.out.print("Enter CC Expiry Date (YYYY-MM-DD, or press Enter to skip): ");
        String ccExpDate = scanner.nextLine();
        System.out.print("Enter CVV (or press Enter to skip): ");
        String cvv = scanner.nextLine();
        System.out.print("Enter Password: ");
        String password = scanner.nextLine();

        boolean registered = userService.registerUser(email, phoneNo, firstName, middleInit, lastName, dob, ccNum, ccExpDate, cvv, password);

        if (registered) {
            System.out.println("Registration successful!");
        } else {
            System.out.println("Registration failed.");
        }
    }
}
