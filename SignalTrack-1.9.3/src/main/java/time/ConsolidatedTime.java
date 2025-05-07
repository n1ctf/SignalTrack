package time;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.IOException;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.time.zone.ZoneRules;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Optional;
import java.util.TimeZone;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpUtils;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;

import net.iakovlev.timeshape.TimeZoneEngine;

public final class ConsolidatedTime implements AutoCloseable {

    public enum OffsetBase {
        GMT, UTC
    }

    protected static final String[] DEFAULT_TIME_SERVER = {"192.168.56.28"};
    
    public static final int DEFAULT_LOCAL_TIME_CORRECTION_SECONDS = 600;
    public static final double DEFAULT_STATION_LATITUDE = 40.0;
	public static final double DEFAULT_STATION_LONGITUDE = -83.0;
	public static final double DEFAULT_STATION_ALTITUDE_METERS = 0.0;
    public static final int SECONDS_PER_DAY = 60 * 60 * 24;
    public static final int SECONDS_PER_5_MINUTES = 60 * 5;
    public static final String OFFSET = "OFFSET";
    public static final String CLOCK = "CLOCK";
    public static final String ZDT_UTC = "ZDT_UTC";
    public static final String FAIL = "FAIL";
    public static final String UPDATES_STOPPED = "UPDATES_STOPPED";
    public static final String CLOCK_STOPPED = "CLOCK_STOPPED";
    public static final String STRATA_CHANGE = "STRATA_CHANGE";
    public static final int DEFAULT_INITIAL_DELAY = 50;
    public static final int DEFAULT_DELAY = 60000;
    public static final int DEFAULT_TIMEOUT = 10000;
    public static final int DEFAULT_CLOCK_UPDATE_PERIOD = 5000;
    public static final int STRATA_DEBOUNCE_COUNTER = 3;
    public static final ZoneId DEFAULT_ZONE_ID = ZoneId.systemDefault();
    public static final Locale DEFAULT_LOCALE = Locale.US;
    public static final int STRATUM_GPS = -1;
    public static final int STRATUM_NTP0 = 0;
    public static final int STRATUM_NTP1 = 1;
    public static final int STRATUM_NTP2 = 2;
    public static final int STRATUM_NTP3 = 3;
    public static final int STRATUM_NTP4 = 4;
    public static final int STRATUM_NTP5 = 5;
    public static final int STRATUM_NTP6 = 6;
    public static final int STRATUM_NTP7 = 7;
    public static final int STRATUM_NTP8 = 8;
    public static final int STRATUM_NTP9 = 9;
    public static final int STRATUM_NTP10 = 10;
    public static final int STRATUM_NTP11 = 11;
    public static final int STRATUM_NTP12 = 12;
    public static final int STRATUM_NTP13 = 13;
    public static final int STRATUM_NTP14 = 14;
    public static final int STRATUM_NTP15 = 15;
    public static final int STRATUM_UNSYNC = 16; 
    
    private static final Logger LOG = Logger.getLogger(ConsolidatedTime.class.getName());
    
    private static double latitudeDegrees = DEFAULT_STATION_LATITUDE;
    private static double longitudeDegrees = DEFAULT_STATION_LONGITUDE;
    private static double altitudeMeters = DEFAULT_STATION_ALTITUDE_METERS;

    private final String operatingSystem = System.getProperty("os.name").toLowerCase(Locale.US);
    
    private final String[] hosts;
    private NtpV3Packet message;
    private int stratum = -1;
    private int version;
    private int leapIndicator;
    private int precision;
    private int poll;
    private String modeName;
    private int mode;
    private double disp;
    private double rootDelayInMillisDouble;
    private int refId;
    private String refAddr;
    private String refName;
    private long refNtpTime;
    private long origNtpTime;
    private long destJavaTime;
    private long rcvNtpTime;
    private long xmitNtpTime;
    private long destNtpTime;
    private long offsetValue = -1;
    private long delayValue;
    private final int timeout;
    private TimeInfo timeInfo;
    private long gpsTime;
    private long ntpTimeLastUpdate = -1;
    private long gpsTimeLastUpdate = -1;
    private int timeStratumChangeTestCounter = STRATA_DEBOUNCE_COUNTER + 1;
    private int timeStratum = -2;
 
