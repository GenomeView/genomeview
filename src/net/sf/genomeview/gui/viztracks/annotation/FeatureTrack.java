/**
 * This file is part of GenomeView, a genome browser and annotation curator
 * 
 * Copyright (C) 2012 Thomas Abeel
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Project: http://genomeview.org/
 */
package net.sf.genomeview.gui.viztracks.annotation;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.genomeview.gui.components.DoubleJSlider;
import net.sf.genomeview.gui.config.BooleanConfig;
import net.sf.genomeview.gui.viztracks.GeneEvidenceLabel.FillMode;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.genomeview.gui.viztracks.TrackConfig;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Location;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;

/**
 * Visualization track for feature tracks.
 * 
 * 
 * 
 * @author Thomas Abeel
 * 
 */
public class FeatureTrack extends Track {

	static class FeatureTrackConfig extends TrackConfig {

		@Override
		protected GridBagPanel getGUIContainer() {
			GridBagPanel out = super.getGUIContainer();

			out.gc.gridy++;
			final JComponent colorGradient = new BooleanConfig("feature:scoreColorGradient_" + type(), "Use score color gradient", model);
			out.add(colorGradient, out.gc);

			/* Filter items based on score */

			out.gc.gridy++;

			final JButton filter = new JButton(new AbstractAction("Filter items by score") {
				// filter.setAction(new
				// AbstractAction("Filter items by score") {
				@Override
				public void actionPerformed(ActionEvent e) {
					final JOptionPane optionPane = new JOptionPane();
					final JLabel label = new JLabel();
					final NumberFormat nf = NumberFormat.getInstance();
					nf.setMaximumFractionDigits(2);
					final DoubleJSlider slider = new DoubleJSlider(minScore, maxScore, getThreshold() > minScore ? getThreshold()
							: minScore, (maxScore - minScore) / 100.0);
					optionPane.setInputValue(slider.getDoubleValue());
					label.setText(nf.format(slider.getDoubleValue()));
					// slider.setMajorTickSpacing((int)((ftm.maxScore-ftm.minScore)/10));
					slider.setPaintTicks(true);
					slider.setPaintLabels(true);

					ChangeListener changeListener = new ChangeListener() {
						public void stateChanged(ChangeEvent changeEvent) {
							DoubleJSlider theSlider = (DoubleJSlider) changeEvent.getSource();
							label.setText(nf.format(slider.getDoubleValue()));
							if (!theSlider.getValueIsAdjusting()) {
								optionPane.setInputValue(theSlider.getDoubleValue());
								theSlider.setDoubleValue(theSlider.getDoubleValue());
							}
						}
					};
					slider.addChangeListener(changeListener);
					optionPane.setMessage(new Object[] { "Select score threshold: ", slider, label });
					optionPane.setMessageType(JOptionPane.QUESTION_MESSAGE);
					optionPane.setOptionType(JOptionPane.OK_CANCEL_OPTION);
					JDialog dialog = optionPane.createDialog(model.getGUIManager().getParent(), "Score threshold");
					// dialog.setModalExclusionType(ModalExclusionType.NO_EXCLUDE);
					dialog.setVisible(true);
					setThreshold((Double) optionPane.getInputValue());
					System.out.println("Input: " + optionPane.getInputValue());

				}

			});
			out.add(filter, out.gc);

			out.gc.gridy++;
			final JComponent colorQualifier = new BooleanConfig("feature:useColorQualifierTag_" + type(), "Use /color qualifier", model);
			out.add(colorQualifier, out.gc);

			this.addObserver(new Observer() {

				@Override
				public void update(Observable o, Object arg) {
					colorGradient.setEnabled(isScoreColorGradientEnabled());
					colorQualifier.setEnabled(isColorQualifierEnabled());
					filter.setEnabled(isScoreColorGradientEnabled());
				}

			});

			return out;

		}

		// private boolean scoreColorGradient, colorQualifier;

		private double minScore = Double.NEGATIVE_INFINITY;

		private double maxScore = Double.POSITIVE_INFINITY;

		/**
		 * @return the minScore
		 */
		public double getMinScore() {
			return minScore;
		}

		/**
		 * @param minScore
		 *            the minScore to set
		 */
		public void setMinScore(double minScore) {
			this.minScore = minScore;
		}

