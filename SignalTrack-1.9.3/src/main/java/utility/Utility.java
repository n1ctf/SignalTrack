package utility;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.Window;

import java.awt.geom.Point2D;

import java.awt.image.BufferedImage;

import java.lang.reflect.InvocationTargetException;

import java.util.Locale;

import java.util.logging.Level;
import java.util.logging.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractButton;

import jssc.SerialPortList;

import io.github.coordinates2country.Coordinates2Country;

public final class Utility {

    private static final Logger LOG = Logger.getLogger(Utility.class.getName());

    private Utility() {
        throw new IllegalStateException("Utility class");
    }
    
    public static boolean isZero(double value){
		return isZero(value, 0.000000001);
	}
	
	public static boolean isZero(double value, double threshold){
	    return value >= -threshold && value <= threshold;
	}
	
	public static boolean equals(double v1, double v2) {
		return equals(v1, v2, 0.000000001);
	}
	
	public static boolean equals(double v1, double v2, double threshold) {
		return Math.abs(v1 - v2) <= threshold;
	}
    
    public static Dimension getScreenSize(Window wnd) {
    	final Dimension ss;

        if (wnd == null) {
            ss = Toolkit.getDefaultToolkit().getScreenSize();
        } else {
            ss = wnd.getToolkit().getScreenSize();
        }
        return ss;
    }

    public static int getScreenResolution(Window wnd) {
    	final int sr;

        if (wnd == null) {
            sr = Toolkit.getDefaultToolkit().getScreenResolution();
        } else {
            sr = wnd.getToolkit().getScreenResolution();
        }
        return sr;
    }

    public static Insets getScreenInsets(Window wnd) {
    	final Insets si;

        if (wnd == null) {
            si = Toolkit.getDefaultToolkit().getScreenInsets(new Frame().getGraphicsConfiguration());
        } else {
            si = wnd.getToolkit().getScreenInsets(wnd.getGraphicsConfiguration());
        }
        return si;
    }

    public static BufferedImage imageToBufferedImage(Image image) {
        final GraphicsEnvironment ge = GraphicsEnvironment
                .getLocalGraphicsEnvironment();
        final GraphicsDevice gs = ge.getDefaultScreenDevice();
        final GraphicsConfiguration gc = gs.getDefaultConfiguration();
        final BufferedImage bimage = gc.createCompatibleImage(image.getWidth(null),
                image.getHeight(null), Transparency.BITMASK);
        final Graphics gb = bimage.createGraphics();
        gb.drawImage(image, 0, 0, null);
        gb.dispose();
        return bimage;
    }

    public static double getGreatCircleDistance(double dLatA, double dLonA, double dLatB, double dLonB) {
        final double EARTH_RADIUS = 6378.136; // kilometers 6378.136
        final double dDLat = StrictMath.toRadians(dLatB - dLatA);
        final double dDLon = StrictMath.toRadians(dLonB - dLonA);
        final double latA = StrictMath.toRadians(dLatA);
        final double latB = StrictMath.toRadians(dLatB);

        final double da = (StrictMath.sin(dDLat / 2.0) * StrictMath.sin(dDLat / 2.0))
                + (StrictMath.sin(dDLon / 2.0) * StrictMath.sin(dDLon / 2.0)
                * StrictMath.cos(latA) * StrictMath.cos(latB));

        final double dc = 2.0 * StrictMath.atan2(StrictMath.sqrt(da),
                StrictMath.sqrt(1.0 - da));

        return EARTH_RADIUS * dc;
    }

    public static String integerToHex(long newInt) {
        String s = Long.toString(newInt, 16);
        if ((s.length() % 2) != 0) {
            s = "0" + s;
        }
        s = s.toUpperCase(Locale.getDefault());
        return s;
    }

    public static String integerToDecimalString(int newInt) {
        String s = Integer.toString(newInt);
        if ((s.length() % 2) != 0) {
            s = "0" + s;
        }
        s = s.toUpperCase(Locale.getDefault());
        return s;
    }

