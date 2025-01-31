package eventplanner.services;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Base64;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;

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
        CallableStatement stmt = null;

        try {
            String storedProcedure = "{CALL CreateOrUpdatePerson(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            stmt = conn.prepareCall(storedProcedure);

            stmt.setString(1, email);
            stmt.setString(2, phoneNo);
            stmt.setString(3, firstName);
            stmt.setString(4, middleInit == null || middleInit.isEmpty() ? null : middleInit);   // Nullable field
            stmt.setString(5, lastName);
            stmt.setString(6, dob);
            stmt.setString(7, ccNum == null || ccNum.isEmpty() ? null : ccNum);        // Nullable field
            stmt.setString(8, ccExpDate == null || ccExpDate.isEmpty() ? null : ccExpDate);   // Nullable field
            stmt.setString(9, cvv == null || cvv.isEmpty() ? null : cvv);         // Nullable field
            stmt.setInt(10, 0);
            stmt.setString(11, hashedPassword);
            stmt.setString(12, saltString);

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

        CallableStatement stmt = null;
        ResultSet rs = null;

        try {
            String storedProcedure = "{CALL ValidateUserLogin(?)}";
            stmt = conn.prepareCall(storedProcedure);

            stmt.setString(1, email);
            rs = stmt.executeQuery();

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

    public int getUserIdByEmail(String email) {
        System.out.println(email);
        if (email == null || email.isBlank()) {
            return -1;
        }

        Connection conn = dbService.getConnection();
        if (conn == null) {
            return -1;
        }

        try {
            CallableStatement stmt = conn.prepareCall("{call GetPersonIDByEmail(?, ?)}");
            stmt.setString(1, email);
            stmt.registerOutParameter(2, Types.INTEGER);

            stmt.execute();

            return stmt.getInt(2);

        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
            return -1;
        }

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



    /**
     * Registers a new review left by the user for a specified venue
     *
     * @return true if registration succeeds, false otherwise.
     */
	public boolean leaveReview(int PersonID, Integer VenueID, Integer EventID, String Title, int Rating,
	                            String Desc) {
	    Connection conn = dbService.getConnection();
	    if (conn == null) {
	        JOptionPane.showMessageDialog(null, "Database connection failed.");
	        return false;
	    }

	    try {
	        String query = "{? = call dbo.AddReview(?, ?, ?, ?, ?, ?)}";
	        CallableStatement stmt = conn.prepareCall(query);

	        stmt.registerOutParameter(1, Types.INTEGER);
	        stmt.setInt(2, PersonID);

	        if (VenueID == -1)
	            stmt.setNull(3, Types.INTEGER);
	         else
	            stmt.setInt(3, VenueID);

	        if (EventID == -1)
	            stmt.setNull(4, Types.INTEGER);
	        else
	            stmt.setInt(4, EventID);

	        stmt.setString(5, Title.isEmpty() ? null : Title);   // Nullable field
	        stmt.setInt(6, Rating);
	        stmt.setString(7, Desc.isEmpty() ? null : Desc); // Nullable field

            stmt.execute();

	     // Get the return code
 			int returnCode = stmt.getInt(1);

 			// Print return code
 			if (returnCode == 0) {
 	            System.out.println("Success!");
 	        } else if (returnCode == 1) {
 	        	JOptionPane.showMessageDialog(null, "ERROR: Required Fields cannot be empty");
 	        } else if (returnCode == 2) {
 	        	JOptionPane.showMessageDialog(null, "ERROR: Specified Person, Venue, and/or Event do not exist.");
 	        } else if (returnCode == 3) {
 	        	JOptionPane.showMessageDialog(null, "ERROR: Same user cannot leave more than 1 review per Event or Venue.");
 	        } else if (returnCode == 5) {
 	        	JOptionPane.showMessageDialog(null, "ERROR: Rating must be between 1 and 5.");
 	        } else
 	        	JOptionPane.showMessageDialog(null, "ERROR: Unknown error has occured.");
	        return returnCode == 0;
	    } catch (SQLException e) {
	        System.err.println("Error leaving review: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
	}
}