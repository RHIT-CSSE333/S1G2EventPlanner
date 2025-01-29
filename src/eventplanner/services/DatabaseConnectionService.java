package eventplanner.services;

import java.sql.*;

public class DatabaseConnectionService {

    private final String url;
    private Connection connection = null;
    private final String databaseName;
    private final String serverName;

    public DatabaseConnectionService(String serverName, String databaseName) {
        this.serverName = serverName;
        this.databaseName = databaseName;
        this.url = "jdbc:sqlserver://" + this.serverName + ";databaseName=" + this.databaseName +
                ";encrypt=true;trustServerCertificate=true;";
    }

    public boolean connect(String user, String pass) {
        try {
            // Establish the connection
            this.connection = DriverManager.getConnection(this.url, user, pass);
            System.out.println("Database connection established！");
            return true;
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
            return false;
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            System.err.println("Error while closing connection: " + e.getMessage());
        }
    }

    public ResultSet executeQuery(String query, Object... params) {
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            return stmt.executeQuery();
        } catch (SQLException e) {
            System.err.println("Query execution failed: " + e.getMessage());
            return null;
        }
    }
}


