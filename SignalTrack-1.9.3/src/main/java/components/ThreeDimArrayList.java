package components;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class ThreeDimArrayList<T1, T2, T3> extends CopyOnWriteArrayList<ThreeDimElement<T1, T2, T3>> {

    private static final long serialVersionUID = 1L;

    public boolean add(T1 t1, T2 t2, T3 t3) {
        return add(new ThreeDimElement<>(t1, t2, t3));
    }

    public void add(int index, T1 t1, T2 t2, T3 t3) {
        add(index, new ThreeDimElement<>(t1, t2, t3));
    }

    public ThreeDimElement<T1, T2, T3> setT1(int index, T1 t1) {
        final ThreeDimElement<T1, T2, T3> e = get(index);
        e.setT1(t1);
        return set(index, e);
    }

    public ThreeDimElement<T1, T2, T3> setT2(int index, T2 t2) {
        final ThreeDimElement<T1, T2, T3> e = get(index);
        e.setT2(t2);
        return set(index, e);
    }

    public ThreeDimElement<T1, T2, T3> setT3(int index, T3 t3) {
        final ThreeDimElement<T1, T2, T3> e = get(index);
        e.setT3(t3);
        return set(index, e);
    }

    public T1 getT1(int index) {
        return get(index).getT1();
    }

    public T2 getT2(int index) {
        return get(index).getT2();
    }

    public T3 getT3(int index) {
        return get(index).getT3();
    }

    public List<T1> getT1List() {
        final List<T1> l1 = new CopyOnWriteArrayList<>();
        for (final ListIterator<ThreeDimElement<T1, T2, T3>> iter = listIterator(); iter.hasNext();) {
            final ThreeDimElement<T1, T2, T3> element = iter.next();
            l1.add(element.getT1());
        }
        return l1;
    }

    public List<T2> getT2List() {
        final List<T2> l2 = new CopyOnWriteArrayList<>();
        for (final ListIterator<ThreeDimElement<T1, T2, T3>> iter = listIterator(); iter.hasNext();) {
            final ThreeDimElement<T1, T2, T3> element = iter.next();
            l2.add(element.getT2());
        }
        return l2;
    }

    public List<T3> getT3List() {
        final List<T3> l3 = new CopyOnWriteArrayList<>();
        for (final ListIterator<ThreeDimElement<T1, T2, T3>> iter = listIterator(); iter.hasNext();) {
            final ThreeDimElement<T1, T2, T3> element = iter.next();
            l3.add(element.getT3());
        }
        return l3;
    }

    public int indexOfT1(T1 t1) {
        return getT1List().indexOf(t1);
    }

    public int indexOfT2(T2 t2) {
        return getT2List().indexOf(t2);
    }

    public int indexOfT3(T3 t3) {
        return getT3List().indexOf(t3);
    }
}
