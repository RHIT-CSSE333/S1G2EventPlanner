package eventplanner.services;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.*;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;
import java.util.Random;

public class UserService { ;
    private static final Random RANDOM = new SecureRandom();
    private static final Base64.Encoder enc = Base64.getEncoder();
    private static final Base64.Decoder dec = Base64.getDecoder();
    private DatabaseConnectionService dbService = null;

    public UserService(DatabaseConnectionService dbService) {
        this.dbService = dbService;
    }

    /**
     * Registers a new user by hashing the password and storing it with a salt.
     *
     * @return true if registration succeeds, false otherwise.
     */
    public boolean registerUser(String email, String phoneNo, String firstName, String middleInit, String lastName,
                                String dob, String ccNum, String ccExpDate, String cvv, String password) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "Database connection failed.");
            return false;
        }

        byte[] salt = getNewSalt();
        String hashedPassword = hashPassword(salt, password);
        String saltString = getStringFromBytes(salt);

        try {
            String query = "INSERT INTO Person (Email, PhoneNo, FirstName, MInit, LastName, DOB, CCNum, CCExpDate, CVV, PasswordHash, PasswordSalt) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, phoneNo);
            stmt.setString(3, firstName);
            stmt.setString(4, middleInit.isEmpty() ? null : middleInit);   // Nullable field
            stmt.setString(5, lastName);
            stmt.setString(6, dob);
            stmt.setString(7, ccNum.isEmpty() ? null : ccNum);        // Nullable field
            stmt.setString(8, ccExpDate.isEmpty() ? null : ccExpDate);   // Nullable field
            stmt.setString(9, cvv.isEmpty() ? null : cvv);         // Nullable field
            stmt.setString(10, hashedPassword);
            stmt.setString(11, saltString);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return false;
        }
    }


    /**
     * Authenticates a user by verifying the input password.
     *
     * @return true if login is successful, false otherwise.
     */
    public boolean loginUser(String email, String inputPassword) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            JOptionPane.showMessageDialog(null, "Database connection failed.");
            return false;
        }

        try {
            String query = "SELECT PasswordHash, PasswordSalt FROM Person WHERE Email = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("PasswordHash");
                String storedSalt = rs.getString("PasswordSalt");

                byte[] saltBytes = dec.decode(storedSalt);
                String hashedInputPassword = hashPassword(saltBytes, inputPassword);

                return storedHash.equals(hashedInputPassword);
            }
        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
        }
        return false;
    }

    public byte[] getNewSalt() {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public String getStringFromBytes(byte[] data) {
        return enc.encodeToString(data);
    }

    public String hashPassword(byte[] salt, String password) {

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, 65536, 128);
        SecretKeyFactory f;
        byte[] hash = null;
        try {
            f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            hash = f.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            JOptionPane.showMessageDialog(null, "An error occurred during password hashing. See stack trace.");
            e.printStackTrace();
        }
        return getStringFromBytes(hash);
    }

}
