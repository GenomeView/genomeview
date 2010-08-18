/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Frame;
import java.util.Observer;

import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Location;

/**
 * Interface for external model methods.
 * 
 * @author Thomas Abeel
 * 
 */
public interface IModel extends Observer {

	/**
	 * Returns the entry that is currently selected.
	 * 
	 * @return the entry that is currently selected
	 */
	public Entry getSelectedEntry();

	/**
	 * Returns the region that is currently selected. If nothin is selected,
	 * this method will return null.
	 * 
	 * @return the region of the sequence that is currently selected.
	 */
	public Location getSelectedRegion();

	public GUIManager getGUIManager();

	public void addTrack(Track track);

	public void updateTracks();

	public void addObserver(Observer observer);

	/**
	 * Returns the entire data set that is currently loaded in GenomeView.
	 * 
	 * @return
	 */
	public EntrySet entries();
}
