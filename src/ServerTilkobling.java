import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

//import com.sun.xml.internal.ws.encoding.MtomCodec.ByteArrayBuffer;

public class ServerTilkobling extends JFrame {

	//private ServerSocket server;
	private DatagramSocket socket;
	private static ServerSocket serverSocket; 
	ExecutorService executorService;
	
	private JTextArea outputArea;
	
	private boolean shutdown = false;
	
	private ArrayList<UserClient> user = new ArrayList<UserClient>();
	private ArrayList<ClientMessage> m = new ArrayList<ClientMessage>();
	
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
		
	private String HDLip = "192.168.10.255";
	private int serverPort = 12345;
	private int datagramPort = 1234;
	private int hdlPort = 6000;
	
	public ServerTilkobling() {
		
		outputArea = new JTextArea();
		outputArea.setFont(new Font("Ariel", Font.PLAIN, 14));
		outputArea.setEditable(false);
		add(new JScrollPane(outputArea), BorderLayout.CENTER);
		outputArea.setText("Server awaiting connections\n");
		
		try {
			socket = new DatagramSocket(datagramPort);
			serverSocket = new ServerSocket(serverPort);
			serverSocket.setReuseAddress(true);
			//serverSocket.bind(new InetSocketAddress(serverPort));
			
			executorService = Executors.newCachedThreadPool();
			
			//sendPacketToHDL();
			startLoginMonitor();
			startMessageListener();
		
			
			//executorService.shutdown();
			
		} catch (IOException ioe) {
			ioe.printStackTrace();
			System.exit(1);
		}
		
		setSize(600, 400);
		setVisible(true);	
	}
	
	private void startLoginMonitor() {
		executorService.execute(() -> {
			while (!shutdown) {
				Random rnd = new Random();
				try {
					
					Socket s = serverSocket.accept(); 
					UserClient client = new UserClient(s);
					user.add(client);
					
					/*
					byte[] data = new byte[100];
		            DatagramPacket receivePacket = new DatagramPacket(data,
		                    data.length);

		            socket.receive(receivePacket);

		            displayMessage("User CONNECTED!" + "\n");
		            displayMessage("\nPacket received:"
		                    + "\nFrom host: "
		                    + receivePacket.getAddress()
		                    + "\nHost port: "
		                    + receivePacket.getPort()
		                    + "\nLength: "
		                    + receivePacket.getLength()
		                    + "\nContaining: "
		                    + new String(receivePacket.getData(), 0,
		                            receivePacket.getLength()));
		            
		            sendPacketToClient(receivePacket);
		            */
					
				} catch (IOException ioe) {
					displayMessage("CONNECTION ERROR: " + ioe + "\n");
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
	
	private void startMessageListener() {
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
									sendPacketToHDL();
								}

							} catch (Exception e) {
								System.out.println("Feil med melding");
								e.printStackTrace();
							}
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
	
	
	private void displayMessage(String text) {
		SwingUtilities.invokeLater(() -> outputArea.append(text));
	}
	
	/*
	 * 
	private void handleMessages(ClientMessage m) throws InterruptedException {
		if (m != null) {
			displayMessage("New message: " + m.name + "\n");
			displayMessage("New message: " + m.address + "\n");
			displayMessage("New message: " + m.subnetNr + "\n");
			displayMessage("New message: " + m.deviceNr + "\n");
		}
	}
	
	private void sendPacketToClient(DatagramPacket receivePacket)
	            throws IOException {
	        displayMessage("\n\nEcho data to client....");

	        DatagramPacket sendPacket = new DatagramPacket(receivePacket.getData(),
	                receivePacket.getLength(), receivePacket.getAddress(),
	                receivePacket.getPort());

	        socket.send(sendPacket);
	        displayMessage("Packet sent\n");
    }
	*/
	
	private void sendPacketToHDL () {
		displayMessage("\n\nEcho data to HDL....");
		
		byte[] HDLData = new byte[31];
		  
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
	
		HDLData[0] = (byte) ipInt1;
		HDLData[1] = (byte) ipInt2;
		HDLData[2] = (byte) ipInt3;
		HDLData[3] = (byte) ipInt4;
		
		HDLData[4] = (byte) mirInt[0];
		HDLData[5] = (byte) mirInt[1];
		HDLData[6] = (byte) mirInt[2];
		HDLData[7] = (byte) mirInt[3];
		HDLData[8] = (byte) mirInt[4];
		HDLData[9] = (byte) mirInt[5];
		HDLData[10] = (byte) mirInt[6];
		HDLData[11] = (byte) mirInt[7];
		HDLData[12] = (byte) mirInt[8];
		HDLData[13] = (byte) mirInt[9];
		HDLData[14] = (byte) 170;
		HDLData[15] = (byte) 170;
		HDLData[16] = (byte) 15;		
		HDLData[17] = (byte) 12;	
		HDLData[18] = (byte) 254;
		HDLData[19] = (byte) 255;
		HDLData[20] = (byte) 254;	
		HDLData[21] = (byte) 0;
		HDLData[22] = (byte) 49;
		HDLData[23] = (byte) 1;
		HDLData[24] = (byte) 17;		
		HDLData[25] = (byte) 1;	
		//Lys på
		//HDLData[26] = (byte) 100;
		//Lys av
		HDLData[26] = (byte) 0;
		
		HDLData[27] = (byte) 0;
		HDLData[28] = (byte) 1;
		
		//Lys på
		//HDLData[29] = (byte) 151;
		//HDLData[30] = (byte) 15;
		
		//Lys av
		HDLData[29] = (byte) 208;
		HDLData[30] = (byte) 164;
		
		/*
		for (byte b : HDLData) {
		    System.out.println(b & 0xFF);
		}
		*/
				
		try {
			DatagramPacket sendPacket;
			sendPacket = new DatagramPacket(HDLData,
			        HDLData.length, InetAddress.getByName(HDLip), hdlPort);
			socket.send(sendPacket);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}
