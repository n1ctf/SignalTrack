package geometry;
/*	License Statement from the NOAA
* The WMM source code is in the public domain and not licensed or
* under copyright. The information and software may be used freely
* by the public. As required by 17 U.S.C. 403, third parties producing
* copyrighted works consisting predominantly of the material produced
* by U.S. government agencies must provide notice with such work(s)
* identifying the U.S. Government material incorporated and stating
* that such material is not subject to copyright protection.*/

import java.util.Calendar;
import java.util.GregorianCalendar;

/** <p>Class to calculate magnetic declination, magnetic field strength,
* inclination etc. for any point on the earth.</p>
* <p>Adapted from the Geomagic software and World Magnetic Model of the NOAA
* Satellite and Information Service, National Geophysical Data Center</p>
* http://www.ngdc.noaa.gov/geomag/WMM/DoDWMM.shtml
* <p>© Deep Pradhan, 2017</p>*/
public class Geomagnetism {

	/**	The input string array which contains each line of input for the wmm.cof input file.
	*	The columns in this file are as follows:	n,	m,	gnm,	hnm,	dgnm,	dhnm*/
	private static final String [] WMM_COF = {
			"    2020.0            WMM-2020        12/10/2019",
			"  1  0  -29404.5       0.0        6.7        0.0",
			"  1  1   -1450.7    4652.9        7.7      -25.1",
			"  2  0   -2500.0       0.0      -11.5        0.0",
			"  2  1    2982.0   -2991.6       -7.1      -30.2",
			"  2  2    1676.8    -734.8       -2.2      -23.9",
			"  3  0    1363.9       0.0        2.8        0.0",
			"  3  1   -2381.0     -82.2       -6.2        5.7",
			"  3  2    1236.2     241.8        3.4       -1.0",
			"  3  3     525.7    -542.9      -12.2        1.1",
			"  4  0     903.1       0.0       -1.1        0.0",
			"  4  1     809.4     282.0       -1.6        0.2",
			"  4  2      86.2    -158.4       -6.0        6.9",
			"  4  3    -309.4     199.8        5.4        3.7",
			"  4  4      47.9    -350.1       -5.5       -5.6",
			"  5  0    -234.4       0.0       -0.3        0.0",
			"  5  1     363.1      47.7        0.6        0.1",
			"  5  2     187.8     208.4       -0.7        2.5",
			"  5  3    -140.7    -121.3        0.1       -0.9",
			"  5  4    -151.2      32.2        1.2        3.0",
			"  5  5      13.7      99.1        1.0        0.5",
			"  6  0      65.9       0.0       -0.6        0.0",
			"  6  1      65.6     -19.1       -0.4        0.1",
			"  6  2      73.0      25.0        0.5       -1.8",
			"  6  3    -121.5      52.7        1.4       -1.4",
			"  6  4     -36.2     -64.4       -1.4        0.9",
			"  6  5      13.5       9.0       -0.0        0.1",
			"  6  6     -64.7      68.1        0.8        1.0",
			"  7  0      80.6       0.0       -0.1        0.0",
			"  7  1     -76.8     -51.4       -0.3        0.5",
			"  7  2      -8.3     -16.8       -0.1        0.6",
			"  7  3      56.5       2.3        0.7       -0.7",
			"  7  4      15.8      23.5        0.2       -0.2",
			"  7  5       6.4      -2.2       -0.5       -1.2",
			"  7  6      -7.2     -27.2       -0.8        0.2",
			"  7  7       9.8      -1.9        1.0        0.3",
			"  8  0      23.6       0.0       -0.1        0.0",
			"  8  1       9.8       8.4        0.1       -0.3",
			"  8  2     -17.5     -15.3       -0.1        0.7",
			"  8  3      -0.4      12.8        0.5       -0.2",
			"  8  4     -21.1     -11.8       -0.1        0.5",
			"  8  5      15.3      14.9        0.4       -0.3",
			"  8  6      13.7       3.6        0.5       -0.5",
			"  8  7     -16.5      -6.9        0.0        0.4",
			"  8  8      -0.3       2.8        0.4        0.1",
			"  9  0       5.0       0.0       -0.1        0.0",
			"  9  1       8.2     -23.3       -0.2       -0.3",
			"  9  2       2.9      11.1       -0.0        0.2",
			"  9  3      -1.4       9.8        0.4       -0.4",
			"  9  4      -1.1      -5.1       -0.3        0.4",
			"  9  5     -13.3      -6.2       -0.0        0.1",
			"  9  6       1.1       7.8        0.3       -0.0",
			"  9  7       8.9       0.4       -0.0       -0.2",
			"  9  8      -9.3      -1.5       -0.0        0.5",
			"  9  9     -11.9       9.7       -0.4        0.2",
			" 10  0      -1.9       0.0        0.0        0.0",
			" 10  1      -6.2       3.4       -0.0       -0.0",
			" 10  2      -0.1      -0.2       -0.0        0.1",
			" 10  3       1.7       3.5        0.2       -0.3",
			" 10  4      -0.9       4.8       -0.1        0.1",
			" 10  5       0.6      -8.6       -0.2       -0.2",
			" 10  6      -0.9      -0.1       -0.0        0.1",
			" 10  7       1.9      -4.2       -0.1       -0.0",
			" 10  8       1.4      -3.4       -0.2       -0.1",
			" 10  9      -2.4      -0.1       -0.1        0.2",
			" 10 10      -3.9      -8.8       -0.0       -0.0",
			" 11  0       3.0       0.0       -0.0        0.0",
			" 11  1      -1.4      -0.0       -0.1       -0.0",
			" 11  2      -2.5       2.6       -0.0        0.1",
			" 11  3       2.4      -0.5        0.0        0.0",
			" 11  4      -0.9      -0.4       -0.0        0.2",
			" 11  5       0.3       0.6       -0.1       -0.0",
			" 11  6      -0.7      -0.2        0.0        0.0",
			" 11  7      -0.1      -1.7       -0.0        0.1",
			" 11  8       1.4      -1.6       -0.1       -0.0",
			" 11  9      -0.6      -3.0       -0.1       -0.1",
			" 11 10       0.2      -2.0       -0.1        0.0",
			" 11 11       3.1      -2.6       -0.1       -0.0",
			" 12  0      -2.0       0.0        0.0        0.0",
			" 12  1      -0.1      -1.2       -0.0       -0.0",
			" 12  2       0.5       0.5       -0.0        0.0",
			" 12  3       1.3       1.3        0.0       -0.1",
			" 12  4      -1.2      -1.8       -0.0        0.1",
			" 12  5       0.7       0.1       -0.0       -0.0",
			" 12  6       0.3       0.7        0.0        0.0",
			" 12  7       0.5      -0.1       -0.0       -0.0",
			" 12  8      -0.2       0.6        0.0        0.1",
			" 12  9      -0.5       0.2       -0.0       -0.0",
			" 12 10       0.1      -0.9       -0.0       -0.0",
			" 12 11      -1.1      -0.0       -0.0        0.0",
			" 12 12      -0.3       0.5       -0.1       -0.1"};

