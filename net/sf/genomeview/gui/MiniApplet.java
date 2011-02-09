/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.MainMenu;
import net.sf.genomeview.gui.viztracks.AnnotationFrame;
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

	private Logger logger = Logger.getLogger(MiniApplet.class.getCanonicalName());
	

	@Override
	public void init() {
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
		
		final Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					model = new Model(idO,configO);
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
					ReferenceMissingMonitor rmm=new ReferenceMissingMonitor(model);
					
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
		Cleaner.exit();
		System.exit(0);
	}

}
