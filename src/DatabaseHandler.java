import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOError;
import java.io.IOException;
import java.security.MessageDigest;
import java.sql.*;
import com.sun.xml.internal.ws.api.addressing.WSEndpointReference.Metadata;

/*****************************************************************************/
/** Purpose:
 * 
 * This class takes care of the creation of the user database, which contains 
 * user information of each resident. A total of four users will be found
 * in this DB.
 * 
 * Common errors; if you get Scheme 'somename" (ex. 'TEST') doesn't exist, try removing the
 * metadata functions in createUserDB.
 * 
 ** Functions:
 * 
 * createNewUserDB		// Creates new table
 * printDB				// Prints all the contents of the table
 * updatePW				// Lets users update their passwords	
 * forgotPW				// Returns the password if conditions are met
 * checkPW				// Compares two passwords (used in forgotPW)
 * logIn				// Lets users log in
 * rememberMe			// Flag in the database
 * insertUsers			// Inserts user at creation of table
 * write				// Outputs string
 * ResultSet			// Gets a resultset
 * hasher				// Hashes passwords
 * 
 */
public class DatabaseHandler {
	
	// Database stuff
	public static String DRIVER = "org.apache.derby.jdbc.EmbeddedDriver";
	public static String dbURL = "jdbc:derby:userDB;create=true;";
	public static String tableName="userDB";
	public static String settingsTable="settingsTable";
	
	// JDBC connection and statements
	private static Connection conn = null;
	private static Statement stmt = null;
	private static PreparedStatement pst = null;
	
	// Strings and stuff
	private static String pWord = "test";
	private static String confirmPW;
	private static String uName = "test";
	private static String loginKey = "";
	private static String newPassword;
	private BufferedWriter output;
	private BufferedReader input;
	
/*****************************************************************************/
	
	/**
	 * Constructor of the databaseHandler. 
	 * Retrieves the correct database URL and creates a new user database. 
	 */
	public DatabaseHandler() {
		try {
			conn = DriverManager.getConnection(dbURL);
			createNewUserDB();
		} catch (SQLException sqle) {sqle.printStackTrace();}
	}
	
	public static Connection getDBconnection() {
		return conn;
	}
	
	/**
	 * Connects to the userDB
	 */
	public static void connectToDB() {
		try {
			conn = DriverManager.getConnection(dbURL, uName, pWord);
		} catch (SQLException sqle) {sqle.printStackTrace();}
	}
	
	/**
	 * Prints the DB (only for testing purposes)
	 */
	public static void printDB() {
		try {
			ResultSet resultSet = null;
			connectToDB(); 
			pst = conn.prepareStatement("SELECT * FROM " + tableName);
			resultSet = pst.executeQuery();
			while(resultSet.next()) {
				String user = resultSet.getString("uName");
				String password = resultSet.getString("pWord");
				String ePW = resultSet.getString("ePWord");
				int id = resultSet.getInt("id");
				//Boolean rMe = resultSet.getBoolean("rememberMe");
				System.out.println("Id: " + id + " Bruker: " + user + " Passord: " +
				password + " Master: " + ePW + "\n");	
		}
		conn.close();
		} catch (SQLException sqle) {sqle.printStackTrace();}	
	}
	
	/**
	 * This function compares two passwords in forgotPW
	 * @param pWord
	 * @param confirmPW
	 * @return
	 */
	public static boolean checkPW(String pWord, String confirmPW) {
		boolean match;
		
		return match = (pWord == confirmPW) ? true : false;
	}
	
	/**
	 * Establishes the database(if it does't exist already).
	 * When run this function will also create the users: root, leilighet1, leilighet2, hybel.
	 */
	public static void createNewUserDB() {
		try {
			// Gets connection
			connectToDB();
			DatabaseMetaData dbmd = conn.getMetaData();// Remove if scheme not exits
			ResultSet rs = dbmd.getTables(null, "test", tableName, null);	// Remove if scheme not exits
			// If the table userDB does not exits, it will be created and users
			// inserted with default passwords
			
			if(rs.next()) {	// Remove if scheme not exits
				stmt = conn.createStatement();
			// Creates the table.
				stmt.executeUpdate("CREATE TABLE " + tableName + " (id INTEGER NOT NULL " +
						"GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
						"uName VARCHAR(255), " + 
						"pWord VARCHAR(255), " +
						"ePWord VARCHAR(255), " + 
						"PRIMARY KEY(id))");
			// Inserts users
			// Root has id: 1
				insertUsers("root", 	  hasher("root"),       hasher("root"));
			// Leilighet1 has id 2, and so on...
				insertUsers("leilighet1", hasher("karaffel"),   hasher("Gk378irfKFJ"));
				insertUsers("leilighet2", hasher("kaffegrut"),  hasher("JdF334hf9F"));
				insertUsers("hybel",      hasher("vannflaske"), hasher("Fm3kMFJFS"));	
			}		// Remove if scheme not exits
			conn.close();
		} catch (SQLException sqle) {sqle.printStackTrace();} 
	}
/*	
	/**
	 * This function will, if the table doesn't exist, create the table filled with commands
	 * to be sent to HDL IP module.
	 /
	public static void createCommandList() {
		try {
			connectToDB();
			DatabaseMetaData dbmd = conn.getMetaData();							// Remove if scheme not exits
			ResultSet rs = dbmd.getTables(null, "test", settingsTable, null);	// Remove if scheme not exits
			if(rs.next()) {														// Remove if scheme not exits
				stmt = conn.createStatement();
				stmt.executeUpdate("CREATE TABLE " + settingsTable + " kommando INTEGER NOT NULL " +
				"GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), " +
				"kommando VARCHAR(255), " + 
				"PRIMARY KEY(kommando))");
			}																	// Remove if scheme not exits
		} catch (SQLException sqle) {sqle.printStackTrace();}
		
	}
*/
	

