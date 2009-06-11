/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.util.TreeMap;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;

/**
 * This class will become a bar chart track. This code was extracted from the
 * GeneEvidenceLabel.
 * 
 * @author Thomas Abeel
 * 
 */
public class BarChartTrack extends Track {
	private String name;
	/* Keeps track of the lowest score */
	private double min = Double.POSITIVE_INFINITY;
	/* Keeps track of the highest score */
	private double max = Double.NEGATIVE_INFINITY;

	public BarChartTrack(Model model, String name) {
		super(model, true,false);
		this.name = name;

	}

	private TreeMap<Location, Double> map = new TreeMap<Location, Double>();

	public void add(Location l, double v) {
		if (v < min)
			min = v;
		if (v > max)
			max = v;
		map.put(l, v);

	}

	@Override
	public String displayName() {
		return name;
	}

	@Override
	public int paint(Graphics g, Entry e, int yOffset, double width) {

		int line = 75;

		// the line on which to paint this feature
	
		double heightScale = 1;
		for (Location l : map.keySet()) {

			double score = map.get(l) / max;
			heightScale = score;

			int x1 = Convert.translateGenomeToScreen(l.start(), model
					.getAnnotationLocationVisible(), width);
			int x2 = Convert.translateGenomeToScreen(l.end() + 1, model
					.getAnnotationLocationVisible(), width);
			if (x2 == x1)
				x2++;
			if (x2 > 0) {
				// assert (l.location().size() == 1);
				// super.collisionMap.addLocation(new Rectangle(x1,
				// thisLine * line + yOffset, x2 - x1, line), l
				// );
				g.fillRect(x1,
						(int) ( yOffset + (1 - heightScale)
								* line), x2 - x1, (int) (line * heightScale));
				// Set<Feature> selected = model.getFeatureSelection();
				// if (selected != null && selected.contains(rf)) {
				if (model.getLocationSelection().contains(l)) {
					g.setColor(Color.BLACK);
					g
							.drawRect(
									x1,
									(int) (yOffset + (1 - heightScale)
											* line), x2 - x1,
									(int) (line * heightScale));

				}
			}
		}

		g.setColor(Color.black);
		g.drawString(displayName(), 10, yOffset + line);

		return line;
	}

}
