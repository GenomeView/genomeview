/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.comparative;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JViewport;

import net.sf.genomeview.core.ColorGradient1;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.SyntenicBlock;
import net.sf.jannot.SyntenicData;
import net.sf.jannot.parser.SyntenicParser;

/**
 * Track to display syntenic information.
 * 
 * @author Thomas Abeel
 * 
 */
public class SyntenicTrack extends Track {
	private static final int ROW_HEIGHT = 15;

	// #paint steps for gradients
	private static final float PAINT_STEPS = 8;

	/**
	 * hitmap : key=a painted Rectangle and value=matching SyntenicBlock. Used
	 * to select when user clicks on it.
	 */
	private HashMap<Rectangle, SyntenicBlock> hitmap = new HashMap<Rectangle, SyntenicBlock>();

	/**
	 * @param model the {@link Model}
	 * @param ref   the {@link DataKey} referring to -presumably - the key under
	 *              which the syntenic data is stored. This should equal the
	 *              {@link SyntenicParser#SYNTENIC_KEY}
	 */
	public SyntenicTrack(Model model, DataKey ref) {
		super(ref, model, true, false);
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent source) {
		super.mouseClicked(x, y, source);
		if (!source.isConsumed()) {
			Rectangle r = hit(x, y);
			System.out.println("clickclick: " + r);
			if (r != null && source.getClickCount() > 1) {
				SyntenicBlock sb = hitmap.get(r);
				Entry e = model.entries().getEntry(sb.target());
				if (e != null) {
					model.setSelectedEntry(e);
					model.vlm.setAnnotationLocationVisible(sb.targetLocation());
					return true;
				}

			}

		}
		return true;

	}

	public String displayName() {
		return "Synteny " + "foo" + " - " + "bar";
	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double width,
			JViewport view, TrackCommunicationModel tcm) {
		hitmap.clear();

//		if (!e.getId().equals(ref)) 
//			// Dont paint when reference does not match
//			return 0;
//		}

		final SyntenicData data = (SyntenicData) entry.get(dataKey);
//		final List<SyntenicBlock> list = model.entries().syntenic.get(entry,
//				null);
		final String reference = entry.getID();
//		final double colorBlockLength = (data.getLength(reference) + 1.0)
//				/ colors;
		// the syntenic data may be shorter than the entire genome
		final Location refrange = data.getRange(reference);

		int row = 1; // row number excluding the reference row at top
		for (String target : data.getReferences()) {
			// paint syntenic gradient graph for all available targets

			if (target.equals(reference)) {
				paintGradient(yOffset, 0f, 1f, refrange, (float) width, g);
			} else {
				for (SyntenicBlock d : data.get(reference, target)) {
					final Location refloc = d.refLocation();
					float startf = (float) refrange.fraction(refloc.start);
					float endf = (float) refrange.fraction(refloc.end);
					if (!d.getRefStrand().equals(d.getTargetStrand())) {
						// direction is reversed or unknown. Swap the srart, end
						// colors
						float tmp = startf;
						startf = endf;
						endf = tmp;
					}

					paintGradient(yOffset + row * ROW_HEIGHT, startf, endf,
							d.targetLocation(), (float) width, g);
					g.setColor(Color.black);
					g.drawString(target, 10, yOffset + 5 + row * ROW_HEIGHT);
				}
				row++;

			}

		}

		return ROW_HEIGHT * row;

	}

	/**
	 * paint gradient, at yoffset from the top, going from color sc to color ec
	 * inside the given range. Only the visible part can actually be painted so
	 * this has to be clipped appropriately
	 * 
	 * @param yoffset the y-offset, distance pixels down from the top
	 * @param sc      the start color, a number in [0,1] 0 the start and 1 the
	 *                end color
	 * @param ec      the endcolor a number in [0,1] 0 the start and 1 the end
	 *                color
	 * @param range   the genome positions to paint this gradient over.
	 * @param width   the width of the view area. FIXME this should be int??
	 * @param g       graphics context {@link Graphics2D}
	 */
	private void paintGradient(int yoffset, float sc, float ec, Location range,
			float width, Graphics2D g) {
		final Location visible = model.vlm.getVisibleLocation();
		float start = (float) visible.fraction(range.start);
		if (start > 1.0)
			return; // entirely outside visible
		float end = (float) visible.fraction(range.end);
		if (end < 0.0)
			return; // entirely outside visible
		int barStart, barEnd;
		float startColor, endColor;
		if (start >= 0.0) { // in visible range
			barStart = (int) (width * start);
			startColor = sc;
		} else {
			barStart = 0;
			startColor = (float) (sc
					+ (ec - sc) * range.fraction(visible.start));
		}
		if (end <= 1.0) {
			barEnd = (int) (width * end);
			endColor = ec;
		} else {
			barEnd = (int) width;
			endColor = (float) (sc + (ec - sc) * range.fraction(visible.end));
		}

		float delta = (barEnd - barStart) / PAINT_STEPS;
		float dcol = (float) (endColor - startColor) / PAINT_STEPS;

		if (dcol < 0.01 | delta < 4) {
			paintPiece(yoffset, g, barStart, barEnd, startColor, endColor);
		} else {
			/*
			 * interpolating between start and end is bad if color distance is
			 * large, because it will skip our inbetween colors. To get around
			 * this we make a number of smaller steps so that we can compute the
			 * proper intermediate colors. A better solution would be a better
			 * GradientPaint class but that's beyond the current scope
			 */
			for (int n = 0; n < PAINT_STEPS; n++) {
				paintPiece(yoffset, g, (int) (barStart + n * delta),
						(int) (barStart + (n + 1) * delta),
						startColor + n * dcol, startColor + (n + 1) * dcol);
			}
		}
	}

	private void paintPiece(int yoffset, Graphics2D g, int barStart, int barEnd,
			float startColor, float endColor) {
		GradientPaint gp = new GradientPaint(barStart, 0,
				ColorGradient1.DEFAULT.get(startColor), barEnd, 0,
				ColorGradient1.DEFAULT.get(endColor));
		g.setPaint(gp);
		Rectangle r = new Rectangle(barStart, yoffset, barEnd - barStart + 1,
				10);
		g.fill(r);
	}

	public String reference() {
		return "foo";
	}

	public String target() {
		return "bar";
	}

	private Rectangle hit(int x, int y) {
		for (Rectangle r : hitmap.keySet()) {
			if (r.contains(x, y)) {
				return r;
			}
		}
		return null;
	}

}
