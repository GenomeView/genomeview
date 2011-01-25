/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks;

import java.awt.Color;
import java.awt.Graphics2D;

import javax.swing.JViewport;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Location;
import net.sf.jannot.StringKey;

public class TickmarkTrack extends Track {
	public static final StringKey key = new StringKey("TICK&*(#%&*(@#%&*(@%(*TICK");

	public TickmarkTrack(Model model) {

		super(key, model, true, false);
	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth,JViewport view) {
		Location r = model.getAnnotationLocationVisible();
		g.setColor(Color.BLACK);
		g.drawLine(0, yOffset + 15, (int) screenWidth + 1, yOffset + 15);

		if (r.start() == r.end()) {
			return 32;
		}
		// determine the tickDistance, we aim for 10 ticks on screen.
		int length = r.length();
		int scale = (int) Math.log10(length / 10.0);
		int multiplier = (int) (length / Math.pow(10, scale + 1));
		int tickDistance = (int) (Math.pow(10, scale) * multiplier);
		if (tickDistance == 0)
			tickDistance = 1;
		// paint the ticks
		int currentTick = (r.start() - r.start() % tickDistance) + 1;
		boolean up = true;
		while (currentTick < r.end()) {
			int xpos = Convert.translateGenomeToScreen(currentTick, r, screenWidth);
			String s = "" + currentTick;

			if (up) {
				g.drawLine(xpos, yOffset + 2, xpos, yOffset + 28);
				g.drawString(s, xpos + 2, yOffset + 14);
			} else {
				g.drawLine(xpos, yOffset + 2, xpos, yOffset + 28);
				g.drawString(s, xpos + 2, yOffset + 26);
			}
			up = !up;

			currentTick += tickDistance;

		}
		return 32;
	}

	@Override
	public String displayName() {
		return "Ruler";
	}

}
