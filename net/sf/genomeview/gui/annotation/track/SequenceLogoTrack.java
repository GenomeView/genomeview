/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import be.abeel.io.Serial;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
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

	
	public void draw(Map<Integer,String> map,Graphics2D g,int numAlign,int position,int lineHeight,Model m,double width,int yOffset){
		
		int left = lineHeight;
		for (int key : map.keySet()) {
			for (char c : map.get(key).toCharArray()) {
				Color ntColor = Configuration.getNucleotideColor(c);
				// System.out.println(c + "\t" + key);
				double fraction = key
						/ (double) numAlign;

				Font font = new Font("Sans serif", 1, lineHeight);
//				float f3 = 1.5F;
//				System.out.println(position + "\t" + c + "\t" + lineHeight
//						+ "\t" + fraction + "\t"
//						+ (fraction * lineHeight));
				Font font2 = font.deriveFont(AffineTransform
						.getScaleInstance(width/lineHeight*1.2, fraction*1.4));

				GlyphVector glyphvector = font2.createGlyphVector(g
						.getFontRenderContext(), "" + c);
//
				Rectangle2D stringSize = font2.getStringBounds("" + c,
						g.getFontRenderContext());
				int x = (int) (((position - model.getAnnotationLocationVisible().start()) * width)+(width-stringSize.getWidth())/2);
				int y = (int) (yOffset + left);
//				System.out.println("\t"+x+"\t" + (y - yOffset));
//				LineMetrics lm=font.getLineMetrics("" + c, g
//						.getFontRenderContext());
//				System.out.println("-"+lm.getDescent()+"\t"+lm.getLeading()+"\t"+lm.getAscent()+"\t"+lm.getHeight());
				g.translate(x, y);
				left -= fraction*lineHeight;
				java.awt.Shape shape = glyphvector.getGlyphOutline(0);
				g.setColor(ntColor);
				g.fill(shape);
				g.translate(-x, -y);

			}
		}
	}
	@Override
	public int paint(Graphics g1, Entry e, int yOffset, double screenWidth) {
		Graphics2D g = (Graphics2D) g1;
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh = 40;
		g.setColor(Color.LIGHT_GRAY);
		g.fillRect(0, yOffset, (int) screenWidth, lineHeigh);
		if (r.length() > 100000) {
			g.setColor(Color.BLACK);
			g.drawString("Too much data in alignment, zoom in to see details",
					5, yOffset + lineHeigh - 2);
			return lineHeigh;
		}
		double width = screenWidth / (double) r.length();
		int grouping = (int) Math.ceil(1.0 / width);
		for (int i = r.start(); i <= r.end(); i += grouping) {
			// TODO do something with zoom-out
			if (model.getAnnotationLocationVisible().length() < 100) {
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
				draw(map,g,e.alignment.numAlignments(), i, lineHeigh, model, width,  yOffset);

			}
		}

		return lineHeigh;

	}

}
