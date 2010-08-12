/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import net.sf.genomeview.core.Configuration;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class GenomeView {
	private static Logger logger;

	private static MainWindow mw;

	/*
	 * Rewrite args if started from file association
	 */
	private static void jnlprewrite(String[] args) {
		if (args.length > 0 && args[0].equals("-open"))
			args[0] = "--file";

	}
	private static Splash splash=null;

	public static void main(final String[] args) {
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					
					splash = new Splash();
				}
			});
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InvocationTargetException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				/* Rewrite JNLP arguments */
				jnlprewrite(args);
				/*
				 * The configuration class needs to be called at least once
				 * before we can start the logger
				 */
				System.out.println("Starting GenomeView " + Configuration.version());

				/* Configure logging */
				try {
					LogManager.getLogManager().readConfiguration(
							GenomeView.class.getResourceAsStream("/conf/logging.conf"));
					logger = Logger.getLogger(GenomeView.class.getCanonicalName());
				} catch (SecurityException e) {
					logger.log(Level.SEVERE, "log initialization", e);
					/*
					 * These exceptions likely indicate that the log could not
					 * be initialized, print also to console
					 */
					e.printStackTrace();

				} catch (IOException e) {
					logger.log(Level.SEVERE, "log initialization", e);
					/*
					 * These exceptions likely indicate that the log could not
					 * be initialized, print also to console
					 */
					e.printStackTrace();
				}

				/* Single instance manager */
				boolean singleInstance = Configuration.getBoolean("general:singleInstance");
				if (singleInstance) {
					if (!ApplicationInstanceManager.registerInstance()) {
						// instance already running.
						System.out.println("Another instance of this application is already running.  Exiting.");
						System.exit(0);
					}
					ApplicationInstanceManager.setApplicationInstanceListener(new ApplicationInstanceListener() {
						public void newInstanceCreated() {
							System.out.println("New instance detected...");
							try {
								assert mw != null;
								mw.init(args);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (ExecutionException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					});
				}

				Authenticator.setDefault(new MyAuthenticator());

				try {
					mw = new MainWindow(args);

				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "main window initialization", e);
				} catch (ExecutionException e) {
					logger.log(Level.SEVERE, "main window initialization", e);
				}
				splash.dispose();
			}
		});
	}

}
