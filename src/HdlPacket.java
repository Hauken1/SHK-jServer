import java.net.*;

/**
 * This class processes the packets recieved from HDL.
 * When the server receives messages from HDL, the packet will be processed here
 * and then sent back to ServerConnection for further processing. 
 * The class checks if the messages received from HDL contains the required fields. 
 * If not it is nullified. 
 */
public class HdlPacket {	
	int sourceAddress;
	int sourceDevice = 0xfeff;
	int command;
	int subNet; 
	int targetAddress;
	InetAddress replyAddress;	
	byte[] data;


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
	/**
	 * Constructor of the HDL Packet class. 
	 */
	public HdlPacket() {
		try {
			replyAddress = InetAddress.getByAddress(new byte[] {0,0,0,0});
		} catch(UnknownHostException e) {}
	}
	
	/**
	 * 
	 * @param a
	 * @return
	 */
	protected static int ubyte(byte a) {
		return ((int) a) & 0xff;  
	}
	
	/**
	 * 
	 * @param h
	 * @param l
	 * @return
	 */
	protected static int ushort(byte h, byte l) {
		return (((short)h << 8) & 0xff00 ) | ((short)1 & 0xff);
	}
	
	// Input parameter 1 to computeCRC16 is from length of data package
	// (not including 0xAA, 0xAA)
	// e.x data package is (170,170,13,1 250,255,254,0,2,1,2,1,1,0,0)
	// So data is (13,1...)
	
	// Input parameter 2, offset, deduct 2 byte from this data package
	// because CRC takes 2 byte.
	// e.g if package length is 11, then len is 13-2 = 11.
	
	/**
	 * Method that computes and validates the CRC of the packet. 
	 * @param data the data to processed
	 * @param offset the offset for where the computation should start
	 * @param count the length of the data to be processed (data length - offset - CRC)
	 * @return returns the CRC. 
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
	
	/**
	 * This method process packets received from HDL
	 * If the packet does not contain the correct format
	 * they are nullified. 
	 * @param data the data to be processed.
	 * @param length the length of the packet
	 * @return returns the processed packet for further use. 
	 */
	protected static HdlPacket parse(byte[] data, int length) {
		// 4 bytes for IP address + 10 bytes for "HDLMIRACLE" + 13 bytes min
		// packet length == 27. 
		
		if(length < 27 || !(new String(data, 4, 10). equals("HDLMIRACLE"))) {
			System.out.println("TEST1");
			return null;
		}
		
		if(length != 4 + 10 + 2 + data[16]) {
			System.out.println("TEST2");
			return null;
		}
		
		if(computeCRC16(data,16, data[16] - 2) != ushort(data[length - 2], data[length - 1])) {
			//Do nothing
			//return null;
		}
		
		int offset = 17;
		
		HdlPacket packet = new HdlPacket();
		packet.sourceAddress = data[17];
		packet.sourceDevice = data[28];
		packet.command = data[22];
		packet.subNet = data[23];
		packet.targetAddress = data[24];
		
		System.out.println("CMD:" + packet.command);
		/*
		System.out.println("CMD:");
		System.out.println(packet.command >> 8);	//Operationg code: higher than 8
		System.out.println(packet.command);
		System.out.println(data[14] + " 14"); //leading
		System.out.println(data[15] + " 15"); //leading
		System.out.println(data[16] + " 16"); //length
		*/
		//System.out.println(new String(data[14]) + " UTF-8"));
		/*
		System.out.println(data[18] + " 16");
		System.out.println(data[19] + " 16");
		System.out.println(data[16] + " 16");
		System.out.println(data[16] + " 16");
		System.out.println(data[16] + " 16");
		System.out.println(data[16] + " 16");
		System.out.println(data[16] + " 16");
		System.out.println(data[16] + " 16");
		*/
		
		//The command is in byte number 21 and 22
		//the packet data starts at byte number 23
		//The current temperatur is in byte number 26
		//For heating, byte 29-32 contains the current temp
		//that the floor heating is set too. This should be 
		//sent to the applikation, based on the subnet of the
		//panel which the command is sent to. 
		
		//System.out.println(packet.command  + " Cmd");
		
		/*
		int ii = 0; 
		for (byte b : data ) {
		    System.out.println((b & 0xFF) + " HDL " + ii++);
		}
		*/
		
		
		if((data.length > 27 )) {
			//byte[] hdldat = new byte[length - 25/*data.length - 480*/ ];
			packet.data = new byte[length - 25];
			for (int i=0, len=length - 25/*data.length - 480*/; i<len; i++) {
			   //hdldat[i] = data[23+i];
			   packet.data[i] = data[23+i]; 
			   System.out.println((packet.data[i] & 0xFF) + " HDL " + i);
				   //System.out.println(hdldat[i] + " data");
			   
			}
			
		} else {
			packet.data = new byte[0];
		}
		
		
		
		
		/*
		packet.sourceAddress = ushort(data[offset], data[offset+1]);
		offset+= 2;
		packet.sourceDevice = ushort(data[offset], data[offset+1]);
		offset+= 2;
		packet.command = ushort(data[offset], data[offset+1]);
		offset+= 2;
		packet.targetAddress = ushort(data[offset], data[offset+1]);
		offset+= 2;
		*/
		/*
		packet.data = new byte[length-27];
		System.arraycopy(data, 25, packet.data, 0, length-27); 	// parses arrays
		*/
		
		return packet;
	}
	/**
	 * 
	 */
	public String toString() {
		return "[" + Integer.toHexString(sourceAddress)
			+ " -> " + Integer.toHexString(targetAddress) 
			+ " : " + Integer.toHexString(command) + "]";
	}
	/**
	 * Returns the source Address of the packet
	 * @return the source Address
	 */
	public int	getSourceAddress() {return sourceAddress; }

