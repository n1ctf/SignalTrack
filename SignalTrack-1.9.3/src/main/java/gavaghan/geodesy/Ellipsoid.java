/* 
 *  Geodesy by Mike Gavaghan
 * 
 *      http://www.gavaghan.org/blog/free-source-code/geodesy-library-vincentys-formula/
 * 
 *  Copyright 2007 Mike Gavaghan - mike@gavaghan.org
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

/*
 * BitCoin tips graciously accepted at 1FB63FYQMy7hpC2ANVhZ5mSgAZEtY1aVLf
 */
package gavaghan.geodesy;

import java.io.Serializable;
import java.util.Objects;

/**
 * Encapsulation of an ellipsoid, and declaration of common reference
 * ellipsoids.
 * 
 * @author <a href="mailto:mike@gavaghan.org">Mike Gavaghan</a>
 */
public class Ellipsoid implements Serializable {
	private static final long serialVersionUID = 1L;

	/** Semi major axis (meters). */
	private final double mSemiMajorAxis;

	/** Semi minor axis (meters). */
	private final double mSemiMinorAxis;

	/** Flattening. */
	private final double mFlattening;

	/** Inverse flattening. */
	private final double mInverseFlattening;

	/**
	 * Construct a new Ellipsoid. This is private to ensure the values are
	 * consistent (flattening = 1.0 / inverseFlattening). Use the methods
	 * fromAAndInverseF() and fromAAndF() to create new instances.
	 * 
	 * @param semiMajor         semi major axis in meters
	 * @param semiMinor         semi minor axis in meters
	 * @param flattening        flattening ratio
	 * @param inverseFlattening inverse flattening ratio
	 */
	private Ellipsoid(double semiMajor, double semiMinor, double flattening, double inverseFlattening) {
		mSemiMajorAxis = semiMajor;
		mSemiMinorAxis = semiMinor;
		mFlattening = flattening;
		mInverseFlattening = inverseFlattening;
	}

	/** The WGS84 ellipsoid. */
	public static final Ellipsoid WGS84 = fromAAndInverseF(6378137.0, 298.257223563);

	/** The GRS80 ellipsoid. */
	public static final Ellipsoid GRS80 = fromAAndInverseF(6378137.0, 298.257222101);

	/** The GRS67 ellipsoid. */
	public static final Ellipsoid GRS67 = fromAAndInverseF(6378160.0, 298.25);

	/** The ANS ellipsoid. */
	public static final Ellipsoid ANS = fromAAndInverseF(6378160.0, 298.25);

	/** The WGS72 ellipsoid. */
	public static final Ellipsoid WGS72 = fromAAndInverseF(6378135.0, 298.26);

	/** The Clarke1858 ellipsoid. */
	public static final Ellipsoid Clarke1858 = fromAAndInverseF(6378293.645, 294.26);

	/** The Clarke1880 ellipsoid. */
	public static final Ellipsoid Clarke1880 = fromAAndInverseF(6378249.145, 293.465);

	/** A spherical "ellipsoid". */
	public static final Ellipsoid Sphere = fromAAndF(6371000, 0.0);

	/**
	 * Build an Ellipsoid from the semi major axis measurement and the inverse
	 * flattening.
	 * 
	 * @param semiMajor         semi major axis (meters)
	 * @param inverseFlattening inverse flattening ratio
	 * @return a new Ellipsoid instance
	 */
	public static Ellipsoid fromAAndInverseF(double semiMajor, double inverseFlattening) {
		final double f = 1.0 / inverseFlattening;
		final double b = (1.0 - f) * semiMajor;

		return new Ellipsoid(semiMajor, b, f, inverseFlattening);
	}

	/**
	 * Build an Ellipsoid from the semi major axis measurement and the flattening.
	 * 
	 * @param semiMajor  semi major axis (meters)
	 * @param flattening flattening ratio
	 * @return a new Ellipsoid instance
	 */
	public static Ellipsoid fromAAndF(double semiMajor, double flattening) {
		final double inverseF = 1.0 / flattening;
		final double b = (1.0 - flattening) * semiMajor;

		return new Ellipsoid(semiMajor, b, flattening, inverseF);
	}

	/**
	 * Get semi-major axis.
	 * 
	 * @return semi-major axis (in meters).
	 */
	public double getSemiMajorAxis() {
		return mSemiMajorAxis;
	}

	/**
	 * Get semi-minor axis.
	 * 
	 * @return semi-minor axis (in meters).
	 */
	public double getSemiMinorAxis() {
		return mSemiMinorAxis;
	}

	/**
	 * Get flattening ratio.
	 * 
	 * @return flattening ratio
	 */
	public double getFlattening() {
		return mFlattening;
	}

	/**
	 * Get inverse flattening ratio.
	 * 
	 * @return inverse flattening ratio
	 */
	public double getInverseFlattening() {
		return mInverseFlattening;
	}

	@Override
	public int hashCode() {
		return Objects.hash(mFlattening, mInverseFlattening, mSemiMajorAxis, mSemiMinorAxis);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Ellipsoid other = (Ellipsoid) obj;
		return Double.doubleToLongBits(mFlattening) == Double.doubleToLongBits(other.mFlattening)
				&& Double.doubleToLongBits(mInverseFlattening) == Double.doubleToLongBits(other.mInverseFlattening)
				&& Double.doubleToLongBits(mSemiMajorAxis) == Double.doubleToLongBits(other.mSemiMajorAxis)
				&& Double.doubleToLongBits(mSemiMinorAxis) == Double.doubleToLongBits(other.mSemiMinorAxis);
	}

	@Override
	public String toString() {
		return "Ellipsoid [mSemiMajorAxis=" + mSemiMajorAxis + ", mSemiMinorAxis=" + mSemiMinorAxis + ", mFlattening="
				+ mFlattening + ", mInverseFlattening=" + mInverseFlattening + "]";
	}
}
