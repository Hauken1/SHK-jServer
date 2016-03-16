import java.awt.BorderLayout;
import java.awt.Font;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
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

public class ServerTilkobling extends JFrame {

	//private ServerSocket server;
	private DatagramSocket socket;
	ExecutorService executorService;
	
	private JTextArea outputArea;
	
	private boolean shutdown = false;
	
	private ArrayList<UserClient> user = new ArrayList<UserClient>();
	private ArrayList<ClientMessage> m = new ArrayList<ClientMessage>();
	
	private ArrayBlockingQueue<String> messages = new ArrayBlockingQueue<String>(50);
		
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
	
}
