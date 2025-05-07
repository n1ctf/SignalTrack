package gps;

/**
 *
 * @author John
 */

/*
 GPGGA Quality Codes
 
 0: Fix not valid 
 1: GPS fix 
 2: Differential GPS fix (DGNSS), SBAS, OmniSTAR VBS, Beacon, RTX in GVBS mode 
 3: Not applicable 
 4: RTK Fixed, xFill 
 5: RTK Float, OmniSTAR XP/HP, Location RTK, RTX 
 6: INS Dead reckoning
*/
public enum FixQuality {
    INVALID, 
    GPS, 
    DGPS, 
    PPS, 
    RTK, 
    FLOAT_RTK, 
    DEAD_RECON, 
    MANUAL, 
    SIMULATION
}
