package utility;

import java.util.Locale;

public final class OsCheck {

	public enum OSType {
		Windows, MacOS, Linux, Other
	}

	// cached result of OS detection
	private static OSType detectedOS;

	/**
	 * detect the operating system from the os.name System property and cache the
	 * result
	 * 
	 * @returns - the operating system detected
	 */
	public static OSType getOperatingSystemType() {
		if (detectedOS == null) {
			final String os = System.getProperty("os.name", "generic").toLowerCase(Locale.ENGLISH);
			if ((os.contains("mac")) || (os.contains("darwin"))) {
				detectedOS = OSType.MacOS;
			} else if (os.contains("win")) {
				detectedOS = OSType.Windows;
			} else if (os.contains("nux")) {
				detectedOS = OSType.Linux;
			} else {
				detectedOS = OSType.Other;
			}
		}
		return detectedOS;
	}
}
