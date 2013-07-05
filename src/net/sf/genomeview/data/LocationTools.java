/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import net.sf.jannot.Location;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class LocationTools {

	/**
	 * Returns the overlap between two locations, or <code>null</code> if there is no overlap.
	 * @param l1
	 * @param l2
	 * @return
	 */
	public static Location getOverlap(Location l1,Location l2){
		int x=Math.max(l1.start, l2.start);
		int y=Math.min(l1.end, l2.end);
		if(x<=y)
			return new Location(x,y);
		else
			return null;
	}
}
