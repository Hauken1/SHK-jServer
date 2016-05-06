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
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;


import com.sun.javafx.binding.Logging;
import com.sun.javafx.property.adapter.ReadOnlyPropertyDescriptor.ReadOnlyListener;

import javafx.scene.chart.PieChart.Data;

/**
 * 
 * The UserClient class stores information and gives each user some functionality. 
 * There is one user for each dwelling unit + administrator (1-4). Each user has their separate 
 * timers and information stores. This information is information about timers, what commands
 * the user has requested from the server and temperature information. 
 *
 */
public class UserClient {
		
		private Socket connection;
		private BufferedReader input;
		private BufferedWriter output;
		private static ArrayList<String> message = new ArrayList<String>();
		private static ArrayList<String> smartMessage = new ArrayList<String>();
		private ArrayList<AlarmClock> dayTimerList = new ArrayList<AlarmClock>();
		private ArrayList<AlarmClock> nightTimerList = new ArrayList<AlarmClock>();
		
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
		
		private int dayChangeHours;
		private int dayChangeMin;
		private int nightChangeHours;
		private int nightChangeMin;
		
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
		/**
		 * Constructor for Userclient that is called when the server first starts up 
		 * The user is given the correct userId. Temperature information and day/night timer 
		 * is initialized. 
		 * @param n UserId of the user. This range from 1-4 (4 total  real users/accounts).
		 */
		public UserClient(int n) {
			userId = n;
			InitTempInfo();
			initDayNightTimer();
			
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
		/**
		 * Returns the userId of the user.
		 * @return userId
		 */
		public int returnUserIDInt() {
			return userId;
		}
		/**
		 * Returns the user id as a String.
		 * @return UserId as string
		 */
		public String returnUserID() {
			String uId;
			uId = Integer.toString(userId);
			return uId;
		}
		
		/**
		 * Returns a boolean, which tells if the user is connected or not. 
		 * @return the connected boolean
		 */
		public boolean returnConnected() {
			return connected;
		}
		
		/**
		 * Resets connection variables
		 */
		public void setFalseConnection(){
			connected = false;
			connection = null;
			input = null;
			output = null;
		}
		/**
		 * Sets this user's socket to a connection.
		 * @param connection the Sockets the users connects to.
		 */
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
		
		/**
		 * Returns the Socket of the user
		 * @return the Socket
		 */
		public Socket returnSocket() {
			return connection;
		}
		
		/**
		 * Tries to read messages from the Socket if the user is connected and the BufferedReader
		 * as information to be read.
		 * @return information that is read
		 * @throws IOException if there is nothing to be read or input is null.
		 */
		public String read() throws IOException {
			if(connected){
				if (input.ready())
					return input.readLine();
				return null;
			}
			return null;
		}
		
		/**
		 * Initialize day and night variables. 
		 */
		public void initDayNightTimer() {
			dayChangeHours = 0;
			dayChangeMin = 0;
			nightChangeHours = 0;
			nightChangeMin = 0;
		}

		/**
		 * Initialize temperature information.
		 */
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
		
		/**
		 * Sets the user temperature information to variables received from application/HDL. 
		 * @param n the channel nr
		 * @param mode mode of the heating controller (day, night, away, holiday)
		 * @param normalTemp the set temperature for holiday
		 * @param dayTemp the set temperature for day
		 * @param nightTemp the set temperature for night
		 * @param awayTemp the set temperature for away
		 * @param currentTemp the current temperature 
		 */
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
				Channel3 = channel;
				CurrentMode3 = mode;
				CurrentNormalTemp3 = normalTemp;
				CurrentDayTemp3 = dayTemp;
				CurrentNightTemp3 = nightTemp;
				CurrentAwayTemp3 = awayTemp;
				CurrentTemp3 = currentTemp;
				break;
			case 4:
				Channel4 = channel;
				CurrentMode4 = mode;
				CurrentNormalTemp4 = normalTemp;
				CurrentDayTemp4 = dayTemp;
				CurrentNightTemp4 = nightTemp;
				CurrentAwayTemp4 = awayTemp;
				CurrentTemp4 = currentTemp;
				break;
			case 5:
				Channel5 = channel;
				CurrentMode5 = mode;
				CurrentNormalTemp5 = normalTemp;
				CurrentDayTemp5 = dayTemp;
				CurrentNightTemp5 = nightTemp;
				CurrentAwayTemp5 = awayTemp;
				CurrentTemp5 = currentTemp;
				
				break;
			case 6:
				Channel6 = channel;
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
		
		/**
		 * adds a message to the message list
		 * @param cmd the message to be added to the list.
		 */
		public void addMessage(String cmd) {
			//System.out.println(cmd + " user mg1");
			message.add(cmd);
		}
		/**
		 * Removes a message from the message list
		 * @param n the number of the message to be removed
		 */
		public void removeMessage(int n) {
			message.remove(n);
		}
		/**
		 * Method that checks if the user has a message which contains the same command as in @param
		 * and checks if this user added it to the list. The userId is added to the end of every message to 
		 * seperate them from each other. 
		 * @param messagecmd the command which is to be checked for
		 * @return true or false based on if the message is found or not. 
		 */
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
		/**
		 * This method is used if the user has forgotten his/her password. 
		 * Tries to log in with the user name and emergency password received from the application.
		 * @return true or false depending on if the login was successful or not. 
		 */
		public boolean emergencyLoginChecker() {
			try {
				String tempName = input.readLine();
				String tempPass = input.readLine();
				
				int ID = DatabaseHandler.logIn(tempName, tempPass);
				
				if(ID == 0){
					sendText(Integer.toString(ID));
					return false;
				}
				else {
					sendText(Integer.toString(ID));
					return true;
				}
			}catch(IOException ioe){
				ioe.printStackTrace();
			}
			return false;
		}
		/**
		 * Method that changes the password of the user to the new password sent by the 
		 * application.
		 * @return true or false depending on if the password was changed or not.
		 * @throws IOException throws if it cant send answer back to application
		 */
		public boolean forgotPassord() throws IOException {
			try{
				String userName = input.readLine();
				String newPass = input.readLine();
				
				DatabaseHandler.updatePW(userName, newPass);
				sendText("PWChanged");
				return true;
			}catch(Exception e){
				e.printStackTrace();
			}
			sendText("NotChanged");
			return false;
		}
		/**
		 * Method that tries to login with the received user name and password. 
		 * @return returns true or false depending on if the login was successful or not. 
		 */
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
		
		/**
		 * Method that changes the password of a user. The user is sending his/her old password
		 * and if a login is successful, the password will be changed to the new one. 
		 * @return
		 */
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
		
		/**
		 * Method that starts a AlarmClock, which act as a daily iterated alarmclock. 
		 * There can only be one alarmclock per user. Day and night alarmclock is seperated.
		 * @param hours the hour of the day for when the alarm should go off.
		 * @param minutes the minutes within the hour for when the alarm should go off. 
		 */
		public void dayModeTimer(String hours, String minutes){
			int hour = Integer.parseInt(hours);
			int mins = Integer.parseInt(minutes);
			
			if(dayTimerList.isEmpty()){
				AlarmClock alarm = new AlarmClock(hour, mins, 0, 2);
				dayTimerList.add(alarm);
				alarm.start();
				System.out.println("Started day timer..");
			}
			else {
				dayTimerList.removeAll(dayTimerList);
				AlarmClock alarm = new AlarmClock(hour, mins, 0, 2);
				dayTimerList.add(alarm);
				alarm.start();
				System.out.println("Deleted existing day timer and started new..");
			}
		}
		
		/**
		 * Method that starts a AlarmClock, which act as a daily iterated alarmclock. 
		 * There can only be one alarmclock per user. Day and night alarmclock is seperated.
		 * @param hours the hour of the day for when the alarm should go off.
		 * @param minutes the minutes within the hour for when the alarm should go off. 
		 */
		public void nightModeTimer(String hours, String minutes){
			int hour = Integer.parseInt(hours);
			int mins = Integer.parseInt(minutes);
			if(nightTimerList.isEmpty()){
				AlarmClock alarm = new AlarmClock(hour, mins, 0, 3);
				nightTimerList.add(alarm);
				alarm.start();
				System.out.println("Started night timer..");
			}
			else {
				nightTimerList.removeAll(nightTimerList);
				AlarmClock alarm = new AlarmClock(hour, mins, 0, 3);
				nightTimerList.add(alarm);
				alarm.start();
				System.out.println("Deleted existing night timer and started new..");
			}
		}
		
		/**
		 * Method that starts a thread which sends a command to HDL, which again 
		 * puts the dwelling unit to day or night mode, when the thread as sleept for
		 * @param s seconds. 
		 * @param s sleep time of the thread
		 * @param m mode to change to
		 */
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
			    		sendDayCommand();
			    		
			    	}
			    	else if(modeToChange.equals("night")){
			    		sendNightCommand();
			    	}
			    	
			    }catch(Exception e){
			    	e.printStackTrace();
			    }
                }
			});
			holidayThread.start();
			
		}
		
		/**
		 * Message that uses the saved temperature information and puts the dwelling unit
		 * to day mode.
		 */
		public void sendDayCommand() {
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
	
		/**
		 * Message that uses the saved temperature information and puts the dwelling unit
		 * to night mode.
		 */
		public void sendNightCommand() {
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
		/**
		 * Checks if smartMessage list is empty or not. 
		 * @return true or false depending on if the smarMessage list is empty
		 */
		boolean returnSmartMessageNotNull() {
			if (!smartMessage.isEmpty()) return true;
			else return false;
		}
		
		/**
		 * returns the size of the smartMessage list.
		 * @return size of smartMessage
		 */
		int SmartMessageSize() {
			return smartMessage.size();
		}
		
		/**
		 * Retrieves smartMessage number @param id.
		 * @param id number of smartMessage to be retrieved.
		 * @return the smartMessage 
		 */
		String getSmartMessageNr(int id) {
			return smartMessage.get(id);
		}
		
		/**
		 * Removes a smartMessage
		 * @param id the smartMessage to be removed
		 */
		public void removeSmartMessageNr ( int id) {
			smartMessage.remove(id);
		}
		
		/**
		 * Class which adds the day and night timer functionality. 
		 * This method retrieves time from the user and then starts a 
		 * daily iterated alarmclock. This method has support for day light saving time, which
		 * again allows the clock to repeat indefinitely. 
		 * 
		 *  Most of this class is taken from: http://www.ibm.com/developerworks/library/j-schedule/
		 *
		 */
		public class AlarmClock {

		    private final Scheduler scheduler = new Scheduler();
		    private final SimpleDateFormat dateFormat =
		        new SimpleDateFormat("dd MMM yyyy HH:mm:ss.SSS");
		    private final int hourOfDay, minute, second, mode;

		    /**
		     * Constructor of the alarmClass
		     * @param hourOfDay hour for when the clock should go off.
		     * @param minute minute within the hour for when the clock should go off.
		     * @param second seconds within the minutes for when the clock should go off. 
		     * @param mode the mode it should change to. 
		     */
	
		    public AlarmClock(int hourOfDay, int minute, int second, int mode) {
		        this.hourOfDay = hourOfDay;
		        this.minute = minute;
		        this.second = second;
		        this.mode = mode;
		    }

		    /**
		     * Method that is called when the clock has started, it will then go off 
		     * when the time input by user goes off. 
		     */
		    public void start() {
		        scheduler.schedule(new SchedulerTask() {
		            /**
		             * Method that is called when the clock goes off. 
		             * Sends a message to HDL, to put the dwelling unit to correct mode.
		             */
		        	public void run() {
		                sendingMessage();
		            }
		        	/**
		        	 * Method calls another method based on the mode input by user. 
		        	 * 2 is day mode and 3 is night mode.
		        	 */
		            private void sendingMessage() {
		                
		                if(mode == 2) {
		                	System.out.println("Sending day message! " +
				                    "\nThe time is: " + dateFormat.format(new Date()));
		                	sendDayCommand();
		                }
		                else if(mode == 3) {
		                	System.out.println("Sending night message! " +
				                    "\nThe time is: " + dateFormat.format(new Date()));
		                	sendNightCommand();
		                }
		            }
		        }, new DailyIterator(hourOfDay, minute, second));
		    }

		}
		
		
		/**
		 * Class which act as the daily iterator of the AlarmClock. 
		 * 
		 *Most of this class is taken from: http://www.ibm.com/developerworks/library/j-schedule/
		 */
		public class DailyIterator implements ScheduleIterator {
		    private final int hourOfDay, minute, second;
		    private final Calendar calendar = Calendar.getInstance();

		    /**
		     * Parameters containing the alarm time.
		     * Called when no date is sent.
		     * @param hourOfDay hour within the day for when the alarm should go off
		     * @param minute minutes within the hour for when the alarm should go off. 
		     * @param second seconds within the minutes for when the alarm should go off. 
		     */
		    public DailyIterator(int hourOfDay, int minute, int second) {
		        this(hourOfDay, minute, second, new Date());
		    }
		    /**
		     * Parameters containing the alarm time
		     * called when date is sent.
		     * @param hourOfDay hour within the day for when the alarm should go off.
		     * @param minute minutes within the hour for when the alarm should go off. 
		     * @param second seconds within the minutes for when the alarm should go off.
		     * @param date the date for when the alarm should go off. 
		     */
		    public DailyIterator(int hourOfDay, int minute, int second, Date date) {
		        this.hourOfDay = hourOfDay;
		        this.minute = minute;
		        this.second = second;
		        calendar.setTime(date);
		        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
		        calendar.set(Calendar.MINUTE, minute);
		        calendar.set(Calendar.SECOND, second);
		        calendar.set(Calendar.MILLISECOND, 0);
		        if (!calendar.getTime().before(date)) {
		            calendar.add(Calendar.DATE, -1);
		        }
		    }

		    /**
		     * Returns the next day
		     */
		    public Date next() {
		        calendar.add(Calendar.DATE, 1);
		        return calendar.getTime();
		    }

		}
		
		/**
		 * Method that retrieves the next date. 
		 *
		 */
		public interface ScheduleIterator {
		    public Date next();
		}
		
		/**
		 * Class that schedules and reschedules the alarmclock. 
		 *
		 */
		public class Scheduler {

			/**
			 * Inner Class which extends a TimerTask and
			 * schedules and reschedules.
			 *
			 */
		    class SchedulerTimerTask extends TimerTask {
		    	
		        private SchedulerTask schedulerTask;
		        private ScheduleIterator iterator;
		        /**
		         * Constructor of the class. 
		         * @param schedulerTask the task to be scheduled 
		         * @param iterator the iterator.
		         */
		        public SchedulerTimerTask(SchedulerTask schedulerTask,
		                ScheduleIterator iterator) {
		            this.schedulerTask = schedulerTask;
		            this.iterator = iterator;
		        }
		        /**
		         * Method that runs the scheduler and reschedules it.
		         */
		        public void run() {
		            schedulerTask.run();
		            reschedule(schedulerTask, iterator);
		        }
		    }

		    private final Timer timer = new Timer();

		    /**
		     * Constructor of the scheduler. 
		     * Does nothing
		     */
		    public Scheduler() {
		    }
		    
		    /**
		     * Method that cancels the timer/clock.
		     */
		    public void cancel() {
		        timer.cancel();
		    }

		    /**
		     * Constructor of the scheduler. 
		     * It receives the next date and checks if a task is already scheduled or not.  
		     * @param schedulerTask the tasks to be scheduled. 
		     * @param iterator the iterator. 
		     */
		    public void schedule(SchedulerTask schedulerTask,
		            ScheduleIterator iterator) {

		        Date time = iterator.next();
		        if (time == null) {
		            schedulerTask.cancel();
		        } else {
		            synchronized(schedulerTask.lock) {
		                if (schedulerTask.state != SchedulerTask.VIRGIN) {
		                  throw new IllegalStateException("Task already scheduled" + "or cancelled");
		                }
		                schedulerTask.state = SchedulerTask.SCHEDULED;
		                schedulerTask.timerTask =
		                    new SchedulerTimerTask(schedulerTask, iterator);
		                timer.schedule(schedulerTask.timerTask, time);
		            }
		        }
		    }

		    /**
		     * Method that reschedules the task.
		     * @param schedulerTask task to be rescheduled
		     * @param iterator the current iterator
		     */
		    private void reschedule(SchedulerTask schedulerTask,
		            ScheduleIterator iterator) {

		        Date time = iterator.next();
		        if (time == null) {
		            schedulerTask.cancel();
		        } else {
		            synchronized(schedulerTask.lock) {
		                if (schedulerTask.state != SchedulerTask.CANCELLED) {
		                    schedulerTask.timerTask =
		                        new SchedulerTimerTask(schedulerTask, iterator);
		                    timer.schedule(schedulerTask.timerTask, time);
		                }
		            }
		        }
		    }

		}
		
		/**
		 * Class that contains information about a task (alarmclock in this case). 
		 * Holds information which allows the scheduler to know if task is already 
		 * scheduled or not. 
		 */
		public abstract class SchedulerTask implements Runnable {

		    final Object lock = new Object();

		    int state = VIRGIN;
		    static final int VIRGIN = 0;
		    static final int SCHEDULED = 1;
		    static final int CANCELLED = 2;

		    TimerTask timerTask;

		    /**
		     * Constructor of the schedulerTask.
		     */
		    protected SchedulerTask() {
		    }
		    
		    /**
		     * Runnable of the schedulertask.
		     */
		    public abstract void run();

		    /**
		     * Method that cancels the task
		     * @return returns the cancelled result. 
		     */
		    public boolean cancel() {
		        synchronized(lock) {
		            if (timerTask != null) {
		                timerTask.cancel();
		            }
		            boolean result = (state == SCHEDULED);
		            state = CANCELLED;
		            return result;
		        }
		    }

		    /**
		     * Method that returns the execution time of the task.
		     * 
		     */
		    public long scheduledExecutionTime() {
		        synchronized(lock) {
		         return timerTask == null ? 0 : timerTask.scheduledExecutionTime();
		        }
		    }

		}
}


