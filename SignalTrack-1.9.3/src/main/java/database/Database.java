package database;

import java.awt.EventQueue;
import java.awt.Point;

import java.awt.geom.Point2D;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import java.io.File;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;

import coverage.CoverageTestObject;
import coverage.MeasurementSet;
import coverage.StaticMeasurement;
import coverage.StaticTestObject;
import coverage.TestTile;

import radio.Measurement;

import radiolocation.FlightInformation;

import utility.Utility;

public class Database implements AutoCloseable {

    public static final String APP_DIR_NAME = "signaltrack";

    public static final String STATIC_TABLE = "STATIC_TABLE";
    public static final String FLIGHT_TABLE = "FLIGHT_TABLE";

    public static final String STATIC_MEASUREMENT_RECORD_APPENDED = "STATIC_MEASUREMENT_RECORD_APPENDED";
    public static final String FLIGHT_INFORMATION_RECORD_APPENDED = "FLIGHT_INFORMATION_RECORD_APPENDED";

    public static final String STATIC_MEASUREMENT_RECORD_READY = "STATIC_MEASUREMENT_RECORD_READY";
    public static final String FLIGHT_INFORMATION_RECORD_READY = "FLIGHT_INFORMATION_RECORD_READY";

    public static final String ALL_STATIC_MEASUREMENT_RECORDS_READY = "ALL_STATIC_MEASUREMENT_RECORDS_READY";

    public static final String STATIC_MEASUREMENT_RECORD_COUNT_READY = "STATIC_MEASUREMENT_RECORD_COUNT_READY";
    public static final String FLIGHT_INFORMATION_RECORD_COUNT_READY = "FLIGHT_INFORMATION_RECORD_COUNT_READY";

    public static final String DATABASE_OPEN = "DATABASE_OPEN";
    public static final String DATABASE_CLOSED = "DATABASE_CLOSED";
    public static final String DATABASE_CREATION_ERROR = "DATABASE_CREATION_ERROR";
    public static final String DATABASE_RESTORE_PROGRESS = "DATABASE_RESTORE_PROGRESS";

    public static final String TILE_TABLE = "TILE_TABLE";
    public static final String MEASUREMENT_TABLE = "MEASUREMENT_TABLE";
    public static final String MEASUREMENT_SET_TABLE = "MEASUREMENT_SET_TABLE";

    public static final String TILE_TABLE_APPENDED = "TILE_TABLE_APPENDED";
    public static final String MEASUREMENT_SET_RECORD_APPENDED = "MEASUREMENT_SET_RECORD_APPENDED";
    public static final String MEASUREMENT_RECORD_APPENDED = "MEASUREMENT_RECORD_APPENDED";

    public static final String TILE_RECORD_READY = "TILE_RECORD_READY";
    public static final String MEASUREMENT_SET_RECORD_READY = "MEASUREMENT_SET_RECORD_READY";
    public static final String MEASUREMENT_RECORD_READY = "MEASUREMENT_RECORD_READY";
    public static final String ALL_MEASUREMENT_RECORDS_READY = "ALL_MEASUREMENT_RECORDS_READY";

    public static final String TILE_DELETED = "TILE_DELETED";
    public static final String TILE_NOT_FOUND = "TILE_NOT_FOUND";

    public static final String TILE_COUNT_READY = "TILE_COUNT_READY";
    public static final String ROW_COUNT_READY = "ROW_COUNT_READY";

    public static final String TILE_COMPLETE_RECORD_COUNT_READY = "TILE_COMPLETE_RECORD_COUNT_READY";
    public static final String TILE_NOT_ACCESSABLE_RECORD_COUNT_READY = "TILE_NOT_ACCESSABLE_RECORD_COUNT_READY";
    public static final String MEASUREMENT_SET_RECORD_COUNT_READY = "MEASUREMENT_SET_RECORD_COUNT_READY";
    public static final String MEASUREMENT_RECORD_COUNT_READY = "MEASUREMENT_RECORD_COUNT_READY";

    private static final Logger LOG = Logger.getLogger(Database.class.toString());

    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    
    private CoverageTestObject testSettings;
	
    private final List<TestTile> tileRecordList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private final List<StaticMeasurement> staticRecordList = Collections.synchronizedList(new CopyOnWriteArrayList<>());
    private final List<FlightInformation> flightRecordList = Collections.synchronizedList(new CopyOnWriteArrayList<>());

    private Integer staticRecordCount = 0;
    private Integer measurementSetRecordCount = 0;
    private Integer measurementRecordCount = 0;
    private Integer tileRecordCount = 0;

    private boolean allStaticRecordsRetrieved;
    private boolean allMeasurementRecordsRetrieved;

    private final BasicDataSource dataSource = new BasicDataSource();

    private final ExecutorService executor = Executors.newCachedThreadPool();

    private DatabaseConfig config;

    public Database(DatabaseConfig config) {
        registerShutdownHook();
        this.config = config;
    }

    public void openDatabase( File databaseFile, CoverageTestObject coverageTestSettings, StaticTestObject staticTestSettings) {
        executor.execute(new OpenDatabase(databaseFile, config.getUserName(), config.getPassword(), coverageTestSettings, staticTestSettings));
    }

    private final class OpenDatabase implements Runnable {

        private  final CoverageTestObject coverageTestSettings;
        private  final StaticTestObject staticTestSettings;

        private OpenDatabase( File databaseFile,  String username,  String password,
                CoverageTestObject coverageTestSettings, StaticTestObject staticTestSettings) {
            this.coverageTestSettings = coverageTestSettings;
            this.staticTestSettings = staticTestSettings;
            dataSource.setDriverClassName(config.getDatasourceClassName());
            dataSource.setUsername(username);
            dataSource.setPassword(password);
            config.setDatabaseFile(databaseFile);
            dataSource.setUrl(config.getDatasourceURL());
        }

