/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * 
 * @author Thomas
 * 
 * @param <T>
 */
public class NoFailIterable<T> implements Iterable<T> {
	private ArrayList<T> list = null;

	public NoFailIterable(ArrayList<T> list) {
		this.list = list;
	}

	@Override
	public Iterator<T> iterator() {
		return new Iterator<T>() {
			private int idx = 0;

			@Override
			public boolean hasNext() {
				return idx <= list.size() - 1;
			}

			@Override
			public T next() {
				return list.get(idx++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
	}
}