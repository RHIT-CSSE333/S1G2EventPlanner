package eventplanner.services;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import eventplanner.models.Event;

import java.text.ParseException;
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
                    dateFormat.format(new Date(rs.getTimestamp("RegistrationDeadline").getTime())),
                        rs.getInt("isPublic") == 0 ? false : true,
                        rs.getInt("PaymentStatus") == 0 ? false : true
                );
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        } 

        return events;
    }

    public boolean addPublicEvent(int venueId, String name, String startTime, String endTime, String registrationDeadline, double price) {
        String query = "{call addEvent(?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

            stmt.setString(1, name);
            stmt.setTimestamp(2, new Timestamp(inputFormat.parse(startTime).getTime()));
            stmt.setTimestamp(3, new Timestamp(inputFormat.parse(endTime).getTime()));
            stmt.setTimestamp(4, new Timestamp(inputFormat.parse(registrationDeadline).getTime()));
            stmt.setInt(5, venueId);
            stmt.setDouble(6, price);
            stmt.setBoolean(7, true);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
            
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        } catch (ParseException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }

    public boolean addPrivateEvent(int personId, int venueId, String name, String startTime, String endTime, String registrationDeadline, double price) {
        String query = "{call CreatePrivateEvent(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);

            stmt.setString(1, name);
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
            stmt.setTimestamp(2, new Timestamp(inputFormat.parse(startTime).getTime()));
            stmt.setTimestamp(3, new Timestamp(inputFormat.parse(endTime).getTime()));
            stmt.setInt(4, venueId);
            stmt.setDouble(5, price);
            stmt.setTimestamp(6, new Timestamp(inputFormat.parse(registrationDeadline).getTime()));
            stmt.setInt(7, personId);
            stmt.setInt(8,0); // set payment status as 0

            stmt.registerOutParameter(9, Types.INTEGER);  // For output EventID

            stmt.execute();

            int eventId = stmt.getInt(9);
            System.out.println("Created Private Event with ID: " + eventId);
            return eventId > 0;

        } catch (SQLException | ParseException e) {
            System.err.println("Error creating private event: " + e.getMessage());
            return false;
        }
    }

    public List<Venue> getAllVenues() {
        String query = "{call GetAllVenues}";
        List<Venue> venues = new ArrayList<>();

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Venue venue = new Venue(rs.getInt("ID"),
                        rs.getString("Name"),
                        rs.getInt("MaxCapacity"),
                        rs.getInt("PricingType"),
                        rs.getDouble("Price"),
                        rs.getString("State"),
                        rs.getString("City"),
                        rs.getString("StreetAddress"),
                        rs.getInt("ZipCode")

                );
                venues.add(venue);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching venues: " + e.getMessage());
        }
        return venues;
    }

}
