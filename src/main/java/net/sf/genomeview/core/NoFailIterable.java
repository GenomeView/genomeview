/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.util.Iterator;
import java.util.List;

/**
 * 
 * Iterable that will go over a list and will keep going even if the lists gets
 * modified while the iteration is in progress.
 * 
 * It will make a best effort to iterator over all elements, even ones that are
 * included after the iterator has started, but makes no guarantee that all
 * elements will be presented.
 * 
 * @author Thomas Abeel
 * 
 * @param <T>
 */
public class NoFailIterable<T> implements Iterable<T> {
	private List<T> list = null;

	public NoFailIterable(List<T> list) {
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
				if(idx>=list.size())
					return null;
				return list.get(idx++);
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
	}
}