/**
 * %HEADER%
 */
package net.sf.jannot;

/**
 * Data for an entry
 * 
 * @author Thomas Abeel
 */
public interface Data<K> {

	/**
	 * Gets data. The selected data should cover [start,end[. The coordinates
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
	public Iterable<K> get(int start, int end);

	public Iterable<K> get();

	/**
	 * @return
	 */
	public boolean canSave();
	
	public String label();

}