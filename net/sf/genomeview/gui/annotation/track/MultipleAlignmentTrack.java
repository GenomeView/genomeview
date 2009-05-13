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
	private int index;
	private String name;

	public MultipleAlignmentTrack(String name,int index, Model model, boolean b) {
		super(model, b);
		this.index=index;
		this.name=name;
	}

	@Override
	public String displayName() {
		return "MA: " + name;
	}

	@Override
	public int paint(Graphics g, Entry e, int yOffset, double screenWidth) {
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh = 15;
		
		Alignment align = e.alignment.getAlignment(index);
		
		if (align != null) {
			if (r.length() > 100000) {
				g.setColor(Color.BLACK);
				g.drawString(
						"Too much data in alignment, zoom in to see details",
						5, yOffset + lineHeigh - 2);
				return lineHeigh;
			}
			double width = screenWidth / (double) r.length();
			int grouping = (int) Math.ceil(1.0 / width);
			for (int i = r.start(); i <= r.end(); i += grouping) {
				char nt=' ';
				double conservation=0;
				boolean dash=false;
				for (int j = 0; j < grouping; j++) {
					nt = align.getNucleotide(i+j);
					conservation += e.alignment.getConservation(i+j);
					if(nt=='-')
						dash=true;
					
				}
				conservation/=grouping;
				if (conservation == 1) {
					g.setColor(Color.BLACK);
				} else if (conservation > 0.75) {
					g.setColor(Color.DARK_GRAY);
				} else if (conservation > 0.5) {
					g.setColor(Color.LIGHT_GRAY);
				} else
					g.setColor(Color.WHITE);
				if (dash) {
					g.setColor(Color.RED);
				}

				g.fillRect((int) ((i - r.start()) * width), yOffset,
						(int) (width*grouping) + 1, lineHeigh);
				if (model.getAnnotationLocationVisible().length() < 100) {
					Rectangle2D stringSize = g.getFontMetrics()
							.getStringBounds("" + nt, g);
					if (conservation == 1) {
						g.setColor(Color.WHITE);
					} else if (conservation > 0.75) {
						g.setColor(Color.WHITE);
					} else if (conservation > 0.5) {
						g.setColor(Color.BLACK);
					} else
						g.setColor(Color.BLACK);
					g.drawString("" + nt,
							(int) (((i - r.start()) * width - stringSize
									.getWidth() / 2) + (width / 2)), yOffset
									+ lineHeigh - 2);
				}
			}

			return lineHeigh;
		}
		return 0;
	}

	public int getIndex() {
		return index;
	}
}
