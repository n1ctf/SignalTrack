package utility;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sun.jna.platform.win32.WinReg.HKEY_LOCAL_MACHINE;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Win32Exception;

public class FriendlyName {
	private static final Logger LOG = Logger.getLogger(FriendlyName.class.getName());
    private static final String ENUM = "SYSTEM\\CurrentControlSet\\Enum\\USB";
    private static final String KEY = "HARDWARE\\DEVICEMAP\\SERIALCOMM";
    private final Map<String, String> friendlyNames;
 
    public FriendlyName() {
        friendlyNames = new HashMap<>();

        final Pattern p = Pattern.compile(".*?\\(([^)]+)\\)");
        for (final String dev : Advapi32Util.registryGetKeys(HKEY_LOCAL_MACHINE, ENUM)) {
            final String sb = ENUM + "\\" + dev;
            for (final String itm : Advapi32Util.registryGetKeys(HKEY_LOCAL_MACHINE, sb)) {
                final String si = sb + "\\" + itm;
                String fn = null;
                try {
                    fn = Advapi32Util.registryGetStringValue(HKEY_LOCAL_MACHINE, si, "FriendlyName");
                } catch (final Win32Exception e) {
                	e.printStackTrace();
                }
                if (fn != null) {
                    final Matcher m = p.matcher(fn);
                    if (m.matches()) {
                        friendlyNames.put(m.group(1), fn);
                    }
                }
            }
        }
    }

    private String get(String key) {
        return friendlyNames.get(key);
    }

    public String getCOM(String name) {
        try {
            for (final Entry<String, Object> sub : Advapi32Util.registryGetValues(HKEY_LOCAL_MACHINE, KEY).entrySet()) {
                final String n = (String) sub.getValue();
                final String fn = get(n);
                if ((fn != null) && fn.startsWith(name)) {
					return n;
				}
            }
        } catch (final IllegalArgumentException e) {
            LOG.log(Level.INFO, e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) {
        final FriendlyName fn = new FriendlyName();
        LOG.log(Level.INFO, "getCOM(): {0}", fn.getCOM(args[0]));
    }

}
