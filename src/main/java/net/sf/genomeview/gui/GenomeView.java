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
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;

import javax.swing.SwingUtilities;

import be.abeel.concurrency.DaemonThread;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DistributingReporter;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.nameservice.NameService;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeView {

	private static WindowManager mw;

	/*
	 * Rewrite args if started from file association
	 */
	private static void jnlprewrite(String[] args) {
		if (args.length > 0 && args[0].equals("-open"))
			args[0] = "--file";

	}

	private static Splash splash = null;

	public static void main(final String[] args) throws IOException {
		// FIXME maybe logger can be moved into Model?
		// why are we not initializing model first?
		final DistributingReporter log = new DistributingReporter();

		try {
			NameService.init(log);
		} catch (ReadFailedException e) {
			// FIXME should this be fatal?
			log.log(Level.WARNING, "Failed to initialize NameService.", e);
		}

		// FIXME do something about the original config?
		// LogConfigurator.config();
		log.log(Level.INFO,
				"Starting GenomeView " + Configuration.instance().version());
		log.log(Level.INFO, "Using language: " + MessageManager.getLocale());
		try {
			SwingUtilities.invokeAndWait(() -> splash = new Splash());
		} catch (InterruptedException | InvocationTargetException e1) {
			log.log(Level.WARNING, "Splash screen failed", e1);
		}

		/* Rewrite JNLP arguments */
		jnlprewrite(args);

		new DaemonThread(new Runnable() {

			@Override
			public void run() {

				/*
				 * The configuration class needs to be called at least once
				 * before we can start the logger
				 */

				log.log(Level.INFO, "Configuration summary:");
				log.log(Level.INFO, "GenomeView version: "
						+ Configuration.instance().version());
				log.log(Level.INFO, "Current date and time: " + new Date());
				log.log(Level.INFO,
						"Command line instructions: " + Arrays.toString(args));
				log.log(Level.INFO, "Number of processors: " + Integer
						.toString(Runtime.getRuntime().availableProcessors()));
				log.log(Level.INFO, "Free memory :"
						+ Long.toString(Runtime.getRuntime().freeMemory()));
				log.log(Level.INFO, "Max memory: "
						+ Long.toString(Runtime.getRuntime().maxMemory()));
				log.log(Level.INFO, "Total JVM: "
						+ Long.toString(Runtime.getRuntime().totalMemory()));
				log.log(Level.INFO,
						"OS: " + ManagementFactory.getOperatingSystemMXBean()
								.getName() + " "
								+ ManagementFactory.getOperatingSystemMXBean()
										.getVersion());
				log.log(Level.INFO, "Architecture: " + ManagementFactory
						.getOperatingSystemMXBean().getArch());
				log.log(Level.INFO,
						"JVM version: " + System.getProperty("java.version")

				);

				CommandLineOptions.init(args, log);
				/* Single instance manager */
				boolean singleInstance = Configuration.instance()
						.getBoolean("general:singleInstance");
				if (singleInstance) {
					if (!ApplicationInstanceManager.registerInstance(args,
							log)) {
						// instance already running.
						log.log(Level.WARNING,
								"Another instance of this application is already running.  Exiting.");
						splash.dispose();
						StaticUtils.forceExit();
						return;
					}

				}

				Authenticator.setDefault(new MyAuthenticator());

				try {
					mw = new WindowManager(args, splash, log);
					ApplicationInstanceManager.setCallback(mw);
				} catch (InterruptedException | ExecutionException e) {
					log.log(Level.WARNING, "main window initialization", e);
					// FIXME seems this should be fatal??
				}
				splash.dispose();
			}
		}).start();
	}
}
