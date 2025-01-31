package eventplanner.services;

import eventplanner.models.Event;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AvailableEventsService {
    private DatabaseConnectionService dbService;

    public AvailableEventsService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

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
                Event event = new Event(
                    rs.getString("Name"),
                    rs.getTimestamp("StartTime").toString(),
                    rs.getTimestamp("EndTime").toString(),
                    rs.getInt("Price"),
                    rs.getString("VenueName"),
                    rs.getString("VenueAddress"),
                    rs.getInt("MaxCapacity"),
                    rs.getTimestamp("RegistrationDeadline").toString()
                );
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
//                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        System.out.println("Database returned " + events.size() + " events.");
        return events;
    }
}
