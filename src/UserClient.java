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
		private static ArrayList<String> smartMessage = new ArrayList<String>();
		int userId; 
		boolean connected = false;
		boolean slept;
		
		private String ModeHoliday = "1";
		private String ModeDay = "2";	
		private String ModeNight = "3";	
		private String ModeAway = "4";	
		
		private String Channel1; 
		private String CurrentMode1; 
		private String CurrentNormalTemp1; 
		private String CurrentDayTemp1;
		private String CurrentNightTemp1; 
		private String CurrentAwayTemp1;
		private String CurrentTemp1;
		
		private String Channel2; 
		private String CurrentMode2; 
		private String CurrentNormalTemp2; 
		private String CurrentDayTemp2;
		private String CurrentNightTemp2; 
		private String CurrentAwayTemp2;
		private String CurrentTemp2;
		
		private String Channel3; 
		private String CurrentMode3; 
		private String CurrentNormalTemp3; 
		private String CurrentDayTemp3;
		private String CurrentNightTemp3; 
		private String CurrentAwayTemp3;
		private String CurrentTemp3;
		
		private String Channel4; 
		private String CurrentMode4; 
		private String CurrentNormalTemp4; 
		private String CurrentDayTemp4;
		private String CurrentNightTemp4; 
		private String CurrentAwayTemp4;
		private String CurrentTemp4;
		
		private String Channel5; 
		private String CurrentMode5; 
		private String CurrentNormalTemp5; 
		private String CurrentDayTemp5;
		private String CurrentNightTemp5; 
		private String CurrentAwayTemp5;
		private String CurrentTemp5;
		
		private String Channel6; 
		private String CurrentMode6; 
		private String CurrentNormalTemp6; 
		private String CurrentDayTemp6;
		private String CurrentNightTemp6; 
		private String CurrentAwayTemp6;
		private String CurrentTemp6;
		
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
				connected = true;						
			} catch (IOException ioe) {
				ioe.printStackTrace(); 
		//		throw new Exception("Kunne ikke åpne stream fra klient");
				}
			}
		
		public UserClient(int n) {
			userId = n;
			InitTempInfo();
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
		
		public int returnUserIDInt() {
			return userId;
		}
		
		public String returnUserID() {
			String uId;
			
			 uId = Integer.toString(userId);
			 return uId;
		}
		
		public boolean returnConnected() {
			return connected;
		}
		
		public void setFalseConnection(){
			connected = false;
			connection = null;
			input = null;
			output = null;
		}
		
		public void setSocket(Socket connection) {
			try {
				connected = true;
				this.connection = connection;
				input = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
				output = new BufferedWriter(new OutputStreamWriter(this.connection.getOutputStream()));
			} catch(Exception e){
				e.printStackTrace();
			}
		}
		
		public Socket returnSocket() {
			return connection;
		}
		
		public String read() throws IOException {
			if(connected){
				if (input.ready())
					return input.readLine();
				return null;
			}
			return null;
		}

		public void InitTempInfo () {
			Channel1 = "0"; 
			CurrentMode1 = "0"; 
			CurrentNormalTemp1 = "0"; 
			CurrentDayTemp1 = "0";
			CurrentNightTemp1 = "0"; 
			CurrentAwayTemp1 = "0";
			CurrentTemp1 = "0";
			
			Channel2 = "0"; 
			CurrentMode2 = "0"; 
			CurrentNormalTemp2 = "0"; 
			CurrentDayTemp2 = "0";
			CurrentNightTemp2 = "0"; 
			CurrentAwayTemp2 = "0";
			CurrentTemp2 = "0";
			
			Channel3 = "0"; 
			CurrentMode3 = "0"; 
			CurrentNormalTemp3 = "0"; 
			CurrentDayTemp3 = "0";
			CurrentNightTemp3 = "0"; 
			CurrentAwayTemp3 = "0";
			CurrentTemp3 = "0";
			
			Channel4 = "0"; 
			CurrentMode4 = "0"; 
			CurrentNormalTemp4 = "0"; 
			CurrentDayTemp4 = "0";
			CurrentNightTemp4 = "0"; 
			CurrentAwayTemp4 = "0";
			CurrentTemp4 = "0";
			
			Channel5 = "0"; 
			CurrentMode5 = "0"; 
			CurrentNormalTemp5 = "0"; 
			CurrentDayTemp5 = "0";
			CurrentNightTemp5 = "0"; 
			CurrentAwayTemp5 = "0";
			CurrentTemp5 = "0";
			
			Channel6 = "0"; 
			CurrentMode6 = "0"; 
			CurrentNormalTemp6 = "0"; 
			CurrentDayTemp6 = "0";
			CurrentNightTemp6 = "0"; 
			CurrentAwayTemp6 = "0";
			CurrentTemp6 = "0";
		}
		public void setTempInfo(int n, String mode, String normalTemp, String dayTemp, String nightTemp,
									String awayTemp, String currentTemp){
			String channel = Integer.toString(n);
			switch(n){
			case 1:
				Channel1 = channel;
				CurrentMode1 = mode;
				CurrentNormalTemp1 = normalTemp;
				CurrentDayTemp1 = dayTemp;
				CurrentNightTemp1 = nightTemp;
				CurrentAwayTemp1 = awayTemp;
				CurrentTemp1 = currentTemp;
				
				break;
			case 2:
				Channel2 = channel;
				CurrentMode2 = mode;
				CurrentNormalTemp2 = normalTemp;
				CurrentDayTemp2 = dayTemp;
				CurrentNightTemp2 = nightTemp;
				CurrentAwayTemp2 = awayTemp;
				CurrentTemp2 = currentTemp;
				
				break;
			case 3:
				Channel1 = channel;
				CurrentMode3 = mode;
				CurrentNormalTemp3 = normalTemp;
				CurrentDayTemp3 = dayTemp;
				CurrentNightTemp3 = nightTemp;
				CurrentAwayTemp3 = awayTemp;
				CurrentTemp3 = currentTemp;
				break;
			case 4:
				Channel1 = channel;
				CurrentMode4 = mode;
				CurrentNormalTemp4 = normalTemp;
				CurrentDayTemp4 = dayTemp;
				CurrentNightTemp4 = nightTemp;
				CurrentAwayTemp4 = awayTemp;
				CurrentTemp4 = currentTemp;
				break;
			case 5:
				Channel1 = channel;
				CurrentMode5 = mode;
				CurrentNormalTemp5 = normalTemp;
				CurrentDayTemp5 = dayTemp;
				CurrentNightTemp5 = nightTemp;
				CurrentAwayTemp5 = awayTemp;
				CurrentTemp5 = currentTemp;
				
				break;
			case 6:
				Channel1 = channel;
				CurrentMode6 = mode;
				CurrentNormalTemp6 = normalTemp;
				CurrentDayTemp6 = dayTemp;
				CurrentNightTemp6 = nightTemp;
				CurrentAwayTemp6 = awayTemp;
				CurrentTemp6 = currentTemp;
				break;
			default:
				break;
			}
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
				//sendText(Integer.toString(ID));				// Sends the Player ID
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
		
		public void holidayTimer(long s, String m) {
			String modeToChange = m;
			slept = false;
			Thread holidayThread = new Thread(new Runnable() {
				
                @Override
                public void run() {
			    try {
			        //Thread sleeps and will run the necessary methods when done
			        TimeUnit.SECONDS.sleep(s);
			        slept = true;
			    } catch(InterruptedException ie) {
			    	ie.printStackTrace();
			    }
			    try {
			    	if(modeToChange.equals("day") && slept) {
			    		 int currentTemp1 = Integer.parseInt(CurrentTemp1);
			             int day1 = Integer.parseInt(CurrentDayTemp1);
			             String wateringFlag1;
			             if (currentTemp1 < day1) wateringFlag1 = "1"; else wateringFlag1 = "0";
 
			    		smartMessage.add("007260112," + Channel1 + ",0,0," + ModeDay + ","
			                     + CurrentNormalTemp1 + "," + CurrentDayTemp1 + "," + CurrentNightTemp1 
			                     + "," + CurrentAwayTemp1 + "," + wateringFlag1 + ","
			                     + "15");
			    		
			    		 int currentTemp2 = Integer.parseInt(CurrentTemp2);
			             int day2 = Integer.parseInt(CurrentDayTemp2);
			             String wateringFlag2;
			             if (currentTemp2 < day2) wateringFlag2 = "1"; else wateringFlag2 = "0";
 
			    		smartMessage.add("007260112," + Channel2 + ",0,0," + ModeDay + ","
			                     + CurrentNormalTemp2 + "," + CurrentDayTemp2 + "," + CurrentNightTemp2 
			                     + "," + CurrentAwayTemp2 + "," + wateringFlag2 + ","
			                     + "15");
			    		
			    		 int currentTemp3 = Integer.parseInt(CurrentTemp3);
			             int day3 = Integer.parseInt(CurrentDayTemp3);
			             String wateringFlag3;
			             if (currentTemp3 < day3) wateringFlag3 = "1"; else wateringFlag3 = "0";
 
			    		smartMessage.add("007260112," + Channel3 + ",0,0," + ModeDay + ","
			                     + CurrentNormalTemp3 + "," + CurrentDayTemp3 + "," + CurrentNightTemp3 
			                     + "," + CurrentAwayTemp3 + "," + wateringFlag3 + ","
			                     + "15");
			    		
			    		int currentTemp4 = Integer.parseInt(CurrentTemp4);
			            int day4 = Integer.parseInt(CurrentDayTemp4);
			            String wateringFlag4;
			            if (currentTemp4 < day4) wateringFlag4 = "1"; else wateringFlag4 = "0";

			    		smartMessage.add("007260112," + Channel4 + ",0,0," + ModeDay + ","
			                     + CurrentNormalTemp4 + "," + CurrentDayTemp4 + "," + CurrentNightTemp4 
			                     + "," + CurrentAwayTemp4 + "," + wateringFlag4 + ","
			                     + "15");
			    		
			    		int currentTemp5 = Integer.parseInt(CurrentTemp5);
			            int day5 = Integer.parseInt(CurrentDayTemp5);
			            String wateringFlag5;
			            if (currentTemp5 < day5) wateringFlag5 = "1"; else wateringFlag5 = "0";

			    		smartMessage.add("007260112," + Channel5 + ",0,0," + ModeDay + ","
			                     + CurrentNormalTemp5 + "," + CurrentDayTemp5 + "," + CurrentNightTemp5 
			                     + "," + CurrentAwayTemp5 + "," + wateringFlag5 + ","
			                     + "15");
			    		
			    		int currentTemp6 = Integer.parseInt(CurrentTemp6);
			            int day6 = Integer.parseInt(CurrentDayTemp6);
			            String wateringFlag6;
			            if (currentTemp6 < day6) wateringFlag6 = "1"; else wateringFlag6 = "0";

			    		smartMessage.add("007260112," + Channel6 + ",0,0," + ModeDay + ","
			                     + CurrentNormalTemp6 + "," + CurrentDayTemp6 + "," + CurrentNightTemp6 
			                     + "," + CurrentAwayTemp6 + "," + wateringFlag6 + ","
			                     + "15");
			    		
			    	}
			    	else if(modeToChange.equals("night")){
			    		int currentTemp1 = Integer.parseInt(CurrentTemp1);
			            int night1 = Integer.parseInt(CurrentNightTemp1);
			            String wateringFlag1;
			            if (currentTemp1 < night1) wateringFlag1 = "1"; else wateringFlag1 = "0";

			    		smartMessage.add("007260112," + Channel1 + ",0,0," + ModeNight + ","
			                     + CurrentNormalTemp1 + "," + CurrentDayTemp1 + "," + CurrentNightTemp1 
			                     + "," + CurrentAwayTemp1 + "," + wateringFlag1 + ","
			                     + "15");
			    		
			    		int currentTemp2 = Integer.parseInt(CurrentTemp2);
			            int night2 = Integer.parseInt(CurrentNightTemp2);
			            String wateringFlag2;
			            if (currentTemp2 < night2) wateringFlag2 = "1"; else wateringFlag2 = "0";

			    		smartMessage.add("007260112," + Channel2 + ",0,0," + ModeNight + ","
			                     + CurrentNormalTemp2 + "," + CurrentDayTemp2 + "," + CurrentNightTemp2 
			                     + "," + CurrentAwayTemp2 + "," + wateringFlag2 + ","
			                     + "15");
			    		
			    		int currentTemp3 = Integer.parseInt(CurrentTemp3);
			            int night3 = Integer.parseInt(CurrentNightTemp3);
			            String wateringFlag3;
			            if (currentTemp3 < night3) wateringFlag3 = "1"; else wateringFlag3 = "0";

			    		smartMessage.add("007260112," + Channel3 + ",0,0," + ModeNight + ","
			                     + CurrentNormalTemp3 + "," + CurrentDayTemp3 + "," + CurrentNightTemp3 
			                     + "," + CurrentAwayTemp3 + "," + wateringFlag3 + ","
			                     + "15");
			    		
			    		int currentTemp4 = Integer.parseInt(CurrentTemp4);
			            int night4 = Integer.parseInt(CurrentNightTemp4);
			            String wateringFlag4;
			            if (currentTemp4 < night4) wateringFlag4 = "1"; else wateringFlag4 = "0";

			    		smartMessage.add("007260112," + Channel4 + ",0,0," + ModeNight + ","
			                     + CurrentNormalTemp4 + "," + CurrentDayTemp4 + "," + CurrentNightTemp4 
			                     + "," + CurrentAwayTemp4 + "," + wateringFlag4 + ","
			                     + "15");

			    		int currentTemp5 = Integer.parseInt(CurrentTemp5);
			            int night5 = Integer.parseInt(CurrentNightTemp5);
			            String wateringFlag5;
			            if (currentTemp5 < night5) wateringFlag5 = "1"; else wateringFlag5 = "0";

			    		smartMessage.add("007260112," + Channel5 + ",0,0," + ModeNight + ","
			                     + CurrentNormalTemp5 + "," + CurrentDayTemp5 + "," + CurrentNightTemp5 
			                     + "," + CurrentAwayTemp5 + "," + wateringFlag5 + ","
			                     + "15");
			    		
			    		int currentTemp6 = Integer.parseInt(CurrentTemp6);
			            int night6 = Integer.parseInt(CurrentNightTemp6);
			            String wateringFlag6;
			            if (currentTemp6 < night6) wateringFlag6 = "1"; else wateringFlag6 = "0";

			    		smartMessage.add("007260112," + Channel6 + ",0,0," + ModeNight + ","
			                     + CurrentNormalTemp6 + "," + CurrentDayTemp6 + "," + CurrentNightTemp6 
			                     + "," + CurrentAwayTemp6 + "," + wateringFlag6 + ","
			                     + "15");
			    	}
			    	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
			    System.out.println("Hello world!");
                }
			});
			holidayThread.start();
			
		}
		boolean returnSmartMessageNotNull() {
			if (!smartMessage.isEmpty()) return true;
			else return false;
		}
		int SmartMessageSize() {
			return smartMessage.size();
		}
		String getSmartMessageNr(int id) {
			return smartMessage.get(id);
		}
		public void removeSmartMessageNr ( int id) {
			smartMessage.remove(id);
		}
}


