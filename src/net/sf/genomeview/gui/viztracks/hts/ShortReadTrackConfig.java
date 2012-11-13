package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JComponent;

import be.abeel.gui.GridBagPanel;
import be.abeel.util.Config;
import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.config.BooleanConfig;
import net.sf.genomeview.gui.config.ConfigListener;
import net.sf.genomeview.gui.viztracks.TrackConfig;
import net.sf.genomeview.gui.viztracks.hts.ShortReadTrackConfig.ReadColor;
import net.sf.jannot.DataKey;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadTrackConfig extends TrackConfig {

	protected ShortReadTrackConfig(Model model, DataKey dataKey) {
		super(model, dataKey);

	}

	enum ReadColor {
		FORWARD_SENSE("shortread:forwardColor"), FORWARD_ANTISENSE("shortread:forwardAntiColor"), REVERSE_SENSE(
				"shortread:reverseColor"), REVERSE_ANTISENSE("shortread:reverseAntiColor"), MATE_DIFFERENT_CHROMOSOME(
				"shortread:mateDifferentChromosome"), PAIRING("shortread:pairingColor"), MISSING_MATE("shortread:missingMateColor");

		private Color c;
		private ColorGradient cg;
		private String cfg;

		private ReadColor(String cfg) {
			this.cfg = cfg;
			reset();

		}

		private void reset() {
			c = Configuration.getColor(cfg);
			cg = new ColorGradient();
			cg.addPoint(Color.WHITE);
			cg.addPoint(c);
			cg.createGradient(100);

		}

	}

	private boolean simplifiedColors = Configuration.getBoolean("track:htsreads:simplifiedColors:" + dataKey);

	public boolean isSimplifiedColors() {
		return simplifiedColors;// Configuration.getBoolean("track:htsreads:simplifiedColors:"
								// + dataKey);
	}

	@Override
	protected GridBagPanel getGUIContainer() {
		GridBagPanel out = super.getGUIContainer();
		out.gc.gridy++;
		final BooleanConfig simplifiedColorsConfig = new BooleanConfig("track:htsreads:simplifiedColors:" + dataKey,
				"Use simplified color scheme", model);
		simplifiedColorsConfig.addConfigListener(new ConfigListener() {
			
			@Override
			public void configurationChanged() {
				simplifiedColors=Configuration.getBoolean("track:htsreads:simplifiedColors:" + dataKey);
				
			}
		});
		out.add(simplifiedColorsConfig, out.gc);
		// this.addObserver(new Observer() {
		//
		// @Override
		// public void update(Observable o, Object arg) {
		// simplifiedColors.(isSimplifiedColors());
		//
		// }
		//
		//
		//
		// });
		return out;
	}

	private ColorGradient grayGradient = null;

	public ColorGradient gradient(ReadColor rc) {
		if (isSimplifiedColors() && rc != ReadColor.MATE_DIFFERENT_CHROMOSOME) {
			if (grayGradient == null) {
				grayGradient = new ColorGradient();
				grayGradient.addPoint(Color.WHITE);
				grayGradient.addPoint(Color.GRAY);
				grayGradient.createGradient(100);
			}

			return grayGradient;

		} else
			return rc.cg;
	}

	public Color color(ReadColor rc) {
		if (isSimplifiedColors() && rc != ReadColor.MATE_DIFFERENT_CHROMOSOME) {
			return Color.GRAY;

		} else
			return rc.c;
	}

}
