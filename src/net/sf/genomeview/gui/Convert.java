/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import net.sf.jannot.Location;

public class Convert {
	/**
	 * Translates a coordinate on the genome to a screen coordinate. This is
	 * only needed for the X coordinate
	 * 
	 * @param g
	 *            the graphics context for the visible Location
	 * @param pos
	 *            the position to translate, this may be outside the visible
	 *            Location
	 * @return
	 */
	public static int translateGenomeToScreen(int pos, Location r,double screenWidth) {
		int genomeWidth = r.length();
		int relativePos = pos - r.start;
		return (int) (screenWidth / genomeWidth * relativePos);

	}

	/**
	 * Translates a coordinate on the screen to a genome coordinate. This is
	 * only needed for the X coordinate
	 * 
	 * @param g
	 *            the graphics context for the visible Location
	 * @param pos
	 *            the position to translate,
	 * 
	 * @return
	 */
	public static int translateScreenToGenome(int pos, Location r,double screenWidth) {
		// Location r = model.getAnnotationLocationVisible();
		int genomeWidth = r.length();
		return (int) (pos / screenWidth * genomeWidth) + r.start;

	}
}
