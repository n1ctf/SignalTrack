import java.awt.EventQueue;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import baeldung.FifoFixedSizeQueue;
import meteorology.MeasurementDataGroup;

public class RainRateTest {

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private FifoFixedSizeQueue<Long> periodMeasurementQueue = new FifoFixedSizeQueue<>(8); // Use 8 measurements to calculate the report rate
	private FifoFixedSizeQueue<MeasurementDataGroup> rainfallQueue = new FifoFixedSizeQueue<>(1440); // 24 hours of measurements every minute
	
	public RainRateTest() {
		scheduler.scheduleAtFixedRate(this::clockUpdate, 0, 500, TimeUnit.MILLISECONDS);
	}
	
	private void clockUpdate() {
		updateRainfallQueue(3);
		updatePeriodMeasurementQueue(System.currentTimeMillis());
		System.out.println("getRainfallMillimetersLast24Hours(): " + getRainfallMillimetersLast24Hours());
		System.out.println("---");
	}
	
	// This method is updated every time a reading is transmitted from the sensor.
	// The GW2000B/WS-90 sensor reports the instantaneous rain rate in millimeters per hour. It does not have an accumulator 
	// bucket, and thus can only estimate the rain rate, by checking periodically for rain on the sensor.
	// The sensor sample rate used by the WS90 calculator is not published.
	// However, we require an actual rain amount that has occurred during each measurement period.
	// This method converts the incoming report in millimeters/hour to millimeters of rain during the average period length
	// and pushes the value to the rainfall queue for later analysis.
	private void updateRainfallQueue(double rainRateMillimetersPerHour) {
		// An average report period is calculated as the data is received:
		double reportPeriodMilliseconds = getAverageMeasurementReportPeriodMillis();
		System.out.println("AverageReportPeriodMillis: " + reportPeriodMilliseconds);
		// The report period is calculated:
		double rainRateMillimetersPerMillisecond = rainRateMillimetersPerHour / 3600000; // 3600000 == number of milliseconds per hour.
		System.out.println("RainRateMM/Sec: " + rainRateMillimetersPerMillisecond);
		if (reportPeriodMilliseconds > 0) {
			double rainfallMillimetersThisPeriod = rainRateMillimetersPerMillisecond * reportPeriodMilliseconds;
			System.out.println("Offered to queue: rainfallMillimetersThisPeriod " + rainfallMillimetersThisPeriod);
			rainfallQueue.offer(new MeasurementDataGroup(rainfallMillimetersThisPeriod, ZonedDateTime.now()));
		}
	}
	
	private void updatePeriodMeasurementQueue(long millis) {
		System.out.println("Period Measurement Queue Size: " + periodMeasurementQueue.size());
		periodMeasurementQueue.offer(millis);
	}
	
	private synchronized long getAverageMeasurementReportPeriodMillis() {
		int n = periodMeasurementQueue.size();
		Long[] millis = periodMeasurementQueue.toArray(new Long[n]);
		long[] period = new long[n];
		if (n > 1) {
			long total = 0;
			for (int i = 0; i < n - 1; i++) {
				long timeOfReport = millis[i + 1];
				long timeOfReportPrevious = millis[i];
				period[i] =  timeOfReport - timeOfReportPrevious;
				total += period[i];
			}
			return total / (n - 1);
		}
		return 0;
	}
	
	private double getRainfallMillimetersLast24Hours() {
		
		int n = findIndexAtZdtMinusMinutes(rainfallQueue, 1);
		System.out.println("index at ZdtMinusMinutes: " + n);
		System.out.println("Rainfall Queue Size: " + rainfallQueue.size());
		
		if (rainfallQueue.size() > 0) {
			MeasurementDataGroup[] mdg = rainfallQueue.toArray(new MeasurementDataGroup[rainfallQueue.size()]);
			double total = 0;
			for (int i = n; i < mdg.length; i++) {
				total += mdg[i].getMagnitude();
			}
			return total;
		}
		return 0;
	}
	
	// This method searches the entire measurement set and finds the first measurement that is so many 'minutes'
	// back in time. This type of search is necessary because we can not be sure exactly when this measurement was reported.
	private int findIndexAtZdtMinusMinutes(FifoFixedSizeQueue<MeasurementDataGroup> queue, long minutes) {
		int n = 0;
		if (!queue.isEmpty()) {
			
			MeasurementDataGroup[] mdg = queue.toArray(new MeasurementDataGroup[queue.size()]);

			ZonedDateTime firstEntry = mdg[0].getZdt();
			ZonedDateTime latestEntry = mdg[mdg.length-1].getZdt();
			
			// Check to make sure the queue is long enough to find an entry.
			if (latestEntry.minusMinutes(minutes).isAfter(firstEntry)) {
				
				System.out.println("Actual Time Limits: " + firstEntry + " " + latestEntry);
				System.out.println("Time to find: " + latestEntry.minusMinutes(minutes));
			
				// Work backwards from end of the FIFO queue, looking back 'minutes' to find the index of the specified record. 
				for (int i = mdg.length-1; i >= 0; i--) {
					if (mdg[i].getZdt().plusMinutes(minutes).isBefore(latestEntry)) {
						n = i;
						break;
					}
				}
			}
		}
		return n;
	}
	
	public static void main(final String[] args) {
		EventQueue.invokeLater(() -> new RainRateTest());
	}
}
