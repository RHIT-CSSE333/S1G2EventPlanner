package eventplanner.services;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
}
