package net.sf.genomeview.data.provider;
/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public interface DataCallback<T> {

	
	public void dataReady(Iterable<T>it);
}
