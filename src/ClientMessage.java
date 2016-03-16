import java.io.Serializable;

import java.io.Serializable;

/**
 * Created by Hauken on 15.03.2016.
 */
public class ClientMessage implements Serializable {

    /**
	 * 
	 */
	public String name;
    public String address;
    public int subnetNr;
    public int deviceNr;

    public ClientMessage(String mName, String mAddress, int mSubnetNr, int mDeviceNr) {
        name = mName;
        address = mAddress;
        subnetNr = mSubnetNr;
        deviceNr = mDeviceNr;

    }

	public ClientMessage() {

	}

}