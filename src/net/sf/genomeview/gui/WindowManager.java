/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.MainMenu;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.Cleaner;

/**
 * MainWindow is the container for a single GenomeView instance.
 * 
 * @author Thomas Abeel
 * 
 */
public class WindowManager extends WindowAdapter implements Observer {
	private static Logger logger = LoggerFactory.getLogger(WindowManager.class.getCanonicalName());

	private GenomeViewWindow window = null;

	private GenomeViewWindow helper = null;

	private Model model = null;

	Model getModel() {
		return model;
	}

	/**
	 * Adds an observer to the model, which will only check if the instance is
	 * still running.
	 * 
	 * @param o
	 */
	void addInstanceObserver(Observer o) {
		model.addObserver(o);
	}

	public WindowManager(String args[], Splash splash) throws InterruptedException, ExecutionException {
		running++;
		logger.info("Started running instance" + running);
		init(args, splash);
	}

	public void dispose() {
		window.dispose();
		if (helper != null)
			helper.dispose();

	}

	/**
	 * Just see if the user tries to close the window directly and tell the
	 * model if this would happen.
	 */
	@Override
	public void windowClosing(WindowEvent e) {

		int result=JOptionPane.showConfirmDialog(model.getGUIManager().getParent(), MessageManager.getString("windowmanager.exit"));
		if(result==JOptionPane.YES_OPTION)
			this.model.exit();
	}

	private static int running = 0;

	/**
	 * Keeps an eye on the model (used to detect exitRequested) and to detect
	 * whether the used has not loaded any data.
	 * 
	 * 
	 * 
	 * 
	 * @param o
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {

		if (model.isExitRequested()) {
			model.deleteObserver(this);
			logger.info("Disposing the window in MainWindow.update()");
			dispose();
			try {
				Configuration.save();
			} catch (IOException e) {
				logger.error( "Problem saving configuration", e);
			}

			running--;
			logger.info("Instances still running: " + running);
			if (running < 1) {
				logger.info("No instances left, exiting VM");
				Cleaner.exit();

				// System.exit(0);
				System.out.println("We should be exiting here, if it doesn't happen, we will need to do some work...");

				for (Frame f : Frame.getFrames()) {
					System.out.println("Disposing loose frame: " + f);
					f.dispose();
				}
				// Dumping all running threads that are holding up the show
				System.out.println("Dumping all running threads");
				Thread[] threads = getAllThreads();
				for (Thread id : threads) {
					System.out.println(id.getName() + "\t" + id.isDaemon() + "\t" + id.isAlive());

				}

				StaticUtils.forceExit();

			}
		}

	}

	private ThreadGroup getRootThreadGroup() {
		// if ( rootThreadGroup != null )
		// return rootThreadGroup;
		ThreadGroup tg = Thread.currentThread().getThreadGroup();
		ThreadGroup ptg;
		while ((ptg = tg.getParent()) != null)
			tg = ptg;
		return tg;
	}

	private Thread[] getAllThreads() {
		final ThreadGroup root = getRootThreadGroup();
		final ThreadMXBean thbean = ManagementFactory.getThreadMXBean();
		int nAlloc = thbean.getThreadCount();
		int n = 0;
		Thread[] threads;
		do {
			nAlloc *= 2;
			threads = new Thread[nAlloc];
			n = root.enumerate(threads, true);
		} while (n == nAlloc);
		return java.util.Arrays.copyOf(threads, n);
	}

	public void init(String[] args, Splash splash) throws InterruptedException, ExecutionException {
		// FIXME special handling if this is not the first time the application
		// is initialized
		if (splash != null)
			splash.setText(MessageManager.getString("windowmanager.parsing_params"));
		CommandLineOptions.init(args);

		if (splash != null)
			splash.setText(MessageManager.getString("windowmanager.creating_windows"));
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		boolean freshwindow = false;

		if (model == null) {
			model = new Model(CommandLineOptions.id());
			model.addObserver(this);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Hotkeys(model));
		}

		if (window == null) {
			freshwindow = true;
			logger.info(MessageManager.getString("windowmanager.creating_new_window"));
			window = new GenomeViewWindow(model, "GenomeView :: " + Configuration.version(), gs[0].getDefaultConfiguration());
			model.getGUIManager().registerMainWindow(window);
			window.setIconImage(Icons.MINILOGO);
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			window.addWindowListener(this);
			window.getRootPane().setTransferHandler(new DropTransferHandler(model));

		}

		/* Make sure the model is empty */
		model.setSilent(true);
		model.clearEntries();

		if (freshwindow) {
			JPanel[] content = MainContent.createContent(model, Configuration.getBoolean("dualscreen") ? gs.length : 1);
			window.setContentPane(content[0]);
			window.setJMenuBar(new MainMenu(model));

			window.pack();
			Rectangle rec = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
			window.setSize(rec.width, rec.height);

			if (content.length > 1) {
				for (int i = 1; i < content.length; i++) {
					helper = new GenomeViewWindow(model, "GenomeView :: " + Configuration.version(), gs[i].getDefaultConfiguration());
					helper.setJMenuBar(new MainMenu(model));
					helper.setIconImage(new ImageIcon(this.getClass().getResource("/images/gv2.png")).getImage());
					helper.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					helper.setContentPane(content[i]);
					helper.setVisible(true);
					helper.pack();
				}
			}
			window.setVisible(true);
			if (splash != null)
				splash.setText(MessageManager.getString("windowmanager.installing_plugins"));
			PluginLoader.load(model);

		}
		if (splash != null)
			splash.setText(MessageManager.getString("windowmanager.loading_data"));
		/* Data specified on command line */
		InitDataLoader idl = new InitDataLoader(model);
		if (CommandLineOptions.goodParse()) {
			String cmdUrl = CommandLineOptions.url();// (String)
														// parser.getOptionValue(urlO);
			String cmdFile = CommandLineOptions.file();// (String)
														// parser.getOptionValue(fileO);
			String session = CommandLineOptions.session();// (String)
															// parser.getOptionValue(sessionO);
			String[] remArgs = CommandLineOptions.remaining();// parser.getRemainingArgs();
			String initialLocation = CommandLineOptions.position();// (String)
																	// parser.getOptionValue(positionO);

			File prevSession = new File(Configuration.getDirectory(), "previous.gvs");
			logger.info("Loading previous session: " + prevSession);
			if (prevSession.exists() && prevSession.length() > 0 && cmdUrl == null && cmdFile == null && session == null
					&& remArgs.length == 0)
				idl.init(null, null, new String[0], null, prevSession.toString());
			else
				idl.init(cmdUrl, cmdFile, remArgs, initialLocation, session);
		} else {
			idl.init(null, null, new String[0], null, null);
		}

		ReferenceMissingMonitor.init(model);
		/* Start acting */
		model.setSilent(false);

	}

}

/**
 * Information about the environment that the application is running in.
 * 
 * @author Thomas Abeel
 * 
 */
class Environment {
	private static boolean applet = false;

	public static boolean isApplet() {
		return applet;
	}

	public static boolean isWebstart() {
		/* While this may not work 100%, it is better than nothing :-/ */
		return System.getProperty("javawebstart.version", null) != null;

	}

	public static boolean isWindows() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("win") >= 0);

	}

	public static boolean isNix() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0);

	}

	public static boolean isMac() {
		String os = System.getProperty("os.name").toLowerCase();
		return (os.indexOf("mac") >= 0);

	}

	public static void setApplet() {
		applet = true;

	}

}
