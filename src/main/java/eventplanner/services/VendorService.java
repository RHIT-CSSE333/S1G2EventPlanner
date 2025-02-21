package eventplanner.services;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import eventplanner.models.Service;
import eventplanner.models.Vendor;

public class VendorService {
    
    private DatabaseConnectionService dbService;

    public VendorService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

    public List<Vendor> getAllVendors() {
        String query = "{call GetAllVendors}";
        List<Vendor> vendors = new ArrayList<>();

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Vendor vendor = new Vendor(rs.getInt("ID"),
                rs.getString("Name")
                );
                vendors.add(vendor);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vendors: " + e.getMessage());
        }
        return vendors;
    }

    public Vendor getVendorById(int id) {
        String query = "{call GetVendorById(?)}";
        Vendor vendor = null;

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                vendor = new Vendor(rs.getInt("ID"),
                rs.getString("Name")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vendor: " + e.getMessage());
        }
        return vendor;
    }

    public List<Service> getVendorServices(int vendorId) {
        String query = "{call GetVendorServices(?)}";
        List<Service> services = new ArrayList<>();

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);
            stmt.setInt(1, vendorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Service service = new Service(rs.getInt("ID"),
                rs.getString("Name"),
                rs.getString("Description"),
                rs.getDouble("Price"),
                rs.getInt("VendorID"),
                ""
                );
                services.add(service);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vendor services: " + e.getMessage());
        }
        return services;
    }

    public List<Service> getAllServices() {
        String query = "{call GetAllServices()}";
        List<Service> services = new ArrayList<>();

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(query);

            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Service service = new Service(rs.getInt("ID"),
                rs.getString("Name"),
                rs.getString("Description"),
                rs.getDouble("Price"),
                rs.getInt("VendorID"),
                ""
                );
                services.add(service);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching vendor services: " + e.getMessage());
        }
        return services;
    }

}
