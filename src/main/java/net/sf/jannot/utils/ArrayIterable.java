package net.sf.jannot.utils;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This Iterator is used to iterate over Arrays.
 */
public class ArrayIterable<T> implements Iterable<T> {
	private Object array;

	/**
	 * create an Iterator for the Array array.
	 * 
	 * @param array
	 *            java.lang.Object
	 * 
	 * @throws UnsupportedOperationException
	 *             if array is not an Array
	 */
	public ArrayIterable(Object array) {
		if (!array.getClass().isArray()) {
			throw new UnsupportedOperationException("ArrayIterator must be "
					+ "initialized with an Array to iterate over.");
		}

		this.array = array;

	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new ArrayIterator<T>(array);
	}
}