		/**
		 * @return the maxScore
		 */
		public double getMaxScore() {
			return maxScore;
		}

		/**
		 * @param maxScore
		 *            the maxScore to set
		 */
		public void setMaxScore(double maxScore) {
			this.maxScore = maxScore;
		}

		public boolean isColorQualifier() {
			return Configuration.getBoolean("feature:useColorQualifierTag_" + type());
			// return colorQualifier;
		}

		public Type type() {
			return (Type) super.dataKey;
		}

		public void setColorQualifier(boolean colorQualifier) {
			// this.colorQualifier = colorQualifier;
			if (isColorQualifier() != colorQualifier) {
				Configuration.set("feature:useColorQualifierTag_" + type(), colorQualifier);
				model.refresh(this);
			}
		}

		private boolean scoreColorGradientEnabled;

		private double threshold = Double.NEGATIVE_INFINITY;

		private boolean colorQualifierEnabled;

		public double getThreshold() {
			return threshold;
		}

		private Color getColor(double normalizedScore) {
			return ColorGradient.fourColorGradient.getColor(normalizedScore);
		}

		@Override
		public boolean isCollapsible() {
			return true;
		}

		public FeatureTrackConfig(Model model, Type type) {
			super(model, type);

		}

		public boolean isScoreColorGradient() {
			return Configuration.getBoolean("feature:scoreColorGradient_" + type());

		}

		public void setScoreColorGradient(boolean scoreColorGradient) {
			// this.scoreColorGradient = scoreColorGradient;
			if (isScoreColorGradient() != scoreColorGradient) {
				Configuration.set("feature:scoreColorGradient_" + type(), scoreColorGradient);
				model.refresh(this);
			}
		}

		public boolean isScoreColorGradientEnabled() {
			return scoreColorGradientEnabled;
		}

		public void setScoreColorGradientEnabled(boolean b) {
			if (b != this.scoreColorGradientEnabled) {
				this.scoreColorGradientEnabled = b;
				setChanged();
				notifyObservers();
			}

		}

		public void setThreshold(double inputValue) {
			this.threshold = inputValue;

		}

		public boolean isColorQualifierEnabled() {
			return colorQualifierEnabled;
		}

		public void setColorQualifierEnabled(boolean b) {
			if (b != colorQualifierEnabled) {
				colorQualifierEnabled = b;
				setChanged();
				notifyObservers();
			}

		}

	}

	private CollisionMap hitmap;
	final private FeatureTrackConfig ftm;

	/**
	 * Returns the type this track represents
	 * 
	 * @return type represented by this track
	 */
	public Type getType() {

		return ftm.type();
	}

