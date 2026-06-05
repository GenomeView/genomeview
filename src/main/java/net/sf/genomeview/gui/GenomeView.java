/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.Arrays;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import be.abeel.concurrency.DaemonThread;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Globals;
import tudelft.utilities.logging.Reporter;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeView {

	private static WindowManager mw;
	// temp splash screen during startup.
	private static Splash splash = null;

	/*
	 * Rewrite args if started from file association
	 */
	private static void jnlprewrite(String[] args) {
		if (args.length > 0 && args[0].equals("-open")) {
			args[0] = "--file";
		}

	}

	/**
	 * starts up genomeview
	 * 
	 * @param args
	 * @throws IOException or ReadFailedException if the initialization fails.
	 *                     This is an fatal exit, not a log-and-continue.
	 */
	public static void main(final String[] args) throws IOException {
		Globals globals = new Globals();
		Reporter log = globals.getLog();
		Configuration configuration = globals.getConfiguration();

		log.log(Level.INFO, "Starting GenomeView " + configuration.version());
		log.log(Level.INFO,
				"Using language: " + globals.getMessageManager().getLocale());
		try {
			SwingUtilities.invokeAndWait(() -> splash = new Splash(globals));
		} catch (InterruptedException | InvocationTargetException e1) {
			log.log(Level.WARNING, "Splash screen failed", e1);
		}

		/* Rewrite JNLP arguments */
		jnlprewrite(args);

		new DaemonThread(new Runnable() {

			@Override
			public void run() {
				try {
					/*
					 * The configuration class needs to be called at least once
					 * before we can start the logger
					 */

					log.log(Level.INFO, "Configuration summary:");
					log.log(Level.INFO,
							"GenomeView version: " + configuration.version());
					log.log(Level.INFO, "Current date and time: " + new Date());
					log.log(Level.INFO, "Command line instructions: "
							+ Arrays.toString(args));
					log.log(Level.INFO,
							"Number of processors: " + Integer.toString(Runtime
									.getRuntime().availableProcessors()));
					log.log(Level.INFO, "Free memory :"
							+ Long.toString(Runtime.getRuntime().freeMemory()));
					log.log(Level.INFO, "Max memory: "
							+ Long.toString(Runtime.getRuntime().maxMemory()));
					log.log(Level.INFO, "Total JVM: " + Long
							.toString(Runtime.getRuntime().totalMemory()));
					log.log(Level.INFO, "OS: "
							+ ManagementFactory.getOperatingSystemMXBean()
									.getName()
							+ " " + ManagementFactory.getOperatingSystemMXBean()
									.getVersion());
					log.log(Level.INFO, "Architecture: " + ManagementFactory
							.getOperatingSystemMXBean().getArch());
					log.log(Level.INFO,
							"JVM version: " + System.getProperty("java.version")

					);

					/* Single instance manager */
					boolean singleInstance = configuration
							.getBoolean("general:singleInstance");
					if (singleInstance) {
						if (!ApplicationInstanceManager.registerInstance(args,
								globals)) {
							// instance already running.
							log.log(Level.WARNING,
									"Another instance of this application is already running.  Exiting.");
							splash.dispose();
							StaticUtils.forceExit();
							return;
						}

					}

					Authenticator.setDefault(new MyAuthenticator(globals));

					mw = new WindowManager(args, splash, globals,
							configuration);
					ApplicationInstanceManager.setCallback(mw);
				} catch (Exception e) {
					log.log(Level.SEVERE, "main initialization failed", e);
					System.exit(-1);
				}
				splash.dispose();
			}
		}).start();
	}
}
