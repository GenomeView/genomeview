/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.io.IOException;
import java.util.logging.LogManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class LogConfigurator {
	
	static private Logger logger;
	
	static public void config(){
		
		
		/* Configure logging */
		try {
			
			LogManager.getLogManager().readConfiguration(GenomeView.class.getResourceAsStream("/conf/logging.conf"));
			logger = LoggerFactory.getLogger(GenomeView.class.getCanonicalName());
		} catch (SecurityException e) {
			logger.error( "log initialization", e);
			/*
			 * These exceptions likely indicate that the log could not be
			 * initialized, print also to console
			 */
			e.printStackTrace();

		} catch (IOException e) {
			logger.error( "log initialization", e);
			/*
			 * These exceptions likely indicate that the log could not be
			 * initialized, print also to console
			 */
			e.printStackTrace();
		}

	}
}
