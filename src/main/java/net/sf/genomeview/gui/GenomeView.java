/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.lang.management.ManagementFactory;
import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.abeel.concurrency.DaemonThread;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeView {
	private static Logger logger = LoggerFactory.getLogger(GenomeView.class.getCanonicalName());

	private static WindowManager mw;

	/*
	 * Rewrite args if started from file association
	 */
	private static void jnlprewrite(String[] args) {
		if (args.length > 0 && args[0].equals("-open"))
			args[0] = "--file";

	}

	private static Splash splash = null;

	public static Model getModel() {
		if (mw == null)
			return null;
		return mw.getModel();
	}

	public static void main(final String[] args) {
		LogConfigurator.config();
		logger.info("Starting GenomeView " + Configuration.version());
		logger.info("Using language: "+MessageManager.getLocale());
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {

					splash = new Splash();
				}
			});
		} catch (InterruptedException e1) {
			// Ignore, it's not like we really care
		} catch (InvocationTargetException e1) {
			// Ignore, it's not like we really care
		}

		/* Rewrite JNLP arguments */
		jnlprewrite(args);
		

		new DaemonThread(new Runnable() {

			@Override
			public void run() {
				
				/*
				 * The configuration class needs to be called at least once before we
				 * can start the logger
				 */
				
				
				
				logger.info("Configuration summary: \n\tGenomeView version: " + Configuration.version() + "\n\t" + "Current date and time: " + new Date() + "\n\t"
						+ "Command line instructions: " + Arrays.toString(args) + "\n\t" + "Number of processors: "
						+ Integer.toString(Runtime.getRuntime().availableProcessors()) + "\n\t" + "Free memory :"
						+ Long.toString(Runtime.getRuntime().freeMemory()) + "\n\t" + "Max memory: " + Long.toString(Runtime.getRuntime().maxMemory()) + "\n\t"
						+ "Total JVM: " + Long.toString(Runtime.getRuntime().totalMemory()) + "\n\t" + "OS: "
						+ ManagementFactory.getOperatingSystemMXBean().getName() + " " + ManagementFactory.getOperatingSystemMXBean().getVersion() + "\n\t"
						+ "Architecture: " + ManagementFactory.getOperatingSystemMXBean().getArch() + "\n\t" + "JVM version: "
						+ System.getProperty("java.version")

				);

				

				CommandLineOptions.init(args);
				/* Single instance manager */
				boolean singleInstance = Configuration.getBoolean("general:singleInstance");
				if (singleInstance) {
					if (!ApplicationInstanceManager.registerInstance(args)) {
						// instance already running.
						System.out.println("Another instance of this application is already running.  Exiting.");
						splash.dispose();
						StaticUtils.forceExit();
						return;
					}

				}

				Authenticator.setDefault(new MyAuthenticator());

				try {
					mw = new WindowManager(args, splash);
					ApplicationInstanceManager.setCallback(mw);
				} catch (InterruptedException e) {
					logger.error( "main window initialization", e);
				} catch (ExecutionException e) {
					logger.error( "main window initialization", e);
				}
				splash.dispose();
			}
		}).start();
	}
}
