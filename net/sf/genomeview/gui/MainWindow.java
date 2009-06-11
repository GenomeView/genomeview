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
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.source.MobySource;
import net.sf.genomeview.gui.menu.MainMenu;
import net.sf.genomeview.gui.task.ReadEntriesWorker;
import net.sf.genomeview.gui.task.ReadFeaturesWorker;
import net.sf.genomeview.plugin.PluginLoader;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.URLSource;
import be.abeel.jargs.AutoHelpCmdLineParser;

/**
 * MainWindow is the container for a single GenomeView instance.
 * 
 */
public class MainWindow implements WindowListener, Observer {
	private static Logger logger = Logger.getLogger(MainWindow.class
			.getCanonicalName());

	private JFrame window;

	private JFrame helper = null;

	private Model model;

	/**
	 * Adds an observer to the model, which will only check if the instance is
	 * still running.
	 * 
	 * @param o
	 */
	void addInstanceObserver(Observer o) {
		model.addObserver(o);
	}

	public MainWindow(String args[]) throws InterruptedException,
			ExecutionException {

		/* Initialize the command line options */
		AutoHelpCmdLineParser parser = new AutoHelpCmdLineParser();
		Option urlO = parser.addHelp(parser.addStringOption("url"),
				"Start GenomeView with data loaded from the URL");

		Option fileO = parser.addHelp(parser.addStringOption("file"),
				"Start GenomeView with data loaded from a file.");
		Option bogasO = parser.addHelp(parser.addStringOption("bogas"),
				"Start GenomeView with BOGAS data");

		Option hideChromViewO = parser.addHelp(parser
				.addBooleanOption("hideCV"), "Hides the chromosome panel.");
		Option hideStructureViewO = parser.addHelp(parser
				.addBooleanOption("hideSV"), "Hides the structure panel.");
		Option hideEvidenceViewO = parser.addHelp(parser
				.addBooleanOption("hideEV"), "Hides the evidence panel.");

		Option configurationO = parser.addHelp(
				parser.addStringOption("config"),
				"Provide additional configuration to load.");

		parse(parser, args);

		if (parser.checkHelp()) {
			System.exit(0);
		}
		
		/* Load the additional configuration */
		String config = (String) parser.getOptionValue(configurationO);
		if (config != null) {
			try {
				if (config.startsWith("http") || config.startsWith("ftp")) {
					Configuration.loadExtra(new URI(config).toURL()
							.openStream());
				} else {
					Configuration.loadExtra(new FileInputStream(config));
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		
		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();
		GraphicsDevice[] gs = ge.getScreenDevices();
		logger.info("Creating new window");
		window = new JFrame("GenomeView :: " + Configuration.version(), gs[0]
				.getDefaultConfiguration());

		window.setIconImage(new ImageIcon(this.getClass().getResource(
				"/images/gv2.png")).getImage());
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		window.addWindowListener(this);

		model = new Model(window);
		model.setSilent(true);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new Hotkeys(model));
		
		/* Data specified on command line */
		String cmdUrl = (String) parser.getOptionValue(urlO);
		String cmdFile = (String) parser.getOptionValue(fileO);
		final String cmdBogas = (String) parser.getOptionValue(bogasO);

		/*
		 * Select data source. If an URL or file are specified on the command
		 * line, that is selected. In other cases a dialog pops-up to let the
		 * user select.
		 * 
		 * If both file and url are specified, the URL is loaded.
		 */
		DataSource[] data = null;
		if (cmdFile == null && cmdUrl == null && cmdBogas == null) {
			logger.info("File, url and bogas options are null!");
			// Sources source = (Sources) JOptionPane.showInputDialog(window,
			// "Select main data source", "Data selection",
			// JOptionPane.INFORMATION_MESSAGE, null, Sources.values(),
			// Sources.values()[0]);
			// if(source!=null)
			// data = DataSourceFactory.create(source, model);

		} else if (cmdUrl != null) {
			logger.info("URL commandline option is set: " + cmdUrl);
			try {
				data = new DataSource[] { new URLSource(new URI(cmdUrl).toURL()) };
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
		} else if (cmdBogas != null) {
			logger.info("BOGAS commandline option is set: " + cmdBogas);
			try {
				data = new DataSource[] { new MobySource(new URI(cmdBogas)
						.toURL()) };
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		/* Which panels to hide */
		Boolean hideChromView = (Boolean) parser.getOptionValue(hideChromViewO,
				Boolean.FALSE);
		Boolean hideStructureView = (Boolean) parser.getOptionValue(
				hideStructureViewO, Boolean.FALSE);
		Boolean hideEvidenceView = (Boolean) parser.getOptionValue(
				hideEvidenceViewO, Boolean.FALSE);

		model.setChromosomeVisible(!hideChromView);
		model.setStructureVisible(!hideStructureView);
		model.setAnnotationVisible(!hideEvidenceView);

		

		JPanel[] content = MainContent.createContent(model, Configuration
				.getBoolean("dualscreen") ? gs.length : 1);

		window.setContentPane(content[0]);
		window.setJMenuBar(new MainMenu(model, this));
		window.setVisible(true);
		window.pack();
		window.setExtendedState(window.getExtendedState()
				+ JFrame.MAXIMIZED_BOTH);

		if (content.length > 1) {
			for (int i = 1; i < content.length; i++) {
				helper = new JFrame("GenomeView :: " + Configuration.version(),
						gs[i].getDefaultConfiguration());
				helper.setJMenuBar(new MainMenu(model, this));
				helper.setIconImage(new ImageIcon(this.getClass().getResource(
						"/images/gv2.png")).getImage());
				helper.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				helper.setContentPane(content[i]);
				helper.setVisible(true);
				helper.pack();
				helper.setExtendedState(helper.getExtendedState()
						+ JFrame.MAXIMIZED_BOTH);
			}
		}

		PluginLoader.load(model);

		/* Load the source, if one was constructed */
		if (data != null) {
			assert (data.length == 1);
			final ReadEntriesWorker rw = new ReadEntriesWorker(data[0], model);
			rw.execute();
			rw.get();

			/* Load additional files */
			String[] remArgs = parser.getRemainingArgs();
			for (String s : remArgs) {
				logger.info("loading additional from commandline: " + s);
				try {
					if (!s.startsWith("http:") && !s.startsWith("ftp:")
							&& !s.startsWith("https:")) {
						DataSource ds = new FileSource(new File(s));
						
						ReadFeaturesWorker rf = new ReadFeaturesWorker(ds,
								model);
						rf.execute();

					} else {
						DataSource ds = new URLSource(new URI(s).toURL());
						
						ReadFeaturesWorker rf = new ReadFeaturesWorker(ds,
								model);
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

		}

		model.addObserver(this);
		/* Start acting */
		model.setSilent(false);
	}

	private void parse(AutoHelpCmdLineParser parser, String[] args) {
		try {
			parser.parse(args);
		} catch (IllegalOptionValueException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownOptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		System.out.println("Closing this window by x-button");
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

	/**
	 * Keeps an eye on the model (used to detect exitRequested)
	 * 
	 * @param o
	 * @param arg
	 */
	@Override
	public void update(Observable o, Object arg) {
		if (model.isExitRequested()) {
			System.out.println("Disposing the window here.");
			dispose();
		}
	}

}