        @Override
        public synchronized void run() {
            try (Connection conn = dataSource.getConnection()) {
            	
                allMeasurementRecordsRetrieved = false;
                allStaticRecordsRetrieved = false;
                
                conn.setAutoCommit(true);

                final List<String> tableList = listTables(conn);

                if (!tableList.contains(TILE_TABLE)) {
                    try (PreparedStatement preparedStatement = conn.prepareStatement(getTileTableDef())) {
                        preparedStatement.executeUpdate();
                    } catch (NullPointerException ex) {
                        LOG.log(Level.WARNING, "NullPointerException", ex);
                    }
                }

                if (!tableList.contains(MEASUREMENT_SET_TABLE)) {
                    try (PreparedStatement preparedStatement = conn.prepareStatement(getMeasurementSetTableDef())) {
                        preparedStatement.executeUpdate();
                    } catch (NullPointerException ex) {
                        LOG.log(Level.WARNING, "NullPointerException", ex);
                    }
                }

                if (!tableList.contains(MEASUREMENT_TABLE)) {
                    try ( PreparedStatement preparedStatement = conn.prepareStatement(getMeasurementTableDef())) {
                        preparedStatement.executeUpdate();
                    } catch (NullPointerException ex) {
                        LOG.log(Level.WARNING, "NullPointerException", ex);
                    }
                }

                if (!tableList.contains(FLIGHT_TABLE)) {
                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = conn.prepareStatement(getFlightTableDef());
                        preparedStatement.executeUpdate();
                    } catch (NullPointerException ex) {
                        LOG.log(Level.WARNING, "NullPointerException", ex);
                    } finally {
                        if (preparedStatement != null) {
                            preparedStatement.close();
                        }
                    }
                }

                if (!tableList.contains(STATIC_TABLE)) {
                    PreparedStatement preparedStatement = null;
                    try {
                        preparedStatement = conn.prepareStatement(getStaticTableDef());
                        preparedStatement.executeUpdate();
                    } catch (NullPointerException ex) {
                        LOG.log(Level.WARNING, "NullPointerException", ex);
                    } finally {
                        if (preparedStatement != null) {
                            preparedStatement.close();
                        }
                    }
                }

                executor.execute(new RetrieveAllStaticTestRecords(staticTestSettings));
                executor.execute(new RetrieveAllCoverageTestRecords(coverageTestSettings));

                pcs.firePropertyChange(DATABASE_OPEN, null, true);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            } catch (NullPointerException ex) {
                LOG.log(Level.WARNING, "NullPointerException", ex);
            } catch (Exception ex) {
                LOG.log(Level.WARNING, "Exception", ex);
            }

            notifyAll();
        }

        private List<String> listTables(Connection conn) {
        	final List<String> list = new CopyOnWriteArrayList<>();
            try {
            	final DatabaseMetaData metaData = conn.getMetaData();
	            try (ResultSet resultSet = metaData.getTables(null, null, null, new String[] { "TABLE" })) {
	            	while (resultSet.next()) {
	                    list.add(resultSet.getString("TABLE_NAME"));
	                }
	            } catch (SQLException ex) {
	                LOG.log(Level.WARNING, "SQLException", ex);
	            }
            } catch (SQLException ex) {
            	LOG.log(Level.WARNING, "Exception", ex);
            }
            return list;
        }

        private String getTileTableDef() {
            return "CREATE TABLE " + TILE_TABLE + " ("
                    + "ID INTEGER NOT NULL CONSTRAINT TileTablePrimaryKey PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "TileReference INTEGER NOT NULL UNIQUE, "
                    + "TestName VARCHAR(256) NOT NULL, "
                    + "Easting BIGINT NOT NULL, "
                    + "Northing BIGINT NOT NULL, "
                    + "Zone INTEGER NOT NULL, "
                    + "Longitude DOUBLE NOT NULL, "
                    + "Latitude DOUBLE NOT NULL, "
                    + "Precision INTEGER NOT NULL, "
                    + "LatBand VARCHAR(1) NOT NULL CONSTRAINT CheckLatBand CHECK (LatBand IN ('N','S')), "
                    + "AvgSinad DOUBLE, "
                    + "AvgBer DOUBLE, "
                    + "AvgdBm DOUBLE, "
                    + "TileSizeWidth DOUBLE NOT NULL, "
                    + "TileSizeHeight DOUBLE NOT NULL, "
                    + "MeasurementCount INTEGER NOT NULL, "
                    + "Accessable BOOLEAN, "
                    + "CONSTRAINT UniqueLonLat UNIQUE (Longitude, Latitude), "
                    + "CONSTRAINT UniqueUTM UNIQUE (Easting, Northing, Zone, LatBand) "
                    + ")";
        }

        private String getMeasurementSetTableDef() {
            return "CREATE TABLE " + MEASUREMENT_SET_TABLE + " ("
                    + "ID BIGINT NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "TestTileID INTEGER NOT NULL CONSTRAINT ReferenceTileTableID REFERENCES " + TILE_TABLE + "(ID) ON DELETE CASCADE, "
                    + "Millis BIGINT NOT NULL, "
                    + "Longitude DOUBLE NOT NULL, "
                    + "Latitude DOUBLE NOT NULL, "
                    + "DopplerDirection DOUBLE, "
                    + "DopplerQuality INTEGER, "
                    + "Marker INTEGER NOT NULL"
                    + ")";
        }

        private String getMeasurementTableDef() {
            return "CREATE TABLE " + MEASUREMENT_TABLE + " ("
                    + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "MeasurementSetID BIGINT NOT NULL CONSTRAINT ReferenceMeasurementSetID REFERENCES " + MEASUREMENT_SET_TABLE + "(ID) ON DELETE CASCADE, "
                    + "ChannelNumber INTEGER NOT NULL,"
                    + "BER DOUBLE, "
                    + "dBm DOUBLE, "
                    + "SINAD DOUBLE, "
                    + "Frequency DOUBLE NOT NULL, "
                    + "SELECTED BOOLEAN NOT NULL, "
                    + "PLTone VARCHAR(5) NOT NULL, "
                    + "DPLCode VARCHAR(4) NOT NULL, "
                    + "DPLInverted BOOLEAN NOT NULL, "
                    + "NAC VARCHAR(4) NOT NULL, "
                    + "SquelchMode INTEGER NOT NULL, "
                    + "ModeName VARCHAR(6) NOT NULL, "
                    + "BandwidthHz INTEGER NOT NULL, "
                    + "CONSTRAINT UniqueMeasurementSetIDChannelNumberFrequency UNIQUE (MeasurementSetID, ChannelNumber, Frequency)"
                    + ")";
        }

        private String getStaticTableDef() {
            return "CREATE TABLE " + STATIC_TABLE + " ("
                    + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "TestName VARCHAR(256) NOT NULL, "
                    + "DateTimeGroupTimeStamp BIGINT NOT NULL, "
                    + "FlightLongitude DOUBLE NOT NULL, "
                    + "FlightLatitude DOUBLE NOT NULL, "
                    + "CourseMadeGoodTrue DOUBLE NOT NULL, "
                    + "SpeedMadeGoodKPH DOUBLE NOT NULL, "
                    + "BarometricAltitudeFeet DOUBLE NOT NULL, "
                    + "RadarAltitudeFeet DOUBLE NOT NULL, "
                    + "GPSAltitudeFeet DOUBLE NOT NULL, "
                    + "FrequencyMHz DOUBLE NOT NULL, "
                    + "dBm DOUBLE NOT NULL, "
                    + "ModeName VARCHAR(6) NOT NULL, "
                    + "Flight INTEGER NOT NULL CONSTRAINT ReferenceFlight REFERENCES " + FLIGHT_TABLE + "(Flight) ON DELETE CASCADE, "
                    + "CONSTRAINT SameFlightAtSameLocationAtSameTimeIntegrityCheck UNIQUE (DateTimeGroupTimeStamp, FlightLongitude, FlightLatitude, Flight), "
                    + "CONSTRAINT ZeroOffsetRendevousIntegrityCheck UNIQUE (DateTimeGroupTimeStamp, FlightLongitude, FlightLatitude)"
                    + ")";
        }

