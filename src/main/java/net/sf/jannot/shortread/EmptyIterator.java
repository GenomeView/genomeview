/**
 * %HEADER%
 */
package net.sf.jannot.shortread;

import net.sf.samtools.util.CloseableIterator;
/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public class EmptyIterator<T> implements CloseableIterator<T> {

	public EmptyIterator() {
	}

	public void close() {
		// ignore
	}

	public boolean hasNext() {
		return false;
	}

	public T next() {
		return null;
	}

	public void remove() {
		// ignore
	}
}
