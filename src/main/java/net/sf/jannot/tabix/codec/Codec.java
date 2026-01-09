/**
 * %HEADER%
 */
package net.sf.jannot.tabix.codec;

import java.util.Iterator;

import net.sf.jannot.tabix.TabixLine;
import be.abeel.util.LRUCache;

/**
 * @author Thomas Abeel
 * 
 */
public abstract class Codec<T> implements Iterable<T> {

	private Iterable<TabixLine> in;
	protected LRUCache<TabixLine, T> lru;
	
	public Codec(Iterable<TabixLine> in,int lruSize) {
		this.in = in;
		this.lru=new LRUCache<TabixLine, T>(lruSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return new CodecIterator(in);
	}

	/**
	 * @param next
	 * @return
	 */
	public abstract T parse(TabixLine next);

	class CodecIterator implements Iterator<T> {

		private Iterator<TabixLine> it;

		/**
		 * @param in
		 */
		public CodecIterator(Iterable<TabixLine> in) {
			this.it = in.iterator();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		@Override
		public T next() {
			return parse(it.next());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove() {
			throw new UnsupportedOperationException();

		}

	}
}
