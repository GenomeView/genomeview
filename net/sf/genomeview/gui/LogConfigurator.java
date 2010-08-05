package net.sf.genomeview.gui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogConfigurator {
	
	static private Logger logger;
	
	static public void config(){
		
		
		/* Configure logging */
		try {
			LogManager.getLogManager().readConfiguration(GenomeView.class.getResourceAsStream("/conf/logging.conf"));
			logger = Logger.getLogger(GenomeView.class.getCanonicalName());
		} catch (SecurityException e) {
			logger.log(Level.SEVERE, "log initialization", e);
			/*
			 * These exceptions likely indicate that the log could not be
			 * initialized, print also to console
			 */
			e.printStackTrace();

		} catch (IOException e) {
			logger.log(Level.SEVERE, "log initialization", e);
			/*
			 * These exceptions likely indicate that the log could not be
			 * initialized, print also to console
			 */
			e.printStackTrace();
		}

	}
}