    private long localTimeCorrectionCounter;
    
    private final ScheduledExecutorService clockScheduler = Executors.newSingleThreadScheduledExecutor();
    private final ScheduledExecutorService refreshScheduler = Executors.newSingleThreadScheduledExecutor();

    private static final Preferences userPrefs = Preferences.userRoot().node(ConsolidatedTime.class.getName());
    
    private ZoneId localZoneId = DEFAULT_ZONE_ID;
    
    private Locale locale = DEFAULT_LOCALE;

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    public ConsolidatedTime() {
        this(DEFAULT_ZONE_ID, DEFAULT_TIME_SERVER, DEFAULT_TIMEOUT, DEFAULT_INITIAL_DELAY, DEFAULT_DELAY, false, false);
    }

    public ConsolidatedTime(ZoneId zoneId) {
        this(zoneId, DEFAULT_TIME_SERVER, DEFAULT_TIMEOUT, DEFAULT_INITIAL_DELAY, DEFAULT_DELAY, true, true);
    }

    public ConsolidatedTime(String host, int timeout) {
        this(DEFAULT_ZONE_ID, toStringArray(host), timeout, 0, 0, false, false);
    }

    public ConsolidatedTime(String[] hosts, int timeout) {
        this(DEFAULT_ZONE_ID, hosts, timeout, 0, 0, false, false);
    }

