/**
 * %HEADER%
 * 
 */
package net.sf.genomeview.core;

import java.util.HashMap;

/**
 * 
 * @author Thomas Abeel
 * 
 * @param <T>
 *            forward key, reverse value type
 * @param <X>
 *            reverse key, forward value type
 */
public class BiMap<T, X> {
	private HashMap<T, X> forward = new HashMap<T, X>();
	private HashMap<X, T> reverse = new HashMap<X, T>();

	public X getForward(T key) {
		return forward.get(key);
	}

	public T getReverse(X key) {
		return reverse.get(key);
	}

	public int size() {
		return forward.size();
	}

	public void clear() {
		forward.clear();
		reverse.clear();
	}

	public void putForward(T e, X i) {
		forward.remove(e);
		reverse.remove(i);
		forward.put(e, i);
		reverse.put(i, e);

	}

	public void putReverse(X i, T e) {
		putForward(e, i);

	}

}