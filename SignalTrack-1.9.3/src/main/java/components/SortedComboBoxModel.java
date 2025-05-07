package components;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.swing.DefaultComboBoxModel;

public class SortedComboBoxModel<E extends Comparable<? super E>> extends DefaultComboBoxModel<E> {
	private static final long serialVersionUID = 1L;

	public SortedComboBoxModel() {
        super();
     }
	
    public SortedComboBoxModel(E[] items) {
        Arrays.sort(items);
        final int size = items.length;
        for (int i = 0; i < size; i++) {
        	if (items[i] instanceof String string) {
        		if (!string.isBlank()) {
        			super.addElement(items[i]);
        		}
        	} else {
        		super.addElement(items[i]);
        	} 
        }
        setSelectedItem(items[0]);
    }

    public SortedComboBoxModel(List<E> items) {
        Collections.sort(items);
        final int size = items.size();
        for (int i = 0; i < size; i++) {
        	if (items.get(i) instanceof String string) {
        		if (!string.isBlank()) {
        			super.addElement(items.get(i));
        		}
        	} else {
        		super.addElement(items.get(i));
        	}
        }
        setSelectedItem(items.get(0));
    }

    @Override
    public void addElement(E element) {
        insertElementAt(element, 0);
    }

    @Override
    public void insertElementAt(E element, int index) {
    	final int size = getSize();
        for (int i = 0; i < size; i++) {
        	final Comparable<? super E> c = getElementAt(index);
            if (c.compareTo(element) > 0) {
                break;
            }
        }
        super.insertElementAt(element, index);
    }
}
