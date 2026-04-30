package net.sf.genomeview.data.provider;

import java.util.List;

import net.sf.jannot.Location;

/**
 * 
 * @author Thomas Abeel
 *
 * @param <T> the data that will be provided in the future
 */
public interface DataCallback<T> {

	/**
	 * This should be called when data is available for given location
	 * 
	 * @param l  the {@link Location}
	 * @param it a list of data of type T
	 */
	public void dataReady(Location l, List<T> it);
}