        private String getFlightTableDef() {
            return "CREATE TABLE " + FLIGHT_TABLE + " ("
                    + "ID INTEGER NOT NULL PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), "
                    + "Flight INTEGER NOT NULL CONSTRAINT UniqueFlight UNIQUE, "
                    + "Registration VARCHAR(10) NOT NULL, "
                    + "Flag VARCHAR(4) NOT NULL, "
                    + "ObservabilityCrossSectionMillimeters BIGINT NOT NULL, "
                    + "Manufacturer VARCHAR(256) NOT NULL, "
                    + "Model VARCHAR(256) NOT NULL, "
                    + "CeilingFeet INTEGER NOT NULL, "
                    + "ServiceCeilingFeet INTEGER NOT NULL, "
                    + "MaximumSafeAirSpeed INTEGER NOT NULL, "
                    + "MinimumSafeAirSpeed INTEGER NOT NULL, "
                    + "RotaryWingCount INTEGER NOT NULL, "
                    + "PropellerCount INTEGER NOT NULL, "
                    + "RecipEngineCount INTEGER NOT NULL, "
                    + "TurbineEngineCount INTEGER NOT NULL, "
                    + "MaximumLoiterTimeMinutes INTEGER NOT NULL, "
                    + "InitialFuelCapacityPounds INTEGER NOT NULL, "
                    + "CurrentFuelCapacityPounds INTEGER NOT NULL, "
                    + "InFlightEmergency BOOLEAN NOT NULL, "
                    + "XPDRMode1MissionCode INTEGER CONSTRAINT CheckValidMissionCode CHECK (XPDRMode1MissionCode BETWEEN 0 AND 31), "
                    + "XPDRMode2Code INTEGER NOT NULL CONSTRAINT CheckValidMode2Code CHECK (XPDRMode2Code BETWEEN 0 AND 4095), "
                    + "XPDRMode3ACode INTEGER NOT NULL CONSTRAINT CheckValidMode3ACode CHECK (XPDRMode3ACode BETWEEN 0 AND 63), "
                    + "XPDRMode3CCode BIGINT NOT NULL CONSTRAINT CheckValidMode3CCode CHECK (XPDRMode3CCode BETWEEN -1250 AND 126750), "
                    + "XPDRMode4Code BIGINT NOT NULL CONSTRAINT CheckValidMode4Code CHECK (XPDRMode4Code BETWEEN " + getMinLong() + " AND " + getMaxLong() + ")"
                    + ")";
        }
        
        private String getMaxLong() {
        	return String.valueOf((long) (2E63 - 1));
        }
        
