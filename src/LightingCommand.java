
public class LightingCommand {

	byte channelNr;
	byte areaNr; 
	byte intensity;
	
	public LightingCommand (int chNr, int intensity) {
		channelNr = (byte) chNr; 
		this.intensity = (byte)intensity; 
		
	}
}
