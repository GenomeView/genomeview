package net.sf.genomeview.gui;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.explorer.DataExplorerManager;
import net.sf.jannot.source.IndexManager;
import net.sf.jannot.source.Locator;
import tudelft.utilities.logging.Reporter;

/**
 * This seems initialization functionality for the {@link Model}
 */
public class InitDataLoader {

	private Model model;

	public InitDataLoader(Model model) {
		this.model = model;
	}

	/**
	 * Process commandline options and command file
	 * 
	 * @param cmdUrl
	 * @param cmdFile
	 * @param remArgs
	 * @param position
	 * @param session
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void init(String cmdUrl, String cmdFile, String[] remArgs,
			String position, String session)
			throws InterruptedException, ExecutionException {
		Configuration conf = model.getConfiguration();

//		SourceCache cache = new SourceCache(
//				new File(conf.getDirectory(), "cache"));
		IndexManager.cacheDir = new File(conf.getDirectory(), "index");
//		this.DataSourceFactory.disableURLCaching = conf
//				.getBoolean("general:disableURLCaching");
		final Reporter log = model.getLog();

		/*
		 * Initialize session, all other arguments will override what the
		 * session does.
		 */
		try {
			if (session != null) {
				Session.loadSession(model, session);
			}
		} catch (IOException e1) {
			model.getLog().log(Level.WARNING, model.getMessageMgr().getString(
					"crashhandler.failed_to_propertly_load_requested_session"),
					e1);
		}

		/*
		 * Select data source. If an URL or file are specified on the command
		 * line, that is selected. In other cases a dialog pops-up to let the
		 * user select.
		 * 
		 * If both file and url are specified, the URL is loaded.
		 */
		// DataSource[] data = null;
		if (cmdFile == null && cmdUrl == null) {
			log.log(Level.INFO, "File and url options are null!");
			// do nothing

		} else if (cmdUrl != null) {
			log.log(Level.INFO, "URL commandline option is set: " + cmdUrl);

			try {
				new DataSourceHelper(model).load(new Locator(cmdUrl, log),
						true);
			} catch (URISyntaxException | IOException e) {
				log.log(Level.WARNING, "problem loading url " + cmdUrl, e);
			}

		} else if (cmdFile != null) {
			log.log(Level.INFO, "File commandline option is set: " + cmdFile);

			try {
				new DataSourceHelper(model).load(new Locator(cmdFile, log),
						true);
			} catch (URISyntaxException | IOException e) {
				log.log(Level.WARNING, "problem loading file " + cmdFile, e);
			}

		}

		/* Load additional files */
		for (String s : remArgs) {
			log.log(Level.INFO, "loading additional from commandline: " + s);
			try {
				new DataSourceHelper(model).load(new Locator(s, log));
			} catch (Exception e) {
				log.log(Level.WARNING, "problem loading  " + s, e);
			}
		}

		if (position != null) {
			log.log(Level.INFO, "Initial position requested to " + position);
			model.setPosition(position);

		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new DataExplorerManager(model);
				model.refresh(this);

			}
		});

	}

}
