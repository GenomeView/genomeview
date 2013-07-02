/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.Cleaner;
/**
 * Minimal version of the applet.
 * 
 * @author Thomas Abeel
 *
 */
public class MiniApplet extends JApplet {

	//FIXME Both applets should have same superclass
	private static final long serialVersionUID = 1L;
	protected Model model;

	private Logger logger = LoggerFactory.getLogger(MiniApplet.class.getCanonicalName());
	

	@Override
	public void init() {
		Environment.setApplet();
		LogConfigurator.config();

		logger.info("Starting GenomeView Mini Applet");
		Authenticator.setDefault(new MyAuthenticator());

		final String configO = getParameter("config");
		logger.info("ConfigO: "+configO);
		final String fileO = getParameter("file");
		logger.info("fileO: "+fileO);
		final String urlO = getParameter("url");
		logger.info("urlO: "+urlO);
		final String positionO = getParameter("position");
		logger.info("positionO: "+positionO);
		final String sessionO = getParameter("session");
		logger.info("sessionO: "+sessionO);
		
		final String extraO = getParameter("extra");
		logger.info("extraO: "+extraO);

		final String idO = getParameter("id");
		logger.info("idO: "+extraO);
		
		CommandLineOptions.init(new String[]{"--config",configO});
		
		final Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					model = new Model(idO);
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
						initLoader.init(urlO, fileO, remArgs, positionO,sessionO);
					} catch (InterruptedException e) {
						logger.info(e.getMessage());
					} catch (ExecutionException e) {
						logger.info(e.getMessage());
					}
					JPanel gvPanel = MainContent.createMiniContent(model);
					getContentPane().add(gvPanel);
					getRootPane().setTransferHandler(new DropTransferHandler(model));
					KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Hotkeys(model));

					PluginLoader.load(model);
					model.setSilent(false);
					ReferenceMissingMonitor.init(model);
					
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
	public void destroy() {
		logger.info("Destroying applet");
		model.exit();

		logger.info("Disposing the window in GVApplet.update()");

		try {
			Configuration.save();
		} catch (IOException e) {
			logger.error( "Problem saving configuration", e);
		}

		Cleaner.exit();

		System.out.println("Applet should be exiting here, if it doesn't happen, we will need to do some work...");

	}

}
