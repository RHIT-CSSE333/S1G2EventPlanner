package eventplanner.services;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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

}
