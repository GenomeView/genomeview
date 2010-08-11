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
import net.sf.genomeview.plugin.PluginLoader;

public class GVApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	protected Model model;

	private Logger logger = Logger.getLogger(GVApplet.class.getCanonicalName());
	
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
		
		final Frame parentFrame = (Frame)SwingUtilities.getAncestorOfClass(Frame.class, this);
		
		try {
			SwingUtilities.invokeAndWait(new Runnable(){
				@Override
				public void run() {
					model = new Model(parentFrame);
					model.setSilent(true);
					model.clearEntries();
					
					
					String[] remArgs;
					if (extraO!=null){
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
					
					JPanel gvPanel = MainContent.createContent(model, 1)[0];
					setContentPane(gvPanel);
					
					setJMenuBar(new MainMenu(model));
					
					getRootPane().setTransferHandler(new DropTransferHandler(model));					
					KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Hotkeys(model));
					
					PluginLoader.load(model);
					
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
	public void destroy(){
		System.exit(0);
	}
	
	
	
}
