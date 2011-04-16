/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.comparative;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.HashMap;

import javax.swing.JViewport;

import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.SyntenicBlock;

/**
 * Track to display syntenic information.
 * 
 * @author Thomas Abeel
 * 
 */
public class SyntenicTrack extends Track {

	// private Entry ref;
	// private String target;
	private ColorGradient gradient;
	private int colors = 512;

	private Rectangle hit(int x, int y) {
		for (Rectangle r : hitmap.keySet()) {
			if (r.contains(x, y)) {
				return r;
			}
		}
		return null;
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent source) {
		super.mouseClicked(x, y, source);
		if (!source.isConsumed()) {
			Rectangle r = hit(x, y);
			System.out.println("clickclick: " + r);
			if (r != null && source.getClickCount() > 1) {
				SyntenicBlock sb = hitmap.get(r);
				Entry e = model.entry(sb.target());
				if (e != null) {
					model.setSelectedEntry(e);
					model.setAnnotationLocationVisible(sb.targetLocation());
					return true;
				}

			}

		}
		return true;

	}

	public SyntenicTrack(Model model, DataKey ref, String target) {
		super(ref, model, true, false);
		// this.ref = ref;
		// this.target = target;
		this.gradient = new ColorGradient();
		gradient.addPoint(Color.red);
		gradient.addPoint(Color.yellow);
		gradient.addPoint(Color.green);
		gradient.addPoint(Color.blue);
		gradient.createGradient(colors);

	}

	@Override
	public String displayName() {
		return "Synteny " + "foo" + " - " + "bar";
	}

	private HashMap<Rectangle, SyntenicBlock> hitmap = new HashMap<Rectangle, SyntenicBlock>();

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double width,JViewport view,TrackCommunicationModel tcm) {
		return 25;
		// hitmap.clear();
		//
		// if (!e.getID().equals(ref)) {
		// // Dont paint when reference does not match
		// return 0;
		// } else {
		// double colorBlockLength = (e.sequence.size() + 1.0) / colors;
		// /* Reference color scheme in 20 steps */
		// if (target.equals(ref)) {
		// for (int i = 0; i <= 20; i++) {
		// int length = model.getAnnotationLocationVisible().end() -
		// model.getAnnotationLocationVisible().start() + 1;
		// int start = model.getAnnotationLocationVisible().start() + (int)
		// (length / 20.0 * i);
		// int end = model.getAnnotationLocationVisible().start() + (int)
		// (length / 20.0 * (i + 1));
		// Color startColor = gradient.getColor((int) (start /
		// colorBlockLength));
		// Color endColor = gradient.getColor((int) (end / colorBlockLength));
		// int screenStart = Convert.translateGenomeToScreen(start,
		// model.getAnnotationLocationVisible(), width);
		// int screenEnd = Convert.translateGenomeToScreen(end,
		// model.getAnnotationLocationVisible(), width);
		// GradientPaint gp = new GradientPaint(screenStart, 0, startColor,
		// screenEnd, 0, endColor);
		// g.setPaint(gp);
		// g.fillRect(screenStart, yOffset + 15, screenEnd - screenStart + 1,
		// 10);
		//
		// }
		//
		// } else {
		//
		// List<SyntenicBlock> list =
		// model.entries().syntenic.get(e,model.getAnnotationLocationVisible());
		// for (SyntenicBlock sb : list) {
		// if (sb.target().equals(target)) {
		//
		// Location targetLoc = sb.targetLocation();
		// Location refLoc = sb.refLocation();
		// if (refLoc.overlaps(model.getAnnotationLocationVisible())) {
		//
		// try {
		// // Color startColor = gradient.getColor((int)
		// // (refLoc.start() / colorBlockLength));
		// // Color endColor = gradient.getColor((int)
		// // (refLoc.end() / colorBlockLength));
		// //
		// // int screenStart =
		// // Convert.translateGenomeToScreen(targetLoc.start(),
		// // model.getAnnotationLocationVisible(), width);
		// // int screenEnd =
		// // Convert.translateGenomeToScreen(targetLoc.end(),
		// // model.getAnnotationLocationVisible(), width);
		// Color startColor = gradient.getColor((int) (targetLoc.start() /
		// colorBlockLength));
		// Color endColor = gradient.getColor((int) (targetLoc.end() /
		// colorBlockLength));
		//
		// int screenStart = Convert.translateGenomeToScreen(refLoc.start(),
		// model.getAnnotationLocationVisible(), width);
		// int screenEnd = Convert.translateGenomeToScreen(refLoc.end(),
		// model.getAnnotationLocationVisible(), width);
		// GradientPaint gp = new GradientPaint(screenStart, 0, startColor,
		// screenEnd, 0, endColor);
		// g.setPaint(gp);
		// Rectangle r = new Rectangle(screenStart, yOffset + 15, screenEnd -
		// screenStart + 1, 10);
		// g.fill(r);
		// r.translate(0, -yOffset);
		// hitmap.put(r, sb);
		// } catch (Exception x) {
		// System.err.println(refLoc);
		// System.err.println(colorBlockLength);
		// }
		// }
		// }
		// }
		// }
		//
		// g.setColor(Color.black);
		// g.drawString(displayName(), 10, yOffset + 13);
		// return 25;
		//
		// }
	}

	public String reference() {
		return "foo";
	}

	public String target() {
		return "bar";
	}

}
