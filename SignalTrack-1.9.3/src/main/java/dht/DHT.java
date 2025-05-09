package dht;

import com.pi4j.util.Console;
import com.pi4j.wiringpi.Gpio;
import com.pi4j.wiringpi.GpioUtil;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 *
 * @author John R. Chartkoff
 */
public class DHT {
	private static final int DEFAULT_DHT_PIN = 3;
	private static final boolean DEFAULT_DEBUG_STATUS = false;
	private static final long START_WAIT = 1000; // milliseconds
	private static final long REQUEST_DWELL = 18; // milliseconds
	private static final long REQUEST_RATE = 2000; // milliseconds
	private static final Logger LOG = Logger.getLogger(DHT.class.getName());

	private final Console console;
	private ScheduledFuture<?> handle;
	private double tempCelsius;
	private double relativeHumidity;

	private boolean debug;
	private int dhtPin;
	private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);

	public enum Event {
		NEW_TEMP, NEW_HUMIDITY
	}

	public DHT() {
		this(DEFAULT_DHT_PIN, DEFAULT_DEBUG_STATUS);
	}

	public DHT(int dhtPin, boolean debug) {
		this.debug = debug;
		this.dhtPin = dhtPin;

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				shutdown();
			}
		});

		// create Pi4J console wrapper/helper
		// (This is a utility class to abstract some of the boilerplate code)
		console = new Console();

		// print program title/header
		console.title("<-- Amateur Radio Station N1CTF -->", "DHT11 Console");

		// allow for user to exit program using CTRL-C
		console.promptForExit();

		// setup wiringPi
		if (Gpio.wiringPiSetup() == -1) {
			console.println(" ==>> GPIO SETUP FAILED");
			return;
		}

		GpioUtil.export(dhtPin, GpioUtil.DIRECTION_OUT);

		start();
	}

	public void readDevice(int pin) {
		int x = 0;
		int lastState = 1;
		int valueRead = 1;
		final int limit = 84;
		final int[] results = new int[limit];

		// set pin low for 18ms to request data
		Gpio.pinMode(pin, Gpio.OUTPUT);
		Gpio.digitalWrite(pin, Gpio.LOW);
		Gpio.delay(REQUEST_DWELL);

		// get ready to receive data back from dht11
		Gpio.pinMode(pin, Gpio.INPUT);
		Gpio.pullUpDnControl(pin, Gpio.PUD_UP); // activate internal pullup

		while (x < limit) {
			int timeout = 0;
			int counter = 2; // offset for time taken to perform read by pi

			while (valueRead == lastState && timeout < 300) {
				Gpio.delayMicroseconds(1);
				valueRead = Gpio.digitalRead(pin);
				counter++;
				timeout++;
			}

			if (timeout < 300) {
				results[x] = counter;
				lastState = valueRead;
			}

			x++;
		}

		// reset our bytes
		final int[] data = { 0, 0, 0, 0, 0 };

		int p;

		for (int i = 4; i < x; i += 2) {
			// shift left so we are ready for next result
			p = ((i - 4) / 2) / 8;
			data[p] = data[p] <<= 1;
			// if more than 30, mark bit as 1
			if (results[i] > 30) {
				data[p] = data[p] |= 1;
			}
		}

		double h = (double) ((data[0] << 8) + data[1]) / 10;

		if (h > 100) {
			h = data[0]; // for DHT11
		}

		double c = (double) (((data[2] & 0x7F) << 8) + data[3]) / 10;

		if (c > 125) {
			c = data[2]; // for DHT11
		}

		if ((data[2] & 0x80) != 0) {
			c = -c;
		}

		if (validData(data)) {
			tempCelsius = c;
			pcs.firePropertyChange(Event.NEW_TEMP.name(), null, c);
			relativeHumidity = h;
			pcs.firePropertyChange(Event.NEW_HUMIDITY.name(), null, h);
			if (debug) {
				console.println("Temp Celsius : " + getTempCelsius());
				console.println("Temp Fahrenheit : " + getTempFahrenheit());
				console.println("Relative Humidity % : " + getRelativeHumidity());
				console.println("Temp Celsius : " + getTempCelsius());
				console.println("Dew Point Celsius : " + getDewPointCelsius());
				console.println("Dew Point Fahrenheit : " + getDewPointFahrenheit());
			}
		} else {
			console.println("checksum error");
		}
	}

	private boolean validData(int[] data) {
		return data[4] == ((data[0] + data[1] + data[2] + data[3]) & 0xff);
	}

	public void stop() {
		handle.cancel(debug);
	}

	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new RunnableImpl(args));
	}

	private void shutdown() {
		if (handle != null) {
			handle.cancel(true);
		}
		if (console != null) {
			console.clearScreen();
		}
	}

	public double getTempCelsius() {
		return tempCelsius;
	}

	public double getTempFahrenheit() {
		return celsiusToFahrenheit(getTempCelsius());
	}

	public double getRelativeHumidity() {
		return relativeHumidity;
	}

	public double getDewPointCelsius() {
		final double h = (Math.log10(getRelativeHumidity()) - 2) / 0.4343
				+ (17.62 * getTempCelsius()) / (243.12 + getTempCelsius());
		return 243.12 * h / (17.62 - h);
	}

	public double getDewPointFahrenheit() {
		return celsiusToFahrenheit(getDewPointCelsius());
	}

	public static double celsiusToFahrenheit(double celsius) {
		return (celsius * 9D / 5D) + 32D;
	}

	public static double fahrenheitToCelsius(double fahrenheit) {
		return (fahrenheit - 32D) * 5D / 9D;
	}

	private static class RunnableImpl implements Runnable {

		private final String[] args;

		public RunnableImpl(String[] args) {
			this.args = args.clone();
		}

		@Override
		public void run() {
			boolean d = false;
			int dhtPin = DEFAULT_DHT_PIN;
			try {
				final Options options = new Options();

				options.addOption(new Option("e", "GPIO Bus DHT11 Line"));
				options.addOption(new Option("d", true, "debug"));

				final CommandLineParser parser = new DefaultParser();

				final CommandLine cmd = parser.parse(options, args);

				if (cmd.hasOption("e")) {
					dhtPin = Integer.parseInt(cmd.getOptionValue("e"));
				}
				if (cmd.hasOption("d")) {
					d = true;
				}
			} catch (final ParseException ex) {
				LOG.log(Level.SEVERE, null, ex);
			}

			new DHT(dhtPin, d);
		}
	}

	public PropertyChangeSupport getPropertyChangeSupport() {
		return pcs;
	}

	public void addPropertyChangeListener(PropertyChangeListener listener) {
		pcs.addPropertyChangeListener(listener);
	}

	public void removePropertyChangeListener(PropertyChangeListener listener) {
		pcs.removePropertyChangeListener(listener);
	}

	public PropertyChangeListener[] getPropertyChangeListeners() {
		return pcs.getPropertyChangeListeners();
	}

	private void start() {
		try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
			handle = scheduler.scheduleAtFixedRate(new MeasurementRequest(), START_WAIT, REQUEST_RATE, TimeUnit.MILLISECONDS);
		}
	}

	private class MeasurementRequest implements Runnable {
		@Override
		public void run() {
			readDevice(dhtPin);
		}
	}
}
