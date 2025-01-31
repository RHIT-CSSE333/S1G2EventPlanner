package eventplanner.services;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import eventplanner.models.Event;

import java.text.SimpleDateFormat;
import java.util.Date;

import eventplanner.models.Venue;


public class VenuesService {
    private DatabaseConnectionService dbService;

    public VenuesService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

    public Venue getVenue(int venueId) {
        Venue result = null;

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall("{call GetVenueInfo(?)}");
            stmt.setInt(1, venueId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                result = new Venue(
                    rs.getInt("Id"),
                    rs.getString("Name"),
                    rs.getInt("MaxCapacity"),
                    rs.getInt("PricingType"),
                    rs.getInt("Price"),
                    rs.getString("State"),
                    rs.getString("City"),
                    rs.getString("StreetAddress"),
                    rs.getInt("ZipCode")
                );

            }

        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        }

        return result;
    }

    public List<Event> getEventsForVenue(int venueId) {
        List<Event> events = new ArrayList<>();
        String query = "{call GetEventsForVenue(?)}";

        Connection conn = null;
        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            stmt.setInt(1, venueId);
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
                    dateFormat.format(new Date(rs.getTimestamp("RegistrationDeadline").getTime()))
                );
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        } 

        return events;
    }
}
