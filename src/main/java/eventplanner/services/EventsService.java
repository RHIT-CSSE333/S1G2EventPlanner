package eventplanner.services;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import eventplanner.models.Event;
import eventplanner.models.Service;

public class EventsService {
    public class EventReturnType {
        public boolean success;
        public int eventId;
        public String errorMessage;

        public EventReturnType(boolean success, int eventId, String errorMessage) {
            this.success = success;
            this.eventId = eventId;
            this.errorMessage = errorMessage;
        }
    }

    public class EventFinancialInfoReturnType {
        public boolean success;
        public double price;
        public String paymentId;
        public String errorMessage;

        public EventFinancialInfoReturnType(boolean success, double price, String paymentId, String errorMessage) {
            this.success = success;
            this.price = price;
            this.paymentId = paymentId;
            this.errorMessage = errorMessage;
        }
    }

    public class EventSprocReturnType {
        public boolean success;
        public String errorMessage;

        public EventSprocReturnType(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    private DatabaseConnectionService dbService;

    public EventsService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

    // This method is for getting all available public events
    // For now, we can assume all available events are already paid
    // If we need improvement in the future, we can modify this method and related stored procedures
    public List<Map<String, Object>> getAvailableEvents() {
        List<Map<String, Object>> events = new ArrayList<>();
        String query = "{CALL ShowAvailableEvents}"; // Stored procedure call

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a, MMM d, yyyy");

                Map<String, Object> event = new HashMap<>();
                event.put("id", rs.getInt("ID"));
                event.put("name", rs.getString("Name"));
                event.put("startTime", dateFormat.format(new Date(rs.getTimestamp("StartTime").getTime())));
                event.put("endTime", dateFormat.format(new Date(rs.getTimestamp("EndTime").getTime())));
                event.put("price", rs.getInt("Price"));
                event.put("venueId", rs.getInt("VenueId"));
                event.put("venueName", rs.getString("VenueName"));
                event.put("venueAddress", rs.getString("VenueAddress"));
                event.put("maxCapacity", rs.getInt("MaxCapacity"));
                event.put("registrationDeadline", dateFormat.format(new Date(rs.getTimestamp("RegistrationDeadline").getTime())));
                event.put("remainingSeats", rs.getInt("RemainingSeats"));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        }
        System.out.println("Database returned " + events.size() + " events.");
        return events;
    }

    

    public EventReturnType createEvent(String name, String startTime, String endTime, int venueId, double price,
                                        String registrattionDeadline, int hostPersonId, String paymentId, boolean isPublic, String checkInId) {
        String query = "{call CreateEvent(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

            boolean paymentStatus = false;   // Not paid yet upon creation

            stmt.setString(1, name);
            stmt.setTimestamp(2, new Timestamp(inputFormat.parse(startTime).getTime()));
            stmt.setTimestamp(3, new Timestamp(inputFormat.parse(endTime).getTime()));
            stmt.setInt(4, venueId);
            stmt.setDouble(5, price);
            stmt.setTimestamp(6, new Timestamp(inputFormat.parse(registrattionDeadline).getTime()));
            stmt.setInt(7, hostPersonId);
            stmt.setBoolean(8, paymentStatus);
            stmt.setString(9, paymentId);
            stmt.setBoolean(10, isPublic);
            stmt.setString(11, checkInId);
            stmt.registerOutParameter(12, Types.INTEGER);

            stmt.execute();

            int eventId = stmt.getInt(12);
            System.out.println("Created Private Event with ID: " + eventId);

            return new EventReturnType(eventId > 0, eventId, "");

        } catch (SQLException | ParseException e) {
            System.err.println("Error creating private event: " + e.getMessage());
            return new EventReturnType(false, -1, e.getMessage());

        }
    }

