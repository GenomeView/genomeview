/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.Observer;

import javax.swing.JFrame;

import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.jannot.Entry;
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
     * Returns the region that is currently selected.
     * 
     * @return the region of the sequence that is currently selected.
     */
    public Location getSelectedRegion();

    public JFrame getParent();

    public GUIManager getGUIManager();
    
    public void addTrack(Track track);

}
