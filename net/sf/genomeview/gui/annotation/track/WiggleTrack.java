/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.wiggle.Graph;

/**
 * 
 * @author Thomas Abeel
 * 
 */
// A SINGLE WIGGLE TRACK CAN CONTAIN MULTIPLE GRAPHS
public class WiggleTrack extends Track {
	private boolean logScaled = false;

	private Tooltip tooltip = new Tooltip();

	private class Tooltip extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		private Tooltip() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(float value, MouseEvent e) {
			StringBuffer text = new StringBuffer();
			text.append(value);
			if (!text.toString().equals(floater.getText())) {
				floater.setText(text.toString());
				this.pack();
			}
			setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);

			if (!isVisible()) {
				setVisible(true);
			}

		}

	}

	private class WigglePopup extends JPopupMenu {
		public WigglePopup() {
			if (!logScaled)
				add(new AbstractAction("Use log scaling") {

					@Override
					public void actionPerformed(ActionEvent e) {
						logScaled = true;
						model.refresh();

					}

				});
			else {
				add(new AbstractAction("Use normal scaling") {

					@Override
					public void actionPerformed(ActionEvent e) {
						logScaled = false;
						model.refresh();

					}

				});
			}
			add(new AbstractAction("Toggle plot mode") {

				@Override
				public void actionPerformed(ActionEvent e) {
					plotType++;
					plotType %= 2;
					model.refresh();
				}

			});
		}
	}

	private static final Logger log = Logger.getLogger(WiggleTrack.class.getCanonicalName());

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent e) {
		super.mouseClicked(x, y, e);
		/* Specific mouse code for this label */
		if (!e.isConsumed() && (Mouse.button2(e) || Mouse.button3(e))) {
			log.finest("Wiggle track consumes button2||button3");
			new WigglePopup().show(e.getComponent(), e.getX(), currentYOffset + e.getY());
			e.consume();
			return true;
		}
		return false;

	}

	@Override
	public boolean mouseExited(int x, int y, MouseEvent e) {
		tooltip.setVisible(false);
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent e) {
		super.mouseMoved(x, y, e);

		if (!e.isConsumed()) {
			Graph g =(Graph)entry.get(dataKey);// entry.graphs.getGraph(name);
			if (g != null) {
				int pos = Convert.translateScreenToGenome(e.getX(), currentVisible, screenWidth);
				tooltip.set(g.value(pos), e);

			}
		}
		return false;

	}

	// private String name;
	private Location currentVisible;
	private int currentYOffset;

	public WiggleTrack(DataKey key, Model model, boolean b) {
		super(key, model, b, true);
		// this.name = name;
	}

	// public WiggleTrack(DataKey key, Data data) {
	// // TODO Auto-generated constructor stub
	// }
	//
	// public WiggleTrack(Model model, DataKey key, Data data) {
	// // TODO Auto-generated constructor stub
	// }

	@Override
	public String displayName() {
		return dataKey.toString();
	}

	// private HashMap<Entry, Graph> graphs = new HashMap<Entry, Graph>();
	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}

	private int plotType = 0;
	private double screenWidth;

	/* Last painted entry */
	private Entry entry;

	@Override
	public int paintTrack(Graphics2D g, Entry e, int yOffset, double screenWidth) {
		this.currentVisible = model.getAnnotationLocationVisible();
		this.currentYOffset = yOffset;
		this.screenWidth = screenWidth;
		this.entry = e;
		int graphLineHeigh = 50;
		g.setColor(Color.BLACK);
		/* keeps track of the space used during painting */
		int yUsed = 0;
		Graph graph = (Graph)e.get(dataKey);//e.graphs.getGraph(name);
		if (graph != null) {
			double width = screenWidth / (double) currentVisible.length();

			int scale = 1;
			int scaleIndex = 0;
			while (scale < (int) Math.ceil(1.0 / width)) {
				scale *= 2;
				scaleIndex++;
			}

			int start = currentVisible.start / scale * scale;
			int end = ((currentVisible.end / scale) + 1) * scale;

			float[] f = graph.get(start - 1, end, scaleIndex);

			int lastX = 0;
			GeneralPath conservationGP = new GeneralPath();
			for (int i = 0; i < f.length; i++) {
				int x = Convert.translateGenomeToScreen(start + i * scale, currentVisible, screenWidth);
				double val = f[i];

				if (val > graph.max())
					val = graph.max();

				if (logScaled) {
					double logrange = log2(graph.max() + 1) - log2(graph.min() + 1);
					val -= log2(graph.min() + 1);
					val = log2(val + 1);
					val /= logrange;
				} else {
					double range = graph.max() - graph.min();
					val -= graph.min();
					val /= range;
				}

				if (!isCollapsed()) {
					/* Draw lines */
					if (plotType == 0) {
						if (i == 0) {
							conservationGP.moveTo(x - 1, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
						}

						conservationGP.lineTo(x, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
					} else {
						int top = (int) (yOffset + (1 - val) * graphLineHeigh);
						g.fillRect(x, top, (int) Math.ceil(2 * width * scale), graphLineHeigh - top + yOffset);
					}
				} else {
					g.setColor(Colors.getColorCoding(val));
					g.fillRect(lastX, yOffset, x - lastX, 10);

				}

				lastX = x;

			}
			if (!isCollapsed()) {
				g.setColor(Color.BLACK);
				g.draw(conservationGP);

			}

			g.setColor(Color.black);
			g.drawString(displayName(), 10, yOffset + yUsed + 15);
			if (isCollapsed())
				yUsed += 10;
			else
				yUsed += graphLineHeigh;
		}

		return yUsed;
	}
}
