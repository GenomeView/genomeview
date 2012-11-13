/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.lang.reflect.InvocationTargetException;
import java.net.Authenticator;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import be.abeel.concurrency.DaemonThread;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class GenomeView {
	private static Logger logger=Logger.getLogger(GenomeView.class.getCanonicalName());

	private static WindowManager mw;

	/*
	 * Rewrite args if started from file association
	 */
	private static void jnlprewrite(String[] args) {
		if (args.length > 0 && args[0].equals("-open"))
			args[0] = "--file";

	}
	private static Splash splash=null;

	
	public static Model getModel(){
		if(mw==null)
			return null;
		return mw.getModel();
	}
	public static void main(final String[] args) {
		
		try {
			SwingUtilities.invokeAndWait(new Runnable() {

				@Override
				public void run() {
					
					splash = new Splash();
				}
			});
		} catch (InterruptedException e1) {
			//Ignore, it's not like we really care
		} catch (InvocationTargetException e1) {
			//Ignore, it's not like we really care
		}
		
		/* Rewrite JNLP arguments */
		jnlprewrite(args);
		/*
		 * The configuration class needs to be called at least once
		 * before we can start the logger
		 */
		System.out.println("Starting GenomeView " + Configuration.version());
		
		new DaemonThread(new Runnable() {

			@Override
			public void run() {
				

				LogConfigurator.config();
				logger.info("GenomeView version "+Configuration.version());
				logger.info("Current date and time: "+new Date());
				logger.info("Command line instructions: "+Arrays.toString(args));
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
					mw = new WindowManager(args,splash);
					ApplicationInstanceManager.setCallback(mw);
				} catch (InterruptedException e) {
					logger.log(Level.SEVERE, "main window initialization", e);
				} catch (ExecutionException e) {
					logger.log(Level.SEVERE, "main window initialization", e);
				}
				splash.dispose();
			}
		}).start();
	}

}