	/** Mean radius of IAU-66 ellipsoid, in km*/
	private static final double IAU66_RADIUS = 6371.2;

	/** Semi-major axis of WGS-1984 ellipsoid, in km*/
	private static final double WGS84_A = 6378.137;

	/** Semi-minor axis of WGS-1984 ellipsoid, in km*/
	private static final double WGS84_B = 6356.7523142;

	/** The maximum number of degrees of the spherical harmonic model*/
	private static final int MAX_DEG = 12;

	/** Geomagnetic declination (decimal degrees) [opposite of variation, positive Eastward/negative Westward]*/
	private double declination = 0;

	/** Geomagnetic inclination/dip angle (degrees) [positive downward]*/
	private double inclination = 0;

	/** Geomagnetic field intensity/strength (nanoTeslas)*/
	private double intensity = 0;

	/** Geomagnetic horizontal field intensity/strength (nanoTeslas)*/
	private double bh;

	/** Geomagnetic vertical field intensity/strength (nanoTeslas) [positive downward]*/
	private double bz;

	/** Geomagnetic North South (northerly component) field intensity/strength (nanoTesla)*/
	private double bx;

	/** Geomagnetic East West (easterly component) field intensity/strength (nanoTeslas)*/
	private double by;

	/** The maximum order of spherical harmonic model*/
	private int maxord;

	/** The Gauss coefficients of main geomagnetic model (nt)*/
	private final double[][] c = new double[13][13];

