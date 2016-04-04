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
		String uName = "";
		String pWord = "";
		int id; 
		
		/**
		 * Constructor for the UserClient class.
		 * Takes a socket object as parameter, etablish reader/writer object on
		 * the socket and reads initial command from the client.
		 * The initial command is a tab delimited string, "LOGIN".
		 * LOGIN: expects the next words to be a username and password.
		 * @param connection The socket that holds the client connection.
		 * @throws IOException	Exeption idicating an error.
		 */
//		public UserClient(Socket connection) throws IOException {
		public UserClient(String args[]) {
		//	this.connection = connection;
			
			try {
				input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			//	Logging.getLogger().info(Arrays.toString(args));
				// Gets "LOGIN"
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
					
				}	
			} catch (IOException ioe) {
				ioe.printStackTrace(); 
		//		throw new Exception("Kunne ikke �pne stream fra klient");
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
	
}

