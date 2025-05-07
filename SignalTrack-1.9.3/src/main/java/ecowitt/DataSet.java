package ecowitt;

import java.util.Objects;

public class DataSet {
	
	private final int command;
	private int position;
	private int numBytes;

	public DataSet(int command, int position, int numBytes) {
		this.command = command;
		this.position = position;
		this.numBytes = numBytes;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public int getNumBytes() {
		return numBytes;
	}

	public void setNumBytes(int numBytes) {
		this.numBytes = numBytes;
	}

	public int getCommand() {
		return command;
	}

	@Override
	public int hashCode() {
		return Objects.hash(command, numBytes, position);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof DataSet other)) {
			return false;
		}
		return command == other.command && numBytes == other.numBytes && position == other.position;
	}

	@Override
	public String toString() {
		return "DataSet [command=" + command + ", position=" + position + ", numBytes=" + numBytes + "]";
	}
}
