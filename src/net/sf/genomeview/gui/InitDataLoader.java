package net.sf.genomeview.gui;

import java.awt.EventQueue;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.DataSourceHelper;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.ReadWorker;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.explorer.GenomeExplorerManager;
import net.sf.genomeview.gui.external.ExternalHelper;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import net.sf.jannot.source.IndexManager;
import net.sf.jannot.source.Locator;
import net.sf.jannot.source.cache.SourceCache;
import be.abeel.net.URIFactory;

public class InitDataLoader {

	private Model model;
	private Logger logger;

	public InitDataLoader(Model model) {
		this.model = model;
		logger = Logger.getLogger(InitDataLoader.class.getCanonicalName());
	}

	public void init(String cmdUrl, String cmdFile, String[] remArgs, String position, String session)
			throws InterruptedException, ExecutionException {

		SourceCache.cacheDir = new File(Configuration.getDirectory(), "cache");
		IndexManager.cacheDir = new File(Configuration.getDirectory(), "index");
		DataSourceFactory.disableURLCaching = Configuration.getBoolean("general:disableURLCaching");

		/*
		 * Initialize session, all other arguments will override what the
		 * session does.
		 */
		try {
			if (session != null)
				Session.loadSession(model, session);
		} catch (IOException e1) {
			CrashHandler.showErrorMessage("Failed to properly load requested session.", e1);
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
			logger.info("File and url options are null!");
			// do nothing

		} else if (cmdUrl != null) {
			logger.info("URL commandline option is set: " + cmdUrl);

			try {
				DataSourceHelper.load(model, new Locator(cmdUrl), true);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReadFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		} else if (cmdFile != null) {
			logger.info("File commandline option is set: " + cmdFile);

			try {
				DataSourceHelper.load(model, new Locator(cmdFile), true);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ReadFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		

		/* Load additional files */
		for (String s : remArgs) {
			logger.info("loading additional from commandline: " + s);
			try {
				if (!s.startsWith("http:") && !s.startsWith("ftp:") && !s.startsWith("https:")) {
					DataSourceHelper.load(model, new Locator(s));
					//
					// ReadWorker rf = new ReadWorker(ds, model);
					// rf.execute();

				} else {
					DataSourceHelper.load(model, new Locator(s));
					// ReadWorker rf = new ReadWorker(ds, model);
					// rf.execute();

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

		if (position != null) {
			logger.info("Initial position requested to "+position);
			ExternalHelper.setPosition(position, model);

		}
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new GenomeExplorerManager(model);
				model.refresh(this);

			}
		});

	}

}
