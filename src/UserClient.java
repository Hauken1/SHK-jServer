import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class UserClient {
		
		private Socket connection;
		
		private BufferedReader input;
		private BufferedWriter output;
		private ObjectInputStream objectInput;
		
        private Message m;
		
		public UserClient(Socket connection) throws IOException {
			this.connection = connection;
			
			input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			output = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));	
			objectInput = new ObjectInputStream(connection.getInputStream());
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
		
		public void read() throws IOException, ClassNotFoundException {
			//if (input.ready())
				//return input.readLine();
			if (objectInput.readObject() != null){
			m = (Message) objectInput.readObject();
			}
		}
		
		//public String returnName() {
			
		//}
		public Message returnMessageInformation() {
			return m;
		}
		/*
		public void getMessage(int i) {
			switch(i) {
			case 0:
				return;
				break;
			case 1:
				return ad
			case 2:
				return
			case 3:
				return m.deviceNr;
			}
			
		}
		*/
		  
		  public class Message implements java.io.Serializable
	        {
	            public String name;
	            public String address;
	            public int subnetNr;
	            public int deviceNr;
	            
	            
	            public String name() {
	  			  return name;
	  		  	}
	            public String address() {
		  			  return address;
	            }
	            public int subnetNr() {
		  			  return subnetNr;
		  		}
	            public int deviceNr() {
		  			  return deviceNr;
		  		}  
	            
	        }
		  
		
}