	/** The Gauss coefficients of secular geomagnetic model (nt/yr)*/
	private final double[][] cd = new double[13][13];

	/** The time adjusted geomagnetic gauss coefficients (nt)*/
	private final double[][] tc = new double[13][13];

	/** The theta derivative of p(n,m) (unnormalized)*/
	private final double[][] dp = new double[13][13];

	/** The Schmidt normalization factors*/
	private final double[] snorm = new double[169];

	/** The sine of (m*spherical coordinate longitude)*/
	private final double[] sp = new double[13];

	/** The cosine of (m*spherical coordinate longitude)*/
	private final double[] cp = new double[13];
	private final double[] fn = new double[13];
	private final double[] fm = new double[13];

	/** The associated Legendre polynomials for m = 1 (unnormalized)*/
	private final double[] pp = new double[13];

	private final double[][] k = new double[13][13];

	/** The variables oldTime (old time), oldAlt (old altitude),
	*	oldLat (old latitude), oldLon (old longitude), are used to
	*	store the values used from the previous calculation to
	*	save on calculation time if some inputs don't change*/
	private double oldTime;
	private double oldAlt;
	private double oldLat;
	private double oldLon;

	/** The date in years, for the start of the valid time of the fit coefficients*/
	private double epoch;

	private double r;

	private double ca;

	private double sa;

	private double ct;

	private double st;
	
	/** Initialize the instance without calculations*/
	public Geomagnetism() {
		// Initialize constants
		maxord = MAX_DEG;
		sp[0] = 0;
		cp[0] = snorm[0] = pp[0] = 1;
		dp[0][0] = 0;

		c[0][0] = 0;
		cd[0][0] = 0;

		epoch = Double.parseDouble(WMM_COF[0].trim().split("\\s+")[0]);

		String[] tokens;

		double gnm;
		double hnm;
		double dgnm;
		double dhnm;
		for (int i = 1, m, n; i < WMM_COF.length; i++) {
			tokens = WMM_COF[i].trim().split("\\s+");
			n = Integer.parseInt(tokens[0]);
			m = Integer.parseInt(tokens[1]);
			gnm = Double.parseDouble(tokens[2]);
			hnm = Double.parseDouble(tokens[3]);
			dgnm = Double.parseDouble(tokens[4]);
			dhnm = Double.parseDouble(tokens[5]);
			if (m <= n) {
				c[m][n] = gnm;
				cd[m][n] = dgnm;
				if (m != 0) {
					c[n][m - 1] = hnm;
					cd[n][m - 1] = dhnm;
				}
			}			
		}
		// Convert Schmidt normalized Gauss coefficients to unnormalized
		snorm[0] = 1;
		double flnmj;
		for (int j, n = 1; n <= maxord; n++) {
			snorm[n] = snorm[n - 1] * (2 * n - 1) / n;
			j = 1;
			for (int m = 0, d1 = 1, d2 = (n - m + d1) / d1; d2 > 0; d2--, m += d1) {
				k[m][n] = (double) (((n - 1) * (n - 1)) - (m * m)) / (double) ((2 * n - 1) * (2 * n - 3));
				if (m > 0) {
					flnmj = ((n - m + 1) * j) / (double) (n + m);
					snorm[n + m * 13] = snorm[n + (m -1) * 13] * Math.sqrt(flnmj);
					j = 1;
					c[n][m - 1] = snorm[n + m * 13] * c[n][m - 1];
					cd[n][m - 1] = snorm[n + m * 13] * cd[n][m - 1];
				}
				c[m][n] = snorm[n + m * 13] * c[m][n];
				cd[m][n] = snorm[n + m * 13] * cd[m][n];
			}
			fn[n] = (n + 1);
			fm[n] = n;
		}
		k[1][1] = 0;
		fm[0] = 0;
		oldTime = oldAlt = oldLat = oldLon = -1000;
	}

	/** Initialize the instance and calculate for given location, altitude and date
	*	@param longitude	Longitude in decimal degrees
	*	@param latitude		Latitude in decimal degrees
	*	@param altitude		Altitude in meters (with respect to WGS-1984 ellipsoid)
	*	@param calendar		Calendar for date of calculation*/
	public Geomagnetism(double longitude, double latitude, double altitude, GregorianCalendar calendar) {
		this();
		calculate(longitude, latitude, altitude, calendar);
	}

