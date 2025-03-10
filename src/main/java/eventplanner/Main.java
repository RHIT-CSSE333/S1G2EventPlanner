package eventplanner;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.properties.EncryptableProperties;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eventplanner.models.Event;
import eventplanner.models.Service;
import eventplanner.models.User;
import eventplanner.models.Vendor;
import eventplanner.models.Venue;
import eventplanner.services.DatabaseConnectionService;
import eventplanner.services.EncryptionServices;
import eventplanner.services.EventsService;
import eventplanner.services.EventsService.EventFinancialInfoReturnType;
import eventplanner.services.EventsService.EventReturnType;
import eventplanner.services.EventsService.EventSprocReturnType;
import eventplanner.services.HelperService;
import eventplanner.services.UserService;
import eventplanner.services.UserService.RegisterUserReturnType;
import eventplanner.services.UserService.UserSprocReturnType;
import eventplanner.services.VendorService;
import eventplanner.services.VenuesService;
import freemarker.template.Configuration;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.rendering.template.JavalinFreemarker;

public class Main {

    // test

    private static final String appUrl = "http://localhost:7070/";

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

            app.get("/", Main::handleIndex);
            app.get("/login", ctx -> ctx.render("/login.ftl", Map.of("error", "")));
            app.get("/signup", Main::handleSignupIII);
            app.get("/logout", Main::handleLogout);
            app.get("/events", Main::handleEvents);
            app.get("/venues", Main::handleVenues);
            app.get("/event/{id}/register", Main::handleEventRegister);
            app.get("/event/{id}/cancel", Main::handleCancelEventRegistration);
            app.get("/event/{id}/invite", Main::handleInvitePage);
            app.get("/event/{id}/reviews", Main::handleEventReviews);
            app.get("/myevents", Main::handleMyEvents);
            app.get("/hostedevents", Main::handleHostedEvents);
            app.get("/venue/{id}", Main::handleVenuePage);
            app.get("/venue/{id}/reviews", Main::handleVenueReviews);
            app.get("/event/{id}/review", ctx -> ctx.render("/review.ftl", Map.of("error", "")));
            app.get("/venue/{id}/review", ctx -> ctx.render("/review.ftl", Map.of("error", "")));
            app.get("/venue/{id}/addevent", Main::handlePublicEvent);
            app.get("/event/{id}/invitees-rsvp-status", Main::handleInviteesRSVPStatus);
            app.get("/inbox", Main::handleInbox);
            app.get("/personalinfo", Main::handlePersonalInfo);
            app.get("/info/updateName", Main::handleUpdateNameGet);
            app.get("/info/updateEmail", Main::handleUpdateEmailGet);
            app.get("/info/updatePhoneNo", Main::handleUpdatePhoneNoGet);
            app.get("/pastevents", Main::handlePastEvents);
            app.get("/pay/host/{eventId}", Main::handlePayForHosts);
            app.get("/pay/guest/{eventId}", Main::handlePayForGuests);
            app.get("/paymentsuccess/host/", Main::handlePaymentSuccessForHosts);
            app.get("/paymentsuccess/guest/", Main::handlePaymentSuccessForGuests);
            app.get("/transactions", Main::handleTransactions);
            app.get("/vendors", Main::handleVendors);
            app.get("/vendors/{id}/services", Main::handleServices);
            app.get("/event/{eventId}/services", Main::handleEventServices);
            app.get("/qr/{eventId}", Main::handleShowCheckInQR);
            app.get("/checkin/{checkInId}", Main::handleCheckIn); 
            app.post("/info/updateName", Main::handleUpdateName);
            app.post("/info/updateEmail", Main::handleUpdateEmail);
            app.post("/info/updatePhoneNo", Main::handleUpdatePhoneNo);
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

    private static void handleEventServices(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        EventsService eventsService = new EventsService(dbService);

        List<Service> services = eventsService.getEventServices(eventId);

        ctx.render("eventservices.ftl", Map.of("services", services, 
        "message", services.isEmpty() ? "No available services at the moment." : ""));
    }