        private String getMinLong() {
        	return String.valueOf((long) -2E63);
        }
    }

    public synchronized void deleteAllTestTiles() {
        tileRecordList.forEach(this::deleteTestTile);
    }

    public synchronized void deleteTestTile(TestTile testTile) {
        executor.execute(new DeleteTestTile(testTile));
    }

    private final class DeleteTestTile implements Runnable {

        private final TestTile testTile;

        private DeleteTestTile(TestTile testTile) {
            this.testTile = testTile;
        }

        @Override
        public void run() {
        	final String sql = "DELETE FROM " + TILE_TABLE + " WHERE "
                        + "Easting = ? AND "
                        + "Northing = ? AND "
                        + "Zone = ? AND "
                        + "LatBand = ? ";
        	
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                
                conn.setAutoCommit(true);

                preparedStatement.setLong(1, testTile.getEasting());
                preparedStatement.setLong(2, testTile.getNorthing());
                preparedStatement.setInt(3, testTile.getGridZone());
                preparedStatement.setString(4, testTile.getLatBand());

                preparedStatement.executeUpdate();

                tileRecordList.remove(tileRecordList.indexOf(testTile));

                pcs.firePropertyChange(TILE_DELETED, null, testTile);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            } 
        }
    }

    public void getTileMeasurementSetCount(JLabel label, TestTile testTile) {
    	final SwingWorker<TestTile, Void> worker = new SwingWorker<TestTile, Void>() {

            @Override
            protected TestTile doInBackground() throws Exception {
                return getTileRecordListElement(testTile);
            }

            @Override
            protected void done() {
                TestTile tt = null;
                try {
                    tt = get();
                } catch (InterruptedException ex) {
                    LOG.log(Level.WARNING, "InterruptedException", ex);
                    Thread.currentThread().interrupt();
                } catch (ExecutionException ex) {
                    LOG.log(Level.WARNING, "ExecutionException", ex);
                }
                if (tt == null) {
                    label.setText("0");
                } else {
                    label.setText("%5d".formatted(tt.getMeasurementCount()));
                }
            }

        };

        worker.execute();
    }

    public DatabaseConfig getConfig() {
        return config;
    }

    public List<TestTile> getTileRecordList() {
        return Collections.unmodifiableList(tileRecordList);
    }

    public boolean isTileCreated(TestTile testTile) {
        if (tileRecordList == null) {
            return false;
        }
        final TestTile tt = getTestTileAt(testTile.getLonLat());
        return tt != null;
    }

    public TestTile getTileRecordListElement(TestTile testTile) {
        if (tileRecordList == null) {
            return null;
        }
        return getTestTileAt(testTile.getLonLat());
    }

    public synchronized TestTile getTestTileAt(Point2D lonlat) {
        if (tileRecordList == null || lonlat == null) {
            return null;
        }
        final Iterator<TestTile> iter = tileRecordList.iterator();
        while (iter.hasNext()) {
        	final TestTile testTile = iter.next();
            if (getPointPrecision(testTile.getLonLat(), 4).equals(getPointPrecision(lonlat, 4))) {
                return testTile;
            }
        }
        return null;
    }

    private synchronized Point2D getPointPrecision(Point2D point, Integer precision) {
    	final double x = Utility.round(point.getX(), precision);
    	final double y = Utility.round(point.getY(), precision);
        return new Point.Double(x, y);
    }

    private final class RetrieveAllCoverageTestRecords implements Runnable {

    	private final CoverageTestObject cto;

        private RetrieveAllCoverageTestRecords(CoverageTestObject cto) {
            this.cto = cto;
        }

        @Override
        public void run() {
        	final String sql = "SELECT * FROM " + TILE_TABLE + " WHERE " + TILE_TABLE + ".TestName = ?";
            
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                
                conn.setAutoCommit(true);
                
                tileRecordCount = requestAndWaitForRowCount(conn, TILE_TABLE);
                pcs.firePropertyChange(TILE_COUNT_READY, null, tileRecordCount);

                measurementRecordCount = requestAndWaitForRowCount(conn, MEASUREMENT_TABLE);
                pcs.firePropertyChange(MEASUREMENT_RECORD_COUNT_READY, null, measurementRecordCount);

                measurementSetRecordCount = requestAndWaitForRowCount(conn, MEASUREMENT_SET_TABLE);
                pcs.firePropertyChange(MEASUREMENT_SET_RECORD_COUNT_READY, null, measurementSetRecordCount);
                
                preparedStatement.setString(1, cto.getTestName());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                	final TestTile testTile = TestTile.toTestTile(getRecordData(resultSet));
	                    pcs.firePropertyChange(TILE_RECORD_READY, null, testTile);
	                    tileRecordList.add(testTile);
	                    requestAllMeasurementSetRecords(testTile);
	                }
                }
                
                allMeasurementRecordsRetrieved = true;

                pcs.firePropertyChange(ALL_MEASUREMENT_RECORDS_READY, null, tileRecordList);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            } catch (NullPointerException ex) {
                LOG.log(Level.WARNING, "NullPointerException", ex);
                invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(null,
                        "Error processing database..."
                        + System.lineSeparator() + "  NULL POINTER EXCEPTION",
                        "Database Error", JOptionPane.INFORMATION_MESSAGE));
            } 
        }

        private void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
            if (EventQueue.isDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        }
    }

    public void requestAllStaticTestRecords(StaticTestObject testSettings) {
        executor.execute(new RetrieveAllFlightRecords());
        executor.execute(new RetrieveAllStaticTestRecords(testSettings));
    }

    private final class RetrieveAllStaticTestRecords implements Runnable {

    	final private StaticTestObject sto;

        private RetrieveAllStaticTestRecords(StaticTestObject sto) {
            this.sto = sto;
        }

        @Override
        public void run() {
        	final String sql = "SELECT * FROM " + STATIC_TABLE + " WHERE TestName = ?";
            
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                
                conn.setAutoCommit(true);
                
                staticRecordCount = requestAndWaitForRowCount(conn, STATIC_TABLE);
                pcs.firePropertyChange(STATIC_MEASUREMENT_RECORD_COUNT_READY, null, staticRecordCount);
 
                preparedStatement.setString(1, sto.getTestFile().getName());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                	final StaticMeasurement staticData = StaticMeasurement.objectArrayToStaticMeasurement(
	                            getRecordData(resultSet));
	                    pcs.firePropertyChange(STATIC_MEASUREMENT_RECORD_READY, null, staticData);
	                    staticRecordList.add(staticData);
	                }
                }

                allStaticRecordsRetrieved = true;

                pcs.firePropertyChange(ALL_STATIC_MEASUREMENT_RECORDS_READY, null, staticRecordList);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            } catch (NullPointerException ex) {
                LOG.log(Level.WARNING, "NullPointerException", ex);
                invokeLaterInDispatchThreadIfNeeded(() -> JOptionPane.showMessageDialog(null, "Error processing database..."
                        + System.lineSeparator() + "  NULL POINTER EXCEPTION", "Database Error", JOptionPane.INFORMATION_MESSAGE));
            }
        }

        private void invokeLaterInDispatchThreadIfNeeded(Runnable runnable) {
            if (EventQueue.isDispatchThread()) {
                runnable.run();
            } else {
                SwingUtilities.invokeLater(runnable);
            }
        }
    }

    public void requestAllFlightRecords() {
        executor.execute(new RetrieveAllFlightRecords());
    }

    private final class RetrieveAllFlightRecords implements Runnable {

        @Override
        public void run() {
        	final String sql = "SELECT * FROM " + FLIGHT_TABLE;
        	
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                
                conn.setAutoCommit(true);

                final int count = requestAndWaitForRowCount(conn, FLIGHT_TABLE);
                
                pcs.firePropertyChange(FLIGHT_INFORMATION_RECORD_COUNT_READY, null, count);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                	final FlightInformation flightData = FlightInformation.objectArrayToFlightInformation(
	                            getRecordData(resultSet));
	                    pcs.firePropertyChange(FLIGHT_INFORMATION_RECORD_READY, null, flightData);
	                    flightRecordList.add(flightData);
	                }
                }

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public void appendStaticRecord(StaticMeasurement staticMeasurement) {
        executor.execute(new AppendStaticRecord(staticMeasurement));
    }

    private final class AppendStaticRecord implements Runnable {

        private final StaticMeasurement staticMeasurement;

        private AppendStaticRecord(StaticMeasurement staticMeasurement) {
            this.staticMeasurement = staticMeasurement;
        }

        @Override
        public void run() {
        	final String values = "?,?,?,?,?,?,?,?,?,?,?,?,?";

        	final String insert = "INSERT INTO " + STATIC_TABLE + " ("
                    + "TestName, "
                    + "DateTimeGroupTimeStamp, "
                    + "FlightLongitude, "
                    + "FlightLatitude, "
                    + "CourseMadeGoodTrue, "
                    + "SpeedMadeGoodKPH, "
                    + "BarometricAltitudeFeet, "
                    + "RadarAltitudeFeet, "
                    + "GPSAltitudeFeet, "
                    + "FrequencyMHz, "
                    + "dBm, "
                    + "ModeName, "
                    + "Flight) "
                    + "VALUES (" + values + ")";

            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                
                conn.setAutoCommit(false);

                preparedStatement.clearParameters();

                preparedStatement.setString(1, staticMeasurement.getTestName());
                preparedStatement.setLong(2, staticMeasurement.getTimeStamp());
                preparedStatement.setDouble(3, staticMeasurement.getPoint().getX());
                preparedStatement.setDouble(4, staticMeasurement.getPoint().getY());
                preparedStatement.setDouble(5, staticMeasurement.getCourseMadeGoodTrue());
                preparedStatement.setDouble(6, staticMeasurement.getSpeedMadeGoodKPH());
                preparedStatement.setDouble(7, staticMeasurement.getBarometricAltitudeFeet());
                preparedStatement.setDouble(8, staticMeasurement.getRadarAltitudeFeet());
                preparedStatement.setDouble(9, staticMeasurement.getGpsAltitudeFeet());
                preparedStatement.setDouble(10, staticMeasurement.getFrequencyMHz());
                preparedStatement.setDouble(11, staticMeasurement.getdBm());
                preparedStatement.setString(12, staticMeasurement.getModeName().name());
                preparedStatement.setInt(13, staticMeasurement.getFlight());

                preparedStatement.executeUpdate();

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
	                if (staticMeasurement.getID() == 0) {
	                    while (resultSet.next()) {
	                        staticMeasurement.setID(resultSet.getInt(1));
	                    }
	                }
                }

                staticRecordList.add(staticMeasurement);

                conn.commit();

                pcs.firePropertyChange(STATIC_MEASUREMENT_RECORD_APPENDED, null, staticMeasurement);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public void appendFlightRecord(FlightInformation flightInformation) {
        executor.execute(new AppendFlightRecord(flightInformation));
    }

    private final class AppendFlightRecord implements Runnable {

        private final FlightInformation fi;
        
        private AppendFlightRecord(FlightInformation fi) {
            this.fi = fi;
        }

        @Override
        public void run() {
        	final String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";

        	final String insert = "INSERT INTO " + FLIGHT_TABLE + " ("
                    + "Flight, "
                    + "Registration, "
                    + "Flag, "
                    + "ObservabilityCrossSectionMillimeters, "
                    + "Manufacturer, "
                    + "Model, "
                    + "CeilingFeet, "
                    + "ServiceCeilingFeet, "
                    + "MaximumSafeAirSpeed, "
                    + "MinimumSafeAirSpeed, "
                    + "RotaryWingCount, "
                    + "PropellerCount, "
                    + "RecipEngineCount, "
                    + "TurbineEngineCount, "
                    + "MaximumLoiterTimeMinutes, "
                    + "InitialFuelCapacityPounds, "
                    + "CurrentFuelCapacityPounds, "
                    + "InFlightEmergency, "
                    + "XPDRMode1MissionCode, "
                    + "XPDRMode2Code, "
                    + "XPDRMode3ACode, "
                    + "XPDRMode3CCode, "
                    + "XPDRMode4Code) "
                    + "VALUES (" + values + ")";
            
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                
                conn.setAutoCommit(true);

                preparedStatement.clearParameters();

                preparedStatement.setInt(1, fi.getFlight());
                preparedStatement.setString(2, fi.getRegistration());
                preparedStatement.setString(3, fi.getFlag());
                preparedStatement.setLong(4, fi.getObservabilityCrossSectionMillimeters());
                preparedStatement.setString(5, fi.getManufacturer());
                preparedStatement.setString(6, fi.getModel());
                preparedStatement.setInt(7, fi.getCeilingFeet());
                preparedStatement.setInt(8, fi.getServiceCeilingFeet());
                preparedStatement.setInt(9, fi.getMaximumSafeAirSpeed());
                preparedStatement.setInt(10, fi.getMinimumSafeAirSpeed());
                preparedStatement.setInt(11, fi.getRotaryWingCount());
                preparedStatement.setInt(12, fi.getPropellerCount());
                preparedStatement.setInt(13, fi.getRecipEngineCount());
                preparedStatement.setInt(14, fi.getTurbineEngineCount());
                preparedStatement.setInt(15, fi.getMaximumLoiterTimeMinutes());
                preparedStatement.setInt(16, fi.getInitialFuelCapacityPounds());
                preparedStatement.setInt(17, fi.getCurrentFuelCapacityPounds());
                preparedStatement.setBoolean(18, fi.isInFlightEmergency());
                preparedStatement.setInt(19, fi.getXPDRMode1MissionCode());
                preparedStatement.setInt(20, fi.getXPDRMode2Code());
                preparedStatement.setInt(21, fi.getXPDRMode3ACode());
                preparedStatement.setLong(22, fi.getXPDRMode3ACode());
                preparedStatement.setLong(23, fi.getXPDRMode4Code());

                preparedStatement.executeUpdate();

                try(ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
	                if (fi.getID() == 0) {
	                    while (resultSet.next()) {
	                        fi.setID(resultSet.getInt(1));
	                    }
	                }
                }
                
                flightRecordList.add(fi);

                pcs.firePropertyChange(FLIGHT_INFORMATION_RECORD_APPENDED, null, fi);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public List<StaticMeasurement> getStaticRecordList() {
        return List.copyOf(staticRecordList);
    }

    public List<FlightInformation> getFlightRecordList() {
        return List.copyOf(flightRecordList);
    }

    public void updateTileRecord(TestTile testTile) {
        executor.execute(new UpdateTileRecord(testTile));
    }

    private final class UpdateTileRecord implements Runnable {

        private final TestTile testTile;

        private UpdateTileRecord(TestTile testTile) {
            this.testTile = testTile;
        }

        @Override
        public void run() {
        	final String sql = "UPDATE " + TILE_TABLE + " "
                    + "SET TileReference = ?, "
                    + "TestName = ?, "
                    + "Easting = ?, "
                    + "Northing = ?, "
                    + "Zone = ?, "
                    + "Longitude = ?, "
                    + "Latitude = ?, "
                    + "Precision = ?, "
                    + "LatBand = ?, "
                    + "AvgSinad = ?, "
                    + "AvgBer = ?, "
                    + "AvgdBm = ?, "
                    + "TileSizeWidth = ?, "
                    + "TileSizeHeight = ?, "
                    + "MeasurementCount = ?, "
                    + "Accessable = ? "
                    + "WHERE ID = ?";
            
        	try (Connection conn = dataSource.getConnection();
        		PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

                preparedStatement.clearParameters();

                preparedStatement.setInt(1, testTile.getTileReference());
                preparedStatement.setString(2, testTile.getTestName());
                preparedStatement.setLong(3, testTile.getEasting());
                preparedStatement.setLong(4, testTile.getNorthing());
                preparedStatement.setInt(5, testTile.getGridZone());
                preparedStatement.setDouble(6, testTile.getLonLat().getX());
                preparedStatement.setDouble(7, testTile.getLonLat().getY());
                preparedStatement.setInt(8, testTile.getPrecision().ordinal());
                preparedStatement.setString(9, testTile.getLatBand());
                preparedStatement.setDouble(10, testTile.getAvgSinad());
                preparedStatement.setDouble(11, testTile.getAvgBer());
                preparedStatement.setDouble(12, testTile.getAvgdBm());
                preparedStatement.setDouble(13, testTile.getTileSize().getX());
                preparedStatement.setDouble(14, testTile.getTileSize().getY());
                preparedStatement.setInt(15, testTile.getMeasurementCount());
                preparedStatement.setBoolean(16, testTile.isAccessable());
                preparedStatement.setInt(17, testTile.getID());
                
                preparedStatement.executeUpdate();

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public void requestAllMeasurementSetRecords(TestTile testTile) {
        executor.execute(new RetrieveAllMeasurementSetRecords(testTile));
    }

    private final class RetrieveAllMeasurementSetRecords implements Runnable {

        private final TestTile testTile;

        private RetrieveAllMeasurementSetRecords(TestTile testTile) {
            this.testTile = testTile;
        }

        @Override
        public void run() {
 
        	final String sql = "SELECT * FROM " + MEASUREMENT_SET_TABLE + " "
                        + "INNER JOIN " + TILE_TABLE + " ON " + TILE_TABLE + ".ID = "
                        + MEASUREMENT_SET_TABLE + ".TestTileID "
                        + "WHERE " + MEASUREMENT_SET_TABLE + ".TestTileID = ?";

            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                
                conn.setAutoCommit(true);

                preparedStatement.setInt(1, testTile.getID());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                	final MeasurementSet measurementSet = MeasurementSet.toMeasurementSet(getRecordData(resultSet));
	                    pcs.firePropertyChange(MEASUREMENT_SET_RECORD_READY, null, measurementSet);
	
	                    requestAllMeasurementRecords(measurementSet);
	                }
                }

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public Integer getMeasurementSetCount() {
        return measurementSetRecordCount;
    }

    public void requestAllMeasurementRecords(MeasurementSet measurementSet) {
        executor.execute(new RetrieveAllMeasurementRecords(measurementSet));
    }

    private final class RetrieveAllMeasurementRecords implements Runnable {

        private final MeasurementSet measurementSet;

        private RetrieveAllMeasurementRecords(MeasurementSet measurementSet) {
            this.measurementSet = measurementSet;
        }

        @Override
        public void run() {
        	final String sql = "SELECT * FROM " + MEASUREMENT_TABLE + " "
                        + "INNER JOIN " + MEASUREMENT_SET_TABLE + " "
                        + "ON " + MEASUREMENT_SET_TABLE + ".ID = " + MEASUREMENT_TABLE + ".MeasurementSetID "
                        + "WHERE " + MEASUREMENT_TABLE + ".ID = ?";
        	
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE)) {
                
                conn.setAutoCommit(true);

                preparedStatement.setLong(1, measurementSet.getId());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                	final Measurement measurement = Measurement.toMeasurement(getRecordData(resultSet));
	                    pcs.firePropertyChange(MEASUREMENT_RECORD_READY, null, measurement);
	                }
                }

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public synchronized void requestTestTile(TestTile testTile) {
    	final String testName = testTile.getTestName();
    	final long easting = testTile.getEasting();
    	final long northing = testTile.getNorthing();
    	final int gridZone = testTile.getGridZone();
    	final String latBand = testTile.getLatBand();
    	final Point2D lonlat = testTile.getLonLat();

    	final Iterator<TestTile> iter = tileRecordList.iterator();

        while (iter.hasNext()) {
        	final TestTile tt = iter.next();
            if ((tt.getTestName().equals(testName) && (tt.getEasting() == easting) && (tt.getNorthing() == northing)
                    && (tt.getGridZone() == gridZone) && tt.getLatBand().equals(latBand))
                    || getPointPrecision(tt.getLonLat(), 4).equals(getPointPrecision(lonlat, 4))) {
                pcs.firePropertyChange(TILE_RECORD_READY, null, tt);
                return;
            }
        }

        if (lonlat != null) {
            executor.execute(new RetrieveTestTileByLonLat(testTile));
        } else {
            executor.execute(new RetrieveTestTileByUTM(testTile));
        }
    }

    private final class RetrieveTestTileByLonLat implements Runnable {

    	final private TestTile testTile;

        private RetrieveTestTileByLonLat(TestTile testTile) {
            this.testTile = testTile;
        }

        @Override
        public void run() {
        	final String sql = "SELECT * FROM " + TILE_TABLE + " "
                        + "WHERE " + TILE_TABLE + ".TestName = ? "
                        + "AND CAST(" + TILE_TABLE + ".Longitude + 0.00005 AS DECIMAL(15,4)) = ? "
                        + "AND CAST(" + TILE_TABLE + ".Latitude + 0.00005 AS DECIMAL(15,4)) = ? ";

            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                
                conn.setAutoCommit(true);

                preparedStatement.setString(1, testSettings.getTestName());
                preparedStatement.setDouble(2, Utility.round(testTile.getLonLat().getX(), 4));
                preparedStatement.setDouble(3, Utility.round(testTile.getLonLat().getY(), 4));

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                if (resultSet.next()) {
	                	final TestTile testTileDataType = TestTile.toTestTile(getRecordData(resultSet));
	                    pcs.firePropertyChange(TILE_RECORD_READY, null, testTileDataType);
	                } else {
	                    pcs.firePropertyChange(TILE_NOT_FOUND, null, testTile);
	                }
                }

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    private final class RetrieveTestTileByUTM implements Runnable {

        private final TestTile testTile;

        private RetrieveTestTileByUTM(TestTile testTile) {
            this.testTile = testTile;
        }

        @Override
        public void run() {
        	final String sql = "SELECT * FROM " + TILE_TABLE + " "
                        + "WHERE " + TILE_TABLE + ".TestName = ? "
                        + "AND " + TILE_TABLE + ".Easting = ? "
                        + "AND " + TILE_TABLE + ".Northing = ? "
                        + "AND " + TILE_TABLE + ".Zone = ? "
                        + "AND " + TILE_TABLE + ".LatBand = ? ";
            
        	try (Connection conn = dataSource.getConnection();
        		PreparedStatement preparedStatement = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                
                conn.setAutoCommit(true);

                preparedStatement.setString(1, testSettings.getTestName());
                preparedStatement.setLong(2, testTile.getEasting());
                preparedStatement.setLong(3, testTile.getNorthing());
                preparedStatement.setInt(4, testTile.getGridZone());
                preparedStatement.setString(5, testTile.getLatBand());

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                if (resultSet.next()) {
	                	final TestTile testTileDataType = TestTile.toTestTile(getRecordData(resultSet));
	                    pcs.firePropertyChange(TILE_RECORD_READY, null, testTileDataType);
	                } else {
	                    pcs.firePropertyChange(TILE_NOT_FOUND, null, testTile);
	                }
                }

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public void requestTileStatistics(CoverageTestObject testSettings) {
        executor.execute(new RetrieveTileStatistics(testSettings));
    }

    private final class RetrieveTileStatistics implements Runnable {

        private final CoverageTestObject testSettings;

        private RetrieveTileStatistics(CoverageTestObject testSettings) {
            this.testSettings = testSettings;
        }

        @Override
        public void run() {
            Integer numberTilesComplete = 0;
            Integer numberTilesNotAccessable = 0;

            final String tileSql = "SELECT * FROM " + TILE_TABLE;
            
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(tileSql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY)) {
                
                conn.setAutoCommit(true);

                try (ResultSet resultSet = preparedStatement.executeQuery()) {
	                while (resultSet.next()) {
	                	final TestTile testTile = TestTile.toTestTile(getRecordData(resultSet));
	                    if (testTile.getMeasurementCount() >= testSettings.getMinSamplesPerTile()) {
	                        numberTilesComplete++;
	                    }
	                    if (!testTile.isAccessable()) {
	                        numberTilesNotAccessable++;
	                    }
	                }
                }

                pcs.firePropertyChange(TILE_COMPLETE_RECORD_COUNT_READY, null, numberTilesComplete);
                pcs.firePropertyChange(TILE_NOT_ACCESSABLE_RECORD_COUNT_READY, null, numberTilesNotAccessable);
                
            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public synchronized void appendTileRecord(TestTile testTile) {
        executor.execute(new AppendTileRecord(testTile));
    }

    private final class AppendTileRecord implements Runnable {

        private final TestTile testTile;

        private AppendTileRecord(TestTile testTile) {
            this.testTile = testTile;
        }

        @Override
        public void run() {
        	final String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?";

        	final String insert = "INSERT INTO " + TILE_TABLE + " ("
                        + "TileReference, "
                        + "TestName, "
                        + "Easting, "
                        + "Northing, "
                        + "Zone, "
                        + "Longitude, "
                        + "Latitude, "
                        + "Precision, "
                        + "LatBand, "
                        + "AvgSinad, "
                        + "AvgBer, "
                        + "AvgdBm, "
                        + "TileSizeWidth, "
                        + "TileSizeHeight, "
                        + "MeasurementCount, "
                        + "Accessable) "
                        + "VALUES (" + values + ")";
        	
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                
                conn.setAutoCommit(true);

                preparedStatement.clearParameters();

                preparedStatement.setInt(1, testTile.getTileReference());
                preparedStatement.setString(2, testTile.getTestName());
                preparedStatement.setLong(3, testTile.getEasting());
                preparedStatement.setLong(4, testTile.getNorthing());
                preparedStatement.setInt(5, testTile.getGridZone());
                preparedStatement.setDouble(6, testTile.getLonLat().getX());
                preparedStatement.setDouble(7, testTile.getLonLat().getY());
                preparedStatement.setInt(8, testTile.getPrecision().ordinal());
                preparedStatement.setString(9, testTile.getLatBand());
                preparedStatement.setDouble(10, testTile.getAvgSinad());
                preparedStatement.setDouble(11, testTile.getAvgBer());
                preparedStatement.setDouble(12, testTile.getAvgdBm());
                preparedStatement.setDouble(13, testTile.getTileSize().getX());
                preparedStatement.setDouble(14, testTile.getTileSize().getY());
                preparedStatement.setInt(15, testTile.getMeasurementCount());
                preparedStatement.setBoolean(16, testTile.isAccessable());

                preparedStatement.executeUpdate();

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
	                while (resultSet.next()) {
	                    testTile.setID(resultSet.getInt(1));
	                }
                }

                tileRecordList.add(testTile);

                pcs.firePropertyChange(TILE_TABLE_APPENDED, null, testTile);
            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public void appendMeasurementSet(TestTile testTile, MeasurementSet measurementSet) {
        executor.execute(new AppendMeasurementSetRecord(testTile, measurementSet));
    }

    private final class AppendMeasurementSetRecord implements Runnable {

        private final TestTile testTile;
        private final MeasurementSet measurementSet;

        private AppendMeasurementSetRecord(TestTile testTile, MeasurementSet measurementSet) {
            this.testTile = testTile;
            this.measurementSet = measurementSet;
        }

        @Override
        public void run() {
        	final String values = "?,?,?,?,?,?,?";
            
        	final String insert = "INSERT INTO " + MEASUREMENT_SET_TABLE + " ("
                        + "TestTileID, "
                        + "Millis, "
                        + "Longitude, "
                        + "Latitude, "
                        + "DopplerDirection, "
                        + "DopplerQuality, "
                        + "Marker) "
                        + "VALUES (" + values + ")";
            
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                            	
            	conn.setAutoCommit(true);

                preparedStatement.clearParameters();

                preparedStatement.setInt(1, testTile.getID());

                measurementSet.setTestTileID(testTile.getID());

                preparedStatement.setLong(2, measurementSet.getMillis());
                preparedStatement.setDouble(3, measurementSet.getPosition().getX());
                preparedStatement.setDouble(4, measurementSet.getPosition().getY());
                preparedStatement.setDouble(5, measurementSet.getDopplerDirection());
                preparedStatement.setInt(6, measurementSet.getDopplerQuality());
                preparedStatement.setInt(7, measurementSet.getMarker());

                preparedStatement.executeUpdate();

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
	                while (resultSet.next()) {
	                    measurementSet.setId(resultSet.getLong(1));
	                }
                }

                measurementSetRecordCount++;

                tileRecordList.get(tileRecordList.indexOf(testTile)).incrementMeasurementCount();

                updateTileRecord(tileRecordList.get(tileRecordList.indexOf(testTile)));

                pcs.firePropertyChange(MEASUREMENT_SET_RECORD_COUNT_READY, null, measurementSetRecordCount);
                pcs.firePropertyChange(MEASUREMENT_SET_RECORD_APPENDED, null, measurementSet);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }

    public synchronized void appendMeasurement(MeasurementSet measurementSet, Measurement measurement) {
        executor.execute(new AppendMeasurementRecord(measurementSet, measurement));
    }

    private final class AppendMeasurementRecord implements Runnable {

        private final MeasurementSet measurementSet;
        private final Measurement measurement;

        private AppendMeasurementRecord(MeasurementSet measurementSet, Measurement measurement) {
            this.measurementSet = measurementSet;
            this.measurement = measurement;
        }

        @Override
        public void run() {
        	final String values = "?,?,?,?,?,?,?,?,?,?,?,?,?,?";

        	final String insert = "INSERT INTO " + MEASUREMENT_TABLE + " ("
                        + "MeasurementSetID, "
                        + "ChannelNumber, "
                        + "BER, "
                        + "DBM, "
                        + "SINAD, "
                        + "FREQUENCY, "
                        + "SELECTED, "
                        + "PLTone, "
                        + "DPLCode, "
                        + "DPLInverted, "
                        + "NAC, "
                        + "SquelchMode, "
                        + "ModeName, "
                        + "BandwidthHz) "
                        + "VALUES (" + values + ")";
                
            try (Connection conn = dataSource.getConnection();
            	PreparedStatement preparedStatement = conn.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                
            	conn.setAutoCommit(true);

                preparedStatement.clearParameters();

                preparedStatement.setLong(1, measurementSet.getId());

                measurement.setMeasurementSetID(measurementSet.getId());

                preparedStatement.setInt(2, measurement.getChannelNumber());

                if (measurement.getBer() == null) {
                    preparedStatement.setDouble(3, 100.0);
                } else {
                    preparedStatement.setDouble(3, measurement.getBer());
                }

                if (measurement.getdBm() == null) {
                    preparedStatement.setDouble(4, -130.0);
                } else {
                    preparedStatement.setDouble(4, measurement.getdBm());
                }

                if (measurement.getSinad() == null) {
                    preparedStatement.setDouble(5, 30.0);
                } else {
                    preparedStatement.setDouble(5, measurement.getSinad());
                }

                preparedStatement.setDouble(6, measurement.getFrequency());

                preparedStatement.setBoolean(7, measurement.getSelected());

                preparedStatement.setString(8, measurement.getPLTone());

                preparedStatement.setString(9, measurement.getDPLCode());

                preparedStatement.setBoolean(10, measurement.isDPLInverted());

                preparedStatement.setString(11, measurement.getNetworkAccessCode());

                preparedStatement.setInt(12, measurement.getSquelchMode().ordinal());

                preparedStatement.setString(13, measurement.getModeName().name());
                
                preparedStatement.setInt(14, measurement.getBandwidthHz());

                preparedStatement.executeUpdate();

                try (ResultSet resultSet = preparedStatement.getGeneratedKeys()) {
	                synchronized (this) {
	                    while (resultSet.next()) {
	                        measurement.setId(resultSet.getInt(1));
	                    }
	                }
                }

                pcs.firePropertyChange(MEASUREMENT_RECORD_APPENDED, null, measurement);

            } catch (SQLException ex) {
                LOG.log(Level.WARNING, "SQLException", ex);
            }
        }
    }
    
	@Override
    public void close() {
        new Thread(() -> {
            String sqlState = null;
            try {
            	dataSource.close();
        		if (executor != null) {
        			try {
        				LOG.log(Level.INFO, "Initializing Database executor Service termination....");
        				executor.shutdown();
        				executor.awaitTermination(20, TimeUnit.SECONDS);
        				LOG.log(Level.INFO, "Database executor Service has gracefully terminated");
        			} catch (InterruptedException e) {
        				LOG.log(Level.SEVERE, "Dtabase executorService has timed out after 20 seconds of waiting to terminate processes.");
        				Thread.currentThread().interrupt();
        			}
        		}
                DriverManager.getConnection(config.getDatasourceURL(), config.getUserName(), config.getPassword()).close();
            } catch (SQLException ex) {
                sqlState = ex.getSQLState();
                if (!"XJ015".equals(ex.getSQLState())) {
                    LOG.log(Level.WARNING, "SQLException", ex);
                }
            } finally {
                int x = 0;
                while (x < 10 && sqlState != null && !"XJ015".equals(sqlState)) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException ex) {
                        LOG.log(Level.WARNING, "InterruptedException", ex);
                        Thread.currentThread().interrupt();
                    } catch (NullPointerException ex) {
                        LOG.log(Level.WARNING, "NullPointerException", ex);
                        break;
                    }
                    x++;
                }
                pcs.firePropertyChange(DATABASE_CLOSED, null, null);
                for (PropertyChangeListener listener : pcs.getPropertyChangeListeners()) {
                    pcs.removePropertyChangeListener(listener);
                }
            }
        }).start();
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        if (!pcs.hasListeners(null)) {
            pcs.addPropertyChangeListener(listener);
        }
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    private static Object[] getRecordData(ResultSet resultSet) {
        try {
        	final int columnCount = resultSet.getMetaData().getColumnCount();
        	final Object[] obj = new Object[columnCount];
            for (int i = 0; i < obj.length; i++) {
                obj[i] = resultSet.getObject(i + 1);
            }
            return obj;
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "SQLException", ex);
        }
        return new Object[0];
    }

    public void requestRowCount(String tableName) {
        executor.execute(new RowCount(tableName));
    }

    public static int requestAndWaitForRowCount(Connection conn, String tableName) {
        int count = 0;

        try (PreparedStatement preparedStatement = conn.prepareStatement("SELECT COUNT(*) AS rowCount FROM " + tableName);
        	ResultSet rs = preparedStatement.executeQuery()) {
            if (rs.next()) {
                count = rs.getInt("rowCount");
            }
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "SQLException", ex);
        }
        return count;
    }

    private final class RowCount implements Runnable {

        private final String tableName;

        private RowCount(String tableName) {
            this.tableName = tableName;
        }

        @Override
        public void run() {
            synchronized (this) {
                try (Connection conn = dataSource.getConnection()) {
                    conn.setAutoCommit(true);
                    final int count = requestAndWaitForRowCount(conn, tableName);
                    pcs.firePropertyChange(ROW_COUNT_READY, null, count);
                } catch (SQLException ex) {
                    LOG.log(Level.WARNING, "SQLException", ex);
                } 
            }
        }
    }

    public boolean allMeasurementRecordsReady() {
        if (measurementRecordCount == 0) {
            return true;
        } else {
            return allMeasurementRecordsRetrieved;
        }
    }

    public boolean allStaticRecordsReady() {
        boolean ready = false;
        if (staticRecordCount == 0) {
            ready = true;
        } else {
            ready = allStaticRecordsRetrieved;
        }
        return ready;
    }

    public static void printDriverStats() throws SQLException {
    	final PoolingDriver driver = (PoolingDriver) DriverManager.getDriver("jdbc:apache:commons:dbcp:");
        try (ObjectPool<? extends Connection> connectionPool = driver.getConnectionPool(APP_DIR_NAME)) {
            LOG.info("NumActive: " + connectionPool.getNumActive());
            LOG.info("NumIdle: " + connectionPool.getNumIdle());
        }
    }

    public static void printResultSetToConsole(ResultSet rs) {
        LOG.info("---------------------------------------------------------");
        LOG.info("Start ResultSet");
        final int columnCount;
        try {
            columnCount = rs.getMetaData().getColumnCount();
            final Object[] obj = new Object[columnCount];
            for (int i = 0; i < obj.length; i++) {
                obj[i] = rs.getObject(i + 1);
                LOG.log(Level.INFO, "{0} = {1}", new Object[] {i, obj[i]});
            }
            LOG.info("End ResultSet");
            LOG.info("---------------------------------------------------------");
        } catch (SQLException ex) {
            LOG.log(Level.WARNING, "SQLException", ex);
        }
    }

    public static void printObjectArrayToConsole(Object[] obj) {
        LOG.info("---------------------------------------------------------");
        LOG.info("Start Object Array");
        for (int i = 0; i < obj.length; i++) {
        	LOG.log(Level.INFO, "{0} = {1}", new Object[] {i, obj[i]});
        }
        LOG.info("End Object Array");
        LOG.info("---------------------------------------------------------");
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                close();
            }
        });
    }
}