    public EventFinancialInfoReturnType getFinancialInfoForHost(int eventId) {
        String query = "{call GetFinancialInfoForHost(?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setInt(1, eventId);
            stmt.registerOutParameter(2, Types.DECIMAL);
            stmt.registerOutParameter(3, Types.CHAR);

            stmt.execute();

            double price = stmt.getDouble(2);
            String paymentId = stmt.getString(3);

            return new EventFinancialInfoReturnType(true, price, paymentId, "");

        } catch (SQLException e) {
            System.err.println("Error creating private event: " + e.getMessage());
            return new EventFinancialInfoReturnType(false, -1, "", e.getMessage());

        }
    }

    public EventFinancialInfoReturnType getFinancialInfoForGuest(int eventId, int userId) {
        String query = "{call GetFinancialInfoForGuest(?, ?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setInt(1, eventId);
            stmt.setInt(2, userId);
            stmt.registerOutParameter(3, Types.DECIMAL);
            stmt.registerOutParameter(4, Types.CHAR);

            stmt.execute();

            double price = stmt.getDouble(3);
            String paymentId = stmt.getString(4);

            return new EventFinancialInfoReturnType(true, price, paymentId, "");

        } catch (SQLException e) {
            System.err.println("Error creating private event: " + e.getMessage());
            return new EventFinancialInfoReturnType(false, -1, "", e.getMessage());

        }
    }

    // This method is used for getting all events (public or private) a user is registered
    public List<Event> getEventsForUser(int userId) {
        List<Event> events = new ArrayList<>();
        String query = "{CALL GetEventsByPerson(?)}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a, MMM d, yyyy");

                Event event = new Event(
                    rs.getInt("Id"),
                    rs.getString("Name"),
                    dateFormat.format(new Date(rs.getTimestamp("StartTime").getTime())),
                    null,
                    -1,
                    rs.getInt("VenueId"),
                    rs.getString("VenueName"),
                    rs.getString("VenueAddress"),
                    -1,
                    null,
                        rs.getBoolean("IsPublic"),
                        true
                );

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        }

        return events;
    }

    public EventSprocReturnType registerForEvent(int personId, int eventId, String paymentId) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            return new EventSprocReturnType(false, "Internal Server Error (no connection to db)");
        }

        try {
            CallableStatement stmt = conn.prepareCall("{? = call RegisterForEvent(?, ?, ?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, personId);
            stmt.setInt(3, eventId);
            stmt.setString(4, paymentId);
            stmt.execute();

            return new EventSprocReturnType(true, "");

        } catch (SQLException e) {
            System.err.println("Error registering: " + e.getMessage());
            return new EventSprocReturnType(false, e.getMessage());
        }
    }

    public boolean cancelEventRegistration(int personId, int eventId) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            CallableStatement stmt = conn.prepareCall("{? = call CancelRegistration(?, ?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, personId);
            stmt.setInt(3, eventId);
            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error canceling registration: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelPrivateEventRegistration(int personId, int eventId) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            CallableStatement stmt = conn.prepareCall("{? = call CancelPrivateRegistration(?, ?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, personId);
            stmt.setInt(3, eventId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error cancelling private event registration: " + e.getMessage());
            return false;
        }
    }


    public List<Event> getUserAttended(int userId) {
        List<Event> events = new ArrayList<>();
        String query = "{CALL ShowAttendedEvents(?)}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, userId);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a, MMM d, yyyy");

                Event event = new Event(
                    rs.getInt("Id"),
                    rs.getString("Name"),
                    dateFormat.format(new Date(rs.getTimestamp("StartTime").getTime())),
                    null,
                    -1,
                    rs.getInt("VenueId"),
                    rs.getString("VenueName"),
                    rs.getString("Address"),
                    -1,
                    null,
                        true,
                        true
                );

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        }

        return events;
    }

    public List<Service> getEventServices(int eventID) {
        List<Service> services = new ArrayList<>();
        String query = "{CALL GetServicesByEvent(?)}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, eventID);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Service service = new Service(
                    -1,
                    rs.getString("ServiceName"),
                    rs.getString("Description"),
                    rs.getDouble("Price"),
                    -1
                );

                services.add(service);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching event services: " + e.getMessage());
        }

        return services;
    }

    public List<Event> getHostedEvents(int userId) {
        List<Event> events = new ArrayList<>();
        String query = "{CALL GetHostedEvents(?)}"; // call stored procedure

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Event event = new Event(
                        rs.getInt("Id"),
                        rs.getString("Name"),
                        rs.getTimestamp("StartTime").toString(),
                        rs.getTimestamp("EndTime").toString(),
                        rs.getDouble("Price"),
                        rs.getInt("VenueId"),
                        rs.getString("VenueName"),
                        rs.getString("VenueAddress"),
                        rs.getInt("MaxCapacity"),
                        rs.getTimestamp("RegistrationDeadline").toString(),
                        rs.getBoolean("isPublic"),
                        rs.getBoolean("PaymentStatus")
                );
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching active hosted events: " + e.getMessage());
        }
        return events;
    }

    public Event getEventById(int eventId) {
        String query = "{CALL GetEventById(?)}";
        Event event = null;

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                event = new Event(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getTimestamp("StartTime").toString(),
                        rs.getTimestamp("EndTime").toString(),
                        rs.getDouble("Price"),
                        rs.getInt("VenueId"),
                        rs.getString("VenueName"),
                        rs.getString("VenueAddress"),
                        rs.getInt("MaxCapacity"),
                        rs.getTimestamp("RegistrationDeadline").toString(),
                        rs.getBoolean("isPublic"),
                        rs.getBoolean("PaymentStatus")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching event by ID: " + e.getMessage());
        }
        return event;
    }

    public boolean inviteUserToEvent(int eventId, int personId, String paymentId) {
        String sql = "{CALL InviteUserToEvent(?, ?, ?)}";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, eventId);
            stmt.setInt(2, personId);
            stmt.setString(3, paymentId);
            stmt.execute();

            return true;
        } catch (SQLException e) {
            System.err.println("Error inviting user: " + e.getMessage());
            return false;
        }
    }

    public int getUserIdByEmail(String email) {
        String sql = "{CALL GetPersonIDByEmail(?, ?)}";
        int personId = -1;

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setString(1, email);
            stmt.registerOutParameter(2, Types.INTEGER);

            stmt.execute();

            personId = stmt.getInt(2);

        } catch (SQLException e) {
            System.err.println("Error fetching PersonID: " + e.getMessage());
        }

        return personId;
    }

    public List<Map<String, Object>> getInviteesRSVPStatus(int eventId) {
        List<Map<String, Object>> invitees = new ArrayList<>();
        String sql = "{CALL GetInviteesRSVPStatus(?)}";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> invitee = new HashMap<>();
                invitee.put("personId", rs.getInt("PersonID"));
                invitee.put("firstName", rs.getString("FirstName"));
                invitee.put("lastName", rs.getString("LastName"));
                invitee.put("email", rs.getString("Email"));
                invitee.put("rsvpStatus", rs.getInt("RSVPStatus"));
                invitees.add(invitee);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invitees: " + e.getMessage());
        }
        return invitees;
    }

    public List<Map<String, Object>> getInvitationsForUser(int personId) {
        List<Map<String, Object>> invitations = new ArrayList<>();
        String sql = "{CALL GetInvitationsForUser(?)}";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, personId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> invitation = new HashMap<>();
                invitation.put("eventId", rs.getInt("EventID"));
                invitation.put("eventName", rs.getString("EventName"));
                invitation.put("startTime", rs.getTimestamp("StartTime").toString());
                invitation.put("endTime", rs.getTimestamp("EndTime").toString());
                invitation.put("registrationDeadline", rs.getTimestamp("RegistrationDeadline").toString());
                invitation.put("rsvpStatus", rs.getInt("RSVPStatus"));
                invitation.put("paymentStatus", rs.getBoolean("PaymentStatus"));

                invitations.add(invitation);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching invitations: " + e.getMessage());
        }
        return invitations;
    }

    public boolean updateRSVPStatus(int personId, int eventId, int rsvpStatus) {
        String sql = "{CALL UpdateRSVPStatus(?, ?, ?)}";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, personId);
            stmt.setInt(2, eventId);
            stmt.setInt(3, rsvpStatus);

            stmt.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error updating RSVP status: " + e.getMessage());
            return false;
        }
    }

    public boolean addSuccessfulPaymentForHosts(String paymentId, int personId) {
        String query = "{call AddSuccessfulPaymentForHosts(?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setString(1, paymentId);
            stmt.setInt(2, personId);
            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error adding payment for host: " + e.getMessage());
            return false;

        }
    }

    public boolean addSuccessfulPaymentForGuests(String paymentId, int personId) {
        String query = "{call AddSuccessfulPaymentForGuests(?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setString(1, paymentId);
            stmt.setInt(2, personId);
            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error adding payment for guest: " + e.getMessage());
            return false;

        }
    }

    public boolean updatePaymentInfo(Integer userId, int eventId, boolean paymentStatus) {
        String query = "{call UpdateGuestPaymentStatus(?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setInt(1, userId);
            stmt.setInt(2, eventId);
            stmt.setBoolean(3, paymentStatus);
            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error updating payment status: " + e.getMessage());
            return false;

        }
    
    }

    public List<Event> getPastPublicEvents() {
        List<Event> events = new ArrayList<>();
        String query = "{CALL ShowPastPublicEvents}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a, MMM d, yyyy");

                Event event = new Event(
                        rs.getInt("ID"),
                        rs.getString("Name"),
                        dateFormat.format(new Date(rs.getTimestamp("StartTime").getTime())),
                        dateFormat.format(new Date(rs.getTimestamp("EndTime").getTime())),
                        rs.getInt("Price"),
                        rs.getInt("VenueID"),
                        rs.getString("VenueName"),
                        rs.getString("VenueAddress"),
                        rs.getInt("MaxCapacity"),
                        null,
                        true,
                        true
                );

                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching past public events: " + e.getMessage());
        }
        return events;
    }

    public List<Map<String, Object>> getEventReviews(int eventId) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        String query = "EXEC ShowEventReviews ?";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            while (rs.next()) {
                String formattedDate = rs.getTimestamp("PostedOn") != null
                        ? dateFormat.format(rs.getTimestamp("PostedOn"))
                        : "Unknown";
                Map<String, Object> review = new HashMap<>();
                review.put("reviewId", rs.getInt("ReviewID"));
                review.put("eventId", rs.getInt("EventID"));
                review.put("eventName", rs.getString("EventName"));
                review.put("title", rs.getString("Title") != null ? rs.getString("Title") : "No Title");
                review.put("rating", rs.getInt("Rating"));
                review.put("comment", rs.getString("Comment") != null ? rs.getString("Comment") : "No Comment");
                review.put("postedOn", formattedDate);
                review.put("reviewerName", rs.getString("ReviewerName"));

                reviews.add(review);
            }

        } catch (SQLException e) {
            System.err.println("Error fetching event reviews: " + e.getMessage());
        }
        return reviews;
    }

    public void addServiceToEvent(int eventId, int serviceId) {
        String query = "{call AddServiceToEvent(?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setInt(1, eventId);
            stmt.setInt(2, serviceId);
            stmt.execute();

        } catch (SQLException e) {
            System.err.println("Error adding service to event: " + e.getMessage());
        }
    }

    public boolean addPendingEventInvitation(String email, int eventId, String invitationId) {
        String query = "{? = call AddPendingEventInvitation(?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setString(2, email);
            stmt.setInt(3, eventId);
            stmt.setString(4, invitationId);
            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error adding service to event: " + e.getMessage());
            return false;
        }
    }

    private boolean completePendingInvitation(int personId, String invitationId, String paymentId) {
        String query = "{CALL CompletePendingInvitation(?, ?, ?)}";
        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, personId);
            stmt.setString(2, invitationId);
            stmt.setString(3, paymentId);

            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.out.println("completePendingInvitation: " + e.getMessage());
            return false;
        } 
    }

    /**
     * Complete all pending invitations for a user
     * 
     * @param personId
     * @param email
     * @return 0 on successful completion of all invitations, 1 on failing to complete some of them, 2 on overall procecure fail
     */
    public int completePendingInvitations(int personId, String email) {
        String query = "{CALL GetPendingInvitations(?)}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        int returnCode = 0;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setString(1, email);

            rs = stmt.executeQuery();

            while (rs.next()) {
                String invitationId = rs.getString("InvitationId");
                
                if (!completePendingInvitation(personId, invitationId,  HelperService.generateRandomIdOfLength50())) {
                    returnCode = 1;
                }
            }
        } catch (SQLException e) {
            returnCode = 2;
        }

        return returnCode;
    }

    public String getCheckInId(int personId, int eventId) {
        String query = "{CALL GetCheckInId(?, ?, ?)}";
        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, personId);
            stmt.setInt(2, eventId);
            stmt.registerOutParameter(3, Types.CHAR);

            stmt.execute();

            return stmt.getString(3);

        } catch (SQLException e) {
            System.out.println("getCheckInId: " + e.getMessage());
            return null;
        }
    }

    public EventSprocReturnType checkIn(Integer personId, String checkInId) {
        String query = "{CALL CheckIn(?, ?)}";
        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, personId);
            stmt.setString(2, checkInId);

            stmt.execute();

            return new EventSprocReturnType(true, "");

        } catch (SQLException e) {
            System.out.println("EventCheckInReturnType: " + e.getMessage());
            return new EventSprocReturnType(false, e.getMessage());
        }
    }

    public int getRemainingSeatsForPrivateEvent(int eventId) {
        String sql = "{CALL GetRemainingSeatsForPrivateEvent(?)}";
        int remainingSeats = -1;

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, eventId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                remainingSeats = rs.getInt("RemainingSeats");
            }
        } catch (SQLException e) {
            System.err.println("Error fetching remaining seats for private event: " + e.getMessage());
        }
        return remainingSeats;
    }

}
