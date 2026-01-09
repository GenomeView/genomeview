package net.sf.jannot;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This Iterator is used to iterate over Lists without fail-fast behavior.
 */
public class ListIterable<T> implements Iterable<T> {

	public ListIterable(List<T> array) {
		this.array = array;
	}

	private List<T> array;

	

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new ListIterator<T>(array);
	}
}
