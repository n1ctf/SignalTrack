package utility;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DefaultComparator implements Comparator<Object>, Serializable {
	private static final long serialVersionUID = 1L;
	public static final int IGNORE_CASE = 0;
	public static final int CASE_SENSITIVE = 1;
	
	private static final Log LOG = LogFactory.getLog(DefaultComparator.class.getName());

	protected int caseSensitive = CASE_SENSITIVE;

	public int getCaseSensitive() {
		return caseSensitive;
	}

	public void setCaseSensitive(int caseSensitive) {
		if (caseSensitive == 0 || caseSensitive == 1) {
			this.caseSensitive = caseSensitive;
		} else {
			throw new IllegalArgumentException(
					"Unable to setCaseSensitive. Accepted values are " + "IGNORE_CASE or CASE_SENSITIVE");
		}
	}

	@Override
	public int compare(Object o1, Object o2) {
		if (o1 == null || o2 == null) {
			return getNullComparison(o1, o2);
		}
		if (o1 instanceof Boolean o1b && o2 instanceof Boolean o2b) {
			return compare(o1b, o2b);
		} else if (o1 instanceof Number o1n && o2 instanceof Number o2n) {
			return compare(o1n, o2n);
		} else if (o1 instanceof Date o1d && o2 instanceof Date o2d) {
			return compare(o1d, o2d);
		} else {
			return this.compare(o1.toString(), o2.toString());
		}
	}

	public int compare(String s1, String s2) {
		if (s1 == null || s2 == null) {
			return getNullComparison(s1, s2);
		} else {
			final int result;
			if (caseSensitive == IGNORE_CASE) {
				result = s1.compareToIgnoreCase(s2);
			} else {
				result = s1.compareTo(s2);
			}

			if (result > 0) {
				return 1;
			} else if (result < 0) {
				return -1;
			} else {
				return 0;
			}
		}
	}

	public int compare(Number o1, Number o2) {
		if (o1 == null || o2 == null) {
			return getNullComparison(o1, o2);
		}
		try {
			if (o1 instanceof BigDecimal o1d) {
				return (o1d).compareTo((BigDecimal) o2);
			} else if (o1 instanceof BigInteger o1d) {
				return (o1d).compareTo((BigInteger) o2);
			} else if (o1 instanceof Byte o1d) {
				return (o1d).compareTo((Byte) o2);
			} else if (o1 instanceof Double o1d) {
				return (o1d).compareTo((Double) o2);
			} else if (o1 instanceof Float o1d) {
				return (o1d).compareTo((Float) o2);
			} else if (o1 instanceof Integer o1d) {
				return (o1d).compareTo((Integer) o2);
			} else if (o1 instanceof Long o1d) {
				return (o1d).compareTo((Long) o2);
			} else if (o1 instanceof Short o1d) {
				return (o1d).compareTo((Short) o2);
			}

		} catch (ClassCastException cce) {
			LOG.warn(cce);
		}

		final Double n1 = o1.doubleValue();
		final Double n2 = o2.doubleValue();
		
		return n1.compareTo(n2);
	}

	public int compare(Date o1, Date o2) {
		if (o1 == null || o2 == null) {
			return getNullComparison(o1, o2);
		} else {
			final long n1 = o1.getTime();
			final long n2 = o2.getTime();
			if (n1 < n2) {
				return -1;
			} else if (n1 > n2) {
				return 1;
			} else {
				return 0;
			}
		}
	}

	public int compare(Boolean o1, Boolean o2) {
		if (o1 == null || o2 == null) {
			return getNullComparison(o1, o2);
		} else {
			final boolean b1 = o1.booleanValue();
			final boolean b2 = o2.booleanValue();
			if (b1 == b2) {
				return 0;
			} else if (b1) {
				return 1;
			} else {
				return -1;
			}
		}
	}

	private int getNullComparison(Object o1, Object o2) {
		if (o1 == null && o2 == null) {
			return 0;
		} else if (o1 == null) {
			return -1;
		} else {
			return 1;
		}
	}
}
