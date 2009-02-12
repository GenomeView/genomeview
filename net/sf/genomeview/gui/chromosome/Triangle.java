/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome;

import java.awt.Polygon;

import net.sf.jannot.Strand;

/**
 * A simple directed triangle implementation.
 * 
 * @author thpar
 * 
 */
public class Triangle extends Polygon {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8488645244273066710L;

	public Triangle(int x, int y, int w, int h, Strand strand) {

		if (strand == Strand.REVERSE) {
			this.addPoint(x + w, y);
			this.addPoint(x + w, y + h);
			this.addPoint(x, y + h / 2);
		} else if (strand == Strand.FORWARD) {
			this.addPoint(x, y);
			this.addPoint(x + w, y + h / 2);
			this.addPoint(x, y + h);
			this.addPoint(x, y);
		} else {
			this.addPoint(x + w / 2, y);
			this.addPoint(x + w, y + h);
			this.addPoint(x, y + h);
		}
	}

	public Triangle(int x, int y, int w, int h) {
		this(x, y, w, h, Strand.UNKNOWN);
	}
}
