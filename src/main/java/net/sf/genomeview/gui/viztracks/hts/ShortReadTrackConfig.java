package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import be.abeel.gui.GridBagPanel;
import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.genomeview.gui.config.BooleanConfig;
import net.sf.genomeview.gui.config.ConfigListener;
import net.sf.genomeview.gui.viztracks.TrackConfig;
import net.sf.jannot.DataKey;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadTrackConfig extends TrackConfig {
	private boolean simplifiedColors; // mutable

	// cached colors and gradients for each color
	private Map<ReadColor, Color> colorCache = new HashMap<>();
	private Map<ReadColor, ColorGradient> gradientCache = new HashMap<>();

	protected ShortReadTrackConfig(Model model, DataKey dataKey) {
		super(model, dataKey);
		simplifiedColors = model.getConfiguration()
				.getBoolean("track:htsreads:simplifiedColors:" + dataKey);
		model.addObserver(new ReadColorObserver());
	}

	private class ReadColorObserver implements Observer {
		@Override
		public synchronized void update(Observable o, Object arg) {
			if (arg == NotificationTypes.CONFIGURATION_CHANGE) {
				colorCache.clear();
				gradientCache.clear();
			}

		}

	}

	public boolean isSimplifiedColors() {
		return simplifiedColors;// Configuration.getBoolean("track:htsreads:simplifiedColors:"
								// + dataKey);
	}

	@Override
	protected GridBagPanel getGUIContainer() {
		GridBagPanel out = super.getGUIContainer();
		out.gc.gridy++;
		final BooleanConfig simplifiedColorsConfig = new BooleanConfig(
				"track:htsreads:simplifiedColors:" + dataKey,
				"Use simplified color scheme", model);
		simplifiedColorsConfig.addConfigListener(new ConfigListener() {

			@Override
			public void configurationChanged() {
				simplifiedColors = model.getConfiguration().getBoolean(
						"track:htsreads:simplifiedColors:" + dataKey);

			}
		});
		out.add(simplifiedColorsConfig, out.gc);
		return out;
	}

	private static ColorGradient grayGradient = new ColorGradient();
	static {
		grayGradient.addPoint(Color.WHITE);
		grayGradient.addPoint(Color.GRAY);
		grayGradient.createGradient(100);

	}

	/**
	 * 
	 * @param rc a {@link ReadColor}
	 * @return gradient from white to {@link #color(ReadColor)}
	 */
	public synchronized ColorGradient gradient(ReadColor rc) {
		if (isSimplifiedColors() && rc != ReadColor.MATE_DIFFERENT_CHROMOSOME) {
			return grayGradient;
		}
		if (!gradientCache.containsKey(rc)) {
			ColorGradient cg = new ColorGradient();
			cg.addPoint(Color.WHITE);
			cg.addPoint(color(rc));
			cg.createGradient(100);

			gradientCache.put(rc, cg);
		}

		return gradientCache.get(rc);

	}

	/**
	 * 
	 * @param rc a {@link ReadColor}
	 * @return the current color associated with rc
	 */
	public synchronized Color color(ReadColor rc) {
		if (isSimplifiedColors() && rc != ReadColor.MATE_DIFFERENT_CHROMOSOME) {
			return Color.GRAY;
		}
		if (!colorCache.containsKey(rc)) {
			colorCache.put(rc,
					getModel().getConfiguration().getColor(rc.getConfigName()));
		}

		return colorCache.get(rc);

	}

}

/**
 * Colors for shortread tracks
 */
enum ReadColor {
	FORWARD_SENSE("shortread:forwardColor"),
	FORWARD_ANTISENSE("shortread:forwardAntiColor"),
	REVERSE_SENSE("shortread:reverseColor"),
	REVERSE_ANTISENSE("shortread:reverseAntiColor"),
	MATE_DIFFERENT_CHROMOSOME("shortread:mateDifferentChromosome"),
	PAIRING("shortread:pairingColor"),
	MISSING_MATE("shortread:missingMateColor"),
	SPLICING("shortread:splicingColor");

	private final String cfg;

	/**
	 * 
	 * @param cfg the name in the config file
	 * @return
	 */
	private ReadColor(String cfg) {
		this.cfg = cfg;
	}

	public String getConfigName() {
		return cfg;
	}

}
