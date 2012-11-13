package net.sf.genomeview.gui.viztracks;

import java.util.Observable;

import net.sf.jannot.Location;

public class TrackCommunicationModel {
	private boolean changed=false;
	
	public boolean isChanged() {
		return changed;
	}
	public void resetChanged() {
		this.changed=false;
	}
	public void setChanged() {
		this.changed = true;
	}

	/* Maximum over all pile-up track */
	private double localPileupMax;
	private Location pileLocation=null;

	public double getLocalPileupMax() {
		return localPileupMax;
	}

	public void setLocalPileupMax(double localPileupMax) {
		this.localPileupMax = localPileupMax;
		setChanged();
	}
	public void updateLocalPileupMax(double coverage, Location visible) {
		if(!visible.equals(pileLocation)){
			localPileupMax=0;
			pileLocation=visible;
			setChanged();
		}
		if(coverage>localPileupMax){
			localPileupMax=coverage;
			setChanged();
		}
		
		
		
	}
	
	

}