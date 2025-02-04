package eventplanner.services;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.*;

import eventplanner.models.Event;

public class EventsService {
    private DatabaseConnectionService dbService;

    public EventsService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

    // This method is for getting all available public events
    // For now, we can assume all available events are already payed
    // If we need improvement in the future, we can modify this method and related stored procedures
    public List<Event> getAvailableEvents() {
        List<Event> events = new ArrayList<>();
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

                Event event = new Event(
                    rs.getInt("Id"),
                    rs.getString("Name"),
                    dateFormat.format(new Date(rs.getTimestamp("StartTime").getTime())),
                    dateFormat.format(new Date(rs.getTimestamp("EndTime").getTime())),
                    rs.getInt("Price"),
                    rs.getInt("VenueId"),
                    rs.getString("VenueName"),
                    rs.getString("VenueAddress"),
                    rs.getInt("MaxCapacity"),
                    dateFormat.format(new Date(rs.getTimestamp("RegistrationDeadline").getTime())),
            true,
       true
                );
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                // if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        System.out.println("Database returned " + events.size() + " events.");
        return events;
    }

    // This method is used for getting all public events a user is registered
    // Again, we can still assume all events are already paied
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

    public boolean registerForEvent(int personId, int eventId) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            return false;
        }

        try {
            CallableStatement stmt = conn.prepareCall("{? = call RegisterForEvent(?, ?)}");
            stmt.registerOutParameter(1, Types.INTEGER);
            stmt.setInt(2, personId);
            stmt.setInt(3, eventId);
            stmt.execute();

            return true;

        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
            return false;
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
            System.err.println("Error logging in: " + e.getMessage());
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
                    -1,
                    rs.getString("VenueName"),
                    rs.getString("VenueAddress"),
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

    public boolean inviteUserToEvent(int eventId, int personId) {
        String sql = "{CALL InviteUserToEvent(?, ?)}";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, eventId);
            stmt.setInt(2, personId);
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


}
