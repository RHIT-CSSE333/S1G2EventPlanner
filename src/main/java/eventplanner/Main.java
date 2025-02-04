package eventplanner;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eventplanner.models.Event;
import eventplanner.models.Venue;
import eventplanner.services.DatabaseConnectionService;
import eventplanner.services.EncryptionServices;
import eventplanner.services.EventsService;
import eventplanner.services.UserService;
import eventplanner.services.VenuesService;
import freemarker.template.Configuration;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.rendering.template.JavalinFreemarker;

public class Main {

    private static String serverUsername = null;
    private static String serverPassword = null;
    private static DatabaseConnectionService dbService = null;
    private static EncryptionServices es = new EncryptionServices();
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

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
            app.get("/venues", Main::handleVenues);
            app.get("/event/{id}/register", Main::handleEventRegister);
            app.get("/event/{id}/cancel", Main::handleCancelEventRegistration);
            app.get("/event/{id}/invite", Main::handleInvitePage);
            app.get("/myevents", Main::handleMyEvents);
            app.get("/hostedevents", Main::handleHostedEvents);
            app.get("/venue/{id}", Main::handleVenuePage);
            app.get("/event/{id}/review", ctx -> ctx.render("/review.ftl", Map.of("error", "")));
            app.get("/venue/{id}/review", ctx -> ctx.render("/review.ftl", Map.of("error", "")));
            app.get("/venue/{id}/addevent", Main::handlePublicEvent);
            app.get("/event/{id}/invitees-rsvp-status", Main::handleInviteesRSVPStatus);
            app.get("/inbox", Main::handleInbox);
            app.get("/payment", Main::handlePayment);


            app.post("/login", Main::handleLogin);
            app.post("/signup", Main::handleSignup);
            app.post("/venue/{id}/addevent", Main::handleAddEventPost);
            app.post("/event/{id}/review", Main::handleAddReview);
            app.post("/venue/{id}/review", Main::handleAddVenue);
            app.post("/event/{id}/invite", Main::handleInvite);
            app.post("/privateevent/{id}/rsvp", Main::handleRSVP);

            app.events(event -> {
                event.handlerAdded(handler -> {
                    System.out.println("Registered route: " + handler.getHttpMethod() + " " + handler.getPath());
                });
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void handlePayment(@NotNull Context ctx) {

    }

    private static void handleRSVP(@NotNull Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        int rsvpStatus = Integer.parseInt(ctx.formParam("rsvpStatus")); // 0 = Yes, 1 = No

        EventsService eventsService = new EventsService(dbService);
        Event event = eventsService.getEventById(eventId);

        if (rsvpStatus == 0) {  // User accepted the invitation
            if (event.getPrice() > 0) {
                // Paid Event, so redirect to payment age
                ctx.redirect("/payment?eventId=" + eventId);
            } else {
                // Free Event, so register the event directly
                boolean success = eventsService.updateRSVPStatus(userId, eventId, rsvpStatus);
                if (success) {
                    ctx.render("success.ftl");
                } else {
                    ctx.result("Failed to update RSVP status.");
                }
            }
        } else {
            // User rejected the invitation
            boolean success = eventsService.updateRSVPStatus(userId, eventId, rsvpStatus);
            if (success) {
                ctx.render("success.ftl");
            } else {
                ctx.result("Failed to update RSVP status.");
            }
        }
    }

    private static void handleInbox(@NotNull Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        EventsService eventsService = new EventsService(dbService);
        List<Map<String, Object>> invitations = eventsService.getInvitationsForUser(userId);

        ctx.render("inbox.ftl", Map.of("invitations", invitations));
    }

    private static void handleInviteesRSVPStatus(@NotNull Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        EventsService eventsService = new EventsService(dbService);
        Event event = eventsService.getEventById(eventId);

        if (event.getIsPublic()) {
            ctx.result("Public events do not have RSVP status to display.");
            return;
        }

        List<Map<String, Object>> invitees = eventsService.getInviteesRSVPStatus(eventId);

        ctx.render("rsvp_status.ftl", Map.of(
                "event", event,
                "invitees", invitees
        ));
    }

    private static void handleInvite(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));

        UserService userService = new UserService(dbService);
        EventsService eventsService = new EventsService(dbService);

        int personId = userService.getUserIdByEmail(ctx.formParam("email"));

        if (personId == -1) {
            ctx.render("invite.ftl", Map.of("error", "User not found.", "eventId", eventId));
            return;
        }

        boolean success = eventsService.inviteUserToEvent(eventId, personId);

        if (success) {
            ctx.render("success.ftl", Map.of("message", "Invitation sent successfully."));
        } else {
            ctx.render("invite.ftl", Map.of("error", "Failed to send invitation.", "eventId", eventId));
        }
    }

