/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import javax.swing.JFrame;

import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.genomeview.plugin.IValueFeature;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Type;

/**
 * Interface for external model methods.
 * 
 * @author Thomas Abeel
 * 
 */
public interface IModel {

    /**
     * Returns the entry that is currently selected.
     * 
     * @return the entry that is currently selected
     */
    public Entry getSelectedEntry();

    /**
     * Adds a value feature to the currently selected entry.
     */
    public void addFeature(IValueFeature vf);

    public void setValueFeatureDisplayType(IValueFeature svf, DisplayType barchartProfile);

    /**
     * Returns the region that is currently selected.
     * 
     * @return the region of the sequence that is currently selected.
     */
    public Location getSelectedRegion();

    public JFrame getParent();

    public void setDisplayType(Type type, DisplayType barchartProfile);

    public void setVisibleOnAnnotation(Type type, boolean b);
    public GUIManager getGUIManager();

}
