import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
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
	ExecutorService executorService;
	
	private JTextArea outputArea;
	
	private boolean shutdown = false;
	
	private ArrayList<UserClient> user = new ArrayList<UserClient>();
	private ArrayList<ClientMessage> m = new ArrayList<ClientMessage>();
	
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
		
	private String IP = "192.168.10.141";
	
	public ServerTilkobling() {
		
		outputArea = new JTextArea();
		outputArea.setFont(new Font("Ariel", Font.PLAIN, 14));
		outputArea.setEditable(false);
		add(new JScrollPane(outputArea), BorderLayout.CENTER);
		outputArea.setText("Server awaiting connections\n");
		
		try {
			//server = new ServerSocket(1234); // Set up serverSocket
			socket = new DatagramSocket(1234);
			executorService = Executors.newCachedThreadPool();
			
			sendPacketToHDL();
			startLoginMonitor();
			//startMessageListener();
		
			
			executorService.shutdown();
			
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
							
								
							} catch (Exception e) {
								System.out.println("Feil med object");
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
	
	private void handleMessages(ClientMessage m) throws InterruptedException {
		if (m != null) {
			displayMessage("New message: " + m.name + "\n");
			displayMessage("New message: " + m.address + "\n");
			displayMessage("New message: " + m.subnetNr + "\n");
			displayMessage("New message: " + m.deviceNr + "\n");
		}
	}
	
	private void displayMessage(String text) {
		SwingUtilities.invokeLater(() -> outputArea.append(text));
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
	
	private void sendPacketToHDL () {
		displayMessage("\n\nEcho data to HDL....");
		//InetAddress ip = InetAddress.getByName("192.168.10.141");
		//byte[] bytes = ip.getAddress();
		byte[] HDLData = new byte[30];
		  ByteBuffer buf = ByteBuffer.allocate(100);
		  
		String ip1 = "192";
		String ip2 = "168";
		String ip3 = "10";
		String ip4 = "141";
		int ipInt1 = Integer.parseInt(ip1);
		int ipInt2 = Integer.parseInt(ip2);
		int ipInt3 = Integer.parseInt(ip3);
		int ipInt4 = Integer.parseInt(ip4);
		
		String h = "H";
		String d = "D";
		String l1 = "L";
		String m = "M";
		String i = "I";
		String r = "R";
		String a = "A";
		String c = "C";
		String l2 = "L";
		String e = "E";

		/*
		int text1 = Integer.parseInt(h);
		int text2 = Integer.parseInt(d);
		int text3 = Integer.parseInt(l1);
		int text4 = Integer.parseInt(m);
		int text5 = Integer.parseInt(i);
		int text6 = Integer.parseInt(r);
		int text7 = Integer.parseInt(a);
		int text8 = Integer.parseInt(c);
		int text9 = Integer.parseInt(l2);
		int text10 = Integer.parseInt(e);
		*/
		String s = "HDLMIRACLE";
		//HDLData[5] = s.(StandardCharsets.US_ASCII);i
		
		//char[] mir = {'H', 'D', 'L','M','I','R','A','C','L','E'};
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
		HDLData[26] = (byte) 100;
		HDLData[27] = (byte) 0;
		HDLData[28] = (byte) 1;		
		HDLData[29] = (byte) 208;
		HDLData[30] = (byte) 164;

		/*
		int n = (int) Integer.parseInt(s);
		for (int j = 0; 0 <= s.length(); j++  ){
			HDLData[4+j] = (byte) s.indexOf(j); 
		}
		*/
		for (byte b : HDLData) {
		    System.out.println(b & 0xFF);
		}
		
				
		/*
		
		    // Get the buffer's capacity
		  int capacity = buf.capacity(); // 10

		    // Use the absolute put().
		    // This method does not affect the position.
		   buf.put((byte)192); // position=0
		   int rem = buf.remaining();
		   System.out.println(rem);
		//ByteBuffer buf = ByteBuffer.wrap(HDLData);
		   buf.put((byte)168);
		   rem = buf.remaining();
		   System.out.println(rem);
		   buf.put((byte)10);
		   buf.put((byte)141);
		   rem = buf.remaining();
		   System.out.println(rem);
		   String test = new String(buf.array(), Charset.forName("UTF-8"));
		   System.out.println(test);
		   
		  */
		  
		   
		   
		   /*
		String addressPart1 = "192";
		String addressPart2 = "168";
		InetAddress addressPart3 = InetAddress.getByName("10");
		InetAddress addressPart4 = InetAddress.getByName("141");
		
		HDLData = ip.getAddress();
		HDLData = 
		HDLData  
		*/ 
		
		
		
		
		//DatagramPacket HDLPacket = new DatagramPacket(buf, length, address, port);
		
	}
	
}
