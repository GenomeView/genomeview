/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import net.sf.genomeview.core.ColorFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.menu.PopUpMenu;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.wiggle.Graph;

// A SINGLE WIGGLE TRACK CAN CONTAIN MULTIPLE GRAPHS
public class WiggleTrack extends Track {
	private boolean logScaled = false;

	class WigglePopup extends JPopupMenu {
		public WigglePopup() {
			if(!logScaled)
				add(new AbstractAction("Use log scaling"){

					@Override
					public void actionPerformed(ActionEvent e) {
						logScaled=true;
						model.refresh();
						
					}
					
				});
			else{
				add(new AbstractAction("Use normal scaling"){

					@Override
					public void actionPerformed(ActionEvent e) {
						logScaled=false;
						model.refresh();
						
					}
					
				});
			}
		}
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent e) {
		/* Specific mouse code for this label */
		if (Mouse.button2(e) || Mouse.button3(e)) {
			new WigglePopup().show(e.getComponent(), e.getX(), currentYOffset+e.getY());
			return true;
		}
		return false;

	}

	private String name;
	private Location currentVisible;
	private int currentYOffset;

	public WiggleTrack(String name, Model model, boolean b) {
		super(model, b, true);
		this.name = name;
	}

	@Override
	public String displayName() {
		return name;
	}

	// private HashMap<Entry, Graph> graphs = new HashMap<Entry, Graph>();
	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}
	@Override
	public int paintTrack(Graphics2D g, Entry e, int yOffset, double screenWidth) {
		currentVisible = model.getAnnotationLocationVisible();
		currentYOffset=yOffset;
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
				double range= graph.max() - graph.min();
				if(logScaled){
					val+=1;
					val = log2(val);
					val /= log2(range);
				}
				else{
					val /=range;
				}
				
				
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
			if (isCollapsed())
				yUsed += 10;
			else
				yUsed += graphLineHeigh;
		}

		return yUsed;
	}
}