    private static void handleIndex(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.render("/index.ftl");
        } else {
            ctx.render("/main.ftl");
        }
    }

    private static void handleCheckIn(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        String checkInId = ctx.pathParam("checkInId");

        EventsService eventsService = new EventsService(dbService);
        EventSprocReturnType eventCheckInReturn = eventsService.checkIn(userId, checkInId);

        if (eventCheckInReturn.success) {
            ctx.render("success.ftl", Map.of("message", "Checked in successfully."));
        } else {
            ctx.render("error.ftl", Map.of("error", eventCheckInReturn.errorMessage));
        }
    }

    private static void handleShowCheckInQR(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("eventId"));

        EventsService eventsService = new EventsService(dbService);

        String checkInId = eventsService.getCheckInId(userId, eventId);

        if (checkInId != null) {
            ctx.render("/qr.ftl", Map.of("checkInId", checkInId));

        } else {
            ctx.render("error.ftl", Map.of("error", "You are not authorized to check in for this event"));
        }
    }

    private static void handleSignupIII(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user != null) {
            ctx.redirect("/");
            return;
        }

        String invitationId = ctx.queryParam("via");

        if (invitationId == null) {
            ctx.render("/register.ftl", Map.of("email", "", "phone", "", "firstName", "", "middleInit", "", "lastName", "", "dob", "", "error", ""));

        } else {
            UserService userService = new UserService(dbService);
            String email = userService.getEmailForPendingInvitation(invitationId);

            if (email == null) {
                ctx.render("/register.ftl", Map.of("error", "Invitation not found", "email", "", "phone", "", "firstName", "", "middleInit", "", "lastName", "", "dob", ""));
            } else {
                ctx.render("/register.ftl", Map.of("error", "",  "email", email, "phone", "", "firstName", "", "middleInit", "", "lastName", "", "dob", ""));
            }
        }

    }

    private static void handleServices(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int vendorId = Integer.parseInt(ctx.pathParam("id"));
        VendorService vendorService = new VendorService(dbService);

        List<Service> services = vendorService.getVendorServices(vendorId);

        ctx.render("services.ftl", Map.of("services", services, 
        "message", services.isEmpty() ? "No available services at the moment." : ""));
    }

    private static void handleVendors(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        VendorService vendorService = new VendorService(dbService);

        List<Vendor> vendors = vendorService.getAllVendors();

        ctx.render("vendors.ftl", Map.of("vendors", vendors, 
        "message", vendors.isEmpty() ? "No available venues at the moment." : ""));



    }

    private static void handleVenueReviews(@NotNull Context ctx) {
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        VenuesService venuesService = new VenuesService(dbService);

        List<Map<String, Object>> reviews = venuesService.getVenueReviews(venueId);

        ctx.render("venue_reviews.ftl", Map.of(
                "reviews", reviews,
                "venueId", venueId,
                "message", reviews.isEmpty() ? "No reviews available for this venue." : ""
        ));
    }

    private static void handleEventReviews(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        EventsService eventsService = new EventsService(dbService);
        List<Map<String, Object>> reviews = eventsService.getEventReviews(eventId);

        ctx.render("event_reviews.ftl", Map.of(
                "reviews", reviews,
                "eventId", eventId,
                "message", reviews.isEmpty() ? "No reviews available for this event." : ""
        ));
    }

    private static void handleTransactions(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);

        List<Map<String, Object>> transactions = userService.getTransactions(userId);
        ctx.render("/transactions.ftl", Map.of("transactions", transactions));
    }

    private static void handlePersonalInfo(@NotNull Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }
    
        UserService userService = new UserService(dbService);
        User user = userService.getUserData(userId);
    
        if (user == null) {
            ctx.render("personalinfo.ftl", Map.of("message", "Create an account to get started!"));
        } else {
            ctx.render("personalinfo.ftl", Map.of("user", user, "message", "", "userSpecific", true));
        }
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
                ctx.redirect("/pay/guest/" + eventId);
            } else {
                // Free Event, so register the event directly
                boolean success = eventsService.updateRSVPStatus(userId, eventId, rsvpStatus);
                success = success && eventsService.updatePaymentInfo(userId, eventId, true);
                if (success) {
                    ctx.render("success.ftl");
                } else {
                    ctx.render("error.ftl", Map.of("error", "Failed to update RSVP status or paymentinfo."));
                }
            }
        } else {
            // User rejected the invitation
            boolean success = eventsService.updateRSVPStatus(userId, eventId, rsvpStatus);
            if (success) {
                ctx.render("success.ftl");
            } else {
                ctx.render("error.ftl", Map.of("error", "Failed to update RSVP status."));
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
            ctx.render("error.ftl", Map.of("error", "Public events do not have RSVP status to display."));
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
        String email = ctx.formParam("email");

        UserService userService = new UserService(dbService);
        EventsService eventsService = new EventsService(dbService);

        int personId = userService.getUserIdByEmail(email);

        boolean success = false;

        if (personId == -1) {
            String invitationId = HelperService.generateRandomIdOfLength50();
            success = eventsService.addPendingEventInvitation(email, eventId, invitationId);

            if (success) {
                String url = "mailto:" 
                                + email
                                + "?subject=You're invited to an event!"
                                + "&body=Sign up for Event Planner at "
                                + appUrl + "signup?via=" + invitationId
                                + " to view your invitation";

                ctx.redirect(url);

            }

        } else {
            success = eventsService.inviteUserToEvent(eventId, personId, HelperService.generateRandomIdOfLength50());
        }

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
                ctx.render("error.ftl", Map.of("error", "Database connection error"));
                return;
            }

            String errorMessage = null;
            if (event == null) {
                logger.error("Event not found with ID: {}", eventId);
                ctx.render("error.ftl", Map.of("error", "Event not found"));
                return;
            }

            if (event.getIsPublic()) {
                errorMessage = "Invitations are only allowed for private events.";
            }

            int remainingSeats = eventsService.getRemainingSeatsForPrivateEvent(eventId);

            // check registration date
            if (errorMessage == null) {
                Date registrationDeadline = event.getRegistrationDeadlineDate();
                Instant instant = Instant.now();
                Date currentUtcTime = Date.from(instant);

                if (registrationDeadline.before(currentUtcTime)) {
                    errorMessage = "The registration deadline has passed. You cannot invite users anymore.";
                }
            }

            if (errorMessage == null && !event.isPaymentStatus()) { // check if host has paid venue fees
                errorMessage = "You need to pay first before inviting people.";
            }

            ctx.render("invite.ftl", Map.of(
                    "event", event,
                    "remainingSeats", remainingSeats,
                    "error", errorMessage == null ? "" : errorMessage
            ));
            System.out.println("Handling event/{id}/invite request");
        } catch (Exception e) {
            logger.error("Error in handleInvitePage: ", e);
            ctx.render("error.ftl", Map.of("error", e.getMessage()));
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
        Integer userId = ctx.sessionAttribute("userId");
        if (userId != null) {
            ctx.redirect("/");
            return;
        }

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
        Integer userId = ctx.sessionAttribute("userId");
        if (userId != null) {
            ctx.redirect("/");
            return;
        }

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

        if (!hasError) {

            UserService userService = new UserService(dbService);
            EventsService eventsService = new EventsService(dbService);

            RegisterUserReturnType returnVal = userService.registerUser(email, phone, firstName, middleInit, lastName, dob, password);

            if (returnVal.personId != -1) {
                int pendingInvitationsStatus = eventsService.completePendingInvitations(returnVal.personId, email);

                if (pendingInvitationsStatus == 0) {
                    ctx.redirect("/login");
        
                } else if (pendingInvitationsStatus == 1) {
                    hasError = true;
                    error.append("<li>Registration completed successfully, but we weren't able to register you for events. Contact the person who invited you. Please <a href='/login'>login</a>.");
                
                } else {
                    hasError = true;
                    error.append("<li>Registration completed successfully, but registering failed. Contact the developers. Please <a href='/login'>login</a>.");
                
                }
            
            } else {
                hasError = true;
                error.append("<li>" + returnVal.errorMsg + "</li>");
            }
        }

        if (hasError) {
            ctx.render("register.ftl", Map.of("email", email, "phone", phone, "firstName", firstName, "middleInit", middleInit, "lastName", lastName, "dob", dob, "error", error.toString()));
        }
    }

    private static void handleLogout(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        ctx.req().getSession().invalidate();
        ctx.redirect("/");
    }

    private static void handlePastEvents(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        EventsService eventsService = new EventsService(dbService);
        List<Event> events = eventsService.getUserAttended(user);

        System.out.println("Handling /pastevents request...");

        ctx.render("pastevents.ftl", Map.of("events", events, 
                                        "message", events.isEmpty() ? "No available events at the moment." : "",
                                        "userSpecific", true));
    }

    private static void handleEvents(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        EventsService eventsService = new EventsService(dbService);
        List<Map<String, Object>> futureEvents = eventsService.getAvailableEvents();
        List<Event> pastEvents = eventsService.getPastPublicEvents();

        ctx.render("events.ftl", Map.of(
                "events", futureEvents,
                "pastEvents", pastEvents,
                "message", futureEvents.isEmpty() && pastEvents.isEmpty() ? "No available events at the moment." : "",
                "userSpecific", false
        ));
    }


    private static void handleEventRegister(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        int userId = user.intValue();
        EventsService eventsService = new EventsService(dbService);

        EventSprocReturnType returnVal = eventsService.registerForEvent(userId, eventId, HelperService.generateRandomIdOfLength50());

        if (returnVal.success) {
            ctx.render("success.ftl");
        } else {
            ctx.render("error.ftl", Map.of("error", returnVal.errorMessage));
        }
    }

    private static void handleUpdateNameGet(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);
        User userObj = userService.getUserData(user);

        if (userObj == null) {
            ctx.render("updateinfo.ftl", Map.of("type", "name", "error", "User not found"));
        } else {
            ctx.render("updateinfo.ftl", Map.of("type", "name", "user", userObj, "error", ""));
        }
    }

    public static void handleUpdateEmailGet(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);
        User userObj = userService.getUserData(user);

        if (userObj == null) {
            ctx.render("updateinfo.ftl", Map.of("type", "email", "error", "User not found"));
        } else {
            ctx.render("updateinfo.ftl", Map.of("type", "email", "user", userObj, "error", ""));
        }
    }

    public static void handleUpdatePhoneNoGet(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);
        User userObj = userService.getUserData(user);

        if (userObj == null) {
            ctx.render("updateinfo.ftl", Map.of("type", "phone", "error", "User not found"));
        } else {
            ctx.render("updateinfo.ftl", Map.of("type", "phone", "user", userObj, "error", ""));
        }
    }

    private static void handleUpdateName(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);
        User userObj = userService.getUserData(user);
        String newFirst = ctx.formParam("firstName");
        String newM = ctx.formParam("Minit");
        String newLast = ctx.formParam("lastName");
        if (userService.updateName(user, newFirst, newM, newLast)) {
            ctx.redirect("/personalinfo");
        } else {
            ctx.render("updateinfo.ftl", Map.of("type", "name", "user", userObj, "error", "Failed to update name"));
        }
    }

    private static void handleUpdateEmail(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);
        User userObj = userService.getUserData(user);
        String newEmail = ctx.formParam("email");
        if (userService.updateEmail(user, newEmail)) {
            ctx.redirect("/personalinfo");
        } else {
            ctx.render("updateinfo.ftl", Map.of("type", "email", "user", userObj, "error", "Email already in use"));
        }
    }
    private static void handleUpdatePhoneNo(@NotNull Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        UserService userService = new UserService(dbService);
        User userObj = userService.getUserData(user);
        String newPhoneNo = ctx.formParam("phoneNo");
        if (userService.updatePhoneNo(user, newPhoneNo)) {
            ctx.redirect("/personalinfo");
        } else {
            ctx.render("updateinfo.ftl", Map.of("type", "phone", "user", userObj, "error", "Phone number already in use"));
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
        if(ctx.formParam("rating") == null || ctx.formParam("rating").isEmpty()) {
            ctx.render("review.ftl", Map.of("error", "Rating cannot be empty"));
            return;
        }
        int rating = Integer.parseInt(ctx.formParam("rating"));
        String desc = ctx.formParam("description");

        UserSprocReturnType returnVal = userService.leaveReview(userId, -1, eventId, title, rating, desc);

        if (returnVal.success) {
            ctx.render("success.ftl");
        } else {
            ctx.render("review.ftl", Map.of("error", returnVal.errorMsg));
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
        if(ctx.formParam("rating") == null || ctx.formParam("rating").isEmpty()) {
            ctx.render("review.ftl", Map.of("error", "Rating cannot be empty"));
            return;
        }
        int rating = Integer.parseInt(ctx.formParam("rating"));
        String desc = ctx.formParam("description");

        UserSprocReturnType returnVal = userService.leaveReview(userId, venueId, -1, title, rating, desc);

        if (returnVal.success) {
            ctx.render("success.ftl");
        } else {
            ctx.render("review.ftl", Map.of("error", returnVal.errorMsg));
        }
    }

    private static void handleCancelEventRegistration(Context ctx) {
        Integer user = ctx.sessionAttribute("userId");
        if (user == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("id"));
        int userId = user.intValue();
        EventsService eventsService = new EventsService(dbService);
        Event event = eventsService.getEventById(eventId);

        boolean success;
        if (event.getIsPublic()) {
            // cancel public event
            success = eventsService.cancelEventRegistration(userId, eventId);
        } else {
            // cancel private event
            success = eventsService.cancelPrivateEventRegistration(userId, eventId);
        }

        if (success) {
            ctx.render("success.ftl");
        } else {
            ctx.render("error.ftl", Map.of("error", "Failed to cancel registration"));
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
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        VenuesService venuesService = new VenuesService(dbService);

        Venue venue = venuesService.getVenue(venueId);
        System.out.println("Venue fetched: " + venue);

        List<Event> eventsForVenue = venuesService.getEventsForVenue(venueId);

        ctx.render("venue.ftl", Map.of("venue", venue,
                                        "events", eventsForVenue, 
                                        "message", eventsForVenue.isEmpty() ? "This venue doesn't have any events posted yet" : ""));
    }

    private static void handlePublicEvent(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        VendorService vendorService = new VendorService(dbService);
        List<Service> services = vendorService.getAllServices();

        ctx.render("addevent.ftl", Map.of("error", "",  
                            "services", services));
    }

    private static void handleAddEventPost(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.render("addevent.ftl", Map.of("error", "You must be logged in to create an event."));
            return;
        }
        
        int venueId = Integer.parseInt(ctx.pathParam("id"));
        String name = ctx.formParam("name");
        String eventType = ctx.formParam("event-type");
        String startTime = ctx.formParam("startTime");
        String endTime = ctx.formParam("endTime");
        String registrationDeadline = ctx.formParam("registrationDeadline");
        double price = Double.parseDouble(ctx.formParam("price"));
        
        EventsService eventsService = new EventsService(dbService);
        EventReturnType eventCreated = null;
        String paymentId = null;
        String checkInId = null;

        try {
            paymentId = HelperService.generateRandomIdOfLength50();
            checkInId = HelperService.generateRandomIdOfLength50();

            if ("private".equals(eventType)) {
                eventCreated = eventsService.createEvent(name, startTime, endTime, venueId, price, registrationDeadline, userId, paymentId, false, checkInId);

            } else if ("public".equals(eventType)) {
                eventCreated = eventsService.createEvent(name, startTime, endTime, venueId, price, registrationDeadline, userId, paymentId, true, checkInId);

            }

            if (eventCreated.success) {
                int eventId = eventCreated.eventId;
                int serviceCount = Integer.parseInt(ctx.formParam("serviceCount"));
                for (int i = 0; i < serviceCount; i++) {
                    String serviceIdParam = "services[" + i + "].id";
                    String serviceIdStr = ctx.formParam(serviceIdParam);
                    if (serviceIdStr != null && !serviceIdStr.isEmpty()) {
                        int serviceId = Integer.parseInt(serviceIdStr);
                        eventsService.addServiceToEvent(eventId, serviceId);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            ctx.render("addevent.ftl", Map.of("error", "Database error: " + e.getMessage()));
            return;
        }

        if (eventCreated.success) {
            if (price != 0)
            ctx.redirect("/pay/host/" + String.valueOf(eventCreated.eventId));
            else
            ctx.redirect("/hostedevents");

        } else {
            ctx.render("addevent.ftl", Map.of("error", eventCreated.errorMessage));
        }
    }

    private static void handlePayForHosts(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        
        EventsService eventsService = new EventsService(dbService);
        EventFinancialInfoReturnType eventFinancialInfo = eventsService.getFinancialInfoForHost(eventId);

        if (!eventFinancialInfo.success) {
            ctx.render("error.ftl", Map.of("error", "Error getting financial info: " + eventFinancialInfo.errorMessage));
        } else {
            String url = buildPaymentUrlForHosts(eventFinancialInfo.paymentId, eventFinancialInfo.price);
            ctx.redirect(url);
        }
    }

    private static void handlePayForGuests(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        int eventId = Integer.parseInt(ctx.pathParam("eventId"));
        
        EventsService eventsService = new EventsService(dbService);
        EventFinancialInfoReturnType eventFinancialInfo = eventsService.getFinancialInfoForGuest(eventId, userId);

        if (!eventFinancialInfo.success) {
            ctx.render("error.ftl", Map.of("error", "Error getting financial info: " + eventFinancialInfo.errorMessage));
        } else {
            String url = buildPaymentUrlForGuests(eventFinancialInfo.paymentId, eventFinancialInfo.price);
            ctx.redirect(url);
        }
    }

    private static void handlePaymentSuccessForHosts(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        String paymentId = ctx.queryParam("item");
        String paymentConfirmationId = ctx.queryParam("confirmation");

        if (isPaymentConfirmationValid(paymentConfirmationId)) {
            EventsService eventsService = new EventsService(dbService);
            if (eventsService.addSuccessfulPaymentForHosts(paymentId, userId)) {
                ctx.redirect("/hostedevents");
            } else {
                ctx.render("error.ftl", Map.of("error", "Payment was not successful"));
            }
            
        } else {
            ctx.render("error.ftl", Map.of("error", "Payment doesn't exist. Please pay again"));
        }
    }

    private static void handlePaymentSuccessForGuests(Context ctx) {
        Integer userId = ctx.sessionAttribute("userId");
        if (userId == null) {
            ctx.redirect("/login");
            return;
        }

        String paymentId = ctx.queryParam("item");

        System.out.println(paymentId);
        String paymentConfirmationId = ctx.queryParam("confirmation");

        if (isPaymentConfirmationValid(paymentConfirmationId)) {
            EventsService eventsService = new EventsService(dbService);
            if (eventsService.addSuccessfulPaymentForGuests(paymentId, userId)) {
                ctx.redirect("/inbox");
            } else {
                ctx.render("error.ftl", Map.of("error", "Payment was not successful"));
            }
            
        } else {
            ctx.render("error.ftl", Map.of("error", "Payment does not exist. Please pay again"));
        }
    }

    private static String buildPaymentUrlForHosts(String paymentId, double price) {
        final String PAYMENT_HOST_URL = "http://localhost:5000/pay/";
        final String CALLBACK_URL = "http://localhost:7070/paymentsuccess/host/";

        String url = PAYMENT_HOST_URL + "?callback=" + CALLBACK_URL + "&amount=" + String.valueOf(price)
                            + "&item=" + paymentId;
        
        return url;
    }

    private static String buildPaymentUrlForGuests(String paymentId, double price) {
        final String PAYMENT_HOST_URL = "http://localhost:5000/pay/";
        final String CALLBACK_URL = "http://localhost:7070/paymentsuccess/guest/";

        String url = PAYMENT_HOST_URL + "?callback=" + CALLBACK_URL + "&amount=" + String.valueOf(price)
                            + "&item=" + paymentId;
        
        return url;
    }

    private static boolean isPaymentConfirmationValid(String paymentConfirmarion) {
        try {
            URL url = new URL("http://localhost:5000/api/verifypayment/" + paymentConfirmarion);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            
            if (conn.getResponseCode() != 200) {
                System.out.println("Failed: HTTP error code: " + conn.getResponseCode());
                return false;
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
            StringBuilder response = new StringBuilder();
            String output;
            while ((output = br.readLine()) != null) {
                response.append(output);
            }
            conn.disconnect();
            
            JSONObject jsonResponse = new JSONObject(response.toString());
            return jsonResponse.optBoolean("valid", false);
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
