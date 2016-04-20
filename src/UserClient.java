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
import java.util.concurrent.TimeUnit;
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
		private static ArrayList<String> message = new ArrayList<String>();
		int userId; 
		
		
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
			try {
				this.connection = connection;
				input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));

					
					
			} catch (IOException ioe) {
				ioe.printStackTrace(); 
		//		throw new Exception("Kunne ikke åpne stream fra klient");
				}
			}
		
		//For testing purposes
		
		public UserClient(int n) {
			userId = n;
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
		public String returnUserID() {
			String uId;
			
			 uId = Integer.toString(userId);
			 return uId;
		}
		public void SetSocket(Socket connection) {
			connection = connection;
		}
		public Socket returnSocket() {
			return connection;
		}
		public String read() throws IOException {
			if (input.ready())
				return input.readLine();
			return null;
		}

		public void addMessage(String cmd) {
			//System.out.println(cmd + " user mg1");
			message.add(cmd);
		}
		
		public void removeMessage(int n) {
			message.remove(n);
		}
		public boolean checkForMessage(String messagecmd) {
			for(int i = 0; i < message.size(); i++) {
				System.out.println(message.get(i) + " UserMessage");
				if( message.get(i).startsWith(messagecmd) && message.get(i).endsWith(Integer.toString(userId))) {
					message.remove(i);
					return true; 
				}
			}
			return false;
		}
		
		public boolean loginChecker() {
			try {
				String tempName = input.readLine();
				String tempPass = input.readLine();

				int ID = DatabaseHandler.logIn(tempName, tempPass);
		
				System.out.println(ID);
					
				if (ID == 0) {
					sendText(Integer.toString(ID));
					return false;
				}
				userId = ID;
				sendText(Integer.toString(ID));				// Sends the Player ID
				return true;	
				
				} catch (IOException ioe) { // catches any errors when trying to read from input
					ioe.printStackTrace();
				}
				return false;
		}
		
		public boolean changePassword() {
			try {
				
				String userName = input.readLine();
				String oldpass = input.readLine();
				String newPass = input.readLine();
				
				//using login method to see if the old password is correct
				int loginTest = DatabaseHandler.logIn(userName, oldpass);
				
				if(loginTest > 0){
					DatabaseHandler.updatePW(userName, newPass);
					sendText("PChanged");
					return true;
				}
				sendText("Failed");
				return false;
			}catch (IOException e) {
				e.printStackTrace();
			}
			return false;
		}
		
		public void holidayTimer(long s) {
			
			Thread holidayThread = new Thread(new Runnable() {
                @Override
                public void run() {
			    try {
			        //Thread sleeps and will run the necessary methods when done
			        TimeUnit.SECONDS.sleep(s);
			    } catch(InterruptedException ie) {}
			    System.out.println("Hello world!");
                }
			});
			holidayThread.start();
			
		}
}


