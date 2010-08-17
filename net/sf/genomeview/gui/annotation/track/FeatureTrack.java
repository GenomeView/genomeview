/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;

import javax.management.Notification;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.annotation.GeneEvidenceLabel.FillMode;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Location;
import net.sf.jannot.Qualifier;
import net.sf.jannot.Type;
import net.sf.jannot.shortread.ReadGroup;
import be.abeel.util.CountMap;

public class FeatureTrack extends Track {

	class FeatureTrackModel {
		private boolean scoreColorGradient, colorQualifier;

		public boolean isColorQualifier() {
			return colorQualifier;
		}

		/* Indicates whether any of the features in this track have a /color tag */
		private boolean colorQualifierEnabled = false;

		public boolean isColorQualifierEnabled() {
			return colorQualifierEnabled;
		}

		public void setColorQualifierEnabled(boolean colorQualifierEnabled) {
			this.colorQualifierEnabled = colorQualifierEnabled;
		}

		public void setColorQualifier(boolean colorQualifier) {
			this.colorQualifier = colorQualifier;
			Configuration.set("feature:useColorQualifierTag_" + type, colorQualifier);
			model.refresh(this);
		}

		private Model model;

		private Color getColor(double normalizedScore) {
			return ColorGradient.fourColorGradient.getColor(normalizedScore);
		}

		public FeatureTrackModel(Model model) {
			this.model = model;
			colorQualifier = Configuration.getBoolean("feature:useColorQualifierTag_" + type);
			
			scoreColorGradient = Configuration.getBoolean("feature:scoreColorGradient_" + type);
		}

		public boolean isScoreColorGradient() {
			return scoreColorGradient;
		}

		public void setScoreColorGradient(boolean scoreColorGradient) {
			this.scoreColorGradient = scoreColorGradient;
			Configuration.set("feature:scoreColorGradient_" + type, scoreColorGradient);
			model.refresh(this);
		}

	}

	/* Type that is represented by this track */
	private Type type;
	private CollisionMap hitmap;
	private FeatureTrackModel ftm;

	/**
	 * Returns the type this track represents
	 * 
	 * @return type represented by this track
	 */
	public Type getType() {

		return Type.get(type.toString());
	}

