package utility;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.logging.Logger;

import java.util.prefs.Preferences;

public final class WinRegistry {
	public static final int HKEY_CURRENT_USER = 0x80000001;
	public static final int HKEY_LOCAL_MACHINE = 0x80000002;
	public static final int REG_SUCCESS = 0;
	public static final int REG_NOTFOUND = 2;
	public static final int REG_ACCESSDENIED = 5;

	private static final Logger LOG = Logger.getLogger(WinRegistry.class.getName());

	private static final int KEY_ALL_ACCESS = 0xf003f;
	private static final int KEY_READ = 0x20019;
	private static Preferences userRoot = Preferences.userRoot();
	private static Preferences systemRoot = Preferences.systemRoot();
	private static Class<? extends Preferences> userClass = userRoot.getClass();
	private static Method regOpenKey;
	private static Method regCloseKey;
	private static Method regQueryValueEx;
	private static Method regEnumValue;
	private static Method regQueryInfoKey;
	private static Method regEnumKeyEx;
	private static Method regCreateKeyEx;
	private static Method regSetValueEx;
	private static Method regDeleteKey;
	private static Method regDeleteValue;
	
	static {
		try {
			regOpenKey = userClass.getDeclaredMethod("WindowsRegOpenKey", new Class[] { int.class, byte[].class, int.class });
			regOpenKey.setAccessible(true);
			regCloseKey = userClass.getDeclaredMethod("WindowsRegCloseKey", new Class[] { int.class });
			regCloseKey.setAccessible(true);
			regQueryValueEx = userClass.getDeclaredMethod("WindowsRegQueryValueEx", new Class[] { int.class, byte[].class });
			regQueryValueEx.setAccessible(true);
			regEnumValue = userClass.getDeclaredMethod("WindowsRegEnumValue", new Class[] { int.class, int.class, int.class });
			regEnumValue.setAccessible(true);
			regQueryInfoKey = userClass.getDeclaredMethod("WindowsRegQueryInfoKey1", new Class[] { int.class });
			regQueryInfoKey.setAccessible(true);
			regEnumKeyEx = userClass.getDeclaredMethod("WindowsRegEnumKeyEx", new Class[] { int.class, int.class, int.class });
			regEnumKeyEx.setAccessible(true);
			regCreateKeyEx = userClass.getDeclaredMethod("WindowsRegCreateKeyEx", new Class[] { int.class, byte[].class });
			regCreateKeyEx.setAccessible(true);
			regSetValueEx = userClass.getDeclaredMethod("WindowsRegSetValueEx", new Class[] { int.class, byte[].class, byte[].class });
			regSetValueEx.setAccessible(true);
			regDeleteValue = userClass.getDeclaredMethod("WindowsRegDeleteValue", new Class[] { int.class, byte[].class });
			regDeleteValue.setAccessible(true);
			regDeleteKey = userClass.getDeclaredMethod("WindowsRegDeleteKey", new Class[] { int.class, byte[].class });
			regDeleteKey.setAccessible(true);
		} catch (NoSuchMethodException | SecurityException e) {
			LOG.warning(e.getMessage());
		}
	}

	private WinRegistry() {}

