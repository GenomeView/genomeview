package net.sf.genomeview.data;

import java.util.Observable;

import net.sf.jannot.Entry;
import net.sf.jannot.Location;

public class VisualLocationModel extends Observable {

	private Entry visibleEntry = null;
	private Location visibleLocation = new Location(0, 0);
	private double screenWidth;

	public Entry getVisibleEntry() {
		if(visibleEntry==null)
			return DummyEntry.dummy;
		return visibleEntry;
	}

	public void setVisibleEntry(Entry visibleEntry) {
		this.visibleEntry = visibleEntry;
		setChanged();
		notifyObservers(visibleEntry);
	}

	public Location getVisibleLocation() {
		return visibleLocation;
	}

	public void setVisibleLocation(Location visibleLocation) {
		this.visibleLocation = visibleLocation;
		notifyObservers(visibleLocation);
	}

	/**
	 * Set the visible area in the evidence and structure frame to the given
	 * Location.
	 * 
	 * start and end one-based [start,end]
	 * 
	 * @param start
	 * @param annotationEnd
	 */

	public void setAnnotationLocationVisible(Location r) {
		setAnnotationLocationVisible(r, false);

	}

	public void setAnnotationLocationVisible(Location r, boolean mayExpand) {
		int modStart = -1;
		int modEnd = -1;
		if (r.start > 1) {
			modStart = r.start;
		} else {
			modStart = 1;
			modEnd = r.length();
		}
		int chromLength = getVisibleEntry().getMaximumLength();
		if (r.end < chromLength || chromLength == 0) {
			modEnd = r.end;
		} else {
			modEnd = chromLength;
			modStart = modEnd - r.length();
			if (modStart < 1)
				modStart = 1;
		}
		Location newZoom = new Location(modStart, modEnd);
		/* When trying to zoom to something really small */
		if (newZoom.length() < 50 && mayExpand) {
			setAnnotationLocationVisible(new Location(modStart - 25, modEnd + 25));
		}
		if (newZoom.length() != visibleLocation.end - visibleLocation.start + 1 && newZoom.length() < 50)
			return;
		// if (newZoom.length() != annotationEnd - annotationStart + 1
		// && newZoom.length() > Configuration.getInt("general:zoomout"))
		// return;
		if (newZoom.start < 1 || newZoom.end < 1)
			return;

		this.visibleLocation = newZoom;
		setChanged();
		notifyObservers(visibleLocation);
		// ZoomChange zc = new ZoomChange(visible, newZoom);
		// zc.doChange();
		// refresh();

	}

	/**
	 * Center the model on a certain position. This will cause the nucleotide
	 * start, end and the normal start and end to change.
	 * 
	 * @param genomePosition
	 *            the position to center on
	 */
	public void center(int genomePosition) {
		int length = (visibleLocation.end - visibleLocation.start) / 2;
		setAnnotationLocationVisible(new Location(genomePosition - length, genomePosition + length));

	}

	public void clear() {
		visibleLocation = new Location(0, 0);

	}

	@Deprecated
	public Location getAnnotationLocationVisible() {
		return getVisibleLocation();
	}

	@Deprecated
	public Entry getSelectedEntry() {
		return getVisibleEntry();

	
	}

	public void setScreenWidth(double d){
		if(d!=screenWidth){
			this.screenWidth=d;
			setChanged();
			notifyObservers();
		}
		
		
	}
	public double screenWidth() {
		return screenWidth;
	}

	

}
