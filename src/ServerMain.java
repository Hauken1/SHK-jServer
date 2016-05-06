import javax.swing.JFrame;

/**
 * Main class of the server.
 * Starts the server when the program is executed.
 */
public class ServerMain {
	
	/**
	 * Main method of the server program. 
	 * Starts a serverConnection class, which adds the server functionality. 
	 * @param args if executed from command line or from another program. 
	 */
	public static void main(String[] args) {
		 ServerConnection application = new ServerConnection();
	}
}