    public ConsolidatedTime(ZoneId localZoneId, String[] hosts, int timeout, int initialDelay, int delay, boolean enableAutomaticUpdates, boolean enableClock) {
        this.localZoneId = localZoneId;
        this.hosts = hosts.clone();
        this.timeout = timeout;
        this.delayValue = delay;
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				close();
			}
		});
        
    	LOG.log(Level.INFO, "Consoldated Time/Operating System Name: {0}", operatingSystem);
        
    	loadPreferences();

        if (enableAutomaticUpdates) {
            initializeAutomaticNetworkTimeUpdates(hosts, initialDelay, delay, timeout);
        }
        if (enableClock) {
            startClock(DEFAULT_CLOCK_UPDATE_PERIOD);
        }
    }
    
    private void loadPreferences() {
		setLocale(forDisplayName(userPrefs.get("Locale", DEFAULT_LOCALE.getDisplayName())));
		setLocalZoneId(ZoneId.of(userPrefs.get("ZoneId", DEFAULT_ZONE_ID.getId())));
	}
    
    private void savePreferences() {
		userPrefs.put("Locale", getLocale().getDisplayName());	
		userPrefs.put("ZoneId", getLocalZoneId().getId());
	}
    
	private static Locale forDisplayName(String displayName) {
		Locale locale = DEFAULT_LOCALE;
		for (Locale loc : Locale.getAvailableLocales()) {
			if (loc.getDisplayName().equals(displayName)) {
				locale = loc;
				break;
			}
		}
		return locale;
	}
	
    public ZonedDateTime getCurrentUTC() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), ZoneId.of("UTC"));
    }

    public ZonedDateTime getCurrentUTCMinus24H() {
    	final Instant nowUtc = Instant.ofEpochMilli(getBestTimeInMillis());
    	final Instant nowMinus24H = nowUtc.minus(24, ChronoUnit.HOURS);
    	final ZoneId utc = ZoneId.of("UTC");
        return ZonedDateTime.ofInstant(nowMinus24H, utc);
    }

    public static ZonedDateTime fromEPAEnvirofactsStyleDateGroup(String dg) {
    	final OffsetDateTime dateTime = OffsetDateTime.parse(dg, DateTimeFormatter.ofPattern("MMM/dd/yyyy"));
        return dateTime.toZonedDateTime();
    }

    public static ZonedDateTime fromEPAEnvirofactsStyleDateTimeGroup(String dtg) {
    	final OffsetDateTime dateTime = OffsetDateTime.parse(dtg, DateTimeFormatter.ofPattern("MMM/dd/yyyyhhaa"));
        return dateTime.toZonedDateTime();
    }
    
    public static ZonedDateTime fromNasaDateTimeGroup(String dtg) {
        return ZonedDateTime.parse(dtg);
    }

    private static String[] toStringArray(String host) {
        return new String[]{host};
    }

    public void requestNetworkTime() {
        requestNetworkTime(hosts);
    }

    public void requestNetworkTime(String[] hosts) {
        new NetworkTimeRefresh(hosts, timeout);
    }

    public void startClock() {
        startClock(DEFAULT_CLOCK_UPDATE_PERIOD);
    }

    public void startClock(int period) {
        initializeClock(period);
    }

    public void stopClock() {
    	
    	if (clockScheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing ConsolidatedTime.clockScheduler service termination....");
				clockScheduler.shutdown();
				clockScheduler.awaitTermination(3, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "ConsolidatedTime.clockScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				clockScheduler.shutdownNow();
				LOG.log(Level.SEVERE, "ConsolidatedTime.clockScheduler service has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    	
        pcs.firePropertyChange(CLOCK_STOPPED, null, true);
    }
    
    public void startAutomaticNetworkTimeUpdates() {
        startAutomaticNetworkTimeUpdates(DEFAULT_INITIAL_DELAY, DEFAULT_DELAY, DEFAULT_TIMEOUT);
    }

    public void startAutomaticNetworkTimeUpdates(int initialDelay, int delay, int timeout) {
        initializeAutomaticNetworkTimeUpdates(hosts, initialDelay, delay, timeout);
    }

    public void stopAutomaticNetworkTimeUpdates() {
    	
    	if (refreshScheduler != null) {
			try {
				LOG.log(Level.INFO, "Initializing ConsolidatedTime.refreshScheduler service termination....");
				refreshScheduler.shutdown();
				refreshScheduler.awaitTermination(3, TimeUnit.SECONDS);
				LOG.log(Level.INFO, "ConsolidatedTime.refreshScheduler service has gracefully terminated");
			} catch (InterruptedException e) {
				refreshScheduler.shutdownNow();
				LOG.log(Level.SEVERE, "ConsolidatedTime.refreshScheduler service has timed out after 3 seconds of waiting to terminate processes.");
				Thread.currentThread().interrupt();
			}
		}
    	
        pcs.firePropertyChange(UPDATES_STOPPED, null, true);
    }

    private void initializeAutomaticNetworkTimeUpdates(final String[] hosts, int initialDelay, int delay, final int timeout) {
        refreshScheduler.scheduleAtFixedRate(new NetworkTimeRefresh(hosts, timeout), initialDelay, delay, TimeUnit.MILLISECONDS);
    }

    private void initializeClock(int period) {
        clockScheduler.scheduleAtFixedRate(this::doUpdateClock, 50, period, TimeUnit.MILLISECONDS);
    }
    
    public void setGpsTimeInMillis(long gpsTime) {
        this.gpsTime = gpsTime;
        gpsTimeLastUpdate = System.currentTimeMillis();
        if (localTimeCorrectionCounter <= 0) {
        	localTimeCorrectionCounter = DEFAULT_LOCAL_TIME_CORRECTION_SECONDS;
        }
        localTimeCorrectionCounter--;
    }
    
    public void doUpdateClock() {
        pcs.firePropertyChange(CLOCK, null, millisFromUnixEpochToFormattedUTCString(getBestTimeInMillis()));
        pcs.firePropertyChange(ZDT_UTC, null, getBestZonedDateTime(ZoneOffset.UTC));
        updateStratumEventWithDebounce();
    }

    public void networkTimeRefresh() {
    	new NetworkTimeRefresh(hosts, timeout);
    }
    
    private void updateStratumEventWithDebounce() {
        if (timeStratumChangeTestCounter > STRATA_DEBOUNCE_COUNTER) {
        	final int ts = getTimeStratum();
            timeStratumChangeTestCounter = 0;
            pcs.firePropertyChange(STRATA_CHANGE, this.timeStratum, ts);
            this.timeStratum = ts;
        } else {
            timeStratumChangeTestCounter++;
        }
    }

    private class NetworkTimeRefresh implements Runnable {
    	private final String[] hosts;
    	private final int timeout;
    	
    	private NetworkTimeRefresh(String[] hosts, int timeout) {
    		this.hosts = hosts;
    		this.timeout = timeout;
    	}

		@Override
		public void run() {
			TimeInfo info = null;
        	try (NTPUDPClient client = new NTPUDPClient()) {
                client.setDefaultTimeout(Duration.ofMillis(timeout));
                client.open();
                for (final String host : hosts) {
                    info = getTimeInfo(client, host);
                    if (info != null) {
		                processResponse(info);
		                break;
		            }  
                }
            } catch (final SocketException e) {
            	LOG.log(Level.WARNING, e.getLocalizedMessage(), e);
            } finally {
            	doUpdateClock();
            }
		}
		
		private TimeInfo getTimeInfo(NTPUDPClient client, String hostName) {
	        TimeInfo ti = null;
	        final InetAddress hostAddr;
	        try {
	            hostAddr = InetAddress.getByName(hostName);
	            ti = client.getTime(hostAddr);
	        } catch (IOException e) {
	            pcs.firePropertyChange(FAIL, null, hostName);
	        }
	        return ti;
	    }

		private void processResponse(TimeInfo info) {
			final long infoTime = info.getReturnTime();

	        timeInfo = info;
	        timeInfo.computeDetails();
	        offsetValue = timeInfo.getOffset();
	        delayValue = timeInfo.getDelay();
	        message = timeInfo.getMessage();
	        stratum = message.getStratum();
	        version = message.getVersion();
	        leapIndicator = message.getLeapIndicator();
	        precision = message.getPrecision();
	        modeName = message.getModeName();
	        mode = message.getMode();
	        poll = message.getPoll();
	        disp = message.getRootDispersionInMillisDouble();
	        rootDelayInMillisDouble = message.getRootDelayInMillisDouble();
	        refId = message.getReferenceId();
	        refAddr = NtpUtils.getHostAddress(refId);
	        refNtpTime = message.getReferenceTimeStamp().ntpValue();
	        rcvNtpTime = message.getReceiveTimeStamp().ntpValue();
	        xmitNtpTime = message.getTransmitTimeStamp().ntpValue();
	        origNtpTime = message.getOriginateTimeStamp().ntpValue();
	        destNtpTime = TimeStamp.getNtpTime(infoTime).ntpValue();
	        destJavaTime = TimeStamp.getNtpTime(infoTime).getTime();
	        ntpTimeLastUpdate = System.currentTimeMillis();
	        
	        if (refId != 0) {
	            if ("127.127.1.0".equals(refAddr)) {
	                refName = "LOCAL"; // This is the ref address for the Local Clock
	            } else if (stratum >= 2) {
	                // If reference id has 127.127 prefix then it uses its own reference clock
	                // defined in the form 127.127.clock-type.unit-num (e.g. 127.127.8.0 mode 5
	                // for GENERIC DCF77 AM; see refclock.htm from the NTP software distribution.
	                if (!refAddr.startsWith("127.127")) {
	                    try {
	                        final InetAddress addr = InetAddress.getByName(refAddr);
	                        final String name = addr.getHostName();
	                        if ((name != null) && !name.equals(refAddr)) {
	                            refName = name;
	                        }
	                    } catch (final UnknownHostException e) {
	                        // some stratum-2 servers sync to ref clock device but fudge stratum level higher... (e.g. 2)
	                        // ref not valid host maybe it's a reference clock name?
	                        // otherwise just show the ref IP address.
	                        refName = NtpUtils.getReferenceClock(message);
	                    }
	                }
	            } else if ((version >= 3) && ((stratum == 0) || (stratum == 1))) {
	                refName = NtpUtils.getReferenceClock(message);
	                // refname usually has at least 3 characters (e.g. GPS, WWV, LCL, etc.)
	            }
	            // otherwise give up on naming...
	        }
	        if ((refName != null) && (refName.length() > 1)) {
	            refAddr += " (" + refName + ")";
	        }
	    }
    }

	public Locale getLocale() {
		return locale;
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}
	
    public ZoneId getLocalZoneId() {
        return localZoneId;
    }

    public void setLocalZoneId(ZoneId localZoneId) {
        this.localZoneId = localZoneId;
    }

    public String getTimeServerHostAddress() {
        return "http://" + hosts[0];
    }

    public String getReferenceAddress() {
        return refAddr;
    }

    public String getReferenceName() {
        return refName;
    }

    public int getReferenceIdentifier() {
        return refId;
    }

    public double getRootDelayInMillis() {
        return rootDelayInMillisDouble;
    }

    public double getRootDispersionInMillis() {
        return disp;
    }

    public String getModeName() {
        return modeName;
    }

    public int getMode() {
        return mode;
    }

    public int getPoll() {
        return poll;
    }

    public int getPrecision() {
        return precision;
    }

    public int getLeapIndicator() {
        return leapIndicator;
    }

    public int getVersion() {
        return version;
    }

    public int getStratum() {
        return stratum;
    }

    public NtpV3Packet getNtpV3Message() {
        return message;
    }

    public TimeInfo getTimeInfo() {
        return timeInfo;
    }

    public long getDestJavaTimeInMillis() {
        return destJavaTime;
    }

    public long getDestNtpTimeInMillis() {
        return destNtpTime;
    }

    public long getDelayValueMillis() {
        return delayValue;
    }

    public long getOffsetValueMillis() {
        return offsetValue;
    }

    public long getTransmitNtpTime() {
        return xmitNtpTime;
    }

    public long getReceiveNtpTime() {
        return rcvNtpTime;
    }

    public long getOriginateNtpTime() {
        return origNtpTime;
    }

    public long getReferenceNtpTime() {
        return refNtpTime;
    }

    public double getLatitudeDegrees() {
		return latitudeDegrees;
	}

	public static void setLatitudeDegrees(double latitudeDegrees) {
		ConsolidatedTime.latitudeDegrees = latitudeDegrees;
	}

	public double getLongitudeDegrees() {
		return longitudeDegrees;
	}

	public static void setLongitudeDegrees(double longitudeDegrees) {
		ConsolidatedTime.longitudeDegrees = longitudeDegrees;
	}

	public double getAltitudeMeters() {
		return altitudeMeters;
	}

	public static void setAltitudeMeters(double altitudeMeters) {
		ConsolidatedTime.altitudeMeters = altitudeMeters;
	}

    public long getNetworkTimeAgeInMillis() {
        return System.currentTimeMillis() - ntpTimeLastUpdate;
    }

    public long getGpsTimeAgeInMillis() {
        return System.currentTimeMillis() - gpsTimeLastUpdate;
    }

    public long getGpsTimeInMillis() {
        return gpsTime + getGpsTimeAgeInMillis();
    }

    public long getBestTimeInMillis() {
        final int ts = getTimeStratum();
        if (ts == -1) {
            return getGpsTimeInMillis();
        }
        if ((ts >= 0) && (ts <= 15)) {
            return getNetworkTimeInMillis();
        }
        return System.currentTimeMillis();
    }

    public DateTimeFormatter epochFormatter() {
    	return epochFormatter(getLocalZoneId());
    }
    
    public static DateTimeFormatter epochFormatter(ZoneId zoneId) {
    	return new DateTimeFormatterBuilder()
            .appendValue(ChronoField.INSTANT_SECONDS, 1, 19, SignStyle.NEVER)
            .optionalStart()
            .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
            .optionalEnd()
            .toFormatter()
            .withZone(zoneId);
    }
    
    
    public Date getDateUTC() {
    	return Date.from(getCurrentUTC().toInstant());
    }
    
    public Date getDateInZoneId() {
    	return Date.from(getBestZonedDateTime(getLocalZoneId()).toInstant());
    }
    
    public long getNetworkTimeInMillis() { 
        return getDestJavaTimeInMillis() + offsetValue;
    }
    
    public Calendar getNetworkTimeInCalendar(TimeZone timeZone) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        cal.setTimeInMillis(getBestTimeInMillis());
        cal.setTimeZone(timeZone);
        return cal;
    }

    public ZonedDateTime getBestLocalZonedDateTime() {
    	return getBestZonedDateTime(getLocalZoneId());
    }
    
    public ZonedDateTime getBestZonedDateTime(ZoneId zoneId) {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), zoneId);
    }

    public LocalDateTime getBestLocalDateTime() {
    	return getBestLocalDateTime(getLocalZoneId());
    }
    
    public LocalDateTime getBestLocalDateTime(ZoneId zoneId) {
    	return LocalDateTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), zoneId);
    }
    
    public LocalDate getBestLocalDateInZoneId() {
    	return getBestLocalDate(getLocalZoneId());
    }
    
    public LocalDate getBestLocalDate(ZoneId zoneId) {
    	return LocalDate.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), zoneId);
    }
    
    public LocalTime getBestLocalTime(ZoneId zoneId) {
    	return LocalTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), zoneId);
    }
    
    public LocalTime getBestLocalTime() {
    	return getBestLocalTime(getLocalZoneId());
    }
    
    public ZonedDateTime getBestZonedDateTimeUTC() {
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), ZoneId.of("UTC"));
    }

    public LocalDateTime getBestLocalDateTimeUTC() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), ZoneId.of("UTC"));
    }

    public LocalDateTime getBestLocalDateAtTimeStationLocation() {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), getZoneIdAtStationLocation());
    }
    
    public static LocalDateTime getLocalDateTimefromUTCAtStationLocation(ZonedDateTime zdt) {
    	return LocalDateTime.ofInstant(Instant.ofEpochSecond(zdt.toEpochSecond()), getZoneIdAtStationLocation());
    }
    
    public static ZoneId getZoneIdAtStationLocation() {
    	return getZoneId(new Point2D.Double(longitudeDegrees, latitudeDegrees));
    }
    
    public static ZoneId getZoneId(Point2D position) {
    	final TimeZoneEngine timeZoneEngine = TimeZoneEngine.initialize();
    	final Optional<ZoneId> zid = timeZoneEngine.query(position.getY(), position.getX());
        ZoneId z = DEFAULT_ZONE_ID;
        if (zid.isPresent()) {
            z = zid.get();
        }
        return z;
    }
    
    public String getISOFormattedLocalTimeString() {
    	final LocalTime time = LocalTime.ofInstant(Instant.ofEpochMilli(getBestTimeInMillis()), getLocalZoneId());
    	return time.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }
    
    public int getTimeStratum() {
        if ((gpsTimeLastUpdate >= 0) && (getGpsTimeAgeInMillis() < 10000)) {
            return -1;
        }
        if ((ntpTimeLastUpdate >= 0) && (getNetworkTimeAgeInMillis() < 90000) && (stratum != -1)) {
            return stratum;
        }
        if ((ntpTimeLastUpdate >= 0) && (millisToHours(getNetworkTimeAgeInMillis()) < 4)) {
            return 15;
        }
        return 16;
    }

    public static long millisToSeconds(long millis) {
        return millis / 1000;
    }

    public static long millisToMinutes(long millis) {
        return millis / 1000 / 60;
    }

    public static long millisToHours(long millis) {
        return millis / 1000 / 60 / 60;
    }

    public static long millisToDays(long millis) {
        return millis / 1000 / 60 / 60 / 24;
    }

    public static long dateTimeDifference(Temporal d1, Temporal d2, ChronoUnit unit) {
        return unit.between(d1, d2);
    }

    public static long zonedDateTimeDifference(ZonedDateTime d1, ZonedDateTime d2, ChronoUnit unit) {
        return unit.between(d1, d2);
    }

    public static long zonedDateTimeDifferenceInHours(ZonedDateTime d1, ZonedDateTime d2) {
        return ChronoUnit.HOURS.between(d1, d2);
    }

    public static long zonedDateTimeAbsoluteDifferenceInHours(ZonedDateTime d1, ZonedDateTime d2) {
        return Math.abs(ChronoUnit.HOURS.between(d1, d2));
    }

    public static boolean isZonedDateTimeWithin(ZonedDateTime t, ZonedDateTime startTime, ZonedDateTime endTime) {
        return !t.isBefore(startTime) && !t.isAfter(endTime);
    }

    public boolean isZonedDateTimeInCurrentDay(ZonedDateTime zdt) {
    	return Duration.between(zdt, getBestZonedDateTime(getLocalZoneId())).abs().toDays() > 1;
    }
    
    public static boolean isLocalTimeWithin(LocalTime t, LocalTime startTime, LocalTime endTime) {
        return !t.isBefore(startTime) && !t.isAfter(endTime);
    }

    public static String millisFromUnixEpochToFormattedUTCString(long millis) {
        final Date date = new Date(millis);
        final DateFormat formatter = new SimpleDateFormat("HH.mm.ss.SSS");
        return formatter.format(date);
    }

    public static String toFormattedISOTime(LocalTime time) {
    	return time.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }
    
    public String calendarToFormattedTimeString(Calendar cal) {
        final long millis = cal.getTimeInMillis();
        final Date date = new Date(millis);
        final DateFormat formatter = new SimpleDateFormat("HH.mm.sss.SSS");
        return formatter.format(date);
    }

    public boolean isItDaylightSavingsTimeNow() {
    	return isItDaylightSavingsTimeNow(getLocalZoneId());
    }
    
    public static boolean isItDaylightSavingsTimeNow(ZoneId zoneId) {
    	final ZonedDateTime now = ZonedDateTime.now(zoneId);
    	final ZoneId z = now.getZone();
    	final ZoneRules zoneRules = z.getRules();
        return zoneRules.isDaylightSavings(now.toInstant());
    }

    public static String getTimeZoneDisplayNameShort(TimeZone timeZone, boolean isDST) {
        return timeZone.getDisplayName(isDST, TimeZone.SHORT);
    }

    public String getZoneIdDisplayShortNoDST() {
    	return getZoneIdDisplayShortNoDST(getLocalZoneId(), getLocale());
    }
    
    public String getZoneIdDisplayShortNoDST(ZoneId zoneId) {
    	return getZoneIdDisplayShortNoDST(zoneId, getLocale());
    }
    
    public String getZoneIdDisplayShortNoDST(Locale locale) {
    	return getZoneIdDisplayShortNoDST(getLocalZoneId(), locale);
    }
    
    public static String getZoneIdDisplayShortNoDST(ZoneId zoneId, Locale locale) {
        return zoneId.getDisplayName(TextStyle.SHORT_STANDALONE, locale);
    }

    public static String getZoneIdDisplayShortWithDST(ZoneId zoneId, Locale locale) {
    	final DateTimeFormatter zoneAbbreviationFormatter = DateTimeFormatter.ofPattern("zzz", locale);
        return ZonedDateTime.now(zoneId).format(zoneAbbreviationFormatter);
    }

    public static long localTimeFromUnixEpocInDefaultTimeZoneToUTC(long millis) {
        final TimeZone tz = TimeZone.getDefault();
        final Calendar c = Calendar.getInstance(tz);
        long localMillis = millis;
        final int offset;
        int time;
        
        c.set(1970, Calendar.JANUARY, 1, 0, 0, 0);
        
        while (localMillis > Integer.MAX_VALUE) {
            c.add(Calendar.MILLISECOND, Integer.MAX_VALUE);
            localMillis -= Integer.MAX_VALUE;
        }
        
        c.add(Calendar.MILLISECOND, (int) localMillis);
        
        time = c.get(Calendar.MILLISECOND);
        time += c.get(Calendar.SECOND) * 1000;
        time += c.get(Calendar.MINUTE) * 60 * 1000;
        time += c.get(Calendar.HOUR_OF_DAY) * 60 * 60 * 1000;
        
        offset = tz.getOffset(c.get(Calendar.ERA), c.get(Calendar.YEAR), c.get(Calendar.MONTH),
        		c.get(Calendar.DAY_OF_MONTH), c.get(Calendar.DAY_OF_WEEK), time);
        
        return millis - offset;
    }
    
    public static ZonedDateTime getStartOfThisDayZonedDateTime(ZonedDateTime zdt) {
		return ZonedDateTime.of(zdt.getYear(), zdt.getMonthValue(), zdt.getDayOfMonth(), 0, 0, 0, 0, zdt.getZone());
	}
    
    public static ZonedDateTime get24HoursBeforeNowZonedDateTime(ZonedDateTime zdt) {
		return ZonedDateTime.ofInstant(zdt.toInstant().minusSeconds(SECONDS_PER_DAY), zdt.getZone());
	}
    
    public static ZonedDateTime get5MinutesBeforeNowZonedDateTime(ZonedDateTime zdt) {
		return ZonedDateTime.ofInstant(zdt.toInstant().minusSeconds(SECONDS_PER_5_MINUTES), zdt.getZone());
	}
    
    public ZonedDateTime getStartOfThisWeekZonedDateTime(Temporal tmp) {
    	return getStartOfThisWeekZonedDateTime(tmp, locale);
    }
    
	public static ZonedDateTime getStartOfThisWeekZonedDateTime(Temporal tmp, Locale locale) {
		final TemporalField fieldISO = WeekFields.of(locale).dayOfWeek();
		return (ZonedDateTime) tmp.with(fieldISO, 1);
	}
	
	public static ZonedDateTime getStartOfThisMonthZonedDateTime(ZonedDateTime zdt) {
		return ZonedDateTime.of(zdt.getYear(), zdt.getMonthValue(), 1, 0, 0, 0, 0, zdt.getZone());
	}
	
	public static ZonedDateTime getStartOfThisYearZonedDateTime(ZonedDateTime zdt) {
		return ZonedDateTime.of(zdt.getYear(), 1, 1, 0, 0, 0, 0, zdt.getZone());
	}
	
	public static int getMinuteOfDay(ZonedDateTime zdt) {
		return (zdt.getHour() * 60) + zdt.getMinute();
	}
	
	public static int getMinuteOfDay(LocalDateTime ldt) {
		return (ldt.getHour() * 60) + ldt.getMinute();
	}

	public static String toMinuteSecondFormat(long seconds) {
		final int minute = (int) (seconds / 60);
		final int second = (int) (seconds - (minute * 60)) ;
		return "%03d:%02d".formatted(minute, second);
	}
	
	public String getFormattedDateTimeGroup(ZonedDateTime zdt) {
		return getFormattedDateTimeGroup(zdt, locale);
	}
	
	public static String getFormattedDateTimeGroup(ZonedDateTime zdt, Locale locale) {
		try {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", locale);
			return dtf.format(zdt) + " " + ConsolidatedTime.getZoneIdDisplayShortWithDST(zdt.getZone(), locale);
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
		}
		return "ERROR";
	}
	
	public String getFormattedDate(ZonedDateTime zdt) {
		return getFormattedDate(zdt, locale);
	}
	
	public static String getFormattedDate(ZonedDateTime zdt, Locale locale) {
		try {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd", locale);
			return dtf.format(zdt);
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
		}
		return "ERROR";	
	}

	public static String getFormattedTime(ZonedDateTime zdt, Locale locale) {
		try {
			final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm z", locale);
			return dtf.format(zdt);
		} catch (NullPointerException ex) {
			LOG.log(Level.WARNING, ex.getLocalizedMessage(), ex);
		}
		return "ERROR";	
	}
	
	public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }
	
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }
    
    @Override
    public void close() {
    	stopAutomaticNetworkTimeUpdates();
    	stopClock();
    	for (PropertyChangeListener pcl : pcs.getPropertyChangeListeners()) {
			pcs.removePropertyChangeListener(pcl);
		}
    	savePreferences();
    }

}

