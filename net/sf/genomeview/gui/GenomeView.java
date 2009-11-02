/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.jnlp.ServiceManager;
import javax.jnlp.SingleInstanceListener;
import javax.jnlp.SingleInstanceService;
import javax.jnlp.UnavailableServiceException;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.genomeview.core.Configuration;
import be.abeel.gui.TitledComponent;

public class GenomeView implements SingleInstanceListener  {
	private static Logger logger;

//	public static GenomeView single;

	static private class MyAuthenticator extends Authenticator {

		private String user = "";
		private char[] pass = new char[0];

		protected PasswordAuthentication getPasswordAuthentication() {
			final JDialog jd = new JDialog();
			jd.setTitle("Enter password");
			jd.setModal(true);
			jd.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.gridy = 0;
			gc.fill = GridBagConstraints.BOTH;

			JLabel jl = new JLabel("Please enter login details for: " + getRequestingPrompt() + " at " + getRequestingHost());
			jd.add(jl, gc);
			gc.gridy++;
			JTextField username = new JTextField(user);
			jd.add(new TitledComponent("User name", username), gc);
			gc.gridy++;
			JPasswordField password = new JPasswordField(new String(pass));

			jd.add(new TitledComponent("Password", password), gc);
			gc.gridy++;
			JButton jb = new JButton("OK");
			jd.add(jb, gc);
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jd.dispose();
				}
			});
			jd.pack();
			StaticUtils.center(jd);
			jd.setVisible(true);
			logger.info("Requesting Host  : " + getRequestingHost());
			logger.info("Requesting Port  : " + getRequestingPort());
			logger.info("Requesting Prompt : " + getRequestingPrompt());
			logger.info("Requesting Protocol: " + getRequestingProtocol());
			logger.info("Requesting Scheme : " + getRequestingScheme());
			logger.info("Requesting Site  : " + getRequestingSite());
			this.user = username.getText();
			this.pass = password.getPassword();
			return new PasswordAuthentication(this.user, this.pass);
		}
	}

	private MainWindow mw;
	public GenomeView(String[] args) {
		jnlprewrite(args);
		Authenticator.setDefault(new MyAuthenticator());
		/*
		 * The configuration class needs to be called at least once before we
		 * can start the logger
		 */
		System.out.println("Starting GenomeView " + Configuration.version());
		/* Configure logging */
		try {
			LogManager.getLogManager().readConfiguration(GenomeView.class.getResourceAsStream("/conf/logging.conf"));
			logger = Logger.getLogger(GenomeView.class.getCanonicalName());
		} catch (SecurityException e) {
			logger.log(Level.SEVERE,"log initialization", e);
			/* These exceptions likely indicate that the log could not be initialized, print also to console */
			e.printStackTrace();
			
		} catch (IOException e) {
			logger.log(Level.SEVERE,"log initialization", e);
			/* These exceptions likely indicate that the log could not be initialized, print also to console */
			e.printStackTrace();
		}

		try {
            SingleInstanceService singleInstanceService =
                (SingleInstanceService)ServiceManager.
                    lookup("javax.jnlp.SingleInstanceService");
            // add the listener to this application!
            singleInstanceService.addSingleInstanceListener(
                (SingleInstanceListener)this );
            logger.info("Registered to the SingleInstanceService");
        } catch(UnavailableServiceException use) {
            logger.warning("SingleInstanceService is not available");
        }
		
        try {
			mw = new MainWindow(args);
			
		} catch (InterruptedException e) {
			logger.log(Level.SEVERE,"main window initialization", e);
		} catch (ExecutionException e) {
			logger.log(Level.SEVERE,"main window initialization", e);
		}
		splash.dispose();
	}

/*
 * Rewrite args if started from file association	
 */
	private void jnlprewrite(String[] args) {
		if(args.length>0&&args[0].equals("-open"))
			args[0]="--file";
		
	}


	public static void main(String[] args) {
		new GenomeView(args);
	}

//	private Set<MainWindow> running = new HashSet<MainWindow>();

	/*
	 * This will create AND SHOW the Splash screen.
	 */
	private Splash splash = new Splash();

//	public void kill(MainWindow ID) {
//		logger.info("Removing " + ID + " from instance manager.");
//		ID.dispose();
//		running.remove(ID);
//		if (running.size() == 0) {
//			logger.info("Closed all models, exiting");
//			try {
//				Configuration.save();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			System.exit(0);
//		}
//
//	}

//	public boolean isInstancesRunning() {
//		return running != null && running.size() > 0;
//	}

	@Override
	public void newActivation(String[] args) {
		
		try {
			if (!Configuration.getBoolean("general:singleInstance")) {
				logger.info("Creating new instance");
				GenomeView.main(args);
			} else {
				logger.info("Reusing old instance");
				assert mw!=null;
				mw.init(args);
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Couldn't create new instance.");
			logger.warning("MainWindow message: " + e.getMessage());
			// check for other instances or close
			
		}
		
	}

}

