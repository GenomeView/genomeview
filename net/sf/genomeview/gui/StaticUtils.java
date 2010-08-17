/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.io.IOException;
import java.net.URI;

import javax.swing.BorderFactory;
import javax.swing.JOptionPane;
import javax.swing.border.Border;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.EditFeatureWindow;
import net.sf.genomeview.gui.dialog.MergeFeatureDialog;
import net.sf.genomeview.gui.dialog.SplitFeatureDialog;

/**
 * 
 * @author Administrator
 * 
 */
public final class StaticUtils {
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

	private static MergeFeatureDialog mergeFeature = null;

	private static SplitFeatureDialog splitFeature;

	/**
	 * Centers the window on the screen. This method should always be called
	 * after pack().
	 * 
	 * @param window
	 *            the window to center
	 */
	public static void center(Window window) {
		// Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		// Dimension windowSize = window.getPreferredSize();
		// window.setLocation(screenSize.width / 2 - (windowSize.width / 2),
		// screenSize.height / 2
		// - (windowSize.height / 2));
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		window.setLocation(bounds.x + bounds.width / 2 - window.getWidth() / 2, bounds.y + bounds.height / 2
				- window.getHeight() / 2);

	}

	public static void upperRight(Window window) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation(screenSize.width / 4 * 3, 50);
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

	public static String shortify(String in) {
		String s = in.replace('\\', '/');
		int idx = s.lastIndexOf('/');
		if (idx > 0)
			s = s.substring(idx);
		return s;
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
	public static void right(Component comp, Frame parent) {
		int width = parent.getWidth();
		comp.setLocation(width - comp.getWidth(), 0);

	}

	public static void browse(URI uri) {
		try {
			Desktop.getDesktop().browse(uri);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open the specified url: " + uri, "URL open failed",
					JOptionPane.ERROR_MESSAGE);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Could not open the specified url: " + uri, "URL open failed",
					JOptionPane.ERROR_MESSAGE);

		}

	}

}
