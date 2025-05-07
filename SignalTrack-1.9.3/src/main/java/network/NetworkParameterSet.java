package network;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.Objects;

public class NetworkParameterSet {
	private int portNumber;
	private InetAddress inetAddress;
	private boolean selected; 
	private String description = "";
	
	public NetworkParameterSet(String hostName, int portNumber, String description) throws UnknownHostException {
		this.inetAddress = InetAddress.getByName(hostName);
		this.portNumber = portNumber;
		this.description = description;
	}

	public NetworkParameterSet(String hostName, int portNumber) throws UnknownHostException {
		this.inetAddress = InetAddress.getByName(hostName);
		this.portNumber = portNumber;
	}
	
	public NetworkParameterSet(InetAddress inetAddress, int portNumber) {
		this.inetAddress = inetAddress;
		this.portNumber = portNumber;
	}
	
	public NetworkParameterSet(URI uri) throws UnknownHostException {
		this.inetAddress = InetAddress.getByName(uri.getHost());
		this.portNumber = uri.getPort();
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public URI getUri() throws URISyntaxException {
		return new URI(null, null, inetAddress.getHostName(), portNumber, null, null, null);
	}

	public void setUri(URI uri) throws UnknownHostException {
		inetAddress = InetAddress.getByName(uri.getHost());
		portNumber = uri.getPort();
	}

	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress inetAddress) {
		this.inetAddress = inetAddress;
	}

	public String getHostName() {
		return inetAddress.getHostName();
	}
	
	public void setHostName(String hostName) throws UnknownHostException {
		inetAddress = InetAddress.getByName(hostName);
	}
	
	public int getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(int portNumber) {
		this.portNumber = portNumber;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		return "NetworkParameterSet [portNumber=" + portNumber + ", inetAddress=" + inetAddress + ", description="
				+ description + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(description, inetAddress, portNumber);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof NetworkParameterSet other)) {
			return false;
		}
		return Objects.equals(description, other.description) && Objects.equals(inetAddress, other.inetAddress)
				&& portNumber == other.portNumber;
	}
	
}
