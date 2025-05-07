package network;

import java.beans.PropertyChangeSupport;
import java.net.InetAddress;

public interface NetworkInterface {

    InetAddress getInetAddress();
    
    int getPortNumber();
    
    boolean isConnected();
 
    void write(Object obj);
    
    void setDebug(boolean debug);

    PropertyChangeSupport getPropertyChangeSupport();

    void connect(InetAddress inetAddress, int portNumber);

    void disconnect();

	void writeUTF(String str);
}
