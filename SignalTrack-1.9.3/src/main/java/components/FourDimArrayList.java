package components;

import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class FourDimArrayList<T1, T2, T3,T4> extends CopyOnWriteArrayList<FourDimElement<T1, T2, T3, T4>> {

    private static final long serialVersionUID = 1L;

    public boolean add(T1 t1, T2 t2, T3 t3, T4 t4) {
        return add(new FourDimElement<>(t1, t2, t3, t4));
    }

    public void add(int index, T1 t1, T2 t2, T3 t3, T4 t4) {
        add(index, new FourDimElement<>(t1, t2, t3, t4));
    }

    public FourDimElement<T1, T2, T3, T4> setT1(int index, T1 t1) {
        final FourDimElement<T1, T2, T3, T4> e = get(index);
        e.setT1(t1);
        return set(index, e);
    }

    public FourDimElement<T1, T2, T3, T4> setT2(int index, T2 t2) {
        final FourDimElement<T1, T2, T3, T4> e = get(index);
        e.setT2(t2);
        return set(index, e);
    }

    public FourDimElement<T1, T2, T3, T4> setT3(int index, T3 t3) {
        final FourDimElement<T1, T2, T3, T4> e = get(index);
        e.setT3(t3);
        return set(index, e);
    }
    
    public FourDimElement<T1, T2, T3, T4> setT4(int index, T4 t4) {
        final FourDimElement<T1, T2, T3, T4> e = get(index);
        e.setT4(t4);
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

    public T4 getT4(int index) {
        return get(index).getT4();
    }
    
    public List<T1> getT1List() {
        final List<T1> l1 = new CopyOnWriteArrayList<>();
        for (final ListIterator<FourDimElement<T1, T2, T3, T4>> iter = listIterator(); iter.hasNext();) {
            final FourDimElement<T1, T2, T3, T4> element = iter.next();
            l1.add(element.getT1());
        }
        return l1;
    }

    public List<T2> getT2List() {
        final List<T2> l2 = new CopyOnWriteArrayList<>();
        for (final ListIterator<FourDimElement<T1, T2, T3, T4>> iter = listIterator(); iter.hasNext();) {
            final FourDimElement<T1, T2, T3, T4> element = iter.next();
            l2.add(element.getT2());
        }
        return l2;
    }
    
    public List<T3> getT3List() {
        final List<T3> l3 = new CopyOnWriteArrayList<>();
        for (final ListIterator<FourDimElement<T1, T2, T3, T4>> iter = listIterator(); iter.hasNext();) {
            final FourDimElement<T1, T2, T3, T4> element = iter.next();
            l3.add(element.getT3());
        }
        return l3;
    }
    
    public List<T4> getT4List() {
        final List<T4> l4 = new CopyOnWriteArrayList<>();
        for (final ListIterator<FourDimElement<T1, T2, T3, T4>> iter = listIterator(); iter.hasNext();) {
            final FourDimElement<T1, T2, T3, T4> element = iter.next();
            l4.add(element.getT4());
        }
        return l4;
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
    
    public int indexOfT4(T4 t4) {
        return getT4List().indexOf(t4);
    }

}
