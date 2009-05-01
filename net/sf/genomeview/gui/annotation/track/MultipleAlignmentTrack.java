/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.geom.Rectangle2D;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Alignment;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;

public class MultipleAlignmentTrack extends Track {
	private String name;

	public MultipleAlignmentTrack(String name, Model model, boolean b) {
		super(model, b);
		this.name = name;
	}

	@Override
	public String displayName() {
		return "MA: " + name;
	}

	@Override
	public int paint(Graphics g, Entry e, int yOffset, double screenWidth) {
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh=15;
		Alignment align = e.alignment.getAlignment(name);
		if (align != null) {
			if(r.length()>1500){
				g.drawString("Too much data, zoom in to see details",
						5, yOffset+lineHeigh-2);
				return lineHeigh;
			}
			double width = screenWidth / (double) r.length();

			for (int i = r.start(); i <= r.end(); i++) {
				char nt;

				nt = align.sequence().getNucleotide(i);

				// if (spliceSitePaint) {
				// Color spliceSite = checkSpliceSite(i, model, forward);
				//
				// if (spliceSite != null) {
				// g.setColor(spliceSite);
				// g.fillRect((int) ((i - r.start()) * width), 3 * lineHeight
				// + (forward ? 0 : tickHeight + lineHeight) + gap
				// + yOffset, (int) (2 * width) + 1, lineHeight - 2
				// * gap);
				// }
				// } else if (nucleotidePaint) {
				 g.setColor(Configuration.getNucleotideColor(nt));
				 g.fillRect((int) ((i - r.start()) * width), yOffset, (int) width + 1, lineHeigh);
				//
				// }

				if (model.getAnnotationLocationVisible().length() < 100) {
					Rectangle2D stringSize = g.getFontMetrics()
							.getStringBounds("" + nt, g);
					g.setColor(Color.black);
					g.drawString("" + nt,
							(int) (((i - r.start()) * width - stringSize
									.getWidth() / 2) + (width / 2)), yOffset+lineHeigh-2);
				}
			}

			return lineHeigh;
		}
		return 0;
	}
}