    public static byte[] hexStringToByteArray(String s) {
        final int len = s.length();
        final byte[] data = new byte[len];
        for (int i = 0; i < len; i++) {
            data[i] = (byte) Character.digit(s.charAt(i), 16);
        }
        return data;
    }

    public static Component getTopLevelAncestor(Component c) {
        while (c != null) {
            if (c instanceof Window) {
                break;
            }
            c = c.getParent();
        }
        return c;
    }

    public static BufferedImage getDefaultIcon(Dimension size) {
        Graphics2D g = null;
        try {
            final BufferedImage bi = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_RGB);
            g = bi.createGraphics();
            g.setColor(Color.GRAY);
            g.setStroke(new BasicStroke(2.0F));
            g.drawRect(0, 0, size.width, size.height);
            g.setColor(new Color(64, 0, 0));
            g.setStroke(new BasicStroke(1.0F));
            g.drawLine(0, 0, size.width, size.height);
            g.drawLine(size.width, 0, 0, size.height);
            return bi;
        } finally {
            if (g != null) {
                g.dispose();
            }
        }
    }

    public static int getPortNumberFromName(String name) {
        int number = 0;
        final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (os.contains("win")) {
            number = Integer.parseInt(name.substring(3));
        }
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            number = Integer.parseInt(name.substring(9));
        }
        return number;
    }

    public static boolean isComPortValid(String portName) {
        boolean isAvailable = false;
        if ((portName == null) || portName.isEmpty()) {
            return isAvailable;
        }
        final String[] ports = SerialPortList.getPortNames();
        for (final String port : ports) {
            if (port.equals(portName)) {
                isAvailable = true;
                break;
            }
        }
        return isAvailable;
    }

    public static int getComNumber(String registryKey) {
        final String friendlyName = getFriendlyName(registryKey);

        if ((friendlyName != null) && (friendlyName.contains("COM"))) {
            final String substr = friendlyName.substring(friendlyName.indexOf("COM"));
            final Matcher matchInt = Pattern.compile("\\d+").matcher(substr);
            if (matchInt.find()) {
                return Integer.parseInt(matchInt.group());
            }
        }
        return -1;
    }

    public static int booleanToInt(boolean value) {
        return value ? 1 : 0;
    }

    public static int concatenateBytes(byte msb, byte lsb) {
        return asInt(msb) << 8 | asInt(lsb);
    }

    public static int asInt(byte b) {
        int i = b;
        if (i < 0) {
            i += 256;
        }
        return i;
    }

    public static byte[] asByteArray(byte addr, int word) {
        return new byte[]{addr, (byte) (word & 0xFF), (byte) ((word >> 8) & 0xFF)};
    }
    
    public static String getFriendlyName(String registryKey) {
        if ((registryKey == null) || registryKey.isEmpty()) {
            throw new IllegalArgumentException("'registryKey' null or empty");
        }
        try {
            final int hkey = WinRegistry.HKEY_LOCAL_MACHINE;
            return WinRegistry.readString(hkey, registryKey, "FriendlyName");
        } catch (final IllegalArgumentException | IllegalAccessException | InvocationTargetException ex) {
            LOG.log(Level.WARNING, ex.getMessage());
        }
        return null;
    }

    public static String getPortNameString(int number) {
    	String name = null;
    	final String os = System.getProperty("os.name").toLowerCase(Locale.getDefault());
        if (os.contains("win")) {
            name = "COM" + number;
        }
        if (os.contains("nix") || os.contains("nux") || os.contains("aix")) {
            name = "/dev/ttyS" + number;
        }
        return name;
    }

    public static byte[] decToBcd(long num) {
        int digits = 0;

        long temp = num;
        while (temp != 0) {
            digits++;
            temp /= 10;
        }

        final int byteLen = (digits % 2) == 0 ? (digits / 2) : ((digits + 1) / 2);
        final boolean isOdd = (digits % 2) != 0;

        final byte[] bcd = new byte[byteLen];

        for (int i = 0; i < digits; i++) {
            final byte tmp = (byte) (num % 10);

            if ((i == (digits - 1)) && isOdd) {
                bcd[i / 2] = tmp;
            } else if ((i % 2) == 0) {
                bcd[i / 2] = tmp;
            } else {
                final byte foo = (byte) (tmp << 4);
                bcd[i / 2] |= foo;
            }

            num /= 10;
        }

        for (int i = 0; i < (byteLen / 2); i++) {
            final byte tmp = bcd[i];
            bcd[i] = bcd[byteLen - i - 1];
            bcd[byteLen - i - 1] = tmp;
        }

        return bcd;
    }

    public static String bcdToString(byte bcd) {
        final StringBuilder sb = new StringBuilder();

        int high = bcd & 0xf0;
        high >>>= 4;
        high &= 0x0f;
        final int low = bcd & 0x0f;

        sb.append(high);
        sb.append(low);

        return sb.toString();
    }

    public static String bcdToString(byte[] bcd) {

        final StringBuilder sb = new StringBuilder();

        for (final byte element : bcd) {
            sb.append(bcdToString(element));
        }

        return sb.toString();
    }

    public static byte[] toBCDArray(long freq, final int len) {

        final byte[] data = new byte[len];

        if ((len & 1) == 1) {
            data[len / 2] &= 0x0f;
            data[len / 2] |= (freq % 10) << 4;
            freq /= 10;
        }

        for (int i = (len / 2) - 1; i >= 0; i--) {
            long a = freq % 10;
            freq /= 10;
            a |= (freq % 10) << 4;
            freq /= 10;
            data[i] = (byte) a;
        }

        return data;
    }

    public static double round(double value, int precision) {
        final int scale = (int) Math.pow(10, precision);
        return (double) Math.round(value * scale) / scale;
    }

    public static void removeButton(Container container) {
    	final Component[] components = container.getComponents();
        for (Component component : components) {
            if (component instanceof AbstractButton) {
                container.remove(component);
            }
        }
    }

    public static String percentToHexByte(float percent) {
        if (percent < 0) {
            return "00";
        }
        if (percent > 1) {
            return "FF";
        }
        return integerToHex(Math.round(percent * 255D));
    }

    public static double byteToPercent(int i) {
        if (i < 0) {
            return 0F;
        }
        return i > 255 ? 1F : i / 255D;
    }

    public static int abs(int n) {
        return (n ^ (n >> 31)) + (n >>> 31);
    }
    
    public static int getRecommendedThreadCount() {
        int mRtnValue = 0;
        final Runtime runtime = Runtime.getRuntime();
        final long maxMemory = runtime.maxMemory();
        final long mTotalMemory = runtime.totalMemory();
        final long freeMemory = runtime.freeMemory();
        final int mAvailableProcessors = runtime.availableProcessors();

        final long mTotalFreeMemory = freeMemory + (maxMemory - mTotalMemory);
        mRtnValue = (int)(mTotalFreeMemory/4200000000l);

        final int mNoOfThreads = mAvailableProcessors-1;
        if(mNoOfThreads < mRtnValue) {
			mRtnValue = mNoOfThreads;
		}

        return mRtnValue;
    }
    
    public static String getStringOfNumbersFromString(String s) {
    	final StringBuilder sb = new StringBuilder();
        s.chars()
        	.mapToObj(c -> (char) c)
        	.filter(c -> Character.isDigit(c) || c == '.' || c == '-')
        	.forEach(sb::append);
        return sb.toString();
    }

    public static Locale getLocale(double latitude, double longitude) {
    	return getLocale(new Point2D.Double(longitude, latitude));
    }
    
    public static Locale getLocale(Point2D position) {
    	return new Locale.Builder().setRegion(Coordinates2Country.countryCode(position.getX(), position.getY()).toUpperCase()).build();
    }
}
