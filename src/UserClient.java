import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.DatagramSocket;
import java.net.Socket;
import java.util.ArrayList;

import javafx.scene.chart.PieChart.Data;

public class UserClient {
		
		private Socket connection;
		
		private BufferedReader input;
		private BufferedWriter output;
		private ArrayList<ClientMessage> message = new ArrayList<ClientMessage>();
		
		
		public UserClient(Socket connection) throws IOException {
			this.connection = connection;
			

			input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
			
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

