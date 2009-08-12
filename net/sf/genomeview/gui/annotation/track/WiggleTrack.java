/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.util.Map;
import java.util.SortedMap;

import net.sf.genomeview.core.ColorFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Graph;
import net.sf.jannot.Location;

// A SINGLE WIGGLE TRACK CAN CONTAIN MULTIPLE GRAPHS
public class WiggleTrack extends Track {

	private String name;

	public WiggleTrack(String name, Model model, boolean b) {
		super(model, b,true);
		this.name = name;
	}

	@Override
	public String displayName() {
		return name;
	}

	// private HashMap<Entry, Graph> graphs = new HashMap<Entry, Graph>();

	@Override
	public int paint(Graphics g1, Entry e, int yOffset, double screenWidth) {
		Graphics2D g = (Graphics2D) g1;
		/* keeps track of the space used during painting */
		int yUsed = 0;
		Graph graph = e.graphs.getGraph(name);
		if (graph != null) {

			SortedMap<Location, Double> vf = graph.get(model.getAnnotationLocationVisible());

			int lastX = -1;
			if (vf.size() == 0) {
				g.setColor(Color.black);
				yUsed = 20;
				g.drawString(graph.getName() + ": no datapoints in this interval", 10, yOffset + yUsed);
				return yUsed;
			}

			if (!isCollapsed()) {
				GeneralPath gp = new GeneralPath();
				/* Move to first point */
				double val = (vf.get(vf.firstKey()) - graph.min()) / (graph.max() - graph.min());
				int y = (int) ((-val * 50) + yOffset + 50);
				int x = Convert.translateGenomeToScreen(vf.firstKey().start(), model.getAnnotationLocationVisible(), screenWidth);
				gp.moveTo(x, y);
				/* Add all intermediate points */
				for(Map.Entry<Location,Double>eld:vf.entrySet()){

					g.setColor(Color.BLACK);
					val = (eld.getValue() - graph.min()) / (graph.max() - graph.min());
					// require inverse of value because we paint
					// top-down.
					y = (int) ((-val * 50) + yOffset + 50);
					x = Convert.translateGenomeToScreen(eld.getKey().start() / 2 + eld.getKey().end() / 2, model.getAnnotationLocationVisible(), screenWidth);

					gp.lineTo(x, y);

				}
				/* Add last point */
				val = (vf.get(vf.lastKey()) - graph.min()) / (graph.max() - graph.min());
				y = (int) ((-val * 50) + yOffset + 50);
				x = Convert.translateGenomeToScreen(vf.lastKey().end(), model.getAnnotationLocationVisible(), screenWidth);
				gp.lineTo(x, y);
				g.draw(gp);
				yUsed = 50;
			} else { /* Color coding */

				for(Map.Entry<Location,Double>eld:vf.entrySet()){
					double val = (vf.get(eld.getValue()) - graph.min()) / (graph.max() - graph.min());
					g.setColor(ColorFactory.getColorCoding(val));
					int x = Convert.translateGenomeToScreen(eld.getKey().start() / 2 + eld.getKey().end() / 2, model.getAnnotationLocationVisible(), screenWidth);
					g.fillRect(lastX, yOffset, x - lastX, 10);
					lastX = x;

				}
				yUsed = 10;
			}

			g.setColor(Color.black);
			g.drawString(graph.getName(), 10, yOffset + yUsed);

		}
		return yUsed;
	}
}
