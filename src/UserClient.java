import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.sun.javafx.binding.Logging;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor.ReadOnlyListener;

import javafx.scene.chart.PieChart.Data;

public class UserClient {
		
		private Socket connection;
		private BufferedReader input;
		private BufferedWriter output;
		private ObjectInputStream objectInput;
		private ArrayList<ClientMessage> message = new ArrayList<ClientMessage>();
		private String uName = "";
		private int uid  = -1;
		private static String tableName = "userDB";
		
		/**
		 * Constructor for the UserClient class.
		 * Takes a socket object as parameter, etablish reader/writer object on
		 * the socket and reads initial command from the client.
		 * The initial command is a tab delimited string, "LOGIN".
		 * LOGIN: expects the next words to be a username and password.
		 * @param connection The socket that holds the client connection.
		 * @throws IOException	Exeption idicating an error.
		 */
		public UserClient(Socket connection) throws IOException {
			this.connection = connection;
			
			try {
				input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
				String args[] = read().split("[\t]");
				Logging.getLogger().info(Arrays.toString(args));
				// Gets "LOGIN"
				if (args[0].equals("LOGIN")) {
				// Then gets a username and a password.
			//		logIn (args, connection.getInetAddress().getHostAddress());
				}	
			} catch (IOException ioe) {
				ioe.printStackTrace(); 
		//		throw new Exception("Kunne ikke åpne stream fra klient");
				}
			}
		
		
		/**
		 * Closes the buffered reader, the buffered writer and the socket connection
		 * @throws IOException if one can't be closed
		 */
		public void close() throws IOException {
			input.close();
			output.close();
			connection.close();
		}
		
		/**
		 * Send the given message to the client. Ensures that all messages
		 * have a trailing newline and are flushed.
		 * @param text the message to send
		 * @throws IOException if an error occurs when sending the message
		 */
		public void sendText(String text) throws IOException {
			output.write(text);
			output.newLine();
			output.flush();
		}
		
		public String read() throws IOException {
			if (input.ready())
				return input.readLine();
			return null;
		}
		/**
		 * Method uses to find a user in the userDB given username and password.
		 * @param args
		 * @param hostname
		 */
	/*	public void logIn(String[] args) {
			try {
				PreparedStatement pst = DatabaseHandler.getDBconnection().
						prepareStatement("SELECT id FROM " + tableName + " WHERE uName = ? AND pWord = ?");
				// Sets uName and pWord
				pst.setString(1, args[1]);
				pst.setString(2, args[2]);
				
				ResultSet result = pst.executeQuery();
				if(!result.next()) {
					sendText("Feil brukernavn/passord");
					throw new Exception("Feil brukernavn/passord");
				}
				
				uid = result.getInt(1);
				uName = result.getString(2);
				pst.close();
				String password = 
				pst = DatabaseHandler.getDBconnection().prepareStatement("UPDATE " + tableName + "SET loginkey = ?");
				pst.setInt(1, key);
				pst.execute();
			
				sendText("Login OK");
				
			} catch (SQLException sqle) {
				sendText("Ooops! En databasefeil :( ");
				throw new Exception("Ooops! En databasefeil :( ");
				}
		}
		
		public static String MD5(String md5) {
			try {
				java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
				byte[] array = md.digest(md5.getBytes());
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < array.length; ++i) {
					sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
				}
				return sb.toString();
			} catch (java.security.NoSuchAlgorithmException e) {
			}
			return null;
		}
*/
}

