/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.List;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.ColorGradient;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.SyntenicBlock;

/**
 * Track to display syntenic information.
 * 
 * @author Thomas Abeel
 * 
 */
public class SyntenicTrack extends Track {

	private Entry ref;
	private Entry target;
	private ColorGradient gradient;
	private int colors = 512;

	public SyntenicTrack(Model model, Entry ref, Entry target) {
		super(model, true);
		this.ref = ref;
		this.target = target;
		this.gradient = new ColorGradient();
		gradient.addPoint(Color.red);
		gradient.addPoint(Color.yellow);
		gradient.addPoint(Color.green);
		gradient.addPoint(Color.blue);
		gradient.createGradient(colors);

	}

	@Override
	public String displayName() {
		return "Syntheny " + ref.getID() + " - " + target.getID();
	}

	@Override
	public int paint(Graphics g1, Entry e, int offset, double width) {
		Graphics2D g = (Graphics2D) g1;
		if (!e.equals(ref)) {
			// Dont paint when reference does not match
			return 0;
		} else {
			double colorBlockLength = (e.sequence.size() + 1) / colors;
			if (target.equals(ref)) {

				for (int i = 0; i <= 20; i++) {
					int length=model
					.getAnnotationLocationVisible().end()-model
					.getAnnotationLocationVisible().start()+1;
					int start=(int)(length/20.0*i);
					int end=(int)(length/20.0*(i+1));
//					System.out.println(start+"\t"+end);
					Color startColor = gradient
							.getColor((int) ( start/ colorBlockLength));
					Color endColor = gradient
							.getColor((int) (end / colorBlockLength));
					int screenStart = Convert.translateGenomeToScreen(start, model
							.getAnnotationLocationVisible(), width);
					int screenEnd = Convert.translateGenomeToScreen(end, model
							.getAnnotationLocationVisible(), width);
					GradientPaint gp = new GradientPaint(screenStart, 0,
							startColor, screenEnd, 0, endColor);
					g.setPaint(gp);
					g.fillRect( screenStart, offset,
							screenEnd-screenStart + 1, 10);

				}

			} else {

				List<SyntenicBlock> list = e.syntenic.get(model
						.getAnnotationLocationVisible());
				for (SyntenicBlock sb : list) {
					if (sb.target().equals(target)) {

						Location targetLoc = sb.targetLocation();
						if (targetLoc.overlaps(model
								.getAnnotationLocationVisible())) {
							Location refLoc = sb.refLocation();
							try {
								Color startColor = gradient
										.getColor((int) (refLoc.start() / colorBlockLength));
								Color endColor = gradient
										.getColor((int) (refLoc.end() / colorBlockLength));

								int screenStart = Convert
										.translateGenomeToScreen(
												targetLoc.start(),
												model
														.getAnnotationLocationVisible(),
												width);
								int screenEnd = Convert
										.translateGenomeToScreen(
												targetLoc.end(),
												model
														.getAnnotationLocationVisible(),
												width);
								GradientPaint gp = new GradientPaint(
										screenStart, 0, startColor, screenEnd,
										0, endColor);
								g.setPaint(gp);
								g.fillRect(screenStart, offset, screenEnd
										- screenStart + 1, 10);
							} catch (Exception x) {
								System.err.println(refLoc);
								System.err.println(colorBlockLength);
							}
						}
					}
				}
			}

			g.setColor(Color.black);
			g.drawString(displayName(), 10, offset + 23);
			return 25;

		}
	}

	public Entry reference() {
		return ref;
	}

	public Entry target() {
		return target;
	}

}
