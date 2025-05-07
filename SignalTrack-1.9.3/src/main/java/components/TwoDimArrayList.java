package components;

import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class TwoDimArrayList<T1, T2> extends CopyOnWriteArrayList<TwoDimElement<T1, T2>> {

    private static final long serialVersionUID = 1L;

    public boolean add(T1 t1, T2 t2) {
        return add(new TwoDimElement<>(t1, t2));
    }

    public void add(int index, T1 t1, T2 t2) {
        add(index, new TwoDimElement<>(t1, t2));
    }

    public TwoDimElement<T1, T2> setT1(int index, T1 t1) {
        final TwoDimElement<T1, T2> e = get(index);
        e.setT1(t1);
        return set(index, e);
    }

    public TwoDimElement<T1, T2> setT2(int index, T2 t2) {
        final TwoDimElement<T1, T2> e = get(index);
        e.setT2(t2);
        return set(index, e);
    }

    public T1 getT1(int index) {
        return get(index).getT1();
    }

    public T2 getT2(int index) {
        return get(index).getT2();
    }

    public List<T1> getT1List() {
        final List<T1> l1 = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        for (final ListIterator<TwoDimElement<T1, T2>> iter = listIterator(); iter.hasNext();) {
            final TwoDimElement<T1, T2> element = iter.next();
            l1.add(element.getT1());
        }
        return l1;
    }

    public List<T2> getT2List() {
        final List<T2> l2 = Collections.synchronizedList(new CopyOnWriteArrayList<>());
        for (final ListIterator<TwoDimElement<T1, T2>> iter = listIterator(); iter.hasNext();) {
            final TwoDimElement<T1, T2> element = iter.next();
            l2.add(element.getT2());
        }
        return l2;
    }

    public int indexOfT1(T1 t1) {
        return getT1List().indexOf(t1);
    }

    public int indexOfT2(T2 t2) {
        return getT2List().indexOf(t2);
    }

}
