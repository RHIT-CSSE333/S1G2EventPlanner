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
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.swing.JOptionPane;

import eventplanner.models.User;

public class UserService {

    public class RegisterUserReturnType {
        public int personId;
        public String errorMsg;

        public RegisterUserReturnType(int personId, String errorMsg) {
            this.personId = personId;
            this.errorMsg = errorMsg;
        }
    }

    public class UserSprocReturnType {
        public boolean success;
        public String errorMsg;

        public UserSprocReturnType(boolean success, String errorMsg) {
            this.success = success;
            this.errorMsg = errorMsg;
        }
    }

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
     * @return Id of the new Person record if registration succeeds, -1 otherwise.
     */
    public RegisterUserReturnType registerUser(String email, String phoneNo, String firstName, String middleInit, String lastName,
                                String dob, String password) {
        Connection conn = dbService.getConnection();
        if (conn == null) {
            System.err.println("Database connection failed.");
            return new RegisterUserReturnType(-1, "Database connection failed.");
        }

        byte[] salt = getNewSalt();
        String hashedPassword = hashPassword(salt, password);
        String saltString = getStringFromBytes(salt);
        CallableStatement stmt = null;

        try {
            String storedProcedure = "{CALL CreatePerson(?, ?, ?, ?, ?, ?, ?, ?, ?)}";
            stmt = conn.prepareCall(storedProcedure);

            stmt.setString(1, email);
            stmt.setString(2, phoneNo);
            stmt.setString(3, firstName);
            stmt.setString(4, middleInit == null || middleInit.isEmpty() ? null : middleInit);   // Nullable field
            stmt.setString(5, lastName);
            stmt.setString(6, dob);        // Nullable field
            stmt.setString(7, hashedPassword);
            stmt.setString(8, saltString);
            stmt.registerOutParameter(9, Types.INTEGER);

            stmt.executeUpdate();

            return new RegisterUserReturnType(stmt.getInt(9), "");
        
        } catch (SQLException e) {
            System.err.println("Error registering user: " + e.getMessage());
            return new RegisterUserReturnType(-1, e.getMessage());
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

    public List<Map<String, Object>> getTransactions(int userId) {
        List<Map<String, Object>> transactions = new ArrayList<>();
        String sql = "{CALL GetTransactions(?)}";

        try {
            Connection conn = dbService.getConnection();
            CallableStatement stmt = conn.prepareCall(sql);
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                Map<String, Object> transaction = new HashMap<>();
                transaction.put("type", rs.getInt("Type"));
                transaction.put("amount", rs.getString("Amount"));
                transaction.put("paidOn", rs.getString("PaidOn"));
                transactions.add(transaction);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching transactions: " + e.getMessage());
        }
        return transactions;
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

    public User getUserData(Integer userID) {
        if (userID == null || userID == 0) {
            return null;
        }

        Connection conn = dbService.getConnection();
        if (conn == null) {
            return null;
        }
        User user = null;
        try {
            CallableStatement stmt = conn.prepareCall("{call GetUserInfo(?)}");
            stmt.setInt(1, userID);

            ResultSet rs = stmt.executeQuery();
            rs.next();

            System.out.println(rs.getString("FirstName"));
            user = new User (
                userID,
                rs.getString("Email"),
                rs.getString("PhoneNo"),
                rs.getString("FirstName"),
                rs.getString("Minit"),
                rs.getString("LastName"),
                rs.getString("DOB")
            );

        } catch (SQLException e) {
            System.err.println("Error logging in: " + e.getMessage());
            return null;
        }
        return user;
    }


    /**
     * Registers a new review left by the user for a specified venue
     *
     * @return true if registration succeeds, false otherwise.
     */
	public UserSprocReturnType leaveReview(int PersonID, Integer VenueID, Integer EventID, String Title, int Rating,
	                            String Desc) {
	    Connection conn = dbService.getConnection();
	    if (conn == null) {
	        return new UserSprocReturnType(false, "Internal Server Error (no db connection)")
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
 	        } else {
 	        	JOptionPane.showMessageDialog(null, "ERROR: Unknown error has occured.");
            }

            return new UserSprocReturnType(returnCode == 0, returnCode == 0 ? "" : "Unknown error");
                
	    } catch (SQLException e) {
	        System.err.println("Error leaving review: " + e.getMessage());
	        e.printStackTrace();
	        return new UserSprocReturnType(false, e.getMessage());
	    }
	}

    public boolean updateEmail(int userID, String newEmail) {
        Connection conn = dbService.getConnection();
	    if (conn == null) {
	        JOptionPane.showMessageDialog(null, "Database connection failed.");
	        return false;
	    }

	    try {
	        String query = "{? = call dbo.updateEmail(?, ?)}";
	        CallableStatement stmt = conn.prepareCall(query);

	        stmt.registerOutParameter(1, Types.INTEGER);
	        stmt.setInt(2, userID);

	        stmt.setString(3, newEmail); 

            stmt.execute();

	        // Get the return code
 			int returnCode = stmt.getInt(1);

 		
	        return returnCode == 0;
	    } catch (SQLException e) {
	        System.err.println("Error leaving review: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
    }

    public boolean updateName(int userID, String firstName, String Minit, String lastName) {
        Connection conn = dbService.getConnection();
	    if (conn == null) {
	        JOptionPane.showMessageDialog(null, "Database connection failed.");
	        return false;
	    }

	    try {
	        String query = "{? = call dbo.updateName(?, ?, ?, ?)}";
	        CallableStatement stmt = conn.prepareCall(query);

	        stmt.registerOutParameter(1, Types.INTEGER);
	        stmt.setInt(2, userID);

	        stmt.setString(3, firstName); 
            stmt.setString(4, Minit.isEmpty() ? null : Minit); 
            stmt.setString(5, lastName); 

            stmt.execute();

	        // Get the return code
 			int returnCode = stmt.getInt(1);

 		
	        return returnCode == 0;
	    } catch (SQLException e) {
	        System.err.println("Error leaving review: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
    }

    public boolean updatePhoneNo(int userID, String newPhoneNo) {
        Connection conn = dbService.getConnection();
	    if (conn == null) {
	        JOptionPane.showMessageDialog(null, "Database connection failed.");
	        return false;
	    }

	    try {
	        String query = "{? = call dbo.updatePhoneNo(?, ?)}";
	        CallableStatement stmt = conn.prepareCall(query);

	        stmt.registerOutParameter(1, Types.INTEGER);
	        stmt.setInt(2, userID);

	        stmt.setString(3, newPhoneNo); 

            stmt.execute();

	        // Get the return code
 			int returnCode = stmt.getInt(1);

 		
	        return returnCode == 0;
	    } catch (SQLException e) {
	        System.err.println("Error leaving review: " + e.getMessage());
	        e.printStackTrace();
	        return false;
	    }
    }

    public String getEmailForPendingInvitation(String invitationId) {
        String query = "{call GetEmailForPendingInvitation(?, ?)}";

        Connection conn = null;
        CallableStatement stmt = null;

        try {
            conn = dbService.getConnection();
            stmt = conn.prepareCall(query);
            
            stmt.setString(1, invitationId);
            stmt.registerOutParameter(2, Types.NVARCHAR);
            stmt.execute();

            return stmt.getString(2);

        } catch (SQLException e) {
            System.err.println("Error adding service to event: " + e.getMessage());
            return null;
        }
    }
}