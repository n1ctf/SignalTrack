package components;

import java.util.Objects;

public class TwoDimElement<T1, T2> {

    private T1 t1;
    private T2 t2;

    public TwoDimElement(T1 t1, T2 t2) {
        this.t1 = t1;
        this.t2 = t2;
    }

    public void setT1(T1 t1) {
        this.t1 = t1;
    }

    public void setT2(T2 t2) {
        this.t2 = t2;
    }

    public T1 getT1() {
        return t1;
    }

    public T2 getT2() {
        return t2;
    }

	@Override
	public int hashCode() {
		return Objects.hash(t1, t2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof TwoDimElement)) {
			return false;
		}
		final TwoDimElement<?, ?> other = (TwoDimElement<?, ?>) obj;
		return Objects.equals(t1, other.t1) && Objects.equals(t2, other.t2);
	}

	@Override
	public String toString() {
		return "TwoDimElement [t1=" + t1 + ", t2=" + t2 + "]";
	}

}
