package network;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import utility.MessageDialog;

public class NetworkInterfaceMonitor extends MessageDialog {

    private static final long serialVersionUID = 1L;

    public static final String SERVER_AVAILABLE = "SERVER_AVAILABLE";
    public static final String SERVER_NOT_AVAILABLE = "SERVER_NOT_AVAILABLE";
    public static final String NETWORK_INTERFACE_AVAILABLE = "NETWORK_INTERFACE_AVAILABLE";
    public static final String NETWORK_INTERFACE_NOT_AVAILABLE = "NETWORK_INTERFACE_NOT_AVAILABLE";

    private transient ScheduledFuture<?> ifHandle = null;
    private transient ScheduledFuture<?> serverHandle = null;

    private boolean interfaceOnline = false;

    private final transient ScheduledExecutorService netIfScheduler = Executors.newScheduledThreadPool(1);
    private final transient ScheduledExecutorService serverScheduler = Executors.newScheduledThreadPool(1);

    private final URL url;
    private String ifaceStr = "";

    public NetworkInterfaceMonitor(URL url) {
        super("Network Interface Status");
        this.url = url;
        configure();
    }

    private void configure() {
        setMessageText("Searching for Network Interfaces");
        adviseOnNetworkInterfaceAvailability();
    }

    public void cancel() {
        if (ifHandle != null) {
            ifHandle.cancel(true);
        }
        if (serverHandle != null) {
            serverHandle.cancel(true);
        }
        close();
    }

    public void adviseOnNetworkInterfaceAvailability() {
        ifHandle = netIfScheduler.scheduleAtFixedRate(new CheckIfAvailablity(), 0, 1000, TimeUnit.MILLISECONDS);
    }

    private class CheckIfAvailablity implements Runnable {

        @Override
        public void run() {
        	final Enumeration<NetworkInterface> interfaces;
            NetworkInterface interf;
            try {
                interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces.hasMoreElements()) {
                    setMessageText("Scanning Network Interfaces");
                }
                while (interfaces.hasMoreElements()) {
                    interf = interfaces.nextElement();
                    if (interf.isUp() && !interf.isLoopback()) {
                        final List<InterfaceAddress> adrs = interf.getInterfaceAddresses();
                        for (final InterfaceAddress adr : adrs) {
                            final InetAddress inadr = adr.getAddress();
                            if (inadr instanceof Inet4Address) {
                                interfaceOnline = true;
                                ifaceStr = interf.getDisplayName() + System.lineSeparator() + "Inet4 Address: " + adr.getAddress();
                                setMessageText("IPV4 Network Interface Is Online" + System.lineSeparator() + ifaceStr);
                                ifHandle.cancel(true);
                                adviseOnServerAvailability(url);
                            }
                        }
                    }
                }
                if (!interfaceOnline) {
                    setMessageText("Network Interface Is Down");
                }
            } catch (final SocketException e) {
                setMessageText("Network Interface Failure");
            }
        }
    }

    public void adviseOnServerAvailability(URL url) {
        setMessageText("Checking for Server At: " + url.getHost());
        serverHandle = serverScheduler.scheduleAtFixedRate(new CheckServerAvailability(url), 100, 1000, TimeUnit.MILLISECONDS);
    }

    private class CheckServerAvailability implements Runnable {

        private final URL url;

        private CheckServerAvailability(URL url) {
            this.url = url;
        }

        @Override
        public void run() {
            try {
            	final HttpURLConnection huc = (HttpURLConnection) url.openConnection();
                huc.setRequestMethod("HEAD");

                final int responseCode = huc.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    serverHandle.cancel(true);
                    setMessageText("Server at: " + url.getHost() + " is responding");
                    firePropertyChange(SERVER_AVAILABLE, null, url.getHost());
                    serverHandle.cancel(true);
                    close();
                }
            } catch (final IOException e) {
                if (!serverHandle.isCancelled()) {
                    setMessageText("Server at " + url.getHost() + " is not available");
                }
            }
        }
    }

}
