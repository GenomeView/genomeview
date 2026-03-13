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

import com.lowagie.text.pdf.GrayColor;

import net.sf.genomeview.core.ColorGradient;
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
	private static final int colors = 512;
	private static final ColorGradient gradient = makeGradient();
	private static final int ROW_HEIGHT = 25;

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
		log.info("painting syntenic track");
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
		final Location visible = model.vlm.getVisibleLocation();
		final int length = visible.length();
		final Location refrange = data.getRange(reference);

		int row = 0;
		for (String target : data.getReferences()) {
			// paint syntenic gradient graph for all available targets

			if (target.equals(reference)) {
				paintGradient(row, 0d, 1d, refrange, width, g);
			} else {
				for (SyntenicBlock d : data.get(reference, target)) {
					final Location refloc = d.refLocation();
					paintGradient(row, refrange.fraction(refloc.start),
							refrange.fraction(refloc.end), d.targetLocation(),
							width, g);
				}

			}

			g.setColor(Color.black);
			g.drawString(target, 10, yOffset + 13 + row * ROW_HEIGHT);

			row++;
		}
//
//
//				/* Reference color scheme in 20 steps */
//				for (int i = 0; i <= 20; i++) {
//					final int start = visible.start()
//							+ (int) (length / 20.0 * i);
//					int end = visible.start() + (int) (length / 20.0 * (i + 1));
//					Color startColor = gradient
//							.getColor((int) (start / colorBlockLength));
//					Color endColor = gradient
//							.getColor((int) (end / colorBlockLength));
//					int screenStart = Convert.translateGenomeToScreen(start,
//							visible, width);
//					int screenEnd = Convert.translateGenomeToScreen(end,
//							visible, width);
//
//					GradientPaint gp = new GradientPaint(screenStart, 0,
//							startColor, screenEnd, 0, endColor);
//					g.setPaint(gp);
//					g.fillRect(screenStart, yOffset + 15,
//							screenEnd - screenStart + 1, 10);
//
//				}
//
//			} else {
//				for (SyntenicBlock sb : data.get(reference, target)) {
//
//					Location targetLoc = sb.targetLocation();
//					Location refLoc = sb.refLocation();
//					if (refLoc.overlaps(visible.start, visible.end)) {
//
//						try {
//							// Color startColor = gradient.getColor((int)
//							// (refLoc.start() / colorBlockLength));
//							// Color endColor = gradient.getColor((int)
//							// (refLoc.end() / colorBlockLength));
//							//
//							// int screenStart =
//							// Convert.translateGenomeToScreen(targetLoc.start(),
//							// model.getAnnotationLocationVisible(), width);
//							// int screenEnd =
//							// Convert.translateGenomeToScreen(targetLoc.end(),
//							// model.getAnnotationLocationVisible(), width);
//							Color startColor = gradient
//									.getColor((int) (targetLoc.start()
//											/ colorBlockLength));
//							Color endColor = gradient.getColor(
//									(int) (targetLoc.end() / colorBlockLength));
//
//							int screenStart = Convert.translateGenomeToScreen(
//									refLoc.start(), visible, width);
//							int screenEnd = Convert.translateGenomeToScreen(
//									refLoc.end(), visible, width);
//							GradientPaint gp = new GradientPaint(screenStart, 0,
//									startColor, screenEnd, 0, endColor);
//							g.setPaint(gp);
//							Rectangle r = new Rectangle(screenStart,
//									yOffset + 15, screenEnd - screenStart + 1,
//									10);
//							g.fill(r);
//							r.translate(0, -yOffset);
//							hitmap.put(r, sb);
//						} catch (Exception x) {
//							log.error("syntenic paint error at " + refLoc, x);
//						}
//					}
//
//				}
//			}
		return ROW_HEIGHT * row;

	}

	/**
	 * paint gradient, at row, going from color i to color j inside the given
	 * range. Only the visible part can actually be painted so this has to be
	 * clipped appropriately
	 * 
	 * @param row   row number
	 * @param sc    the start color, a number in [0,1] 0 the start and 1 the end
	 *              color
	 * @param ec    the endcolor a number in [0,1] 0 the start and 1 the end
	 *              color
	 * @param range the genome positions to paint this gradient over.
	 * @param width the width of the view area. FIXME this should be int??
	 * @param g     graphics context {@link Graphics2D}
	 */
	private void paintGradient(int row, double sc, double ec, Location range,
			double width, Graphics2D g) {
		final Location visible = model.vlm.getVisibleLocation();
		double start = visible.fraction(range.start);
		if (start > 1.0)
			return; // entirely outside visible
		double end = visible.fraction(range.end);
		if (end < 0.0)
			return; // entirely outside visible
		int barStart, barEnd;
		double startColor, endColor;
		if (start >= 0.0) { // in visible range
			barStart = (int) (width * start);
			startColor = sc;
		} else {
			barStart = 0;
			startColor = sc + (ec - sc) * range.fraction(visible.start);
		}
		if (end <= 1.0) {
			barEnd = (int) (width * end);
			endColor = ec;
		} else {
			barEnd = (int) width;
			endColor = sc + (ec - sc) * range.fraction(visible.end);
		}

		GradientPaint gp = new GradientPaint(barStart, 0,
				new GrayColor((float) startColor), barEnd, 0,
				new GrayColor((float) endColor));
		g.setPaint(gp);
		Rectangle r = new Rectangle(barStart, row * ROW_HEIGHT + 5,
				barEnd - barStart + 1, 10);
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

	private static ColorGradient makeGradient() {
		ColorGradient gradient = new ColorGradient();
		gradient.addPoint(Color.red);
		gradient.addPoint(Color.yellow);
		gradient.addPoint(Color.green);
		gradient.addPoint(Color.blue);
		gradient.createGradient(colors);
		return gradient;
	}

}
