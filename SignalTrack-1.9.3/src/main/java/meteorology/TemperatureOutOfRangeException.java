package meteorology;

public class TemperatureOutOfRangeException extends RuntimeException {
	private static final long serialVersionUID = -1L;
	
	public TemperatureOutOfRangeException(String errorMessage) {
		super(errorMessage);
	}

}
