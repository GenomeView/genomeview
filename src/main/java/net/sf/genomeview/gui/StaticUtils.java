/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.Random;
import java.util.logging.Level;

import be.abeel.net.URIFactory;
import net.sf.genomeview.core.Globals;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.EditFeatureWindow;
import net.sf.genomeview.gui.dialog.SplitFeatureDialog;
import tudelft.utilities.logging.Reporter;

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

	public static final Random rg = new Random(System.currentTimeMillis());

	private static EditFeatureWindow editStructure = null;

	public static EditFeatureWindow getEditStructure(Model model) {
		// model.startGroupChange("Edit structure");
		if (editStructure == null) {
			editStructure = new EditFeatureWindow(model);
		}
		return editStructure;
	}

	private static SplitFeatureDialog splitFeature;

	/**
	 * Centers the window on the screen. This method should always be called
	 * after pack().
	 * 
	 * @param window the window to center
	 */
	public static void center(Window parent, Window window) {
		Rectangle bounds = null;
		if (parent != null) {
			bounds = parent.getBounds();
		} else {
			bounds = GraphicsEnvironment.getLocalGraphicsEnvironment()
					.getMaximumWindowBounds();
		}

		window.setLocation(bounds.x + bounds.width / 2 - window.getWidth() / 2,
				bounds.y + bounds.height / 2 - window.getHeight() / 2);

	}

	public static void upperRight(Window window) {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation(screenSize.width / 4 * 3, 50);
	}

	public static SplitFeatureDialog splitFeature(Model model) {
		if (splitFeature == null) {
			splitFeature = new SplitFeatureDialog(model);
		}
		return splitFeature;
	}

	public static String shortify(String in) {
		String s = in.replace('\\', '/');
		int idx = s.lastIndexOf('/');
		if (idx > 0) {
			s = s.substring(idx);
		}
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

	/**
	 * Opens a browser window at specified URI. In case of a problem a warning
	 * is logged to provided logger.
	 * 
	 * @param uri the uri to open, as string. The string will be converted using
	 *            {@link URIFactory#uri(String)}, which also handles special
	 *            characters
	 * @param log the {@link Reporter} to log issues
	 */
	public static void browse(String uristring, Globals globals) {
		try {
			Desktop.getDesktop().browse(URIFactory.uri(uristring));
		} catch (Throwable e) {
			globals.getLog().log(Level.WARNING,
					globals.getMessageManager().formatMessage(
							"staticutils.couldnt_open_url_warn",
							new Object[] { uristring }),
					e);
		}

	}

	public static void forceExit() {
		/*
		 * Due to some bugs in AWT, Swing and some other stuff, we need to force
		 * webstart applications to shut down
		 * 
		 * http://stackoverflow.com/questions/212009/do-i-have-to-explicitly
		 * -call-system-exit-in-a-webstart-application
		 * 
		 * http://stackoverflow.com/questions/216315/what-is-the-best-way
		 * -to-detect-whether-an-application-is-launched-by-webstart
		 */
		if (!Environment.isApplet()
				|| (Environment.isApplet() && !Environment.isMac())) {
			// This will make sure the application exits.
			// We don't want to do this on Mac because it will exit the
			// browser as well if running as an Applet.
			System.exit(0);
		}

	}

	/**
	 * @param as a string
	 * @return substring of as up to the first "." char
	 */
	public static String chopchop(String as) {
		if (as.indexOf('.') >= 0) {
			return as.substring(0, as.indexOf('.'));
		} else {
			return as;
		}
	}

	/**
	 * sand box the run of f and log any {@link Throwable}. Input arguments for
	 * f can be put in the direct context of the call. f can not return anything
	 * directly because there is no return value incase of an exception.
	 * 
	 * @param f          the {@link Executable} to run in the sandbox
	 * @param r          the reporter to log any exceptions to
	 * @param errorlevel the level to use when logging
	 * @param errormsg   the error message to use when logging an exception
	 */
	public static void run(Executable f, Reporter r, Level errorlevel,
			String errormsg) {
		try {
			f.run();
		} catch (Throwable e) {
			r.log(errorlevel, errormsg, e);
		}
	}

}
