package gps;

/**
 *
 * @author John
 */

/*
  gpsd Status Indicator Codes
  
  0=no signal
  1=searching signal
  2=signal acquired
  3=signal detected but unusable
  4=code locked and time synchronized
  5, 6, 7=code and carrier locked and time synchronized
*/
public enum FixStatus {
	NO_SIGNAL,
    SEARCHING,
    ACQUIRED,
    DETECTED_UNUSABLE,
    LOCK_SYNC,
    CARRIER_LOCK_SYNC,    
}
