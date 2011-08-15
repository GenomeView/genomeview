package net.sf.genomeview.gui.viztracks.comparative;

import java.awt.Graphics;

import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Location;
import net.sf.jannot.alignment.maf.AbstractAlignmentBlock;
import net.sf.jannot.alignment.maf.AbstractAlignmentSequence;

class MAFVizBuffer {
	private int[] counts = null;

	public MAFVizBuffer(Iterable<AbstractAlignmentBlock> abs, double screenWidth, Location visible) {
		counts = new int[(int) Math.ceil(screenWidth)];
		for (AbstractAlignmentBlock ab : abs) {
//			AbstractAlignmentSequence as = ab.getAlignmentSequence(0);
			int start = Convert.translateGenomeToScreen(ab.start(), visible, screenWidth);
			int end = Convert.translateGenomeToScreen(ab.end(), visible, screenWidth);
			for (int i = start; i < end; i++) {
				if (i >= 0 && i < counts.length)
					counts[i] += ab.size();
			}
		}
	}

	public int draw(Graphics g, int yOffset, int lineHeight) {
		int yMax = 0;
		for (int i = 0; i < counts.length; i++) {
			if (counts[i] > yMax)
				yMax = counts[i];
			g.drawLine(i, yOffset, i, yOffset + counts[i] * lineHeight);
		}

		return yMax * lineHeight;
	}

}
