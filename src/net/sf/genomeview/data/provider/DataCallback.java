package net.sf.genomeview.data.provider;

import java.util.List;

/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public interface DataCallback<T> {

	
	public void dataReady(List<T>it);
}