	/** Initialize the instance and calculate for given location, altitude and current date
	*	@param longitude	Longitude in decimal degrees
	*	@param latitude		Latitude in decimal degrees
	*	@param altitude		Altitude in meters (with respect to WGS-1984 ellipsoid)*/
	public Geomagnetism(double longitude, double latitude, double altitude) {
		this();
		calculate(longitude, latitude, altitude);
	}

	/** Initialize the instance and calculate for given location, zero altitude and current date
	*	@param longitude	Longitude in decimal degrees
	*	@param latitude		Latitude in decimal degrees*/
	public Geomagnetism(double longitude, double latitude) {
		this();
		calculate(longitude, latitude);
	}

	/** Calculate for given location, altitude and date
	*	@param longitude	Longitude in decimal degrees
	*	@param latitude		Latitude in decimal degrees
	*	@param altitude		Altitude in meters (with respect to WGS-1984 ellipsoid)
	*	@param calendar		Calendar for date of calculation*/
	public void calculate(double longitude, double latitude, double altitude, GregorianCalendar calendar) {
		final double rlon = Math.toRadians(longitude);
		final double rlat = Math.toRadians(latitude);
		final double altitudeKm = Double.isNaN(altitude) ? 0 : altitude / 1000;
		final double yearFraction = calendar.get(Calendar.YEAR) + (double) calendar.get(Calendar.DAY_OF_YEAR)
					/ calendar.getActualMaximum(Calendar.DAY_OF_YEAR);
		final double dt = yearFraction - epoch;
		final double srlon = Math.sin(rlon);
		final double srlat = Math.sin(rlat);
		final double crlon = Math.cos(rlon);
		final double crlat = Math.cos(rlat);
		final double srlat2 = srlat * srlat;
		final double crlat2 = crlat * crlat;
		final double a2 = WGS84_A * WGS84_A;
		final double b2 = WGS84_B * WGS84_B;
		final double c2 = a2 - b2;
		final double a4 = a2 * a2;
		final double b4 = b2 * b2;
		final double c4 = a4 - b4;

		sp[1] = srlon;
		cp[1] = crlon;

		// Convert from geodetic coordinates. to spherical coordinates.
		if (altitudeKm != oldAlt || latitude != oldLat) {
			final double q = Math.sqrt(a2 - c2 * srlat2);
			final double q1 = altitudeKm * q;
			final double q2 = ((q1 + a2) / (q1 + b2)) * ((q1 + a2) / (q1 + b2));
			final double r2 = ((altitudeKm * altitudeKm) + 2 * q1 + (a4 - c4 * srlat2) / (q * q));
			ct = srlat / Math.sqrt(q2 * crlat2 + srlat2);
			st = Math.sqrt(1 - (ct * ct));
			r = Math.sqrt(r2);
			final double d = Math.sqrt(a2 * crlat2 + b2 * srlat2);
			ca = (altitudeKm + d) / r;
			sa = c2 * crlat * srlat / (r * d);
		}
		if (longitude != oldLon) {
			for (int m = 2; m <= maxord; m++) {
				sp[m] = sp[1] * cp[m - 1] + cp[1] * sp[m - 1];
				cp[m] = cp[1] * cp[m - 1] - sp[1] * sp[m - 1];
			}
		}
		final double aor = IAU66_RADIUS / r;
		double ar = aor * aor;
		double br = 0;
		double bt = 0;
		double bp = 0;
		double bpp = 0;
		double par;
		double parp;
		double temp1;
		double temp2;

		for (int n = 1; n <= maxord; n++) {
			ar *= aor;
			for (int m = 0, d3 = 1, d4 = (n + m + d3) / d3; d4 > 0; d4--, m += d3) {

				// Compute unnormalized associated legendre polynomials and derivatives via recursion relations
				if (altitudeKm != oldAlt || latitude != oldLat) {
					if (n == m) {
						snorm[n + m * 13] = st * snorm[n - 1 + (m - 1) * 13];				
						dp[m][n] = st * dp[m - 1][n - 1]+ ct * snorm[n - 1 + (m - 1) * 13];
					}
					if (n == 1 && m == 0) {
						snorm[n + m * 13] = ct * snorm[n - 1 + m * 13];
						dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13];
					}
					if (n > 1 && n != m) {
						if (m > n - 2) {
							snorm[n - 2 + m * 13] = 0;
						}
						if (m > n - 2) {
							dp[m][n - 2] = 0;
						}
						snorm[n + m * 13] = ct * snorm[n - 1 + m * 13] - k[m][n] * snorm[n - 2 + m * 13];
						dp[m][n] = ct * dp[m][n - 1] - st * snorm[n - 1 + m * 13] - k[m][n] * dp[m][n - 2];
					}
				}

				// Time adjust the gauss coefficients
				if (yearFraction != oldTime) {
					tc[m][n] = c[m][n] + dt * cd[m][n];

					if (m != 0) {
						tc[n][m - 1] = c[n][m - 1]+ dt * cd[n][m - 1];
					}
				}

				// Accumulate terms of the spherical harmonic expansions
				par = ar * snorm[ n + m * 13];
				if (m == 0) {
					temp1 = tc[m][n] * cp[m];
					temp2 = tc[m][n] * sp[m];
				} else {
					temp1 = tc[m][n] * cp[m] + tc[n][m - 1] * sp[m];
					temp2 = tc[m][n] * sp[m] - tc[n][m - 1] * cp[m];
				}

				bt = bt - ar * temp1 * dp[m][n];
				bp += (fm[m] * temp2 * par);
				br += (fn[n] * temp1 * par);

				// Special case: north/south geographic poles
				if (st == 0 && m == 1) {
					pp[n] = n == 1 ? pp[n - 1] : ct * pp[n - 1] - k[m][n] * pp[n - 2];
					parp = ar * pp[n];
					bpp += (fm[m] * temp2 * parp);
				}
			}
		}

