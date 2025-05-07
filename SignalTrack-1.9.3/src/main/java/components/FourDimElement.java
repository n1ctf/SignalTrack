package components;

import java.util.Objects;

public class FourDimElement<T1, T2, T3, T4> {

    private transient T1 t1;
    private transient T2 t2;
    private transient T3 t3;
    private transient T4 t4;

    public FourDimElement(T1 t1, T2 t2, T3 t3, T4 t4) {
        this.t1 = t1;
        this.t2 = t2;
        this.t3 = t3;
        this.t4 = t4;
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
    
    public void setT4(T4 t4) {
        this.t4 = t4;
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
    
    public T4 getT4() {
        return t4;
    }

	@Override
	public int hashCode() {
		return Objects.hash(t1, t2, t3, t4);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FourDimElement))
			return false;
		FourDimElement<?, ?, ?, ?> other = (FourDimElement<?, ?, ?, ?>) obj;
		return Objects.equals(t1, other.t1) && Objects.equals(t2, other.t2) && Objects.equals(t3, other.t3)
				&& Objects.equals(t4, other.t4);
	}

	@Override
	public String toString() {
		return "FourDimElement [t1=" + t1 + ", t2=" + t2 + ", t3=" + t3 + ", t4=" + t4 + "]";
	}
    
}
