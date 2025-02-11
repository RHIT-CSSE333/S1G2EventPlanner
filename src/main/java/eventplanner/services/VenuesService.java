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


    // legacy -- TODO: delete
    // public EventReturnType addPublicEvent(int venueId, String name, String startTime, String endTime, String registrationDeadline, double price, String paymentId) {
    //     String query = "{call addEvent(?, ?, ?, ?, ?, ?, ?, ?, ?)}";

    //     Connection conn = null;
    //     CallableStatement stmt = null;

    //     try {
    //         conn = dbService.getConnection();
    //         stmt = conn.prepareCall(query);

    //         SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");

    //         stmt.setString(1, name);
    //         stmt.setTimestamp(2, new Timestamp(inputFormat.parse(startTime).getTime()));
    //         stmt.setTimestamp(3, new Timestamp(inputFormat.parse(endTime).getTime()));
    //         stmt.setTimestamp(4, new Timestamp(inputFormat.parse(registrationDeadline).getTime()));
    //         stmt.setInt(5, venueId);
    //         stmt.setDouble(6, price);
    //         stmt.setBoolean(7, true);
    //         stmt.setString(8, paymentId);

    //         stmt.registerOutParameter(9, Types.INTEGER);  // For output EventID

    //         stmt.execute();

    //         int eventId = stmt.getInt(10);


    //         int rowsInserted = stmt.executeUpdate();
    //         return new EventReturnType(eventId > 0, eventId, "");
            
    //     } catch (SQLException e) {
    //         System.err.println("Error registering user: " + e.getMessage());
    //         return new EventReturnType(false, -1, e.getMessage());
    //     } catch (ParseException e) {
    //         System.err.println("Error registering user: " + e.getMessage());
    //         return new EventReturnType(false, -1, e.getMessage());
    //     }
    // }

    // // legacy -- TODO: delete
    // public EventReturnType addPrivateEvent(int personId, int venueId, String name, String startTime, String endTime, String registrationDeadline, double price, String paymentId) {
    //     String query = "{call CreatePrivateEvent(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";

    //     Connection conn = null;
    //     CallableStatement stmt = null;

    //     try {
    //         conn = dbService.getConnection();
    //         stmt = conn.prepareCall(query);

    //         stmt.setString(1, name);
    //         SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
    //         stmt.setTimestamp(2, new Timestamp(inputFormat.parse(startTime).getTime()));
    //         stmt.setTimestamp(3, new Timestamp(inputFormat.parse(endTime).getTime()));
    //         stmt.setInt(4, venueId);
    //         stmt.setDouble(5, price);
    //         stmt.setTimestamp(6, new Timestamp(inputFormat.parse(registrationDeadline).getTime()));
    //         stmt.setInt(7, personId);
    //         stmt.setInt(8,0); // set payment status as 0
    //         stmt.setString(9,paymentId);

    //         stmt.registerOutParameter(10, Types.INTEGER);  // For output EventID

    //         stmt.execute();

    //         int eventId = stmt.getInt(10);
    //         System.out.println("Created Private Event with ID: " + eventId);

    //         return new EventReturnType(eventId > 0, eventId, "");

    //     } catch (SQLException | ParseException e) {
    //         System.err.println("Error creating private event: " + e.getMessage());
    //         return new EventReturnType(false, -1, e.getMessage());
    //     }
    // }

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

    public List<Map<String, Object>> getVenueReviews(int venueId) {
        List<Map<String, Object>> reviews = new ArrayList<>();
        String query = "EXEC ShowVenueReviews ?";  // 需要在 SQL 里创建这个存储过程

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
