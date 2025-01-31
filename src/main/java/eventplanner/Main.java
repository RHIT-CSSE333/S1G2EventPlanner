package eventplanner;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import eventplanner.models.Event;
import eventplanner.models.Venue;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.http.staticfiles.Location;
import io.javalin.rendering.template.JavalinFreemarker;
import io.javalin.rendering.template.JavalinThymeleaf;
import eventplanner.services.EventsService;
import eventplanner.services.UserService;
import eventplanner.services.VenuesService;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Configuration;

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
        try {
            setUpDatabase();
            System.out.println("Starting server.");

            Javalin app = Javalin.create(config -> {
                config.staticFiles.add("/public");
                config.fileRenderer(new JavalinFreemarker(getFreemarkerConfiguration()));
            }).start(7070);

            app.get("/", ctx -> ctx.render("/index.ftl"));
            app.get("/login", ctx -> ctx.render("/login.ftl", Map.of("error", "")));
            app.get("/signup", ctx -> ctx.render("/register.ftl", Map.of("error", "")));
            app.get("/logout", Main::handleLogout);
            app.get("/events", Main::handleEvents);
            app.get("/event/{id}/register", Main::handleEventRegister);
            app.get("/event/{id}/cancel", Main::handleCancelEventRegistration);
            app.get("/myevents", Main::handleMyEvents);
            app.get("/venue/{id}", Main::handleVenuePage);
            app.get("/venue/{id}/addpublic", Main::handleAddPublicEvent);

            app.post("/login", Main::handleLogin);
            app.post("/signup", Main::handleSignup);
            app.post("/venue/{id}/addpublic", Main::handleAddPublicEventPost);

            app.events(event -> {
                event.handlerAdded(handler -> {
                    System.out.println("Registered route: " + handler.getHttpMethod() + " " + handler.getPath());
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void handleLogin(Context ctx) {
        String email = ctx.formParam("email");
        String password = ctx.formParam("password");

        UserService userService = new UserService(dbService);

        if (userService.loginUser(email, password)) {
            ctx.sessionAttribute("userId", userService.getUserIdByEmail(email));
            ctx.redirect("/events");

        } else {
            ctx.render("login.ftl", Map.of("error", "Authentication error"));
        }
    }

    private static void handleSignup(Context ctx) {
        boolean hasError = false;
        StringBuilder error = new StringBuilder();
        
        String email = ctx.formParam("email");
        String phone = ctx.formParam("phone");
        String firstName = ctx.formParam("firstName");
        String middleInit = ctx.formParam("middleInit");
        String lastName = ctx.formParam("lastName");
        String dob = ctx.formParam("dob");
        String password = ctx.formParam("password");
        String confirmPassword = ctx.formParam("confirmPassword");

        if (email.isBlank() || phone.isBlank() || firstName.isBlank() || lastName.isBlank() || dob.isBlank()
                || (middleInit.isBlank() && !middleInit.isEmpty()) || password.isBlank() || confirmPassword.isBlank()) {
            hasError = true;
            error.append("<li>Fields cannot be empty or only contain spaces</li>");
        }

        if (!confirmPassword.equals(password)) {
            hasError = true;
            error.append("<li>Passwords should match</li>");
        }

        UserService userService = new UserService(dbService);

        if (userService.registerUser(email, phone, firstName, middleInit, lastName, dob, null, null, null, password)) {
            ctx.redirect("/login");

        } else {
            hasError = true;
            error.append("<li>Registration failed on the database side</li>");
        }

        if (hasError) {
            ctx.render("register.ftl", Map.of("error", error.toString()));
        }
    }

    private static void handleLogout(Context ctx) {
        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    private static void handleEvents(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
        }

        EventsService eventsService = new EventsService(dbService);
        List<Event> events = eventsService.getAvailableEvents();

        System.out.println("Handling /events request...");

        ctx.render("events.ftl", Map.of("events", events, 
                                        "message", events.isEmpty() ? "No available events at the moment." : "",
                                        "userSpecific", false));

    }

    private static void handleEventRegister(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        int userId = user.intValue();
        EventsService eventsService = new EventsService(dbService);

        if (eventsService.registerForEvent(userId, eventId)) {
            ctx.render("success.ftl");
        } else {
            ctx.result("error");
        }
    }

    private static void handleCancelEventRegistration(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        int userId = user.intValue();
        EventsService eventsService = new EventsService(dbService);

        if (eventsService.cancelEventRegistration(userId, eventId)) {
            ctx.render("success.ftl");
        } else {
            ctx.result("error");
        }
    }

    private static void handleMyEvents(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {        // TODO: refirect doesn't work
            ctx.redirect("/login");
        }

        int userId = user.intValue();
        EventsService eventsService = new EventsService(dbService);
        List<Event> events = eventsService.getEventsForUser(userId);

        ctx.render("events.ftl", Map.of("events", events, 
                                        "message", events.isEmpty() ? "You haven't signed up for any events yet" : "",
                                        "userSpecific", true));
    }

    private static void handleVenuePage(Context ctx) {
        // TODO: convert to UTC on server when adding event
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        VenuesService venuesService = new VenuesService(dbService);

        Venue venue = venuesService.getVenue(venueId);
        List<Event> eventsForVenue = venuesService.getEventsForVenue(venueId);

        ctx.render("venue.ftl", Map.of("venue", venue,
                                        "events", eventsForVenue, 
                                        "message", eventsForVenue.isEmpty() ? "This venue doesn't have any events posted yet" : ""));
    }

    private static void handleAddPublicEvent(Context ctx) {
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        ctx.render("addevent.ftl", Map.of("error", ""));
    }

    private static void handleAddPublicEventPost(Context ctx) {
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        String name = ctx.formParam("name");
        String startTime = ctx.formParam("startTime");
        String endTime = ctx.formParam("endTime");
        String registrationDeadline = ctx.formParam("registrationDeadline");
        int price = Integer.parseInt(ctx.formParam("price"));
        
        VenuesService venuesService = new VenuesService(dbService);

        if (venuesService.addPublicEvent(venueId, name, startTime, endTime, registrationDeadline, price)) {
            ctx.render("success.ftl");
        } else {
            ctx.render("addevent.ftl", Map.of("error", "Error creating an event"));
        }
    }

    private static void setUpDatabase() {
        Properties props = loadProperties();
        serverUsername = props.getProperty("serverUsername");
        serverPassword = props.getProperty("serverPassword");

        dbService = new DatabaseConnectionService(props.getProperty("serverName"), props.getProperty("databaseName"));

        if (!dbService.connect(serverUsername, serverPassword)) {
            System.err.println("Connection to database failed.");
            System.exit(1);
        }
    }

    private static Configuration getFreemarkerConfiguration() {
        Configuration freemarkerConfig = new Configuration(Configuration.VERSION_2_3_31);
        freemarkerConfig.setClassLoaderForTemplateLoading(Main.class.getClassLoader(), "/templates");

        return freemarkerConfig;
    }

    private static Properties loadProperties() {
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



    // public static void main(String[] args) {
    //     Properties props = loadProperties();
    //     serverUsername = props.getProperty("serverUsername");
    //     serverPassword = props.getProperty("serverPassword");

    //     dbService = new DatabaseConnectionService(props.getProperty("serverName"), props.getProperty("databaseName"));

    //     if (!dbService.connect(serverUsername, serverPassword)) {
    //         System.err.println("Connection to database failed.");
    //         System.exit(1);
    //     }


    //     try {
    //         // TODO: test login and register on webpage (API needed)
    //         // TODO: show available events on webpage (API needed)

    //         // test register, login, show available events in command-line
    //         Scanner scanner = new Scanner(System.in);
    //         UserService userService = new UserService(dbService);
    //         AvailableEventsService availableEventsService = new AvailableEventsService(dbService);
            
    //         while(true) {
    //         	System.out.println("Login or Register? l to login, r to register,s to show all available events, c to close,\nand lre to leave a review for event");
    //             String choice = scanner.nextLine().trim();
    //             System.out.println(choice);
	//             if(choice.equals("l")) {
	//             	login(scanner, userService);
	//             } else if(choice.equals("r")) {
	//             	register(scanner,userService); 
	//             } else if(choice.equals("lr")) {
	//             	leaveReview(scanner,userService);
	//             } else if(choice.equals("s")) {
    //                 showAvailableEvents(scanner, availableEventsService);
    //             } else if(choice.equals("c")) {
	//             	break;
	//             }
    //         }

    //     } catch (Exception e) {
    //         System.err.println("An error occurred: " + e.getMessage());
    //         e.printStackTrace();
    //     } finally {
    //         dbService.closeConnection();
    //         System.out.println("\nDatabase connection closed.");
    //     }
    // }

    // /*private static void leaveEventReview(Scanner scanner, UserService userService) {
    // 	System.out.println("\n=== LEAVE REVIEW ===");
    // 	System.out.print("Enter Email: ");
    // 	String email = scanner.nextLine();

    // 	System.out.print("Enter Event Name: ");
    // 	String eventName = scanner.nextLine();

    //     System.out.print("Enter Event Start Time (YYYY-MM-DD HH:MM): ");
    //     String eventStartDate = scanner.nextLine();

    //     System.out.print("Enter Venue Name: ");
    //     String venueName = scanner.nextLine();

    // 	System.out.print("Enter Title (Can be empty): ");
    // 	String title = scanner.nextLine();

    // 	System.out.print("Enter Rating (1-5): ");
    // 	int rating = scanner.nextInt();
    // 	scanner.nextLine();  

    // 	System.out.print("Enter Description (Can be empty): ");
    // 	String desc = scanner.nextLine();

    //     boolean registered = userService.leaveEventReview(email, eventStartDate, eventStartDate, venueName, title, rating, desc);
    //     if (registered) {
    //         System.out.println("Review successful!");
    //     } else {
    //         System.out.println("Review failed.");
    //     }
	// }*/

    // private static void showAvailableEvents(Scanner scanner, AvailableEventsService availableEventsService) {
    //     System.out.println("\n=== Available Public Events ===");
    //     List<String> availableEvents = availableEventsService.getAvailableEvents();
    //     for (String event : availableEvents) {
    //         System.out.println(event);
    //     }
    // }

    // private static void leaveReview(Scanner scanner, UserService userService) {
    //     System.out.println("\n=== LEAVE REVIEW ===");
    //     System.out.print("Enter PersonID: ");
    //     int personID = scanner.nextInt();
    //     scanner.nextLine();

    //     System.out.print("Enter VenueID: ");
    //     Integer venueID = scanner.nextInt();
    //     scanner.nextLine();

    //     System.out.print("Enter EventID: ");
    //     Integer eventID = scanner.nextInt();
    //     scanner.nextLine();

    //     System.out.print("Enter Title (Can be empty): ");
    //     String title = scanner.nextLine();

    //     System.out.print("Enter Rating (1-5): ");
    //     int rating = scanner.nextInt();
    //     scanner.nextLine();

    //     System.out.print("Enter Description (Can be empty): ");
    //     String desc = scanner.nextLine();

    //     System.out.print("Enter PostedOn (MM-DD-YYYY HH:MM:SS.DDD): ");
    //     String postedOn = scanner.nextLine();

    //     boolean registered = userService.leaveReview(personID, venueID, eventID, title, rating, desc, postedOn);
    //     if (registered) {
    //         System.out.println("Review successful!");
    //     } else {
    //         System.out.println("Review failed.");
    //     }

    // }

	// private static void login(Scanner scanner, UserService userService) {
    // 	 System.out.println("\n=== USER LOGIN ===");
    //      System.out.print("Enter Email: ");
    //      String loginEmail = scanner.nextLine();
    //      System.out.print("Enter Password: ");
    //      String loginPassword = scanner.nextLine();

    //      boolean loginSuccess = userService.loginUser(loginEmail, loginPassword);
    //      if (loginSuccess) {
    //          System.out.println("Login successful!");
    //      } else {
    //          System.out.println("Login failed. Invalid email or password.");
    //      }
	// }

	// public static Properties loadProperties() {
    //     String configPath = System.getProperty("user.dir") + "/EventPlannerApp.properties";
    //     StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
    //     encryptor.setPassword(es.getEncryptionPassword());
    //     FileInputStream fis = null;
    //     EncryptableProperties props = new EncryptableProperties(encryptor);
    //     try {
    //         fis = new FileInputStream(configPath);
    //         props.load(fis);
    //     } catch (FileNotFoundException e) {
    //         System.out.println("EventPlannerApp.properties file not found.");
    //         e.printStackTrace();
    //         System.exit(1);
    //     } catch (IOException e) {
    //         System.out.println("EventPlannerApp.properties file could not be opened.");
    //         e.printStackTrace();
    //         System.exit(1);
    //     } finally {
    //         if (fis != null) {
    //             try {
    //                 fis.close();
    //             } catch (IOException e) {
    //                 System.out.println("Input Stream could not be closed.");
    //                 e.printStackTrace();
    //             }
    //         }
    //     }
    //     return props;
    // }
    
    // private static void register(Scanner scanner, UserService userService) {
    // 	System.out.println("\n=== USER REGISTRATION ===");
    //     System.out.print("Enter Email: ");
    //     String email = scanner.nextLine();
    //     System.out.print("Enter Phone Number: ");
    //     String phoneNo = scanner.nextLine();
    //     System.out.print("Enter First Name: ");
    //     String firstName = scanner.nextLine();
    //     System.out.print("Enter Middle Initial (or press Enter to skip): ");
    //     String middleInit = scanner.nextLine();
    //     System.out.print("Enter Last Name: ");
    //     String lastName = scanner.nextLine();
    //     System.out.print("Enter Date of Birth (YYYY-MM-DD): ");
    //     String dob = scanner.nextLine();
    //     System.out.print("Enter Credit Card Number (or press Enter to skip): ");
    //     String ccNum = scanner.nextLine();
    //     System.out.print("Enter CC Expiry Date (YYYY-MM-DD, or press Enter to skip): ");
    //     String ccExpDate = scanner.nextLine();
    //     System.out.print("Enter CVV (or press Enter to skip): ");
    //     String cvv = scanner.nextLine();
    //     System.out.print("Enter Password: ");
    //     String password = scanner.nextLine();

    //     boolean registered = userService.registerUser(email, phoneNo, firstName, middleInit, lastName, dob, ccNum, ccExpDate, cvv, password);

    //     if (registered) {
    //         System.out.println("Registration successful!");
    //     } else {
    //         System.out.println("Registration failed.");
    //     }
    // }
}
