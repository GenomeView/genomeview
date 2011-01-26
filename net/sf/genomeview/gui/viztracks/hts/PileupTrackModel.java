/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

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

	public PileupTrackModel(Model model) {
		this.model = model;
	}

	boolean isDetailed() {
		return model.getAnnotationLocationVisible().length() < PileupSummary.CHUNK;
	}

	private boolean dynamicScaling = false;

	private boolean logscaling = false;

	public boolean isLogscaling() {
		return logscaling;
	}

	public void setLogscaling(boolean logscaling) {
		this.logscaling = logscaling;
	}

	public void setDynamicScaling(boolean dynamicScaling) {
		this.dynamicScaling = dynamicScaling;
	}

	public boolean isDynamicScaling() {
		return dynamicScaling;
	}

	public Location lastQuery = null;
	/* Data for detailed zoom */
	public NucCounter nc;

	/* Data for pileupgraph barchart */
	public int[][] detailedRects = null;

	private double screenWidth;

	public double getScreenWidth() {
		return screenWidth;
	}

	public void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;

	}

	public int translateFromMouse(int x) {
		int start = model.getAnnotationLocationVisible().start;
		int xGenome = Convert.translateScreenToGenome(x, model.getAnnotationLocationVisible(), screenWidth);
		return xGenome - start;
	}

}