	public FeatureTrack(Model model, Type key) {
		super(key, model, true, new FeatureTrackConfig(model, key));
		ftm = (FeatureTrackConfig) config;
		hitmap = new CollisionMap(model);

		floatingWindow = new FeatureInfoWindow(ftm);

	}

	
	@Override
	public int paintTrack(Graphics2D g, int yOffset, double width, JViewport view, TrackCommunicationModel tcm) {
		boolean forceLabels = Configuration.getBoolean("track:forceFeatureLabels");
		boolean collision = false;
		hitmap.clear();
		Location visible = model.getAnnotationLocationVisible();
		// List<Feature> types = entry.annotation.getByType(type,);
		// FeatureAnnotation annot = entry.getAnnotation(type);
		FeatureAnnotation annot = (FeatureAnnotation) entry.get(ftm.type());
		if (annot.qualifierKeys().contains("color") || annot.qualifierKeys().contains("colour"))
			ftm.setColorQualifierEnabled(true);
		/* If there are proper scores, enable color gradient */
		if (annot.getMaxScore() - annot.getMinScore() > 0.00001) {
			ftm.setScoreColorGradientEnabled(true);
			ftm.setMaxScore(annot.getMaxScore());
			ftm.setMinScore(annot.getMinScore());
		}

		/* Get feature estimate */
		boolean manyFeature = false;
		int estimate = annot.getEstimateCount(visible);
		if (estimate > 25 * Configuration.getInt("annotationview:maximumNoVisibleFeatures")) {

			g.setColor(Color.BLACK);
			g.drawString(ftm.type() + ": Too many features to display, zoom in to see features", 10, yOffset + 10);
			return 20 + 5;
		} else if (estimate > Configuration.getInt("annotationview:maximumNoVisibleFeatures")) {
			manyFeature = true;
		}
		Iterable<Feature> list = annot.get(visible.start, visible.end);
		g.translate(0, yOffset + 2);
		CollisionMap fullBlockMap = new CollisionMap(model);

		int lineThickness = Configuration.getInt("evidenceLineHeight");

		int lines = 0;

		for (Feature rf : list) {

			/* Skip feature that do not satisfy threshold filter */
			if (rf.getScore() <= ftm.getThreshold())
				continue;
			int thisLine = 0;

			Color c = Configuration.getColor("TYPE_" + rf.type());
			if (ftm.isColorQualifier() && rf.getColor() != null) {
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
				Rectangle r = new Rectangle(x1 - closenessOverlap, thisLine * lineThickness, maxX - x1 + 2 * closenessOverlap,
						lineThickness);

				if (!config.isCollapsed() && !manyFeature) {
					// only when the blocks should be tiled, do we need to
					// determine an empty place.
					if (!collision)
						collision = fullBlockMap.collision(r);
					while (fullBlockMap.collision(r)) {
						thisLine++;

						if (thisLine > lines)
							lines = thisLine;
						r = new Rectangle(x1 - closenessOverlap, thisLine * lineThickness, maxX - x1 + 2 * closenessOverlap, lineThickness);
					}
				}
				fullBlockMap.addLocation(r, null);
				/*
				 * Create one or more rectangles, in order not to have to
				 * reproduce them on every drawing occasion. Make sure they are
				 * ordered from left to right.
				 */
				Location[] loc = rf.location();

				/* Draw rectangles that are the features */
				ArrayList<Rectangle> rectList = new ArrayList<Rectangle>();
				for (Location l : loc) {

					int subX1 = Convert.translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible(), width);
					int subX2 = Convert.translateGenomeToScreen(l.end() + 1, model.getAnnotationLocationVisible(), width);
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
					g.drawLine(x1, thisLine * lineThickness, x1 - trianglehalf, thisLine * lineThickness + trianglehalf);
					g.drawLine(x1 - trianglehalf, thisLine * lineThickness + trianglehalf, x1, thisLine * lineThickness + lineThickness - 5);
					break;
				case FORWARD:// forward arrow
					g.drawLine(x2, thisLine * lineThickness, x2 + trianglehalf, thisLine * lineThickness + trianglehalf);
					g.drawLine(x2 + trianglehalf, thisLine * lineThickness + trianglehalf, x2, thisLine * lineThickness + lineThickness - 5);
					break;
				default:// do nothing
					break;

				}

				g.setColor(backColor);

				// Set<Feature> selected = model.getFeatureSelection();
				Set<Location> intersection = new HashSet<Location>();
				for (Location l : loc)
					intersection.add(l);
				intersection.retainAll(model.selectionModel().getLocationSelection());

				if (intersection.size() > 0) {
					g.setColor(Color.BLACK);
					drawRects(g, rectList, FillMode.DRAW);

				}

				/*
				 * If the first location takes more than 50 px, we draw name of
				 * the feature in it
				 */
				if (forceLabels || x2 - x1 > 100) {
					int a = Convert.translateGenomeToScreen(loc[0].start(), model.getAnnotationLocationVisible(), width);
					int b = Convert.translateGenomeToScreen(loc[0].end() + 1, model.getAnnotationLocationVisible(), width);
					if (forceLabels || b - a > 100) {
						Font resetFont = g.getFont();
						g.setColor(c.darker().darker().darker());
						g.setFont(new Font("SansSerif", Font.PLAIN, 10));
						g.drawString(rf.toString(), a + 5, thisLine * lineThickness + 9);
						g.setFont(resetFont);
					}

				}

			}
		}

		g.translate(0, -yOffset - 2);
		return (lines + 1) * lineThickness + 4;

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

	private FeatureInfoWindow floatingWindow = null;

	private static class FeatureInfoWindow extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public FeatureInfoWindow(FeatureTrackConfig ftm) {
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
					String name = f.toString();
					if (name.length() > 50)
						name = name.substring(0, 50);
					text.append("Name : " + name + "<br />");
					text.append("Start : " + f.start() + "<br />");
					text.append("End : " + f.end() + "<br />");

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

}
