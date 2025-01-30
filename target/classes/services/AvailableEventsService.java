package eventplanner.services;

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

    public List<String> getAvailableEvents() {
        List<String> events = new ArrayList<>();
        String query = "{CALL ShowAvailableEvents}"; // Stored procedure call

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String event = String.format("Name: %s, Start: %s, End: %s, Price: $%d, Venue: %s, Address: %s, Max Capacity: %d, Registration Deadline: %s",
                        rs.getString("Name"),
                        rs.getTimestamp("StartTime"),
                        rs.getTimestamp("EndTime"),
                        rs.getInt("Price"),
                        rs.getString("VenueName"),
                        rs.getString("VenueAddress"),
                        rs.getInt("MaxCapacity"),
                        rs.getTimestamp("RegistrationDeadline"));
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing resources: " + e.getMessage());
            }
        }
        return events;
    }
}
