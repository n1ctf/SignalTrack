package hebrew;

public class CalendarDate {

	private int day;
	private int month;
	private int year;
	
	public CalendarDate(int day, int month, int year) {
		this.day = day;
		this.month = month;
		this.year = year;
	}

	public CalendarDate(CalendarDate date) {
		this.day = date.getDay();
		this.month = date.getMonth();
		this.year = date.getYear();
	}

	public int getDay() {
		return day;
	}

	public int getMonth() {
		return month;
	}

	public int getYear() {
		return year;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public boolean areDatesEqual(CalendarDate date) {
		if ((day == date.getDay()) && (month == date.getMonth()) && (year == date.getYear()))
			return true;
		else
			return false;
	}

	public int getHashCode() {
		return (year - 1583) * 366 + month * 31 + day;
	}

	public String toString() {
		return day + "." + month + "." + year;
	}

}
