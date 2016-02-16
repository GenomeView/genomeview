package net.sf.jannot;

import java.lang.reflect.Array;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This Iterator is used to iterate over Lists without fail-fast behavior.
 */
public class LocatedListIterable<T extends Located> implements Iterable<T> {

	private Location location;

	public LocatedListIterable(List<T> array, Location l) {
		this.location = l;
		this.array = array;
	}

	private List<T> array;

	/**
	 * create an Iterator for the Array array.
	 * 
	 * @param array
	 *            java.lang.Object
	 * 
	 * @throws UnsupportedOperationException
	 *             if array is not an Array
	 */
	public LocatedListIterable(List<T> array) {
		this(array, null);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new LocatedListIterator<T>(array,location);
	}
}
