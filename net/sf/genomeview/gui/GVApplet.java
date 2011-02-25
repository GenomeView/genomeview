/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.MainMenu;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.Cleaner;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GVApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	protected Model model;

	private Logger logger = Logger.getLogger(GVApplet.class.getCanonicalName());
	private JPanel gvPanel;

	@Override
	public void init() {
		System.out.println("Applet init");

		LogConfigurator.config();

		logger.info("Starting GenomeView Applet");
		Authenticator.setDefault(new MyAuthenticator());

		final String configO = getParameter("config");
		logger.info("ConfigO: " + configO);
		final String fileO = getParameter("file");
		logger.info("fileO: " + fileO);
		final String urlO = getParameter("url");
		logger.info("urlO: " + urlO);
		final String positionO = getParameter("position");
		logger.info("positionO: " + positionO);
		final String sessionO = getParameter("session");
		logger.info("sessionO: " + sessionO);

		final String extraO = getParameter("extra");
		logger.info("extraO: " + extraO);

		final String idO = getParameter("id");
		logger.info("idO: " + extraO);

		final Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					model = new Model(idO, configO);
					model.getGUIManager().registerMainWindow(parentFrame);
					model.setSilent(true);
					model.clearEntries();

					String[] remArgs;
					if (extraO != null) {
						remArgs = extraO.trim().split("\\s+");
					} else {
						remArgs = new String[0];
					}

					InitDataLoader initLoader = new InitDataLoader(model);
					try {
						initLoader.init(urlO, fileO, remArgs, positionO, sessionO);
					} catch (InterruptedException e) {
						logger.info(e.getMessage());
					} catch (ExecutionException e) {
						logger.info(e.getMessage());
					}
					gvPanel = MainContent.createContent(model, 1)[0];
					getContentPane().add(gvPanel);
					setJMenuBar(new MainMenu(model));

					getRootPane().setTransferHandler(new DropTransferHandler(model));
					KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Hotkeys(model));

					PluginLoader.load(model);
					model.setSilent(false);
					ReferenceMissingMonitor rmm = new ReferenceMissingMonitor(model);
				}
			});
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void start() {

	}

	@Override
	public void stop() {

	}

	@Override
	public void destroy() {
		logger.info("Destroying applet");
		model.exit();

		logger.info("Disposing the window in GVApplet.update()");

		try {
			Configuration.save();
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Problem saving configuration", e);
		}

		Cleaner.exit();

		System.out.println("Applet should be exiting here, if it doesn't happen, we will need to do some work...");

	}

}
