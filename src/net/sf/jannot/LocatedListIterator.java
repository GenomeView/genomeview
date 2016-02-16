package net.sf.jannot;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * This Iterator is used to iterate over Arrays.
 */
public class LocatedListIterator<T extends Located> implements Iterator<T> {
	/** DOCUMENT ME! */
	private List<T> array;

	/* Number of values that have been returned so far */
	private int returned;

	/** DOCUMENT ME! */
	private int size;

	private int index;
	private Location location;

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
	public LocatedListIterator(List<T> array, Location location) {
		this.array = array;
		this.location = location;
		this.index=0;
		returned = 0;
		if (location == null)
			size = array.size();
		else {
			for(int i=0;i<array.size();i++){
				Located f=array.get(i);
				if (location.overlaps(f.start(),f.end())) {
					//System.out.println(l+"\t"+location);
					size++;
				}
			}
		}
//		/System.out.println("Size in located iterator: "+size);
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
	 * Returns the next element in the iteration.
	 * 
	 * @return the next element in the iteration.
	 * 
	 * @throws NoSuchElementException
	 *             iteration has no more elements.
	 */
	public synchronized T next() throws NoSuchElementException {
		if (location == null) {
			return array.get(returned++);
		} else {
			try {
				Located f=array.get(index);
				//while (!array.get(index).getLocation().overlaps(location)) {
				while(!location.overlaps(f.start(), f.end())){
					index++;
				f=array.get(index);
				}
				returned++;
				return array.get(index++);
			} catch (Exception e) {
				System.err.println(index+"\t"+array.size() + "\t" + size + "\t" + returned);
				throw new NoSuchElementException();
			}
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
