package net.sf.jannot;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This Iterator is used to iterate over Arrays.
 */
public class ListIterator<T> implements Iterator<T> {
	/** DOCUMENT ME! */
	private List<T> array;

	/* Number of values that have been returned so far */
	private int returned;

	/** DOCUMENT ME! */
	private int size;

	/**
	 * create an Iterator for the Array array.
	 * 
	 * @param array
	 *            java.lang.Object
	 * @param location
	 * 
	 * @throws UnsupportedOperationException
	 *             if array is not an Array
	 */
	public ListIterator(List<T> array) {
		this.array = array;
		returned = 0;
		size = array.size();

	}

	/**
	 * Returns <tt>true</tt> if the iteration has more elements. (In other
	 * words, returns <tt>true</tt> if <tt>next</tt> would return an element
	 * rather than throwing an exception.)
	 * 
	 * @return <tt>true</tt> if the iterator has more elements.
	 */
	public boolean hasNext() {
		return (returned < size);
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
		return array.get(returned++);

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
