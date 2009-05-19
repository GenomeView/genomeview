/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Alignment;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;

/**
 * Represents the conservation at a position with a sequence logo.
 * 
 * @author Thomas Abeel
 * 
 */
public class SequenceLogoTrack extends Track {
	public SequenceLogoTrack(Model model) {
		super(model, true);
	}

	// private int index;
	private String name;

	@Override
	public String displayName() {
		return "Multiple alignment sequence logo";
	}

	@Override
	public int paint(Graphics g1, Entry e, int yOffset, double screenWidth) {
		Graphics2D g=(Graphics2D)g1;
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh = 25;

		if (r.length() > 100000) {
			g.setColor(Color.BLACK);
			g.drawString("Too much data in alignment, zoom in to see details",
					5, yOffset + lineHeigh - 2);
			return lineHeigh;
		}
		double width = screenWidth / (double) r.length();
		int grouping = (int) Math.ceil(1.0 / width);
		for (int i = r.start(); i <= r.end(); i += grouping) {
			char nt = ' ';
			double conservation = 0;
			boolean dash = false;
			// for (int j = 0; j < grouping; j++) {
			// nt = align.getNucleotide(i + j);
			// conservation += e.alignment.getConservation(i + j);
			// if (nt == '-')
			// dash = true;
			//
			// }
			// conservation /= grouping;
			// if (conservation == 1) {
			// g.setColor(Color.BLACK);
			// } else if (conservation > 0.75) {
			// g.setColor(Color.DARK_GRAY);
			// } else if (conservation > 0.5) {
			// g.setColor(Color.LIGHT_GRAY);
			// } else
			// g.setColor(Color.WHITE);
			// if (dash) {
			// g.setColor(Color.RED);
			// }
			//
			// g.fillRect((int) ((i - r.start()) * width), yOffset,
			// (int) (width * grouping) + 1, lineHeigh);
			// TODO do something with zoom-out
			if (model.getAnnotationLocationVisible().length() < 100) {
				Rectangle2D stringSize = g.getFontMetrics().getStringBounds(
						"" + nt, g);
				SortedMap<Integer, String> map = new TreeMap<Integer, String>(
						Collections.reverseOrder());

				map.put(e.alignment.getNucleotideCount('a', i), "A");
				if (map.containsKey(e.alignment.getNucleotideCount('c', i))) {
					map.put(e.alignment.getNucleotideCount('c', i), map
							.get(e.alignment.getNucleotideCount('c', i))
							+ "C");
				} else {
					map.put(e.alignment.getNucleotideCount('c', i), "C");
				}
				if (map.containsKey(e.alignment.getNucleotideCount('g', i))) {
					map.put(e.alignment.getNucleotideCount('g', i), map
							.get(e.alignment.getNucleotideCount('g', i))
							+ "G");
				} else {
					map.put(e.alignment.getNucleotideCount('g', i), "G");
				}
				if (map.containsKey(e.alignment.getNucleotideCount('t', i))) {
					map.put(e.alignment.getNucleotideCount('t', i), map
							.get(e.alignment.getNucleotideCount('t', i))
							+ "T");
				} else {
					map.put(e.alignment.getNucleotideCount('t', i), "T");
				}
				int left = 25;
				for (int key : map.keySet()) {
					for (char c : map.get(key).toCharArray()) {
						Color ntColor = Configuration.getNucleotideColor(c);
						System.out.println(c + "\t" + key);
						int fraction = (int) (key
								/ (double) e.alignment.numAlignments() * lineHeigh);

						Font font = new Font("Sans serif", 1, lineHeigh);
						float f3 = 1.5F;
						if (c == 'W' || c == 'M')
							f3 = 1.2F;
//						float f4 = 0.0F;
//						if (c == 'I')
//							f4 = 4.5F;
						Font font2 = font.deriveFont(AffineTransform
								.getScaleInstance(f3, fraction));
						GlyphVector glyphvector = font2.createGlyphVector(
								g.getFontRenderContext(), String
										.valueOf(c));
//						g.translate((float) i + f4, i2);
//						g.drawString("" + nt,
//								(int) (((i - r.start()) * width - stringSize
//										.getWidth() / 2) + (width / 2)), yOffset
//										+ lineHeigh - 2);
						int x=(int) (((i - r.start()) * width - stringSize
								.getWidth() / 2) + (width / 2));
						int y=yOffset
						+ lineHeigh - left+fraction;
						g.translate(x,y);
						left-=fraction;
						java.awt.Shape shape = glyphvector.getGlyphOutline(0);
						g.setColor(ntColor);
						g.fill(shape);
						g.translate(-x, -y);

					}
				}

			}
		}

		return lineHeigh;

	}

}