		if (st == 0) {
			bp = bpp;
		} else {
			bp /= st;
		}

		// Rotate magnetic vector components from spherical to geodetic coordinates
		// bx must be the east-west field component
		// by must be the north-south field component
		// bz must be the vertical field component.
		bx = -bt * ca - br * sa;
		by = bp;
		bz = bt * sa - br * ca;

		// Compute declination (dec), inclination (dip) and total intensity (ti)
		bh = Math.sqrt((bx * bx) + (by * by));
		intensity = Math.sqrt((bh * bh) + (bz * bz));
		//	Calculate the declination.
		declination = Math.toDegrees(Math.atan2(by, bx));
		inclination = Math.toDegrees(Math.atan2(bz, bh));

		oldTime = yearFraction;
		oldAlt = altitudeKm;
		oldLat = latitude;
		oldLon = longitude;
	}

	/** Calculate for given location, altitude and current date
	*	@param longitude	Longitude in decimal degrees
	*	@param latitude		Latitude in decimal degrees
	*	@param altitude		Altitude in metres (with respect to WGS-1984 ellipsoid)*/
	void calculate(double longitude, double latitude, double altitude) {
		calculate(longitude, latitude, altitude, new GregorianCalendar());
	}

	/** Calculate for given location, zero altitude and current date
	*	@param longitude	Longitude in decimal degrees
	*	@param latitude		Latitude in decimal degrees*/
	void calculate(double longitude, double latitude) {
		calculate(longitude, latitude, 0);
	}

	/** @return Geomagnetic declination (degrees) [opposite of variation, positive Eastward/negative Westward]*/
	double getDeclination() {
		return declination;
	}

	/** @return Geomagnetic inclination/dip angle (degrees) [positive downward]*/
	double getInclination() {
		return inclination;
	}

	/** @return Geomagnetic field intensity/strength (nanoTeslas)*/
	double getIntensity() {
		return intensity;
	}

	/** @return Geomagnetic horizontal field intensity/strength (nanoTeslas)*/
	double getHorizontalIntensity() {
		return bh;
	}

	/** @return Geomagnetic vertical field intensity/strength (nanoTeslas) [positive downward]*/
	double getVerticalIntensity() {
		return bz;
	}

	/** @return Geomagnetic North South (northerly component) field intensity/strength (nanoTeslas)*/
	double getNorthIntensity() {
		return bx;
	}

	/** @return Geomagnetic East West (easterly component) field intensity/strength (nanoTeslas)*/
	double getEastIntensity() {
		return by;
	}
}