	public FeatureTrack(Model model, Type key) {
		super(key, model, true, true);
		hitmap = new CollisionMap(model);
		this.type = key;
		ftm = new FeatureTrackModel(model);
		

	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double width, JViewport view) {

		boolean collision = false;
		hitmap.clear();
		Location visible = model.getAnnotationLocationVisible();
		// List<Feature> types = entry.annotation.getByType(type,);
		// FeatureAnnotation annot = entry.getAnnotation(type);
		FeatureAnnotation annot = (FeatureAnnotation) entry.get(type);
		if (annot.qualifierKeys().contains("color") || annot.qualifierKeys().contains("colour"))
			ftm.setColorQualifierEnabled(true);
		// System.out.println("Min-max: "+annot.getMinScore()+"\t"+annot.getMaxScore());
		// System.out.println("FA: "+type+"\t"+annot);
		// System.out.println(entry.)
		int estimate = annot.getEstimateCount(visible);
		// System.out.println("Estimated number of features: "+estimate);

		if (estimate > Configuration.getInt("annotationview:maximumNoVisibleFeatures")) {

			g.setColor(Color.BLACK);
			g.drawString(type + ": Too many features to display, zoom in to see features", 10, yOffset + 10);
			return 20 + 5;
		}
		Iterable<Feature> list = annot.get(visible.start, visible.end);
		g.translate(0, yOffset);
		CollisionMap fullBlockMap = new CollisionMap(model);

		int lineThickness = Configuration.getInt("evidenceLineHeight");
		// if (model.isShowTextOnStructure(type)) {
		// lineThickness += 10;
		// }
		int lines = 0;
		// int paintedFeatures=0;
		for (Feature rf : list) {

			// paintedFeatures++;
			// if (!model.isSourceVisible(rf.getSource()))
			// continue;
			// the line on which to paint this feature
			int thisLine = 0;

			Color c = Configuration.getColor("TYPE_" + rf.type());
			if (ftm.isColorQualifierEnabled() && ftm.isColorQualifier()) {
				String color = rf.getColor();
				if (color != null) {
					c = Colors.decodeColor(color);
				}
			}
			if (ftm.isScoreColorGradient()) {
				double range = annot.getMaxScore() - annot.getMinScore();
				if (range > 0.00001)
					c = ftm.getColor(rf.getScore() / range);
			}

			g.setColor(c);
			int x1 = Convert.translateGenomeToScreen(rf.start(), model.getAnnotationLocationVisible(), width);
			int x2 = Convert.translateGenomeToScreen(rf.end() + 1, model.getAnnotationLocationVisible(), width);

			// TODO is this not always the case?
			if (x2 > 0) {

				int maxX = x2;

				/*
				 * How close can items be together before they are considered
				 * overlapping?
				 */
				int closenessOverlap = Configuration.getInt("closenessOverlap");
				Rectangle r = new Rectangle(x1 - closenessOverlap, thisLine * lineThickness, maxX - x1 + 2
						* closenessOverlap, lineThickness);
				// only when the blocks should be tiled, do we need to
				// determine an empty place.
				if (!collision)
					collision = fullBlockMap.collision(r);
				if (!isCollapsed()) {

					while (fullBlockMap.collision(r)) {
						thisLine++;

						if (thisLine > lines)
							lines = thisLine;
						r = new Rectangle(x1 - closenessOverlap, thisLine * lineThickness, maxX - x1 + 2
								* closenessOverlap, lineThickness);
					}
				}
				fullBlockMap.addLocation(r, null);
				/*
				 * Create one or more rectangles, in order not to have to
				 * reproduce them on every drawing occasion. Make sure they are
				 * ordered from left to right.
				 */
				SortedSet<Location> loc = rf.location();

				/* Draw rectangles that are the features */
				ArrayList<Rectangle> rectList = new ArrayList<Rectangle>();
				for (Location l : loc) {

					int subX1 = Convert.translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible(), width);
					int subX2 = Convert.translateGenomeToScreen(l.end() + 1, model.getAnnotationLocationVisible(),
							width);
					Rectangle rec = new Rectangle(subX1, thisLine * lineThickness, subX2 - subX1, lineThickness - 5);
					/* Add this rectangle to the location hits */
					hitmap.addLocation(rec, l);
					rectList.add(rec);

				}

				drawRects(g, rectList, FillMode.FILL);
				Color backColor = g.getColor();
				g.setColor(g.getColor().darker());
				drawRects(g, rectList, FillMode.DRAW);

				/* Put triangle */
				int trianglehalf = (lineThickness - 5) / 2;
				switch (rf.strand()) {
				case REVERSE:// reverse arrow
					g
							.drawLine(x1, thisLine * lineThickness, x1 - trianglehalf, thisLine * lineThickness
									+ trianglehalf);
					g.drawLine(x1 - trianglehalf, thisLine * lineThickness + trianglehalf, x1, thisLine * lineThickness
							+ lineThickness - 5);
					break;
				case FORWARD:// forward arrow
					g
							.drawLine(x2, thisLine * lineThickness, x2 + trianglehalf, thisLine * lineThickness
									+ trianglehalf);
					g.drawLine(x2 + trianglehalf, thisLine * lineThickness + trianglehalf, x2, thisLine * lineThickness
							+ lineThickness - 5);
					break;
				default:// do nothing
					break;

				}
				// if (model.isShowTextOnStructure(type) && name != null) {
				// g.drawString(name.getValue(), x1, thisLine
				// * lineThickness + yOffset + 20);
				// }
				g.setColor(backColor);

				// Set<Feature> selected = model.getFeatureSelection();
				Set<Location> intersection = new HashSet<Location>(loc);
				intersection.retainAll(model.selectionModel().getLocationSelection());

				if (intersection.size() > 0) {
					g.setColor(Color.BLACK);
					drawRects(g, rectList, FillMode.DRAW);

				}

				/*
				 * If the first location takes more than 50 px, we draw name of
				 * the feature in it
				 */
				if (x2 - x1 > 100) {
					int a = Convert.translateGenomeToScreen(loc.first().start(), model.getAnnotationLocationVisible(),
							width);
					int b = Convert.translateGenomeToScreen(loc.first().end() + 1,
							model.getAnnotationLocationVisible(), width);
					if (b - a > 100) {
						Font resetFont = g.getFont();
						g.setColor(c.darker().darker().darker());
						g.setFont(new Font("SansSerif", Font.PLAIN, 10));
						g.drawString(rf.toString(), a + 5, thisLine * lineThickness + 9);
						g.setFont(resetFont);
					}

				}

			}
		}
		if (Configuration.getBoolean("showTrackName")) {
			g.setColor(Color.black);
			g.drawString(type.toString(), 10, lineThickness);
		}
		g.translate(0, -yOffset);
		return (lines + 1) * lineThickness + 5;

