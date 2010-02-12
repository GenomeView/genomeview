/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.Observable;

/**
 * Keeps track where the mouse is.
 * 
 * @author Thomas Abeel
 * 
 */
public class MouseModel extends Observable{

	/**
	 * The coordinate (nt) where to mouse is currently hovering. -1 when the
	 * mouse is outside of the track window.
	 */
	private int currentCoord;

	/**
	 * Set the current coordinate: the place (nt) where the mouse is currently
	 * hovering. -1 when the mouse is outside of the track window.
	 */
	public void setCurrentCoord(int currentCoord) {
		this.currentCoord = currentCoord;
		setChanged();
		notifyObservers();
	}

	/**
	 * Get the current coordinate: the place (nt) where the mouse is currently
	 * hovering. -1 when the mouse is outside of the track window.
	 * 
	 * @return currentCoord
	 */
	public int getCurrentCoord() {
		return currentCoord;
	}
}
