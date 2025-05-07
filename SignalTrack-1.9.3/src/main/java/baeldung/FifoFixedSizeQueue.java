package baeldung;

/**
 *
 * @author Baeldung
 */
import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FifoFixedSizeQueue<E> extends AbstractQueue<E> {

    /** The queued items */
    private final Object[] items;

    /** Number of elements in the queue */
    private int count;

    public FifoFixedSizeQueue(int capacity) {
        items = new Object[capacity];
        count = 0;
    }

    @Override
    public boolean offer(E e) {
        if (e == null) {
            throw new NullPointerException("Queue doesn't allow nulls");
        }
        if (count == items.length) {
            this.poll();
        }
        this.items[count] = e;
        count++;
        return true;
    }

    @Override
    public E poll() {
        if (count <= 0) {
            return null;
        }
        @SuppressWarnings("unchecked")
        final E item = (E) items[0];
        shiftLeft();
        count--;
        return item;
    }
    
    private void shiftLeft() {
        int i = 1;
        while (i < items.length) {
            if (items[i] == null) {
                break;
            }
            items[i - 1] = items[i];
            i++;
        }
    }

    @SuppressWarnings("unchecked")
	@Override
    public E peek() {
        return count <= 0 ? null : (E) items[0];
    }
    
    @SuppressWarnings("unchecked")
    public E peek(int i) {
        return count <= 0 ? null : (E) items[i];
    }
    
    @Override
    public int size() {
        return count;
    }

    @SuppressWarnings("unchecked")
	@Override
    public Iterator<E> iterator() {
    	final List<E> list = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            list.add((E) items[i]);
        }
        return list.iterator();
    }
    
}