	/**
	 * Read a value from key and value name
	 * 
	 * @param hkey
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @param valueName
	 * @return the value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static String readString(int hkey, String key, String valueName)
			throws IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readString(systemRoot, hkey, key, valueName);
		} else if (hkey == HKEY_CURRENT_USER) {
			return readString(userRoot, hkey, key, valueName);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Read value(s) and value name(s) form given key
	 * 
	 * @param hkey
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @return the value name(s) plus the value(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static Map<String, String> readStringValues(int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readStringValues(systemRoot, hkey, key);
		} else if (hkey == HKEY_CURRENT_USER) {
			return readStringValues(userRoot, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Read the value name(s) from a given key
	 * 
	 * @param hkey
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @return the value name(s)
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static List<String> readStringSubKeys(int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			return readStringSubKeys(systemRoot, hkey, key);
		} else if (hkey == HKEY_CURRENT_USER) {
			return readStringSubKeys(userRoot, hkey, key);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Create a key
	 * 
	 * @param hkey
	 *            HKEY_CURRENT_USER/HKEY_LOCAL_MACHINE
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void createKey(int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		int[] ret;
		if (hkey == HKEY_LOCAL_MACHINE) {
			ret = createKey(systemRoot, hkey, key);
			regCloseKey.invoke(systemRoot, ret[0]);
		} else if (hkey == HKEY_CURRENT_USER) {
			ret = createKey(userRoot, hkey, key);
			regCloseKey.invoke(userRoot, ret[0]);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
		if (ret[1] != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + ret[1] + "  key=" + key);
		}
	}

	/**
	 * Write a value in a given key/value name
	 * 
	 * @param hkey
	 * @param key
	 * @param valueName
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void writeStringValue(int hkey, String key, String valueName, String value)
			throws IllegalAccessException, InvocationTargetException {
		if (hkey == HKEY_LOCAL_MACHINE) {
			writeStringValue(systemRoot, hkey, key, valueName, value);
		} else if (hkey == HKEY_CURRENT_USER) {
			writeStringValue(userRoot, hkey, key, valueName, value);
		} else {
			throw new IllegalArgumentException("hkey=" + hkey);
		}
	}

	/**
	 * Delete a given key
	 * 
	 * @param hkey
	 * @param key
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void deleteKey(int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		int rc = -1;
		if (hkey == HKEY_LOCAL_MACHINE) {
			rc = deleteKey(systemRoot, hkey, key);
		} else if (hkey == HKEY_CURRENT_USER) {
			rc = deleteKey(userRoot, hkey, key);
		}
		if (rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key);
		}
	}

	/**
	 * delete a value from a given key/value name
	 * 
	 * @param hkey
	 * @param key
	 * @param value
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	public static void deleteValue(int hkey, String key, String value)
			throws IllegalAccessException, InvocationTargetException {
		int rc = -1;
		if (hkey == HKEY_LOCAL_MACHINE) {
			rc = deleteValue(systemRoot, hkey, key, value);
		} else if (hkey == HKEY_CURRENT_USER) {
			rc = deleteValue(userRoot, hkey, key, value);
		}
		if (rc != REG_SUCCESS) {
			throw new IllegalArgumentException("rc=" + rc + "  key=" + key + "  value=" + value);
		}
	}

	// =====================

	private static int deleteValue(Preferences root, int hkey, String key, String value)
			throws IllegalAccessException, InvocationTargetException {
		final int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_ALL_ACCESS);
		if (handles[1] != REG_SUCCESS) {
			return handles[1]; // can be REG_NOTFOUND, REG_ACCESSDENIED
		}
		final int rc = ((Integer) regDeleteValue.invoke(root, handles[0], toCstr(value))).intValue();
		regCloseKey.invoke(root, handles[0]);
		return rc;
	}

	private static int deleteKey(Preferences root, int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		return ((Integer) regDeleteKey.invoke(root, hkey, toCstr(key))).intValue();
		// can REG_NOTFOUND, REG_ACCESSDENIED, REG_SUCCESS
	}

	private static String readString(Preferences root, int hkey, String key, String value)
			throws IllegalAccessException, InvocationTargetException {
		final int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_READ);
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		final byte[] valb = (byte[]) regQueryValueEx.invoke(root, handles[0], toCstr(value));
		regCloseKey.invoke(root, handles[0]);
		return (valb != null ? new String(valb, StandardCharsets.UTF_8).trim() : null);
	}

	private static Map<String, String> readStringValues(Preferences root, int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		final HashMap<String, String> results = new HashMap<>();
		final int[] handles = (int[]) regOpenKey.invoke(root, hkey, toCstr(key), KEY_READ);
		if (handles[1] != REG_SUCCESS) {
			return null;
		}
		final int[] info = (int[]) regQueryInfoKey.invoke(root, handles[0]);

		final int count = info[0]; // count
		final int maxlen = info[3]; // value length max
		for (int index = 0; index < count; index++) {
			final byte[] name = (byte[]) regEnumValue.invoke(root, handles[0], index, maxlen + 1);
			final String value = readString(hkey, key, new String(name, StandardCharsets.UTF_8));
			results.put(new String(name, StandardCharsets.UTF_8).trim(), value);
		}
		regCloseKey.invoke(root, handles[0]);
		return results;
	}

	private static List<String> readStringSubKeys(Preferences root, int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		final List<String> results = new ArrayList<>();
		final int[] handles = (int[]) regOpenKey.invoke(root, (hkey), toCstr(key), KEY_READ);
		if (handles[1] != REG_SUCCESS) {
			return results;
		}
		final int[] info = (int[]) regQueryInfoKey.invoke(root, handles[0]);

		final int count = info[0]; // Fix: info[2] was being used here with
									// wrong results. Suggested by davenpcj,
									// confirmed by Petrucio
		final int maxlen = info[3]; // value length max
		for (int index = 0; index < count; index++) {
			final byte[] name = (byte[]) regEnumKeyEx.invoke(root, handles[0], (index), (maxlen + 1));
			results.add(new String(name, StandardCharsets.UTF_8).trim());
		}
		regCloseKey.invoke(root, (handles[0]));
		return results;
	}

	private static int[] createKey(Preferences root, int hkey, String key)
			throws IllegalAccessException, InvocationTargetException {
		return (int[]) regCreateKeyEx.invoke(root, (hkey), toCstr(key));
	}

	private static void writeStringValue(Preferences root, int hkey, String key, String valueName, String value)
			throws IllegalAccessException, InvocationTargetException {
		final int[] handles = (int[]) regOpenKey.invoke(root, (hkey), toCstr(key), KEY_ALL_ACCESS);

		regSetValueEx.invoke(root, handles[0], toCstr(valueName), toCstr(value));
		regCloseKey.invoke(root, handles[0]);
	}

	// utility
	private static byte[] toCstr(String str) {
		final byte[] result = new byte[str.length() + 1];

		for (int i = 0; i < str.length(); i++) {
			result[i] = (byte) str.charAt(i);
		}
		result[str.length()] = 0;
		return result;
	}
}
