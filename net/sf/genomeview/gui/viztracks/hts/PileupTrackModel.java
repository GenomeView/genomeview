/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Location;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class PileupTrackModel {

	private Model model;

	PileupTrackModel(Model model) {
		this.model = model;
	}

	boolean isDetailed() {
		return model.getAnnotationLocationVisible().length() < PileupSummary.CHUNK;
	}

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

	void setLogscaling(boolean logscaling) {
		this.logscaling = logscaling;
	}

	void setDynamicScaling(boolean dynamicScaling) {
		this.dynamicScaling = dynamicScaling;
	}

	boolean isDynamicScaling() {
		if (globalSettings)
			return Configuration.getBoolean("pileup:dynamicScaling");
		else
			return dynamicScaling;
	}

	Location lastQuery = null;
	/* Data for detailed zoom */
	NucCounter nc;

	/* Data for pileupgraph barchart */
	int[][] detailedRects = null;

	private double screenWidth;

	double getScreenWidth() {
		return screenWidth;
	}

	void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;

	}

	int translateFromMouse(int x) {
		int start = model.getAnnotationLocationVisible().start;
		int xGenome = Convert.translateScreenToGenome(x, model.getAnnotationLocationVisible(), screenWidth);
		return xGenome - start;
	}

	void setGlobalSettings(boolean globalSettings) {
		this.globalSettings = globalSettings;
	}

	boolean isGlobalSettings() {
		return globalSettings;
	}

}