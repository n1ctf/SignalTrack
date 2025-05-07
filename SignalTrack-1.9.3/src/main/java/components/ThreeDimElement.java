package components;

import java.util.Objects;

public class ThreeDimElement<T1, T2, T3> {

    private T1 t1;
    private T2 t2;
    private T3 t3;

    public ThreeDimElement(T1 t1, T2 t2, T3 t3) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
    }

    public void setT1(T1 t1) {
        this.t1 = t1;
    }

    public void setT2(T2 t2) {
        this.t2 = t2;
    }

    public void setT3(T3 t3) {
        this.t3 = t3;
    }

    public T1 getT1() {
        return t1;
    }

    public T2 getT2() {
        return t2;
    }

    public T3 getT3() {
        return t3;
    }

	@Override
	public int hashCode() {
		return Objects.hash(t1, t2, t3);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof ThreeDimElement)) {
			return false;
		}
		final ThreeDimElement<?, ?, ?> other = (ThreeDimElement<?, ?, ?>) obj;
		return Objects.equals(t1, other.t1) && Objects.equals(t2, other.t2) && Objects.equals(t3, other.t3);
	}

	@Override
	public String toString() {
		return "ThreeDimElement [t1=" + t1 + ", t2=" + t2 + ", t3=" + t3 + "]";
	}

}
