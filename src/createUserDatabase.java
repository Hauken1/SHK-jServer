import java.security.MessageDigest;
import java.sql.*;

/**
 * This class takes care of the creation of the user database, which contains 
 * user information of each resident. A total of four users will be found
 * in this DB.
 * 
 * @author Edvard, created on 22.03/16
 */
public class userDatabase {
	private final static String dbURL = "jdbc:derby:uDB";
	private static String tableName="Brukerdatabase";
	
	// JDBC connection
	private static Connection conn = null;
	private static Statement stmt = null;
	
	
	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			createNewUserDB();
		}
	}
	
	/**
	 * Establishes the database.
	 * When run this function will also create the users: root, leilighet1, leilighet2, hybel.
	 */
	private static void createNewUserDB() {
		try {
			Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
			// Get a connection
			conn = DriverManager.getConnection(dbURL);
			// Creates the DB.
			stmt.execute("CREATE TABLE " + "," + tableName + " id bigint NOT NULL GENERATED ALWAY AS IDENTITY (START WITH 1, INCREMENT BY 1");
			insertUsers("root", "root");
			insertUsers("leilighet1", "leilighet1");
			insertUsers("leilighet2", "leilighet2");
			insertUsers("hybel", "hybel");
	
		} catch (Exception except) {
			except.printStackTrace();
		}
	}
	
	/**
	 * Server will use this function to connect to DB.
	 */
	public static void connectToDB() {
		try {
			stmt = conn.createStatement();
			conn = DriverManager.getConnection(dbURL);
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
		}
	}
	
	/**
	 * This function allows the creation of new users.
	 * @param uName Username 
	 * @param pWord Password
	 */
	public static void insertUsers(String uName, String pWord) {
		try {
			stmt = conn.createStatement();
			// Inserting new user into user database.
			stmt.execute("INSERT INTO " + tableName + " (username , password) VALUES (" + uName + "," + pWord +"' )");
			// closing connection
			stmt.close();
		} catch (SQLException sqlException) {
			sqlException.printStackTrace();
			System.out.println("Ooops, fikk ikke lagt " + uName + " i " + tableName ", sorry!");
		}
	}
	
	public static void updatePW(String[] uName) {
 		stmt = conn.createStatement();
		stmt.executeUpdate("UPDATE " + tableName + "SET password= ");
	}
	
	
	/**
	 * Login
	 * 
	 * @param uName The username to login with.
	 * @param pWord The password of the user.
	 * @return returns Returns the user ID.
	 */
	public static int logIn(String uName, String pWord) {
		String query = "SELECT _ID FROM users WHERE username=\'" + uName + "\' AND password =\'" + hasher(pWord) +"\'";
		
		try {
			ResultSet result = getResult(query);
			if(result.next()) {
				return (int) result.getObject(1);
			}
		} catch(SQLException sqlException) {
			sqlException.printStackTrace();
		}
	}
	/**
	 * Method for hashing a String, in this case the password. 
	 * "Stolen" from from http://stackoverflow.com/questions/5531455/how-to-encode-some-string-with-sha256-in-java/11009612#11009612
	 * 
	 * @param base The String to hash
	 * @return Returns the hashed String.
	 */
	private static String hasher(String base) {
		StringBuilder hexString = new StringBuilder();
		
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(base.getBytes("UTF-8"));
			
			for(int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) {
					hexString.append('0');
				}
			hexString.append(hex);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return hexString.toString();
	}
	
	
	
}
