/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.jannot.Location;
import net.sf.jannot.refseq.Sequence;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class PileupTrackModel {

	private Model model;
	private VizBuffer vizBuffer = null;
	PileupTrackModel(Model model) {
		this.model = model;
	}

	void setVizBuffer(VizBuffer vb){
		vizBuffer=vb;
	}
	
	VizBuffer getVizBuffer(){
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

	void setLogscaling(boolean logscaling) {
		this.logscaling = logscaling;
	}

	void setDynamicScaling(boolean dynamicScaling) {
		this.dynamicScaling = dynamicScaling;
	}

	boolean isDynamicScaling() {
		if (globalSettings)
			return Configuration.getBoolean("pileup:dynamicRange");
		else
			return dynamicScaling;
	}

	Location lastQuery = null;
//	/* Data for detailed zoom */
//	NucCounter nc;



	private double screenWidth;

	double getScreenWidth() {
		return screenWidth;
	}

	void setScreenWidth(double screenWidth) {
		this.screenWidth = screenWidth;

	}

	

	void setGlobalSettings(boolean globalSettings) {
		this.globalSettings = globalSettings;
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

	public void setMaxValue(Double d) {
		this.maxValue = d;

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
		this.tcm=tcm;
		
	}

	public boolean isCrossTrackScaling() {
		return Configuration.getBoolean("pileup:crossTrackScaling");
	}
	
	public void setCrossTrackScaling(boolean b){
		Configuration.set("pileup:crossTrackScaling",""+b);
	}

}