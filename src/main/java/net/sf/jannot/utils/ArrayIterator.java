package net.sf.jannot.utils;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This Iterator is used to iterate over Arrays.
 */
public class ArrayIterator<T> implements Iterator<T> {
	/** DOCUMENT ME! */
	private Object array;

	/** DOCUMENT ME! */
	private int position;

	/** DOCUMENT ME! */
	private int length;

	/**
	 * create an Iterator for the Array array.
	 * 
	 * @param array
	 *            java.lang.Object
	 * 
	 * @throws UnsupportedOperationException
	 *             if array is not an Array
	 */
	public ArrayIterator(Object array) {
		if (!array.getClass().isArray()) {
			throw new UnsupportedOperationException("ArrayIterator must be "
					+ "initialized with an Array to iterate over.");
		}

		this.array = array;
		position = 0;
		length = Array.getLength(array);
	}

	/**
	 * Returns <tt>true</tt> if the iteration has more elements. (In other
	 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
	 * rather than throwing an exception.)
	 * 
	 * @return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() {
		return (position < length);
	}

	/**
	 * Returns the next element in the interation.
	 * 
	 * @return the next element in the interation.
	 * 
	 * @throws NoSuchElementException
	 *             iteration has no more elements.
	 */
	public synchronized T next() throws NoSuchElementException {
		try {
			T result = (T) Array.get(array, position);
			position++;

			return result;
		} catch (ArrayIndexOutOfBoundsException e) {
			throw new NoSuchElementException(e.getMessage());
		}
	}

	/**
	 * Removes from the underlying collection the last element returned by the
	 * iterator (optional operation). This method can be called only once per
	 * call to <tt>next</tt>. The behavior of an iterator is unspecified if the
	 * underlying collection is modified while the iteration is in progress in
	 * any way other than by calling this method.
	 * 
	 * @throws UnsupportedOperationException
	 *             if the <tt>remove</tt> operation is not supported by this
	 *             Iterator.
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}
}