    private static void handleInvitePage(Context ctx) {
        try {
            Integer userId = ctx.sessionAttribute("userId");
            if (userId == null) {
                ctx.redirect("/login");
                return;
            }

            int eventId = Integer.parseInt(ctx.pathParam("id"));
            EventsService eventsService = new EventsService(dbService);
            Event event = eventsService.getEventById(eventId);

            if (!dbService.isConnected()) {
                logger.error("Debugging: Database connection lost while handling invite page");
                ctx.status(500).result("Database connection error");
                return;
            }

            String errorMessage = null;
            if (event == null) {
                logger.error("Event not found with ID: {}", eventId);
                ctx.status(404).result("Event not found");
                return;
            }

            if (event.getIsPublic()) {
                errorMessage = "Invitations are only allowed for private events.";
            }

            // check registration date
            if (errorMessage == null) {
                Date registrationDeadline = event.getRegistrationDeadlineDate();
                Instant instant = Instant.now();
                Date currentUtcTime = Date.from(instant);

                if (registrationDeadline.before(currentUtcTime)) {
                    errorMessage = "The registration deadline has passed. You cannot invite users anymore.";
                }
            }

            ctx.render("invite.ftl", Map.of(
                    "event", event,
                    "error", errorMessage == null ? "" : errorMessage
            ));
            System.out.println("Handling event/{id}/invite request");
        } catch (Exception e) {
            logger.error("Error in handleInvitePage: ", e);
            ctx.status(500).result("Internal Server Error");
        }
    }

    private static void handleHostedEvents(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        EventsService eventsService = new EventsService(dbService);
        List<Event> activeEvents = eventsService.getHostedEvents(userId);

        ctx.render("hosted_events.ftl", Map.of(
                "events", activeEvents,
                "message", activeEvents.isEmpty() ? "You have no hosted events." : ""
        ));
        System.out.println("Handling /hostedevents request");
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

    private static void handleAddReview(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        int userId = user.intValue();
        UserService userService = new UserService(dbService);

        String title = ctx.formParam("title");
        int rating = Integer.parseInt(ctx.formParam("rating"));
        String desc = ctx.formParam("description");

        if (userService.leaveReview(userId, -1, eventId, title, rating, desc)) {
            ctx.render("success.ftl");
        } else {
            ctx.result("error");
        }
    }

    private static void handleAddVenue(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int venueId = Integer.parseInt(ctx.pathParam("id"));
        int userId = user.intValue();
        UserService userService = new UserService(dbService);

        String title = ctx.formParam("title");
        int rating = Integer.parseInt(ctx.formParam("rating"));
        String desc = ctx.formParam("description");

        if (userService.leaveReview(userId, venueId, -1, title, rating, desc)) {
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
        if (user == null) {
            ctx.redirect("/login");
            return;
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

    private static void handlePublicEvent(Context ctx) {
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        ctx.render("addevent.ftl", Map.of("error", ""));
    }

    private static void handleAddEventPost(Context ctx) {
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        String name = ctx.formParam("name");
        String eventType = ctx.formParam("event-type");
        String startTime = ctx.formParam("startTime");
        String endTime = ctx.formParam("endTime");
        String registrationDeadline = ctx.formParam("registrationDeadline");
        double price = Double.parseDouble(ctx.formParam("price"));
        Integer userId = ctx.sessionAttribute("userId");

        if (userId == null) {
            ctx.render("addevent.ftl", Map.of("error", "You must be logged in to create an event."));
            return;
        }

        VenuesService venuesService = new VenuesService(dbService);
        boolean success = true;

        try {
            if ("private".equals(eventType)) {
                success = venuesService.addPrivateEvent(userId, venueId, name, startTime, endTime, registrationDeadline, price);
            } else if ("public".equals(eventType)) {
                success = venuesService.addPublicEvent(venueId, name, startTime, endTime, registrationDeadline, price);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.render("addevent.ftl", Map.of("error", "Database error: " + e.getMessage()));
            return;
        }

        if (success) {
            ctx.render("success.ftl");
        } else {
            ctx.render("addevent.ftl", Map.of("error", "Error creating the event."));
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

    private static void handleVenues(Context ctx) {
        VenuesService venuesService = new VenuesService(dbService);
        List<Venue> venues = venuesService.getAllVenues();

        ctx.render("venues.ftl", Map.of(
                "venues", venues,
                "message", venues.isEmpty() ? "No available venues." : ""
        ));
    }

}