		// }

	}

	/**
	 * A double click on the evidence label wants us to zoom to the double
	 * clicked feature. The chromosome view should pan to the correct location,
	 * but should not change zoom level.
	 * 
	 * @param e
	 *            the MouseEvent
	 */
	private boolean doubleClick(MouseEvent e) {
		Location locationHit = hitmap.uniqueLocation(e.getX(), e.getY());
		if (locationHit != null) {
			Feature featHit = locationHit.getParent();
			model.selectionModel().setLocationSelection(featHit);
			int l = featHit.length();
			model.setAnnotationLocationVisible(new Location(featHit.start() - (l / 20), featHit.end() + (l / 20)));
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseExited(int x, int y, MouseEvent e) {
		floatingWindow.set(null, e);
		return false;
	}

	private void drawRects(Graphics g, ArrayList<Rectangle> rectList, FillMode fm) {
		Point lastPoint = null;

		for (Rectangle rect : rectList) {
			switch (fm) {
			case FILL:
				g.fillRect(rect.x, rect.y, rect.width, rect.height);
				break;
			case DRAW:
				g.drawRect(rect.x, rect.y, rect.width, rect.height);
				break;
			case BACKDROP:
				// TODO some nice indication that something is highlighted?
				break;
			}
			if (lastPoint != null) {
				g.drawLine(rect.x, rect.y + rect.height / 2, lastPoint.x, lastPoint.y);
			}
			lastPoint = new Point(rect.x + rect.width, rect.y + rect.height / 2);
		}

	}

	private FeatureInfoWindow floatingWindow = new FeatureInfoWindow();

	private class FeatureInfoWindow extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public FeatureInfoWindow() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(Set<Feature> features, MouseEvent e) {
			if (features == null || features.size() == 0) {
				floater.setText("");
				setVisible(false);
			} else {
				StringBuffer text = new StringBuffer();
				text.append("<html>");
				for (Feature f : features) {
					text.append("Name : " + f.toString() + "<br />");
					text.append("Start : " + f.start() + "<br />");
					text.append("End : " + f.end() + "<br />");
					int aggregateLenght = agg(f.location());
					if (aggregateLenght < Configuration.getInt("featuretrack:meanshortread")) {
						// Collection<DataSource> sources =
						// model.getSelectedEntry().shortReads.getSources();
						//
						// CountMap<Integer> cm = new CountMap<Integer>();
						// for (DataSource source : sources) {
						// cm.clear();
						// ReadGroup rg =
						// model.getSelectedEntry().shortReads.getReadGroup(source
						// );
						// ShortReadCoverage src = rg.getCoverage();
						// for (Location l : f.location()) {
						// for (int i = l.start(); i <= l.end(); i++) {
						// cm.count((int) (src.get(Strand.FORWARD, i - 1) +
						// src.get(Strand.REVERSE, i - 1)));
						// }
						// }
						//
						// text.append("Mean short read coverage (" +
						// StaticUtils.shortify(source.toString()) + "): "
						// + median(cm) + "<br />");
						// }
						Iterable<ReadGroup> sources = model.getSelectedEntry().shortReads();

						CountMap<Integer> cm = new CountMap<Integer>();
						for (ReadGroup rg : sources) {
							cm.clear();

							// // ShortReadCoverage src = rg.getCoverage();
							// for (Location l : f.location()) {
							// for (int i = l.start(); i <= l.end(); i++) {
							// cm.count((int) (src.get(Strand.FORWARD,
							// i - 1) + src.get(Strand.REVERSE,
							// i - 1)));
							// }
							// }

							// text.append("Mean short read coverage ("
							// + StaticUtils.shortify(rg.toString())
							// + "): " + median(cm) + "<br />");
						}
					}
					// }

				}
				text.append("</html>");
				if (!text.toString().equals(floater.getText())) {
					floater.setText(text.toString());
					setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);
					this.pack();
					setVisible(true);
				}
			}
		}

		/**
		 * Calculates the sum of the lengths of the individual locations.
		 * 
		 * @param location
		 * @return
		 */
		private int agg(SortedSet<Location> location) {
			int sum = 0;
			for (Location l : location)
				sum += l.length();
			return sum;
		}

		// private int median(CountMap<Integer> cm) {
		// int total = cm.totalCount();
		// int sum = 0;
		// for (java.util.Map.Entry<Integer, Integer> e : cm.entrySet()) {
		// sum += e.getValue();
		// if (sum > total / 2)
		// return e.getKey();
		// }
		// throw new RuntimeException(
		// "This should not happen while calculating the median.");
		// }

	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent e) {
		Set<Feature> hits = hitmap.featureHits(x, e.getY());
		floatingWindow.set(hits, e);
		return false;
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent e) {
		super.mouseClicked(x, y, e);
		if (!e.isConsumed()) {
			// catch double clicks
			if (e.getClickCount() == 2) {
				doubleClick(e);
			}

			/* try selecting something */
			Location locationHit = hitmap.uniqueLocation(e.getX(), e.getY());
			if (Mouse.button1(e)) {
				if (locationHit == null && !Mouse.modifier(e)) {
					model.selectionModel().clearLocationSelection();
				} else if (locationHit != null && e.isShiftDown()) {
					if (model.selectionModel().getLocationSelection().contains(locationHit)) {
						model.selectionModel().removeLocationSelection(locationHit);
					} else {
						model.selectionModel().addLocationSelection(locationHit);
					}
				} else if (locationHit != null && !Mouse.modifier(e)) {
					model.selectionModel().setLocationSelection(locationHit);

				}
				return true;
			}
			return false;
		}
		return true;

	}

	@Override
	public String displayName() {
		return "Feature: " + type.toString();
	}

	@Override
	public List<JMenuItem> getMenuItems() {
		ArrayList<JMenuItem> out = new ArrayList<JMenuItem>();
		/* Color gradient coloring based on score */
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(ftm.isScoreColorGradient());
		item.setAction(new AbstractAction("Use score color gradient") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ftm.setScoreColorGradient(item.isSelected());

			}

		});
		out.add(item);
		if (ftm.isColorQualifierEnabled()) {
			final JCheckBoxMenuItem colorQualifier = new JCheckBoxMenuItem();
			colorQualifier.setSelected(ftm.isColorQualifier());
			colorQualifier.setAction(new AbstractAction("Use /color qualifier") {
				@Override
				public void actionPerformed(ActionEvent e) {
					ftm.setColorQualifier(colorQualifier.isSelected());

				}

			});
			out.add(colorQualifier);
		}
		return out;
	}
}
