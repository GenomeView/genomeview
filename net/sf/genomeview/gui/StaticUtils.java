/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JPopupMenu;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.EditFeatureWindow;
import net.sf.genomeview.gui.dialog.MergeFeatureDialog;
import net.sf.genomeview.gui.dialog.SplitFeatureDialog;
import net.sf.genomeview.gui.menu.PopUpMenu;


public abstract class StaticUtils {
    /*
     * This class should never be instantiated, so we make the constructor
     * private.
     */
    private StaticUtils() {
    };

    private static EditFeatureWindow editStructure = null;

    public static EditFeatureWindow getEditStructure(Model model) {
        // model.startGroupChange("Edit structure");
        if (editStructure == null) {
            editStructure = new EditFeatureWindow(model);
        }
        return editStructure;
    }

    private static PopUpMenu structurePopup = null;

    private static MergeFeatureDialog mergeFeature = null;

    private static SplitFeatureDialog splitFeature;

    public static JPopupMenu popupMenu(Model model) {
        if (structurePopup == null)
            structurePopup = new PopUpMenu(model);

        return structurePopup;
    }

    /**
     * Centers the window on the screen. This method should always be called
     * after pack().
     * 
     * @param window
     *            the window to center
     */
    public static void center(Window window) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension windowSize = window.getPreferredSize();
        window.setLocation(screenSize.width / 2 - (windowSize.width / 2), screenSize.height / 2
                - (windowSize.height / 2));

    }

    public static MergeFeatureDialog getMergeFeature(Model model) {
        if (mergeFeature == null)
            mergeFeature = new MergeFeatureDialog(model);
        return mergeFeature;
    }

    public static SplitFeatureDialog splitFeature(Model model) {
        if (splitFeature == null)
            splitFeature = new SplitFeatureDialog(model);
        return splitFeature;
    }

    public static String escapeHTML(String in) {
        in = in.replaceAll("<", "&lt;");
        in = in.replaceAll(">", "&gt;");

        return in;

    }

    /**
     * Put this component in the top-right corner of the supplied JFrame
     * 
     */
    public static void right(Component comp, JFrame parent) {
        int width = parent.getWidth();
        comp.setLocation(width - comp.getWidth(), 0);

    }

}
