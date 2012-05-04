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
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBox;

import javax.swing.JOptionPane;
import javax.swing.JRadioButton;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.PileProvider;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.genomeview.gui.viztracks.TrackConfig;
import net.sf.jannot.DataKey;
import net.sf.jannot.Location;
import net.sf.jannot.refseq.Sequence;

import org.broad.igv.track.WindowFunction;

import be.abeel.gui.GridBagPanel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class PileupTrackConfig extends TrackConfig {

	private static Logger log = Logger.getLogger(PileupTrackConfig.class.getCanonicalName());

	@Override
	protected GridBagPanel getGUIContainer() {
		GridBagPanel out = super.getGUIContainer();
	
		/*
		 * Scale across tracks
		 */
		final JCheckBox itemCrossTrack = new JCheckBox();
		itemCrossTrack.setSelected(isCrossTrackScaling());
		itemCrossTrack.setAction(new AbstractAction("Scale across tracks") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setCrossTrackScaling(itemCrossTrack.isSelected());

			}

		});
		out.gc.gridy++;
		out.add(itemCrossTrack, out.gc);

		/*
		 * Use global settings
		 */
		final JCheckBox itemGlobal = new JCheckBox();
		itemGlobal.setSelected(isGlobalSettings());
		itemGlobal.setAction(new AbstractAction("Track uses defaults") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setGlobalSettings(itemGlobal.isSelected());
			}

		});
		out.gc.gridy++;
		out.add(itemGlobal, out.gc);

		/*
		 * Threshold line
		 */
		JButton item = new JButton(new AbstractAction("Add threshold line") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String in = JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Input the height of the new threshold line", "Input value", JOptionPane.QUESTION_MESSAGE);
				if (in != null) {
					try {
						Double d = Double.parseDouble(in);
						addLine(new Line(d));
					} catch (Exception ex) {
						log.log(Level.WARNING, "Unparseble value for maximum in PileupTrack: " + in, ex);
					}
				}

			}

		});
		out.gc.gridy++;
		out.add(item, out.gc);

		item = new JButton(new AbstractAction("Clear threshold lines") {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearLines();

			}

		});
		out.gc.gridy++;
		out.add(item, out.gc);

		// if (!isGlobalSettings()) {

		/* Log scaling of line graph */
		final JCheckBox item4 = new JCheckBox();
		item4.setSelected(isLogscaling());
		item4.setAction(new AbstractAction("Use log scaling for line graph") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setLogscaling(item4.isSelected());

			}

		});
		out.gc.gridy++;
		out.add(item4, out.gc);

		/* Dynamic scaling for both plots */
		final JCheckBox item3 = new JCheckBox();
		item3.setSelected(isDynamicScaling());
		item3.setAction(new AbstractAction("Use dynamic scaling for plots") {
			@Override
			public void actionPerformed(ActionEvent e) {
				setDynamicScaling(item3.isSelected());

			}

		});
		out.gc.gridy++;
		out.add(item3, out.gc);

		/* Maximum value */
		final JButton item2 = new JButton(new AbstractAction("Set maximum value") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String in = JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Input the maximum value, choose a negative number for unlimited", "Input maximum value",
						JOptionPane.QUESTION_MESSAGE);
				if (in != null) {
					try {
						Double d = Double.parseDouble(in);
						setMaxValue(d);
					} catch (Exception ex) {
						log.log(Level.WARNING, "Unparseble value for maximum in PileupTrack: " + in, ex);
					}
				}

			}
		});
		out.gc.gridy++;
		out.add(item2, out.gc);

		final ButtonGroup bg = new ButtonGroup();
		for (final WindowFunction wf : provider.getWindowFunctions()) {
			final JRadioButton jbm = new JRadioButton();
			jbm.setAction(new AbstractAction(wf.toString()) {

				@Override
				public void actionPerformed(ActionEvent e) {
					jbm.setSelected(true);
					// System.out.println("Requesting "+wf.toString());
					provider.requestWindowFunction(wf);
				}
			});
			if (provider.isCurrentWindowFunction(wf))
				jbm.setSelected(true);
			bg.add(jbm);

			out.gc.gridy++;
			out.add(jbm, out.gc);
		}
		// }

		this.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				item4.setEnabled(!isGlobalSettings());
				item3.setEnabled(!isGlobalSettings());
				item2.setEnabled(!isGlobalSettings());
				for (AbstractButton jb : Collections.list(bg.getElements())) {
					jb.setEnabled(!isGlobalSettings());
				}

			}

		});

		return out;
	}

	private VizBuffer vizBuffer = null;

	private PileProvider provider;

	PileupTrackConfig(Model model, DataKey key, PileProvider provider) {
		super(model, key);
		this.provider = provider;
	}

	void setVizBuffer(VizBuffer vb) {
		vizBuffer = vb;
	}

	VizBuffer getVizBuffer() {
		return vizBuffer;
	}

	// boolean isDetailed() {
	// return model.getAnnotationLocationVisible().length() < 16000;
	// }

	private boolean dynamicScaling = Configuration.getBoolean("pileup:dynamicScaling");

	private boolean logscaling = Configuration.getBoolean("pileup:logScale");

	/*
	 * Flag to keep track whether we want to use the global settings for scaling
	 * and so on.
	 */
	private boolean globalSettings = true;

	boolean isLogscaling() {
		if (globalSettings)
			return Configuration.getBoolean("pileup:logScale");
		else
			return logscaling;
	}

	private ArrayList<Line> lines = new ArrayList<Line>();

	ArrayList<Line> getLines() {
		return lines;
	}

	void setLogscaling(boolean logscaling) {
		this.logscaling = logscaling;
		setChanged();
		notifyObservers();

	}

	void setDynamicScaling(boolean dynamicScaling) {
		this.dynamicScaling = dynamicScaling;
		setChanged();
		notifyObservers();
	}

	boolean isDynamicScaling() {
		if (globalSettings)
			return Configuration.getBoolean("pileup:dynamicRange");
		else
			return dynamicScaling;
	}

	Location lastQuery = null;
	// /* Data for detailed zoom */
	// NucCounter nc;

	private double screenWidth;

	double getScreenWidth() {
		return screenWidth;
	}

	void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;

	}

	public void setGlobalSettings(boolean globalSettings) {
		this.globalSettings = globalSettings;
		setChanged();
		notifyObservers();
	}

	boolean isGlobalSettings() {
		return globalSettings;
	}

	private double maxValue = -1;
	private TrackCommunicationModel tcm;


	public double maxValue() {
		if (globalSettings)
			return Configuration.getDouble("pileup:maxPile");
		else
			return maxValue;
	}

	public void setMaxValue(double d) {
		this.maxValue = d;
		setChanged();
		notifyObservers();

	}

	public boolean isBarchart() {
		return true;
	}

	public Sequence sequence() {
		return model.getSelectedEntry().sequence();
	}

	public TrackCommunicationModel getTrackCommunication() {
		return tcm;

	}

	public void setTrackCommunication(TrackCommunicationModel tcm) {
		this.tcm = tcm;
		

	}

	public boolean isCrossTrackScaling() {
		return Configuration.getBoolean("pileup:crossTrackScaling");
	}

	public void setCrossTrackScaling(boolean b) {
		Configuration.set("pileup:crossTrackScaling", "" + b);
		setChanged();
		notifyObservers();
	}

	public void addLine(Line line) {
		lines.add(line);
		setChanged();
		notifyObservers();

	}

	public void clearLines() {
		lines.clear();
		setChanged();
		notifyObservers();

	}

	

}

class Line {
	public Line(double d) {
		this.height = d;
	}

	private double height = 0;

	public double value() {
		return height;
	}
}