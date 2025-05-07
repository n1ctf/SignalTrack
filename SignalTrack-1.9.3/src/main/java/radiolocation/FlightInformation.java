package radiolocation;

import java.io.Serializable;
import java.util.Objects;

public class FlightInformation implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private int id;
	private int flight = 1;
	private String registration = " ";
	private String flag = "US";
	private long observabilityCrossSectionMillimeters = 1000;
	private String manufacturer = " ";
	private String model  = " ";
	private int ceilingFeet = 5000;
	private int serviceCeilingFeet = 2000;
	private int maximumSafeAirSpeed = 200;
	private int minimumSafeAirSpeed = 30;
	private int rotaryWingCount;
	private int propellerCount;
	private int recipEngineCount;
	private int turbineEngineCount;
	private int maximumLoiterTimeMinutes;
	private int initialFuelCapacityPounds;
	private int currentFuelCapacityPounds;
	private boolean inFlightEmergency;
	private int mode1MissionCode = 1;
	private int mode2Code = 1;
	private int mode3ACode = 1;
	private long mode3CCode = 1200;
	private long mode4Code = -1;
	
	public FlightInformation() {} //TODO: add mode 4 hash
	
	public FlightInformation(FlightInformation flightInformation) {
		this.id = flightInformation.id;
		this.flight = flightInformation.flight;
		this.registration = flightInformation.registration;
		this.flag = flightInformation.flag;
		this.observabilityCrossSectionMillimeters = flightInformation.observabilityCrossSectionMillimeters;
		this.manufacturer = flightInformation.manufacturer;
		this.model = flightInformation.model;
		this.ceilingFeet = flightInformation.ceilingFeet;
		this.serviceCeilingFeet = flightInformation.serviceCeilingFeet;
		this.maximumSafeAirSpeed = flightInformation.maximumSafeAirSpeed;
		this.minimumSafeAirSpeed = flightInformation.minimumSafeAirSpeed;
		this.rotaryWingCount = flightInformation.rotaryWingCount;
		this.propellerCount = flightInformation.propellerCount;
		this.recipEngineCount = flightInformation.recipEngineCount;
		this.turbineEngineCount = flightInformation.turbineEngineCount;
		this.maximumLoiterTimeMinutes = flightInformation.maximumLoiterTimeMinutes;
		this.initialFuelCapacityPounds = flightInformation.initialFuelCapacityPounds;
		this.currentFuelCapacityPounds = flightInformation.currentFuelCapacityPounds;
		this.inFlightEmergency = flightInformation.inFlightEmergency;
		this.mode1MissionCode = flightInformation.mode1MissionCode;
		this.mode2Code = flightInformation.mode2Code;
		this.mode3ACode = flightInformation.mode3ACode;
		this.mode3CCode = flightInformation.mode3CCode;
		this.mode4Code = flightInformation.mode4Code;
	}
	
	public FlightInformation(int flight) {
		this.flight = flight;
	}
	
	public FlightInformation(int flight, String registration, String flag, long observabilityCrossSectionMillimeters, 
	String manufacturer, String model, int ceilingFeet, int serviceCeilingFeet, int maximumSafeAirSpeed, 
	int minimumSafeAirSpeed, int rotaryWingCount, int propellerCount, int recipEngineCount, int turbineEngineCount, 
	int maximumLoiterTimeMinutes, int initialFuelCapacityPounds, int currentFuelCapacityPounds, boolean inFlightEmergency, 
	int XPDRMode1MissionCode, int XPDRMode2Code, int XPDRMode3ACode, long XPDRMode3CCode, long mode4Code) {
		this.flight = flight;
		this.registration = registration;
		this.flag = flag;
		this.observabilityCrossSectionMillimeters = observabilityCrossSectionMillimeters;
		this.manufacturer = manufacturer;
		this.model = model;
		this.ceilingFeet = ceilingFeet;
		this.serviceCeilingFeet = serviceCeilingFeet;
		this.maximumSafeAirSpeed = maximumSafeAirSpeed;
		this.minimumSafeAirSpeed = minimumSafeAirSpeed;
		this.rotaryWingCount = rotaryWingCount;
		this.propellerCount = propellerCount;
		this.recipEngineCount = recipEngineCount;
		this.turbineEngineCount = turbineEngineCount;
		this.maximumLoiterTimeMinutes = maximumLoiterTimeMinutes;
		this.initialFuelCapacityPounds = initialFuelCapacityPounds;
		this.currentFuelCapacityPounds = currentFuelCapacityPounds;
		this.inFlightEmergency = inFlightEmergency;
		this.mode1MissionCode = XPDRMode1MissionCode;
		this.mode2Code = XPDRMode2Code;
		this.mode3ACode = XPDRMode3ACode;
		this.mode3CCode = XPDRMode3CCode;
		this.mode4Code = mode4Code;
	}

	public static Object[] flightInformationToObjectArray(FlightInformation fi) {
		final Object[] obj = new Object[23];
		obj[0] = fi.id;
		obj[1] = fi.flight;
		obj[2] = fi.registration;
		obj[3] = fi.flag;
		obj[4] = fi.observabilityCrossSectionMillimeters;
		obj[5] = fi.manufacturer;
		obj[6] = fi.model;
		obj[7] = fi.ceilingFeet;
		obj[8] = fi.serviceCeilingFeet;
		obj[9] = fi.maximumSafeAirSpeed;
		obj[10] = fi.minimumSafeAirSpeed;
		obj[11] = fi.rotaryWingCount;
		obj[12] = fi.propellerCount;
		obj[13] = fi.recipEngineCount;
		obj[14] = fi.turbineEngineCount;
		obj[15] = fi.maximumLoiterTimeMinutes;
		obj[16] = fi.initialFuelCapacityPounds;
		obj[17] = fi.currentFuelCapacityPounds;
		obj[18] = fi.inFlightEmergency;
		obj[19] = fi.mode1MissionCode;
		obj[20] = fi.mode2Code;
		obj[21] = fi.mode3ACode;
		obj[22] = fi.mode3CCode;
		obj[23] = fi.mode4Code;
		return obj;
	}
	
	public static FlightInformation objectArrayToFlightInformation(Object[] obj) {
		final FlightInformation fi = new FlightInformation();
		fi.id = (int) obj[0];
		fi.flight = (int) obj[1];
		fi.registration = (String) obj[2];
		fi.flag = (String) obj[3];
		fi.observabilityCrossSectionMillimeters = (long) obj[4];
		fi.manufacturer = (String) obj[5];
		fi.model = (String) obj[6];
		fi.ceilingFeet = (int) obj[7];
		fi.serviceCeilingFeet = (int) obj[8];
		fi.maximumSafeAirSpeed = (int) obj[9];
		fi.minimumSafeAirSpeed = (int) obj[10];
		fi.rotaryWingCount = (int) obj[11];
		fi.propellerCount = (int) obj[12];
		fi.recipEngineCount = (int) obj[13];
		fi.turbineEngineCount = (int) obj[14];
		fi.maximumLoiterTimeMinutes = (int) obj[15];
		fi.initialFuelCapacityPounds = (int) obj[16];
		fi.currentFuelCapacityPounds = (int) obj[17];
		fi.inFlightEmergency = (boolean) obj[18];
		fi.mode1MissionCode = (int) obj[19];
		fi.mode2Code = (int) obj[20];
		fi.mode3ACode = (int) obj[21];
		fi.mode3CCode = (long) obj[22];
		fi.mode4Code = (long) obj[23];
		return fi;
	}
	
	public int getID() {
		return id;
	}
	 
	public void setID(int id) {
		this.id = id;
	}

	public int getFlight() {
		return flight;
	}

	public void setFlight(int flight) {
		this.flight = flight;
	}

	public String getRegistration() {
		return registration;
	}

	public void setRegistration(String registration) {
		this.registration = registration;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}

	public long getObservabilityCrossSectionMillimeters() {
		return observabilityCrossSectionMillimeters;
	}

	public void setObservabilityCrossSectionMillimeters(long observabilityCrossSectionMillimeters) {
		this.observabilityCrossSectionMillimeters = observabilityCrossSectionMillimeters;
	}

	public String getManufacturer() {
		return manufacturer;
	}

	public void setManufacturer(String manufacturer) {
		this.manufacturer = manufacturer;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public int getCeilingFeet() {
		return ceilingFeet;
	}

	public void setCeilingFeet(int ceilingFeet) {
		this.ceilingFeet = ceilingFeet;
	}

	public int getServiceCeilingFeet() {
		return serviceCeilingFeet;
	}

	public void setServiceCeilingFeet(int serviceCeilingFeet) {
		this.serviceCeilingFeet = serviceCeilingFeet;
	}

	public int getMaximumSafeAirSpeed() {
		return maximumSafeAirSpeed;
	}

	public void setMaximumSafeAirSpeed(int maximumSafeAirSpeed) {
		this.maximumSafeAirSpeed = maximumSafeAirSpeed;
	}

	public int getMinimumSafeAirSpeed() {
		return minimumSafeAirSpeed;
	}

	public void setMinimumSafeAirSpeed(int minimumSafeAirSpeed) {
		this.minimumSafeAirSpeed = minimumSafeAirSpeed;
	}

	public int getRotaryWingCount() {
		return rotaryWingCount;
	}

	public void setRotaryWingCount(int rotaryWingCount) {
		this.rotaryWingCount = rotaryWingCount;
	}

	public int getPropellerCount() {
		return propellerCount;
	}

	public void setPropellerCount(int propellerCount) {
		this.propellerCount = propellerCount;
	}

	public int getRecipEngineCount() {
		return recipEngineCount;
	}

	public void setRecipEngineCount(int recipEngineCount) {
		this.recipEngineCount = recipEngineCount;
	}

	public int getTurbineEngineCount() {
		return turbineEngineCount;
	}

	public void setTurbineEngineCount(int turbineEngineCount) {
		this.turbineEngineCount = turbineEngineCount;
	}

	public int getMaximumLoiterTimeMinutes() {
		return maximumLoiterTimeMinutes;
	}

	public void setMaximumLoiterTimeMinutes(int maximumLoiterTimeMinutes) {
		this.maximumLoiterTimeMinutes = maximumLoiterTimeMinutes;
	}

	public int getInitialFuelCapacityPounds() {
		return initialFuelCapacityPounds;
	}

	public void setInitialFuelCapacityPounds(int initialFuelCapacityPounds) {
		this.initialFuelCapacityPounds = initialFuelCapacityPounds;
	}

	public int getCurrentFuelCapacityPounds() {
		return currentFuelCapacityPounds;
	}

	public void setCurrentFuelCapacityPounds(int currentFuelCapacityPounds) {
		this.currentFuelCapacityPounds = currentFuelCapacityPounds;
	}

	public boolean isInFlightEmergency() {
		return inFlightEmergency;
	}

	public void setInFlightEmergency(boolean inFlightEmergency) {
		this.inFlightEmergency = inFlightEmergency;
	}

	public int getXPDRMode1MissionCode() {
		return mode1MissionCode;
	}

	public void setXPDRMode1MissionCode(int xPDRMode1MissionCode) {
		mode1MissionCode = xPDRMode1MissionCode;
	}

	public int getXPDRMode2Code() {
		return mode2Code;
	}

	public void setXPDRMode2Code(int XPDRMode2Code) {
		this.mode2Code = XPDRMode2Code;
	}

	public int getXPDRMode3ACode() {
		return mode3ACode;
	}

	public void setXPDRMode3ACode(int XPDRMode3ACode) {
		this.mode3ACode = XPDRMode3ACode;
	}

	public long getXPDRMode3CCode() {
		return mode3CCode;
	}

	public void setXPDRMode3CCode(long XPDRMode3CCode) {
		this.mode3CCode = XPDRMode3CCode;
	}

	public long getXPDRMode4Code() {
		return mode4Code;
	}

	public void setXPDRMode4Code(long XPDRMode4Code) {
		this.mode4Code = XPDRMode4Code;
	}
	
	public static FlightInformation copy(FlightInformation flightInformation) throws CloneNotSupportedException {
        return new FlightInformation(flightInformation);
    }

	@Override
	public int hashCode() {
		return Objects.hash(ceilingFeet, currentFuelCapacityPounds, flag, flight, id, inFlightEmergency,
				initialFuelCapacityPounds, manufacturer, maximumLoiterTimeMinutes, maximumSafeAirSpeed,
				minimumSafeAirSpeed, mode1MissionCode, mode2Code, mode3ACode, mode3CCode, mode4Code, model,
				observabilityCrossSectionMillimeters, propellerCount, recipEngineCount, registration, rotaryWingCount,
				serviceCeilingFeet, turbineEngineCount);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FlightInformation))
			return false;
		FlightInformation other = (FlightInformation) obj;
		return ceilingFeet == other.ceilingFeet && currentFuelCapacityPounds == other.currentFuelCapacityPounds
				&& Objects.equals(flag, other.flag) && flight == other.flight && id == other.id
				&& inFlightEmergency == other.inFlightEmergency
				&& initialFuelCapacityPounds == other.initialFuelCapacityPounds
				&& Objects.equals(manufacturer, other.manufacturer)
				&& maximumLoiterTimeMinutes == other.maximumLoiterTimeMinutes
				&& maximumSafeAirSpeed == other.maximumSafeAirSpeed && minimumSafeAirSpeed == other.minimumSafeAirSpeed
				&& mode1MissionCode == other.mode1MissionCode && mode2Code == other.mode2Code
				&& mode3ACode == other.mode3ACode && mode3CCode == other.mode3CCode && mode4Code == other.mode4Code
				&& Objects.equals(model, other.model)
				&& observabilityCrossSectionMillimeters == other.observabilityCrossSectionMillimeters
				&& propellerCount == other.propellerCount && recipEngineCount == other.recipEngineCount
				&& Objects.equals(registration, other.registration) && rotaryWingCount == other.rotaryWingCount
				&& serviceCeilingFeet == other.serviceCeilingFeet && turbineEngineCount == other.turbineEngineCount;
	}

	@Override
	public String toString() {
		return "FlightInformation [id=" + id + ", flight=" + flight + ", registration=" + registration + ", flag="
				+ flag + ", observabilityCrossSectionMillimeters=" + observabilityCrossSectionMillimeters
				+ ", manufacturer=" + manufacturer + ", model=" + model + ", ceilingFeet=" + ceilingFeet
				+ ", serviceCeilingFeet=" + serviceCeilingFeet + ", maximumSafeAirSpeed=" + maximumSafeAirSpeed
				+ ", minimumSafeAirSpeed=" + minimumSafeAirSpeed + ", rotaryWingCount=" + rotaryWingCount
				+ ", propellerCount=" + propellerCount + ", recipEngineCount=" + recipEngineCount
				+ ", turbineEngineCount=" + turbineEngineCount + ", maximumLoiterTimeMinutes="
				+ maximumLoiterTimeMinutes + ", initialFuelCapacityPounds=" + initialFuelCapacityPounds
				+ ", currentFuelCapacityPounds=" + currentFuelCapacityPounds + ", inFlightEmergency="
				+ inFlightEmergency + ", mode1MissionCode=" + mode1MissionCode + ", mode2Code=" + mode2Code
				+ ", mode3ACode=" + mode3ACode + ", mode3CCode=" + mode3CCode + ", mode4Code=" + mode4Code + "]";
	}

}
