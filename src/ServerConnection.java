import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.activation.DataHandler;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.derby.database.Database;

//import com.sun.xml.internal.ws.encoding.MtomCodec.ByteArrayBuffer;

/**
 * This class is the core of the server. 
 * This class makes all the users, got five separate threads
 * which listens to different message received from HDL and user applications. It creates the database.
 * It also has different message handlers which runs method based on the messages
 * that is received. 
 *   
 */
public class ServerConnection extends JFrame {

	private DatagramSocket socket; //HDL Socket. Sends to and receives date from HDL.
	private DatagramSocket hdlSocket;
	private static ServerSocket serverSocket; //ServerSocket which listen for app users.
	ExecutorService executorService; //Thread executor.
	private JTextArea outputArea;
	private boolean shutdown = false;
	boolean uniqueUser = false;	
	private ArrayList<UserClient> user = new ArrayList<UserClient>(); //List which contains valid users.
	private ArrayList<UserClient> tempUser = new ArrayList<UserClient>(); //List which contains temporary users
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);	
	public static final int MAX_PACKET_SIZE = 512; //Max size of a packet. 
	

	private InetAddress replyAddress; 
	private int serverPort = 12345; //Port of the server. 
	private int datagramPort = 1234;
	
	InetAddress listenAddress;
	
	//******HDL******\\
	int sourceAddress = 510;
	int sourceDevice = 65279; //0xfeff;
	private String HDLip = "192.168.10.255";
	private String HDLReceivingIP = "0.0.0.0";
	private int hdlPort = 6000; //Fixxed HDL Port
	//******HDL*******\
	
	int algo = 0;
	
	/**
	 * Constructor of the ServerConnection class. 
	 * This method creates all the listeners, created the database and sets up sockets.  
	 */
	public ServerConnection() {
		try {
			//listenAddress = InetAddress.getLocalHost();
			
			socket = new DatagramSocket(null);
			socket.setBroadcast(true);
			socket.bind(new InetSocketAddress(InetAddress.getByName(HDLReceivingIP), hdlPort));
			
			serverSocket = new ServerSocket(serverPort);
			serverSocket.setReuseAddress(true);
			
			executorService = Executors.newCachedThreadPool();
			
			DatabaseHandler.createNewUserDB();
			//DatabaseHandler.updatePW("hybel","vannflaske");
			//DatabaseHandler.printDB();
			for(int i=1; i <= 4; i++ ) {
				UserClient client = new UserClient(i);
				user.add(client);
				System.out.println("Adder user..." + i);
			}
			startLoginMonitor();
			startAPPLoginListener();
			startAPPMessageListener();
			startHDLMessageListener();
			startSmartMessageListener();
				
			/**/
			/*
			String msg1 = "Command:000002117,1,0";
			CommandMessageController(msg1.substring(8,msg1.length()));
			
			*/
			//String msg2 = "Command:007262112,1";
			//CommandMessageController(msg2.substring(8,msg2.length()));
			
			//Varmestyring
			//UserClient u = new UserClient();
			//String msg2 = "Command:007260112,1,0,0,1,29,29,29,29,1,5";

			//CommandMessageController(u, msg2.substring(8,msg2.length()));
			//info 7262
			//lese av temperatur p� varmestyringskontroller
			
			//String msg3 = "Command:007262112,1";
			//String msg2 = "Command:006468120";
			//String msg2 = "Command:007262112,1";
			//CommandMessageController(msg2.substring(8,msg2.length()));
			//CommandMessageController(msg3.substring(8,msg3.length()));
			//Rele command 49 (singel channel ligthing) 
			//String msg2 = "Command:000002117,1,0";
			//CommandMessageController(u,msg2.substring(8,msg2.length()));
			/*
			String msg3 = "Command:000049114,2,100,0,1";
			CommandMessageController(msg3.substring(8,msg3.length()));
			*/
			
			/* Vindu
			String msg2 = "Command:058336113,2,1";
			CommandMessageController(msg2.substring(8,msg2.length()));
			
			/*
			String msg3 = "Command:007262112";
			CommandMessageController(msg2.substring(8,msg3.length()));
			*/
			//String msg2 = "Command:"
			//executorService.shutdown();

			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}			
	}
	/**
	 * Method that execute a thread to start listen for user connections. 
	 * User connections is put into a temp user list. Users is replaces if a user tries to connect again
	 * while that user already connected earlier. 
	 */
	private void startLoginMonitor() {
		executorService.execute(() -> {
			while (!shutdown) {
				Random rnd = new Random();
				try {
					Socket s = serverSocket.accept(); 
					
					if(!(tempUser.isEmpty())) {
						for(int i=0; i < tempUser.size(); i++) {
							if((s == tempUser.get(i).returnSocket()) && uniqueUser == false){
								uniqueUser = true;
								tempUser.remove(i);
								UserClient client = new UserClient(s);
								tempUser.add(client);
								System.out.println("Replaced user...");	
							}		
						}
						if(uniqueUser == false){
							UserClient client = new UserClient(s);
							tempUser.add(client);
						}
					}
					else {
						UserClient client = new UserClient(s);
						tempUser.add(client);
						System.out.println("User connected...");
					}
					uniqueUser = false;
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
				 try {
						TimeUnit.MILLISECONDS.sleep(rnd.nextInt(200) * 10);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		});
	}
	
	/**
	 * Method that executes a thread to start to listen for the applications login functionality. 
	 * This is forgot password, change password and logins. 
	 * When a user is logged in, that user is moved to another list which holds the "Valid"/online users.
	 * Users is removed if they cannot receive messages. 
	 */
	private void startAPPLoginListener() {
		executorService.execute(() -> {
			while (!shutdown) {
				Random rnd = new Random();
				try {
					synchronized(tempUser) {
						Iterator<UserClient> i = tempUser.iterator();
						while (i.hasNext()) {
							UserClient u = i.next();
							try {
								String msg = u.read();
								if (msg != null) {
									System.out.println(msg);							
									if (msg.equals("Disconnect")) {
										System.out.println("Removing user:" + u.returnUserID());
										i.remove();
										tempUser.remove(i);
										//shandleLogout(p);
									}
									else if(msg.equals("Login")){
										if(u.loginChecker()){ 
											System.out.println("User logged in...");
											int id = u.returnUserIDInt();
											Socket connection = u.returnSocket();
											i.remove();
											System.out.println("Removed temp user");
											for(int ii=0; ii< user.size(); ii++) {
												if(user.get(ii).userId == id) {
													user.get(ii).setSocket(connection);
													user.get(ii).sendText(Integer.toString(id));
												}
											}
										
										}
									}
									else if(msg.equals("EmergencyLogin")){
										if(u.emergencyLoginChecker()){
											System.out.println("User forgot pw");
										}
									}
									else if (msg.equals("ForgotPw")){
										if(u.forgotPassord()){
											System.out.println("User got a new pw");
										}
									}
									else if(msg.equals("ChangePW")){
										if(u.changePassword()) System.out.println("User changed password...");
									}
									
								}
							} catch (Exception e) {
								System.out.println("Error with message");
								e.printStackTrace();
							}
							/*
							if(!u.returnSocket().isConnected() && u.returnSocket().isClosed()) {
								System.out.println("Fjerner bruker:" + u.returnUserID());
								user.remove(u);
								i.remove();
							}
							*/
						algo++;
						if(algo == 100 && (u.userId >= 0)) {
							try {
								algo = 0;
								u.sendText("Ping");
							} catch (Exception e) {
								i.remove();
								System.out.println("Removed user");
							}
						}
						if(algo > 100) algo = 0;
						}
					}
				 try {
						TimeUnit.MILLISECONDS.sleep(rnd.nextInt(200) * 10);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception ie) {
					ie.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Method that executes a thread to start listen for user messages. 
	 * This messages are then processed and is handled correctly. 
	 */
	private void startAPPMessageListener() {
		executorService.execute(() -> {
			while (!shutdown) {
				Random rnd = new Random();
				try {
					synchronized(user) {
						Iterator<UserClient> i = user.iterator();
						while (i.hasNext()) {
							UserClient u = i.next();
							try {
								String msg = u.read();
								if (msg != null) {
									System.out.println(msg);							
									if (msg.equals("Disconnect")) {
										System.out.println("User:" + u.returnUserID() + " Disconnected");
										//i.remove();
										//user.remove(i);
										//shandleLogout(p);
									}
									else if (msg.startsWith("Command:")) {
										CommandMessageController(u, msg.substring(8,msg.length()));
										 
									}
									/*
									else if(msg.equals("Login")){
										if(u.loginChecker()) 
											System.out.println("User logged in...");
									}
									*/
								
									else if (msg.startsWith("Monitor:")){ // Monitoring-related
										handleMonitoringControllerMessages(msg.substring(8,msg.length()), u);
									}
									else if (msg.startsWith("HolidayT:")) {
										handleHolidayTimerMessages(msg.substring(9,msg.length()),u);	
									}
									else if (msg.startsWith("DayNightSetT:")){
										handleDayNightTimer(msg.substring(13,msg.length()),u);
									}
									else {
										
									}
								}

							} catch (Exception e) {
								System.out.println("Error with message");
								e.printStackTrace();
							}
							/*
							if(!u.returnSocket().isConnected() && u.returnSocket().isClosed()) {
								System.out.println("Fjerner bruker:" + u.returnUserID());
								user.remove(u);
								i.remove();
							}
							*/
						algo++;
						if(algo == 100 && (u.userId >= 0)) {
							try {
								algo = 0;
								if(u.returnConnected()) u.sendText("Ping");
							} catch (Exception e) {
								//i.remove();
								u.setFalseConnection();
								System.out.println("Connection of user removed");
							}
						}
						if(algo > 100) algo = 0;
						}
					}
				 try {
						TimeUnit.MILLISECONDS.sleep(rnd.nextInt(100) * 10);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				
				
				} catch (Exception ie) {
					ie.printStackTrace();
				}
			}
		});
	}
	
	/**
	 * Method that executes a thread to start listen for packets from HDL.
	 * Depending on the commands of these packets, they are handled accordingly. 
	 * For instance, if a user has requested temperature information, and this listener
	 * receives a packets which contains this information, that information is sent to the correct
	 * user. 
	 */
	private void startHDLMessageListener() {
		executorService.execute(() -> {
			while (!shutdown) {
				Random rnd = new Random();
				try {
					 byte[] data = new byte[MAX_PACKET_SIZE];
					 DatagramPacket receivePacket = new DatagramPacket(data,
							 data.length);
		             socket.receive(receivePacket);
		             
		             /*
		             displayMessage("\n\nPacket received from HDL:"
			                    + "\nFrom host: "
			                    + receivePacket.getAddress()
			                    + "\nHost port: "
			                    + receivePacket.getPort()
			                    + "\nLength: "
			                    + receivePacket.getLength()
			                    + "\nContaining: "
			                    + new String(receivePacket.getData(), 0,
			                            receivePacket.getLength()));
		             */
		            HdlPacket p = HdlPacket.parse(receivePacket.getData(), receivePacket.getLength());
		            
		            //Tempinfo received when temperature is changed on the heating controller
		            if(p != null) {
		            	if(p.command == 93/*71 / 69*/) {	
		            		String messageCommand = "7260"/*"6470"*/;
		       
		            		/*
		            		int ii = 0; 
		            		for (byte b : p.data) {
		            		    System.out.println((b & 0xFF) + " HDL " + ii++);
		            		}
		           			*/
		            		//3 is current temp, 6, 7, 8 and 9 is what the current heating modes is set to currently
		            	//	int currentTemp = (p.data[3] &  0xff); //Not valid as long as heat controller is not monitoring cTemp.
		            		
		            		int channel = (p.data[2] &  0xff);	//Channel of heating controller
		            		int currentMode = (p.data[5] &  0xff); //Channel mode
		            		int normalTemp = (p.data[6] &  0xff); //Normal temp
		            		int dayTemp = (p.data[7] &  0xff);	//Day temp
		            		int nightTemp = (p.data[8] &  0xff); //Night temp
		            		int awayTemp = (p.data[9] &  0xff); //Away temp
		            		int currentTemp = (p.data[11] &  0xff);
		            		
		            		/*
		            		System.out.println(channel + " cC");
		            		System.out.println(currentMode + " cM");
		            		System.out.println(normalTemp + " normal");
		            		System.out.println(dayTemp + " day");
		            		System.out.println(nightTemp + " night");
		            		System.out.println(awayTemp + " away");
		            		System.out.println(currentTemp + " ct");
		            		*/
		            		String sChannel = Integer.toString(channel);
		            		String sCurrentMode = Integer.toString(currentMode);
		            		String sCurrentNormalTemp = Integer.toString(normalTemp);
		            		String sCurrentDayTemp = Integer.toString(dayTemp);
		            		String sCurrentNightTemp = Integer.toString(nightTemp);
		            		String sCurrentAwayTemp = Integer.toString(awayTemp);
		            		String sCurrentTemp = Integer.toString(currentTemp);
		            		
		            		for(int i = 0; i < user.size(); i++) {
		            			UserClient u = user.get(i);
								if(u.checkForMessage(messageCommand)) {
									u.setTempInfo(channel, sCurrentMode, sCurrentNormalTemp, sCurrentDayTemp, sCurrentNightTemp,
													sCurrentAwayTemp, sCurrentTemp);
									u.sendText("TempInfo:" + sChannel + sCurrentMode + sCurrentNormalTemp + sCurrentDayTemp
													+ sCurrentNightTemp + sCurrentAwayTemp + sCurrentTemp);
									System.out.println("Message sent to user:" + i);
									System.out.println("TempInfo:" + sChannel + sCurrentMode + sCurrentNormalTemp + sCurrentDayTemp
											+ sCurrentNightTemp + sCurrentAwayTemp + sCurrentTemp);
			            		}	
							}
		            		
		            		
		            	}
		            	if(p.command == 95){	//Tempinfo for the a certain channel of the heating controller 
		            		String messageCommand = "7262";
		            		/*
		            		for (int i = 1; i < p.data.length; i++) {
		            			System.out.println(p.data[i] + " cT");
		            		}
		            		*/
		            		int channel = (p.data[2] &  0xff);
		            		int currentMode = (p.data[5] &  0xff);
		            		int currentNormalTemp = (p.data[6] &  0xff);
		            		int currentDayTemp = (p.data[7] &  0xff);
		            		int currentNightTemp =(p.data[8] &  0xff);
		            		int currentAwayTemp = (p.data[9] &  0xff);
		            		int currentTemp = (p.data[11] &  0xff);
		            		
		            		String sChannel = Integer.toString(channel);
		            		String sCurrentMode = Integer.toString(currentMode);
		            		String sCurrentNormalTemp = Integer.toString(currentNormalTemp);
		            		String sCurrentDayTemp = Integer.toString(currentDayTemp);
		            		String sCurrentNightTemp = Integer.toString(currentNightTemp);
		            		String sCurrentAwayTemp = Integer.toString(currentAwayTemp);
		            		String sCurrentTemp = Integer.toString(currentTemp);
		            		
		            		/*
		            		System.out.println(channel + " cC");
		            		System.out.println(currentMode + " cM");
		            		System.out.println(currentNormalTemp + " cNT");
		            		System.out.println(currentDayTemp + " cDT");
		            		System.out.println(currentNightTemp + " cNT");
		            		System.out.println(currentAwayTemp + " cAT");
		            		System.out.println(currentTemp + " cT");
		            		*/
		            		for(int i = 0; i < user.size(); i++) {
		            			UserClient u = user.get(i);
								if(u.checkForMessage(messageCommand)) {
									u.setTempInfo(channel, sCurrentMode, sCurrentNormalTemp, sCurrentDayTemp, sCurrentNightTemp,
											sCurrentAwayTemp, sCurrentTemp);
									u.sendText("TempInfo:" + sChannel + sCurrentMode + sCurrentNormalTemp + sCurrentDayTemp
													+ sCurrentNightTemp + sCurrentAwayTemp + sCurrentTemp);
									System.out.println("Message sent to user:" + i);
									System.out.println("TempInfo:" + sChannel + sCurrentMode + sCurrentNormalTemp + sCurrentDayTemp
											+ sCurrentNightTemp + sCurrentAwayTemp + sCurrentTemp);
			            		}	
							}	
		            	}
		            	else p = null; 
		            	
		            }
		            
					} catch (Exception e) {
							System.out.println("Message error");
							e.printStackTrace();
					}
					
				 try {
						TimeUnit.MILLISECONDS.sleep(rnd.nextInt(100) * 10);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		 
			}
		});
	}
	
	/**
	 * This method executes a thread which iterates between each user. This thread checks if a
	 * user has any smartMessages. SmartMessages is messages which is generated through timers
	 * and should be sent to HDL when a time input by user has been reached. 
	 */
	private void startSmartMessageListener() {
		executorService.execute(() -> {
			while (!shutdown) {
				Random rnd = new Random();
				try {
					synchronized(user) {
						Iterator<UserClient> i = user.iterator();
						while (i.hasNext()) {
							UserClient u = i.next();
							if(u.returnSmartMessageNotNull()){
								int n = u.SmartMessageSize();
								for(int ii = 0; ii < n; ii++) {
									String msg = u.getSmartMessageNr(ii);
									CommandMessageController(u, msg);
									u.removeSmartMessageNr(ii);
									--n;
									System.out.println("Sent message from timer to HDL");
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				 try {
						TimeUnit.MILLISECONDS.sleep(rnd.nextInt(1000) * 10);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
		});
	}
	
	/**
	 * This method takes a message made up of a specific format and takes it apart, field
	 * by field. If the command field contains a command where the user has requested information. 
	 * That command is saved + the id of the user that requested. 
	 * When the server has received a message containing all this information from HDL later, it will
	 * be sent to that user. 
	 * @param u the user being iterated through
	 * @param msg the message to be taken apart and processed. 
	 */
	private void CommandMessageController(UserClient u, String msg) {
		//System.out.println(msg);
		int cmd;
		int subnetNr;
		int dNr;
		byte[] data;
		if (msg.charAt(1) == 0) {
			if (msg.charAt(2)== 0) {
				if(msg.charAt(3)== 0) {
					if(msg.charAt(4)== 0) {
						if(msg.charAt(5) == 0) {
							cmd = msg.charAt(6);
						}
						else {
							//System.out.println("1 test");
							cmd = Integer.parseInt(msg.substring(5,6));
						}
					}
					else {
						//System.out.println("2 test");
						cmd = Integer.parseInt(msg.substring(4,6));
					}
				}
				else {
					//System.out.println(" msgCmd 3-6");
					
					cmd = Integer.parseInt(msg.substring(3,6));
					//System.out.println(" msgCmd");
				}
			} 
			else {
				//System.out.println(" msgCmd 2-6");
				cmd = Integer.parseInt(msg.substring(2,6));
			}
		}
		else {
			//System.out.println("Test slutt");
			if(msg.substring(1,6).endsWith("7262")){
				//System.out.println(" msgCmd 1-6 slutt");
				u.addMessage(msg.substring(2,6) + u.returnUserID());
			}
			else if(msg.substring(1, 6).endsWith("7260")){
				u.addMessage(msg.substring(2,6) + u.returnUserID());
			}
			else if(msg.substring(1, 6).endsWith("6470")){
				u.addMessage(msg.substring(2,6) + u.returnUserID());
			}
			cmd = Integer.parseInt(msg.substring(1,6));
		}
		
		//System.out.println(cmd + " Cmd slutt");
			
		System.out.println(cmd + " CMD");
			
		subnetNr = Integer.parseInt(msg.substring(6,7));
		//System.out.println(subnetNr + " subnet");
		
		if(msg.charAt(8)== 0 ) {
			dNr = msg.charAt(9);
		}
		else dNr = Integer.parseInt(msg.substring(7,9));
		//System.out.println(dNr + " dnr");
		
		//Puts additional data from msg into a byte array. 
		if((msg.length() >= 10 )) {
			String[] byteString = msg.substring(10, msg.length()).split(",");
			data = new byte[byteString.length];
	
			for (int i=0, len=data.length; i<len; i++) {
			   data[i] = Byte.parseByte(byteString[i].trim());  
			   System.out.println(data[i] + " data");
			}
			
		} else {
			data = new byte[0];
		}
		
		sendPacketToHDL(cmd, subnetNr, dNr, data);
	}
	
	/**
	 * Handles a specific message. Does nothing currently, as it has not been needed. 
	 * @param msg the message to be processed. 
	 * @param u the user being iterated through. 
	 */
	private void handleMonitoringControllerMessages(String msg, UserClient u) {
		
		
	}
	
	/**
	 * Method that process a message from the user, wher the user requested a day or night timer, which
	 * sets the mode to day or night on the time requested by the user. 
	 * This method starts a AlarmClock in UserClient, which is scheduled on the specific time
	 * and then rescheduled for the next day after it has gone off. 
	 * @param msg the message to be processed
	 * @param u the user being iterated through. 
	 */
	private void handleDayNightTimer(String msg, UserClient u){
		char set = msg.charAt(0);
		String[] textString = msg.substring(1, msg.length()).split(",");
		String[] data = new String[textString.length];
		
		for (int i=0, len=data.length; i<len; i++) {
		   data[i] = textString[i].trim();  
		   System.out.println(data[i] + " data");
		}
		
		switch (set){
			case 'D':	//Only day is changed
				int dayHour = Integer.parseInt(data[1]);
				int dayMin = Integer.parseInt(data[2]);
				String dHours = String.format("%02d", dayHour);
				String dMinutes = String.format("%02d", dayMin);
				System.out.println(dHours);
				System.out.println(dMinutes);
				u.dayModeTimer(dHours, dMinutes);
				break;
			case 'N':	//Only night is changed
				int nightHour = Integer.parseInt(data[1]);
				int nightMin = Integer.parseInt(data[2]);
				String nHours = String.format("%02d", nightHour);
				String nMinutes = String.format("%02d", nightMin);
				u.nightModeTimer(nHours, nMinutes);
				break;
			case 'B':	//Day and night is changed
				int dayHourB = Integer.parseInt(data[1]);
				int dayMinB = Integer.parseInt(data[2]);
				int nightHourB = Integer.parseInt(data[3]);
				int nightMinB = Integer.parseInt(data[4]);
				String bDHours = String.format("%02d", dayHourB);
				String bDMinutes = String.format("%02d", dayMinB);
				String bNHours = String.format("%02d", nightHourB);
				String bNMinutes = String.format("%02d", nightMinB);
				u.dayModeTimer(bDHours, bDMinutes);
				u.nightModeTimer(bNHours, bNMinutes);
				break;
			default:
				break;
		}
		
	}
	
	/**
	 * Method that process a message from a user, where the user requested a 
	 * holiday timer. This method process the message and starts a message 
	 * that runs for the period input by the user. 
	 * @param msg the message to be processed. 
	 * @param u the user being iterated through.
	 */
	private void handleHolidayTimerMessages(String msg, UserClient u){
		
		try {
		String[] textString = msg.substring(0, msg.length()).split(",");
		String[] data = new String[textString.length];

		
		for (int i=0, len=data.length; i<len; i++) {
		   data[i] = textString[i].trim();  
		   System.out.println(data[i] + " data");
		}
		
		//int year = Integer.parseInt(data[0]);
		int month = Integer.parseInt(data[1]);
		int day = Integer.parseInt(data[2]);
		int hours = Integer.parseInt(data[3]);
		int minutes = Integer.parseInt(data[4]);
		int changeHours = Integer.parseInt(data[5]);
		String modeToChange = data[6];
		
		String fMonth = String.format("%02d", month);
		String fDay = String.format("%02d", day);
		String fHours = String.format("%02d", hours);
		String fMinutes = String.format("%02d", minutes);
		
		System.out.println(fMonth);
		System.out.println(fDay);
		System.out.println(fHours);
		System.out.println(fMinutes);
		
		Date d1 = new Date();
		String holidayDate = data[0] + "-" + fMonth + "-" + fDay + " " + fHours + ":" + fMinutes;
		Date d2 = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(holidayDate);
		System.out.println(d2);
		System.out.println(d1);	
		
		long seconds = (d2.getTime()-d1.getTime())/1000;
		System.out.println(seconds);
		if(changeHours != 0){
			changeHours = changeHours * 3600; //hours to seconds
		}
		seconds = seconds - changeHours;
		u.holidayTimer(seconds, modeToChange);
        
		
		}catch(Exception e){
			e.printStackTrace();
			
		}
		
	}
	/**
	 * Used for testing the holidaytimer. 
	 * @param n the user which has the timer. 
	 */
	void sendHolidayMessage(int n) {
		CommandMessageController(user.get(n), "");
	}
	
	/**
	 * This method takes the message to be sent to HDL, sends it to another method that constructs
	 * it into the correct format, and then tries to send the packet to HDL. 
	 * @param cmd the command of the message. This decides what the device should do with the message.
	 * @param subnetNr the subnet of the device being sent to. 
	 * @param deviceNr the device number of the device being sent to. 
	 * @param addData the data to be sent. 
	 */
	private void sendPacketToHDL (int cmd, int subnetNr, int deviceNr, byte[] addData) {
		//displayMessage("\n\nSending data to HDL....");
		
		byte[] HDLData = new byte[31];
		
		//Retrieves the data to be sent + valid CRC
		HDLData = getBytes(addData, cmd, subnetNr, deviceNr);
		
		/*
		int i = 0;
		for (byte b : HDLData) {
		    System.out.println((b & 0xFF) + " " + i++);
		}
		*/
		if (HDLData != null) {	
		
			//Tries to send packet to HDL		
			try {
				DatagramPacket sendPacket;
				sendPacket = new DatagramPacket(HDLData,
				        HDLData.length, InetAddress.getByName(HDLip), hdlPort);
				socket.send(sendPacket);
				//displayMessage("\nPackage sent to HDL....");
			} catch (UnknownHostException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * This method takes the data to be sent to HDL and constructs a valid packet, which HDL can
	 * process. It constructs a packet based on fields in the correct order
	 * which is required by HDL to be able to process the information. It adds the 
	 * required "magic string" "HDLMIRACLE", CRC computes and validates the data to be sent.  
	 * 
	 * @param data the data to be sent to the device
	 * @param cmd the command, which decides what the device should do with the message. 
	 * @param subnet the subet of the device which is being sent to
	 * @param devicenr the device number of the device which should receive the packet. 
	 * @return the packet to be sent. 
	 */
	public byte[] getBytes(byte[] data, int cmd, int subnet, int devicenr) {
		byte[] p = new byte[27 + (data != null ? data.length : 0)];
			  
		try {
			replyAddress = InetAddress.getByName("192.168.10.141");
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return null;
		}
		if(replyAddress != null) {
			System.arraycopy(replyAddress.getAddress(), 0, p, 0, 4);
		}
		
		/*
		String ip1 = "192";
		String ip2 = "168";
		String ip3 = "10";
		String ip4 = "141";
		int ipInt1 = Integer.parseInt(ip1);
		int ipInt2 = Integer.parseInt(ip2);
		int ipInt3 = Integer.parseInt(ip3);
		int ipInt4 = Integer.parseInt(ip4);
		
		
		p[0] = (byte) ipInt1;	//0-3 contains the IP of sender. Default 192.168.10.141
		p[1] = (byte) ipInt2;
		p[2] = (byte) ipInt3;
		p[3] = (byte) ipInt4;
		*/
		
		byte[] magic = "HDLMIRACLE".getBytes();
		System.arraycopy(magic, 0, p, 4, magic.length);
		
		//Setting the array values from leading code (0xAA)
		
		int i = 14;
		p[i++] = (byte) 0xaa;	//Leading code: set at 170
		p[i++] = (byte) 0xaa;	//Leading code
		p[i++] = (byte) (p.length -16);	//Data package length
		p[i++] = (byte) (sourceAddress >> 8);//Original subnet ID
		p[i++] = (byte) sourceAddress;//Original device ID
		p[i++] = (byte) (sourceDevice >> 8);//Original device type: higher than 8
		p[i++] = (byte) sourceDevice;//Original device type: lower than 8
		p[i++] = (byte) (cmd >> 8);	//Operationg code: higher than 8
		p[i++] = (byte) cmd;	//Operating code: lower than 8
		p[i++] = (byte) subnet;	//Subnet ID of target device
		p[i++] = (byte) devicenr;	//Device ID of target device
		
		if (data != null) {
			System.arraycopy(data, 0, p, i, data.length); 
		}												 
		//Computes the correct CRC for the data package
		int crc = computeCRC16(p, 16, p.length - 18);
		i = p.length -2; // -2 for the CRC
		p[i++] = (byte)(crc >> 8);
		p[i++] = (byte)crc;
		
		//returns data to be sent
		return p;
	}
	
	/**
	 * Method that computes the CRC of the message to be sent. 
	 * @param data the data to be computed and validated.
	 * @param offset the offset, which decides the where the CRC should start computing from. 
	 * @param count the length of the data to be computed. This value is -18 because it is the 
	 * (length - offset - CRC). The CRC is two bytes (16 bits).
	 * @return returns the CRC: 
	 */
	protected static int computeCRC16(byte[] data, int offset, int count) {
		int crc = 0;
		int dat;
		
		for (int i = offset; i < offset + count; ++i) {
			dat = (crc >>> 8) & 0xff;
			crc = (crc << 8) & 0xffff;
			crc^= CRCTable[(dat ^(int)data[i]) & 0xff];
		}
		return crc & 0xffff;
	}
/********************************* CRC table *********************************/
	
	/**
	 * The CRC table which contains CRC values, used to validate the messages 
	 * to be sent to HDL. Messages without correct CRC will not be processed correctly
	 * by HDL. 
	 * This table is retrieved from HDL. 
	 */
	 protected static final int[] CRCTable = {
		 0x0000, 0x1021, 0x2042, 0x3063, 0x4084, 0x50a5, 0x60c6, 0x70e7,
		 0x8108, 0x9129, 0xa14a, 0xb16b, 0xc18c, 0xd1ad, 0xe1ce, 0xf1ef,
		 0x1231, 0x0210, 0x3273, 0x2252, 0x52b5, 0x4294, 0x72f7, 0x62d6,
		 0x9339, 0x8318, 0xb37b, 0xa35a, 0xd3bd, 0xc39c, 0xf3ff, 0xe3de,
		 0x2462, 0x3443, 0x0420, 0x1401, 0x64e6, 0x74c7, 0x44a4, 0x5485,
		 0xa56a, 0xb54b, 0x8528, 0x9509, 0xe5ee, 0xf5cf, 0xc5ac, 0xd58d,
		 0x3653, 0x2672, 0x1611, 0x0630, 0x76d7, 0x66f6, 0x5695, 0x46b4,
		 0xb75b, 0xa77a, 0x9719, 0x8738, 0xf7df, 0xe7fe, 0xd79d, 0xc7bc,
		 0x48c4, 0x58e5, 0x6886, 0x78a7, 0x0840, 0x1861, 0x2802, 0x3823,
		 0xc9cc, 0xd9ed, 0xe98e, 0xf9af, 0x8948, 0x9969, 0xa90a, 0xb92b,
		 0x5af5, 0x4ad4, 0x7ab7, 0x6a96, 0x1a71, 0x0a50, 0x3a33, 0x2a12,
		 0xdbfd, 0xcbdc, 0xfbbf, 0xeb9e, 0x9b79, 0x8b58, 0xbb3b, 0xab1a,
		 0x6ca6, 0x7c87, 0x4ce4, 0x5cc5, 0x2c22, 0x3c03, 0x0c60, 0x1c41,
		 0xedae, 0xfd8f, 0xcdec, 0xddcd, 0xad2a, 0xbd0b, 0x8d68, 0x9d49,
		 0x7e97, 0x6eb6, 0x5ed5, 0x4ef4, 0x3e13, 0x2e32, 0x1e51, 0x0e70,
		 0xff9f, 0xefbe, 0xdfdd, 0xcffc, 0xbf1b, 0xaf3a, 0x9f59, 0x8f78,
		 0x9188, 0x81a9, 0xb1ca, 0xa1eb, 0xd10c, 0xc12d, 0xf14e, 0xe16f,
		 0x1080, 0x00a1, 0x30c2, 0x20e3, 0x5004, 0x4025, 0x7046, 0x6067,
		 0x83b9, 0x9398, 0xa3fb, 0xb3da, 0xc33d, 0xd31c, 0xe37f, 0xf35e,
		 0x02b1, 0x1290, 0x22f3, 0x32d2, 0x4235, 0x5214, 0x6277, 0x7256,
		 0xb5ea, 0xa5cb, 0x95a8, 0x8589, 0xf56e, 0xe54f, 0xd52c, 0xc50d,
		 0x34e2, 0x24c3, 0x14a0, 0x0481, 0x7466, 0x6447, 0x5424, 0x4405,
		 0xa7db, 0xb7fa, 0x8799, 0x97b8, 0xe75f, 0xf77e, 0xc71d, 0xd73c,
		 0x26d3, 0x36f2, 0x0691, 0x16b0, 0x6657, 0x7676, 0x4615, 0x5634,
		 0xd94c, 0xc96d, 0xf90e, 0xe92f, 0x99c8, 0x89e9, 0xb98a, 0xa9ab,
		 0x5844, 0x4865, 0x7806, 0x6827, 0x18c0, 0x08e1, 0x3882, 0x28a3,
		 0xcb7d, 0xdb5c, 0xeb3f, 0xfb1e, 0x8bf9, 0x9bd8, 0xabbb, 0xbb9a,
		 0x4a75, 0x5a54, 0x6a37, 0x7a16, 0x0af1, 0x1ad0, 0x2ab3, 0x3a92,
		 0xfd2e, 0xed0f, 0xdd6c, 0xcd4d, 0xbdaa, 0xad8b, 0x9de8, 0x8dc9,
		 0x7c26, 0x6c07, 0x5c64, 0x4c45, 0x3ca2, 0x2c83, 0x1ce0, 0x0cc1,
		 0xef1f, 0xff3e, 0xcf5d, 0xdf7c, 0xaf9b, 0xbfba, 0x8fd9, 0x9ff8,
		 0x6e17, 0x7e36, 0x4e55, 0x5e74, 0x2e93, 0x3eb2, 0x0ed1, 0x1ef0 
	};
	 

/******************************* CRC table end *******************************/
	 	 
} //Servertilkobling slutt

/*
 * Unused code*****************************************************************
 */
	
	// Input parameter 1, Buffer, to CRCCeck is from length of data package
	// (not including 0xAA, 0xAA)
	// e.x data package is (170,170,13,1 250,255,254,0,2,1,2,1,1,0,0)
	// So Buffer is (13,1...)
	
	// Input parameter 2, len, deduct 2 byte from this data package
	// because CRC takes 2 byte.
	// e.g if package length is 11, then len is 13-2 = 11.
/*
	private byte CRCPack(byte[] buffer,int len) {
		
		int crc = 0;
		int i = 16;
		byte dat = 0;
		byte ptrCount = buffer[16];
		
		while (len-- != 0) {
			dat=(byte) (crc>>8);
			crc<<=8;
			crc^=CRCTable[dat^ptrCount];
			ptrCount=buffer[i++];			
		}
		
		ptrCount = (byte) (crc>>8);
		ptrCount = buffer[i++];
		ptrCount = (byte) crc;
		
		return ptrCount;	
		
	}
	
	// Input parameter 1, buffer, to CRCCeck is from length of data package
	// (not including 0xAA, 0xAA)
	// e.x data package is (170,170,11,1 250,255,254,0,2,1,2,1,1,0,0)
	// So buffer is (11...)
	
	// Input parameter 2, len, deduct 2 byte from this data package
	// because CRC takes 2 byte.
	// e.g if package length is 11, then len is 11-2 = 9.
	
	 boolean  CRCCheck(byte[] buffer, byte len) {		// CRC Checksum

		int crc = 0;
		byte dat;
		byte ptrCount = 0;
		boolean isRight;
		
		try {
			while (len--!= 0) {							// Goes through the  
				dat = (byte) (crc>>8);					// package byte wise.
				crc = crc<<8;	
				
				crc^= CRCTable[(dat^buffer[ptrCount])]; //XOR's crc value with
				ptrCount++;								// stuff in CRCTable
			}
		} catch (Exception e) {
			System.out.println(e.getMessage() + "(CheckCRC");
		}
		
		dat=(byte) crc;
		
	// True if...no fucking clue whats going on here
	// False if... whatever, I don't care.
		return isRight = (ptrCount==(crc>>8)&&(ptrCount+1==dat)) ? true:false;
	}
	
	*************HDL RELATED**************
	
			 hdlSocket = new DatagramSocket(null);
		     InetSocketAddress address = new InetSocketAddress("192.168.10.255", 6000);
		     hdlSocket.bind(address);
			//serverSocket.bind(new InetSocketAddress(serverPort));
				String ip1 = "192";
		String ip2 = "168";
		String ip3 = "10";
		String ip4 = "141";
		int ipInt1 = Integer.parseInt(ip1);
		int ipInt2 = Integer.parseInt(ip2);
		int ipInt3 = Integer.parseInt(ip3);
		int ipInt4 = Integer.parseInt(ip4);
		
		String mir = "HDLMIRACLE";
		int[] mirInt = new int[mir.length()];
		
		for (int k = 0; k < mir.length(); k++) {
			try {
				mirInt[k] = mir.charAt(k);
			} catch(NumberFormatException numberE){};
		}
	
		
		HDLData[0] = (byte) ipInt1;	//0-3 contains the IP of sender. Default 192.168.10.141
		HDLData[1] = (byte) ipInt2;
		HDLData[2] = (byte) ipInt3;
		HDLData[3] = (byte) ipInt4;
		
		HDLData[4] = (byte) mirInt[0]; //4-13 contains the required string "HDLMIRACLE"
		HDLData[5] = (byte) mirInt[1];
		HDLData[6] = (byte) mirInt[2];
		HDLData[7] = (byte) mirInt[3];
		HDLData[8] = (byte) mirInt[4];
		HDLData[9] = (byte) mirInt[5];
		HDLData[10] = (byte) mirInt[6];
		HDLData[11] = (byte) mirInt[7];
		HDLData[12] = (byte) mirInt[8];
		HDLData[13] = (byte) mirInt[9];
		
		
		HDLData[14] = (byte) 170;	//Leading code
		HDLData[15] = (byte) 170;	//Leading code
		HDLData[16] = (byte) 15;	//Data package length	
		HDLData[17] = (byte) 12;	//Original subnet ID
		HDLData[18] = (byte) 254;	//Original device ID 
		HDLData[19] = (byte) 255;	//Original device type - higher than 8
		HDLData[20] = (byte) 254;	//Original device type - lower than 8
		HDLData[21] = (byte) 0;		//Operation code - higher than 8
		HDLData[22] = (byte) 49;	//Operation code - lower than 8
		HDLData[23] = (byte) 1;		//Target subnet
		HDLData[24] = (byte) 17;	//Target devicenr	
		HDLData[25] = (byte) 1;		//Channel nr
		//Lys p�
		HDLData[26] = (byte) 100;	//Intensity
		//Lys av
		//HDLData[26] = (byte) 0;
		
		HDLData[27] = (byte) 0;		//Running time - higher than 8
		HDLData[28] = (byte) 1;		//Running time - lower than 8
		
		//Lys p�
		//HDLData[29] = (byte) 151;	//CRC - highter than 8
		//HDLData[30] = (byte) 15;	//CRC - lower than 8
		
		//Lys av
		HDLData[29] = (byte) 0;
		HDLData[30] = (byte) 0; 
		
		
		 * 
	private void handleMessages(ClientMessage m) throws InterruptedException {
		if (m != null) {
			displayMessage("New message: " + m.name + "\n");
			displayMessage("New message: " + m.address + "\n");
			displayMessage("New message: " + m.subnetNr + "\n");
			displayMessage("New message: " + m.deviceNr + "\n");
		}
	}
	
	
	
*/




 
