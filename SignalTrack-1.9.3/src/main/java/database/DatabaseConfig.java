package database;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


public class DatabaseConfig {
	public static final String DEFAULT_USERNAME = "signaltrack";
	public static final String DEFAULT_PASSWORD = "password";
	
	private static final String[] DATASOURCE_CLASS_NAME_CATALOG = {
		"com.mysql.jdbc.Driver", 
		"org.apache.derby.jdbc.EmbeddedDriver",
		"oracle.jdbc.OracleDriver (oci)",
		"oracle.jdbc.OracleDriver (thin)",
		"org.postgresql.Driver"
	};
	
	private static final Logger LOG = Logger.getLogger(DatabaseConfig.class.getName());
    private static final Preferences userPrefs = Preferences.userRoot().node(DatabaseConfig.class.getName());
	
    private String cryptoKey;
    private String password;
    private String userName;
    private int driver;
    private int port;
    private boolean useAESEncryption;
    private InetAddress inetAddress;
    private File databaseFile;

    public DatabaseConfig(boolean clearAllPrefs) {
		if (clearAllPrefs) {
			try {
				userPrefs.clear();
			} catch (final BackingStoreException ex) {
				LOG.log(Level.WARNING, ex.getMessage());
			}
		}
		getSettingsFromRegistry();
		initializeShutdownHook();
	}

	public File getDatabaseFile() {
		return databaseFile;
	}

	public void setDatabaseFile(File databaseFile) {
		this.databaseFile = databaseFile;
	}

	public String getCryptoKey() {
		return cryptoKey;
	}
	
	public void setCryptoKey(String cryptoKey) {
		this.cryptoKey = cryptoKey;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setPassword(char[] password) {
		this.password = Arrays.toString(password);
	}
	
	public int getDriver() {
		return driver;
	}
		
	public void setDriver(int driver) {
		this.driver = driver;
	}

	public String getDatasourceURL() {
		return switch (driver) {
			case 0 -> "jdbc:mysql://" + inetAddress.getHostAddress() + ":" + port + 
					File.pathSeparator + databaseFile.getName().replace("\\", File.pathSeparator).replace(".sql",  "");
			case 1 -> "jdbc:derby:codejava/webdb1;create=true";
					//"jdbc:derby:" + databaseFile.getPath() + ";create=true";
			case 2 -> "jdbc:oracle:oci:@" + inetAddress.getHostAddress() + 
					":" + port + ":" + databaseFile.getName().replace("\\", File.pathSeparator).replace(".sql",  "");
			case 3 -> "jdbc:oracle:thin:@" + inetAddress.getHostAddress() + 
					":" + port + ":" + databaseFile.getName().replace("\\", File.pathSeparator).replace(".sql",  "");
			case 4 -> "jdbc:postgresql://" + inetAddress.getHostAddress() + ":" + port + 
					File.pathSeparator + databaseFile.getName().replace("\\", File.pathSeparator).replace(".sql",  "");
			default -> null;
		};
	}

	public String getDatasourceClassName() {
		return DATASOURCE_CLASS_NAME_CATALOG[driver].split(" ")[0];
	}
	
	public String[] getDatasourceClassNameList() {
		return DATASOURCE_CLASS_NAME_CATALOG.clone();
	}
	
	public boolean isUseAESEncryption() {
		return useAESEncryption;
	}
	
	public void setUseAESEncryption(boolean useAESEncryption) {
		this.useAESEncryption = useAESEncryption;
	}
	
	public String getUserName() {
		return userName;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public InetAddress getInetAddress() {
		return inetAddress;
	}

	public void setInetAddress(InetAddress ip) {
		this.inetAddress = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
    public static String getNewDatabaseName() {
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		return sdf.format(Calendar.getInstance().getTime()) + ".sql";
    }
    
	private void getSettingsFromRegistry() {
		try {
			userName = userPrefs.get("UserName", DEFAULT_USERNAME);
			password = userPrefs.get("Password", DEFAULT_PASSWORD);
			driver = userPrefs.getInt("Driver", 1);
			cryptoKey = userPrefs.get("AESKeyCypher", "");
			useAESEncryption = userPrefs.getBoolean("UseAESKey", false);
			inetAddress = InetAddress.getByAddress(userPrefs.getByteArray("InetAddress", new byte[] {127, 0, 0, 1}));
			port = userPrefs.getInt("port", 3306);
			databaseFile = new File(userPrefs.get("DatabaseFileName", getNewDatabaseName()));
		} catch (UnknownHostException ex) {
			LOG.log(Level.WARNING, ex.getMessage());
		}
	}
	
	public void saveSettings() {
		userPrefs.put("UserName", userName);
		userPrefs.put("Password", password);
		userPrefs.putInt("Driver", driver);
		userPrefs.put("AESKeyCypher", cryptoKey);
		userPrefs.putBoolean("UseAESKey", useAESEncryption);
		userPrefs.putByteArray("InetAddress", inetAddress.getAddress());
		userPrefs.putInt("port", port);
		userPrefs.put("DatabaseFileName", databaseFile.getName());
	}

	private void initializeShutdownHook() {
		Runtime.getRuntime().addShutdownHook(new Thread() {
	        @Override    
	        public void run() {
	            saveSettings();
	        }
	    });
	}
}
