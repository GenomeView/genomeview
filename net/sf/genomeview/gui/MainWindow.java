/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import jargs.gnu.CmdLineParser.IllegalOptionValueException;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.UnknownOptionException;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.DataSourceFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.MainMenu;
import net.sf.genomeview.gui.task.ReadWorker;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.Location;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import be.abeel.jargs.AutoHelpCmdLineParser;

/**
 * MainWindow is the container for a single GenomeView instance.
 * 
 */
public class MainWindow implements WindowListener, Observer {
	private static Logger logger = Logger.getLogger(MainWindow.class.getCanonicalName());

	private JFrame window = null;

	private JFrame helper = null;

	private Model model = null;

	/**
	 * Adds an observer to the model, which will only check if the instance is
	 * still running.
	 * 
	 * @param o
	 */
	void addInstanceObserver(Observer o) {
		model.addObserver(o);
	}

	public MainWindow(String args[]) throws InterruptedException, ExecutionException {
		running++;
		logger.info("Started running instance" +running);
		init(args);
	}

	private void parse(AutoHelpCmdLineParser parser, String[] args) {
		try {
			parser.parse(args);
		} catch (IllegalOptionValueException e) {
			logger.log(Level.SEVERE,"parsing command line options", e);
		} catch (UnknownOptionException e) {
			logger.log(Level.SEVERE,"", e);
		}

	}

	public void dispose() {
		window.dispose();
		if (helper != null)
			helper.dispose();

	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	/**
	 * Just see if the user tries to close the window directly and tell the
	 * model if this would happen.
	 */
	@Override
	public void windowClosing(WindowEvent e) {
		this.model.exit();
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub

	}

	private static int running=0;
	/**
	 * Keeps an eye on the model (used to detect exitRequested)
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
				logger.log(Level.SEVERE,"saving configuration", e);
			}
			running--;
			logger.info("Instances still running: "+running);
			if(running<1){
				logger.info("No instances left, exiting VM");
				System.exit(0);
			}
		}
	}

	public void init(String[] args) throws InterruptedException, ExecutionException {
		//FIXME special handling if this is not the first time the application is initialized
		
		/* Initialize the command line options */
		AutoHelpCmdLineParser parser = new AutoHelpCmdLineParser();
		Option urlO = parser.addHelp(parser.addStringOption("url"), "Start GenomeView with data loaded from the URL");

		Option fileO = parser.addHelp(parser.addStringOption("file"), "Start GenomeView with data loaded from a file.");

		Option configurationO = parser.addHelp(parser.addStringOption("config"), "Provide additional configuration to load.");

		Option positionO = parser.addHelp(parser.addStringOption("position"), "Provide the initial region that should be visible.");

		parse(parser, args);

		if (parser.checkHelp()) {
			System.exit(0);
		}

		/* Load the additional configuration */
		String config = (String) parser.getOptionValue(configurationO);
		if (config != null) {
			try {
				if (config.startsWith("http") || config.startsWith("ftp")) {
					Configuration.loadExtra(new URI(config).toURL().openStream());
				} else {
					Configuration.loadExtra(new FileInputStream(config));
				}
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE,"loading extra configuration", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE,"loading extra configuration", e);
			} catch (URISyntaxException e) {
				logger.log(Level.SEVERE,"loading extra configuration", e);
			}
		}

		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		boolean freshwindow = false;
		if (window == null) {
			freshwindow=true;
			logger.info("Creating new window");
			window = new JFrame("GenomeView :: " + Configuration.version(), gs[0].getDefaultConfiguration());

			window.setIconImage(new ImageIcon(this.getClass().getResource("/images/gv2.png")).getImage());
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			window.addWindowListener(this);
			

		}
		if (model == null){
			model = new Model(window);
			window.getRootPane().setTransferHandler(new DropTransferHandler(model));
			model.addObserver(this);
			KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Hotkeys(model));
		}
		/* Make sure the model is empty */
		model.setSilent(true);
		model.clearEntries();

		

		if (freshwindow) {
			JPanel[] content = MainContent.createContent(model, Configuration.getBoolean("dualscreen") ? gs.length : 1);

			window.setContentPane(content[0]);
			window.setJMenuBar(new MainMenu(model, this));
			window.setVisible(true);
			window.pack();
			window.setExtendedState(JFrame.MAXIMIZED_BOTH);
			if (content.length > 1) {
				for (int i = 1; i < content.length; i++) {
					helper = new JFrame("GenomeView :: " + Configuration.version(), gs[i].getDefaultConfiguration());
					helper.setJMenuBar(new MainMenu(model, this));
					helper.setIconImage(new ImageIcon(this.getClass().getResource("/images/gv2.png")).getImage());
					helper.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
					helper.setContentPane(content[i]);
					helper.setVisible(true);
					helper.pack();
				}
			}

			PluginLoader.load(model);

		}
		/* Data specified on command line */
		String cmdUrl = (String) parser.getOptionValue(urlO);
		String cmdFile = (String) parser.getOptionValue(fileO);

		/*
		 * Select data source. If an URL or file are specified on the command
		 * line, that is selected. In other cases a dialog pops-up to let the
		 * user select.
		 * 
		 * If both file and url are specified, the URL is loaded.
		 */
		DataSource[] data = null;
		if (cmdFile == null && cmdUrl == null) {
			logger.info("File and url options are null!");
			// do nothing
			
		} else if (cmdUrl != null) {
			logger.info("URL commandline option is set: " + cmdUrl);
			try {
				data = new DataSource[] { DataSourceFactory.createURL(new URL(cmdUrl)) };
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (cmdFile != null) {
			logger.info("File commandline option is set: " + cmdFile);
			try {
				data = new DataSource[] { new FileSource(new File(cmdFile)) };

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		/* Load the source, if one was constructed */
		if (data != null) {
			
			assert (data.length == 1);
			logger.info("Loading with priority: "+data[0]);
			final ReadWorker rw = new ReadWorker(data[0], model);
			rw.execute();
			rw.get();
		}
		/* Load additional files */
		String[] remArgs = parser.getRemainingArgs();
		for (String s : remArgs) {
			logger.info("loading additional from commandline: " + s);
			try {
				if (!s.startsWith("http:") && !s.startsWith("ftp:") && !s.startsWith("https:")) {
					DataSource ds = new FileSource(new File(s));

					ReadWorker rf = new ReadWorker(ds, model);
					rf.execute();

				} else {
					DataSource ds = DataSourceFactory.createURL(new URL(s));
					ReadWorker rf = new ReadWorker(ds, model);
					rf.execute();

				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		String initialLocation = (String) parser.getOptionValue(positionO);
		if (initialLocation != null) {
			String[] arr = initialLocation.split(":");
			assert arr.length==2||arr.length==3;
			if(arr.length==3){
				model.setSelectedEntry(model.entry(arr[0]));
				model.setAnnotationLocationVisible(new Location(Integer.parseInt(arr[1]), Integer.parseInt(arr[2])));
			}else if(arr.length==2){
				model.setAnnotationLocationVisible(new Location(Integer.parseInt(arr[0]), Integer.parseInt(arr[1])));	
			}
			

		}
		
		/* Start acting */
		model.setSilent(false);

	}

}
