/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import net.sf.genomeview.core.ColorFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.shortread.ShortReadCoverage;
import net.sf.jannot.wiggle.Graph;

// A SINGLE WIGGLE TRACK CAN CONTAIN MULTIPLE GRAPHS
public class WiggleTrack extends Track {

	private String name;
	private Location currentVisible;

	public WiggleTrack(String name, Model model, boolean b) {
		super(model, b, true);
		this.name = name;
	}

	@Override
	public String displayName() {
		return name;
	}

	// private HashMap<Entry, Graph> graphs = new HashMap<Entry, Graph>();

	@Override
	public int paintTrack(Graphics2D g, Entry e, int yOffset, double screenWidth) {
		currentVisible = model.getAnnotationLocationVisible();
		int graphLineHeigh = 50;
		/* keeps track of the space used during painting */
		int yUsed = 0;
		Graph graph = e.graphs.getGraph(name);
		if (graph != null) {
			double width = screenWidth / (double) currentVisible.length() / 2.0;

			int scale = 1;
			int scaleIndex = 0;
			while (scale < (int) Math.ceil(1.0 / width)) {
				scale *= 2;
				scaleIndex++;
			}

			int start = currentVisible.start / scale * scale;
			int end = ((currentVisible.end / scale) + 1) * scale;

			GeneralPath conservationGP = new GeneralPath();

			float[] f = graph.get(start - 1, end, scaleIndex);

			int lastX = 0;
			for (int i = 0; i < f.length; i++) {
				int x = Convert.translateGenomeToScreen(start + i * scale, currentVisible, screenWidth);
				double val = f[i];

				if (val > graph.max())
					val = graph.max();

				val -= graph.min();
				val /= graph.max() - graph.min();
				if (!isCollapsed()) {
					/* Draw lines */
					if (i == 0) {
						conservationGP.moveTo(x - 1, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
					}

					conservationGP.lineTo(x, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
				} else {
					g.setColor(ColorFactory.getColorCoding(val));
					g.fillRect(lastX, yOffset, x - lastX, 10);
					lastX = x;
				}

			}
			if (!isCollapsed()) {
				g.setColor(Color.BLACK);
				g.draw(conservationGP);

			}

			g.setColor(Color.black);
			g.drawString(graph.getName(), 10, yOffset + yUsed + 15);
			if(isCollapsed())
				yUsed+=10;
			else
				yUsed += graphLineHeigh;
		}

		return yUsed;
	}
}