	/**
	 * This function allows the creation of new users.
	 * @param uName Username 
	 * @param pWord Password
	 *
	public static void insertCommands(String command) {
		try{
			connectToDB();
			stmt = conn.createStatement();
			// Inserting new user into user database.
			stmt.execute("INSERT INTO " + tableName + " (uName , pWord, rememberMe) VALUES ('" + uName + "','" + pWord +  "','" + rememberMe +"')");
			// closing connection
			conn.close();
		} catch (SQLException sqle) {sqle.printStackTrace();}
	}
	*/
	/**
	 * This function allows the creation of new users.
	 * @param uName Username 
	 * @param pWord Password
	 */
	public static void insertUsers(String uName, String pWord, String ePWord) {
		try{
			connectToDB();
			stmt = conn.createStatement();
			// Inserting new user into user database.
			stmt.execute("INSERT INTO " + tableName + " (uName , pWord, ePWord) VALUES ('" + uName + "','" + pWord +  "','" + ePWord +"')");
			// closing connection
			conn.close();
		} catch (SQLException sqle) {sqle.printStackTrace();}
	}
	
	/**
	 * This function will allow users to update their password when logged in
	 * @param uName The username
	 * @param pWord The password
	 * @param newPassword
	 */
	public static void updatePW(String uName, String newPassword) {
		try {
			connectToDB();
			pst = conn.prepareStatement("UPDATE " + tableName + " SET pWord = ? WHERE uName = ?"); 
			pst.setString(1, hasher(newPassword));
			pst.setString(2, uName);
			pst.executeUpdate();		
		} catch(SQLException sqle) {sqle.printStackTrace();}
	}
	
	/**
	 * Method for querying the database and retrieve a ResultSet.
	 * @param query The query to perform
	 * @return		Returns the ResultSet
	 * @throws SQLException	If not able to connect to the database.
	 */
	private static ResultSet getResult(String query) throws SQLException {
		stmt = conn.createStatement();			
		return stmt.executeQuery(query);
	}
	
	/**
	 * Letting users log in
	 * @param uName
	 * @param pWord
	 */
	public static int logIn(String tempUName, String tempPWord) {
		String uName, pWord, mPw;
		int id;
		
		try {
			connectToDB();
			String query = "SELECT id, uName, pWord, ePWord FROM " + tableName;
			Statement stmt = (Statement) conn.createStatement();
			stmt.executeQuery(query);
			ResultSet rs = stmt.getResultSet();
			
			while(rs.next()) {

				uName = rs.getString("uName");	// Gets from DB
				pWord = rs.getString("pWord");	// -||-
				mPw = rs.getString("ePWord");
				id    = rs.getInt("id");		// -||-
				
				// Checks if inputs equals the ones in DB. Hashes the input PW 
				// to see if it matches the one in the DB.
				if(uName.equals(tempUName) && pWord.equals(hasher(tempPWord))) {
				// If everything is OK, it returns the id of the user		
					return id;
				}
				else if(uName.equals(tempUName) && mPw.equals(hasher(tempPWord))) return id;
			}
		} catch (SQLException sqle) {
			sqle.printStackTrace();
			}
		// False uName/pWord
		return 0;
	}
	
	/**
	 * Method for hashing a String, in this case the password. 
	 * "Stolen" from from http://stackoverflow.com/questions/5531455/how-to-encode-some-string-with-sha256-in-java/11009612#11009612
	 * 
	 * @param password The String to hash
	 * @return Returns the hashed String.
	 */
	public static String hasher(String password) {
		StringBuilder hexString = new StringBuilder();
		
		try{
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] hash = digest.digest(password.getBytes("UTF-8"));
			
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
	

	/**
	 * Outputs a string. Nothing more, nothing less.
	 * @param s Incoming parameter
	 * @throws IOException 
	 */
	public void write(String s) throws IOException {
		output.write(s);
		output.newLine();
		output.flush();
	}	
}

/*************************************END*************************************/
/*
if (args[0].equals("LOGIN")) {
	// Then gets a username and a password.
	uName = args[1];
	pWord = DatabaseHandler.hasher(args[2]);
	// Stores the userID
	id = DatabaseHandler.logIn (uName, pWord);
	
	// If wrong username or password
	if (id == 0) {
		sendText("Feil brukernavn eller passord");			
	}
	
	else {
		
	}
	
*/
