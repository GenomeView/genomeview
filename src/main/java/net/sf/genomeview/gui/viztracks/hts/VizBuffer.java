/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Graphics2D;
/**
 * 
 * @author Thomas Abeel
 *
 */
interface VizBuffer {
	int draw(Graphics2D g, int y, double screenWidth);

	String getTooltip(int mouseX);

}
