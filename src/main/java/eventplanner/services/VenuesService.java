package eventplanner.services;

import java.sql.Connection;
import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.*;

import eventplanner.models.Event;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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
                        true,
                        rs.getInt("PaymentStatus") != 0
                );
                events.add(event);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching available events: " + e.getMessage());
        } 

        return events;
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
                System.out.println("Venue fetched: " + (venue != null ? venue.getName() : "null"));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching venues: " + e.getMessage());
        }
        return venues;
    }

    public List<Map<String, Object>> getVenueReviews(int venueId) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        String query = "EXEC ShowVenueReviews ?";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, venueId);
            ResultSet rs = stmt.executeQuery();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            while (rs.next()) {
                String formattedDate = rs.getTimestamp("PostedOn") != null
                        ? dateFormat.format(rs.getTimestamp("PostedOn"))
                        : "Unknown";
                Map<String, Object> review = new HashMap<>();
                review.put("reviewId", rs.getInt("ReviewID"));
                review.put("venueId", rs.getInt("VenueID"));
                review.put("venueName", rs.getString("VenueName"));
                review.put("title", rs.getString("Title") != null ? rs.getString("Title") : "No Title");
                review.put("rating", rs.getInt("Rating"));
                review.put("comment", rs.getString("Comment") != null ? rs.getString("Comment") : "No Comment");
                review.put("postedOn", formattedDate);
                review.put("reviewerName", rs.getString("ReviewerName"));
                reviews.add(review);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching venue reviews: " + e.getMessage());
        }
        return reviews;
    }


}
