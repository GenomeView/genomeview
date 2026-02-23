/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import net.sf.genomeview.data.GenomeViewScheduler;


/**
 * Data provider for visualization tracks. 
 * 
 * Methods should return immediately,
 * data fetching should be done in background threads.
 * 
 * @see GenomeViewScheduler
 * 
 * @author Thomas Abeel
 * 
 * @param <T>
 */
public interface DataProvider<T> {

	/**
	 * Get data. The selected data should cover [start,end[. The coordinates
	 * are one based.
	 * 
	 * @param start
	 *            the start coordinate, this one will be included. This is a
	 *            one-based coordinate.
	 * @param end
	 *            the end coordinate, this one will not be included. This is a
	 *            one-based coordinate.
	 * @return the selected data.
	 */
	public void get(int start, int end,DataCallback<T> cb);

//	public Iterable<Status> getStatus(int start, int end);


}
