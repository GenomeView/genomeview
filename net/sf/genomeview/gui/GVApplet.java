package net.sf.genomeview.gui;

import java.awt.Component;
import java.awt.Frame;
import java.awt.KeyboardFocusManager;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.JApplet;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.MainMenu;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.Cleaner;

public class GVApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	protected Model model;

	private Logger logger = Logger.getLogger(GVApplet.class.getCanonicalName());
	private JPanel gvPanel;

	@Override
	public void init() {
		LogConfigurator.config();

		logger.info("Starting GenomeView Applet");
		Authenticator.setDefault(new MyAuthenticator());

		final String configO = getParameter("config");
		final String fileO = getParameter("file");
		final String urlO = getParameter("url");
		final String positionO = getParameter("position");
		final String extraO = getParameter("extra");

		final Frame parentFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, this);

		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					model = new Model(parentFrame);
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
						initLoader.init(configO, fileO, urlO, remArgs, positionO);
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
					System.out.println(gvPanel.getPreferredSize());
					System.out.println(getRootPane().getParent().getSize());
					// final Frame parentFrame = (Frame)
					// SwingUtilities.getAncestorOfClass(Frame.class, this);
					// parentFrame.pack();
					// gvPanel.validate();
					model.setSilent(false);
					
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
