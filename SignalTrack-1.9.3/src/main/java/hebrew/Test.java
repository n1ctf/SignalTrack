package hebrew;

public class Test {
	public static void main(String[] args) {
		CalendarTime t;
		Location loc = new Location("Pforzheim", 4854, 842, 1, 263);
		AstronomicalCalculations a = new AstronomicalCalculations();
		System.out.println("Pforzheim, 20/11/2005...");
		t = a.GetSunrise(11, 20, 2005, loc);
		System.out.println("Sunrise: " + t.formatTime24() + ", " + t.formatTime12());
		t = a.GetSunset(11, 20, 2005, loc);
		System.out.println("Sunset: " + t.formatTime24());
		t.addMinutes(45);
		System.out.println("Sunset + 45 minutes: " + t.formatTime24());
		t = a.GetSunset(11, 20, 2005, loc);
		t.subtractMinutes(18);
		System.out.println("Sunset - 18 minutes: " + t.formatTime24());
		t = a.GetSunriseDegreesBelowHorizon(11, 20, 2005, 11, loc);
		System.out.println("Sunrise - 11° below horizon: " + t.formatTime24());
		t = a.GetSunsetDegreesBelowHorizon(11, 20, 2005, 8.75, loc);
		System.out.println("Sunset + 8.75° below horizon: " + t.formatTime24());
		CalendarTime sunr = a.GetSunrise(11, 20, 2005, loc);
		CalendarTime suns = a.GetSunset(11, 20, 2005, loc);
		t = a.GetProportionalHours(3, sunr, suns);
		System.out.println("Proportional hour (3): " + t.formatTime24());
		int shaaZmanit = a.GetShaaZmanit(sunr, suns);
		System.out.println("Sha'a Zmanit: " + CalendarTime.formatTimeShaaZmanit(shaaZmanit));
	}
}