	/**
	 * Returns the source Device of the packet. 
	 * @return the source Device
	 */
	public int	getSourceDevice()  {return sourceDevice; }
	/**
	 * Returns the target address of the packet. 
	 * @return the target address
	 */
	public int	getTargetAddress() {return targetAddress; }
	/**
	 * Returns the command of the packet
	 * @return the command
	 */
	public int	getCommand()	   {return command; }
	/**
	 * Returns the data of the packet
	 * @return the data
	 */
	public byte[] getData()		   {return data; }

	/**
	 * Sets the target address to be @param a
	 * @param a new target address
	 */
	public void setTagetAddress(int a) {targetAddress = a & 0xffff; }
	/**
	 * Sets the source address to be @param a
	 * @param a new source address
	 */
	public void setSourceAddress(int a){sourceAddress = a & 0xffff; }
	/**
	 * Sets the source device to be @param a
	 * @param a new source device
	 */
	public void setSourceDevice(int a) {sourceDevice = a & 0xffff; }
	/**
	 * Sets the command to be @param a
	 * @param a new command
	 */
	public void setCommand(int a) 	   {command = a & 0xffff; }
	/**
	 * Sets the data to be @param d
	 * @param d the new data
	 */
	public void setData(byte[] d)      {data = d; }
	/**
	 * Sets the reply address to be @param addr
	 * @param addr the new reply address
	 */
	public void setReplyAddress(InetAddress addr)  {replyAddress = addr; }
	
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
	public byte[] getBytes() {
		byte[] p = new byte[27 + (data != null ? data.length : 0)];
		
		if(replyAddress != null) {
			System.arraycopy(replyAddress.getAddress(), 0, p, 0, 4);
		}
		
		byte[] magic = "HDLMIRACLE".getBytes();
		System.arraycopy(magic, 0, p, 4, magic.length);
		
		//Setting the array values from leading code (0xAA)
		
		int i = 14;
		p[i++] = (byte) 0xaa;
		p[i++] = (byte) 0xaa;
		p[i++] = (byte)(p.length -16);
		p[i++] = (byte)(sourceAddress >> 8);
		p[i++] = (byte)sourceAddress;
		p[i++] = (byte)(sourceDevice >> 8);
		p[i++] = (byte)sourceDevice;
		p[i++] = (byte)(command >> 8);
		p[i++] = (byte)command;
		p[i++] = (byte)(targetAddress >> 8);
		p[i++] = (byte)targetAddress;
		
		if (data != null) {
			System.arraycopy(data, 0, p, i, data.length); 
		}												 
		
		int crc = computeCRC16(p, 16, p.length - 18);
		i = p.length -2; // -2 for the CRC
		p[i++] = (byte)(crc >> 8);
		p[i++] = (byte)crc;
		
		return p;
	}
}