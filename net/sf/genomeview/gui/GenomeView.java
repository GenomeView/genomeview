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
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import be.abeel.gui.TitledComponent;

public class GenomeView {
	private static Logger logger;
	
	static private class MyAuthenticator extends Authenticator {
		
			private String user="";
			private char[]pass=new char[0];
	
			protected PasswordAuthentication getPasswordAuthentication() {
			final JDialog jd = new JDialog();
			jd.setTitle("Enter password");
			jd.setModal(true);
			jd.setLayout(new GridBagLayout());
			GridBagConstraints gc=new GridBagConstraints();
			gc.gridx=0;
			gc.gridy=0;
			gc.fill=GridBagConstraints.BOTH;
			
			JLabel jl = new JLabel("Please enter login details for: "+getRequestingPrompt() +" at "+getRequestingHost());
			jd.add(jl,gc);
			gc.gridy++;
			JTextField username = new JTextField(user);
			jd.add(new TitledComponent("User name",username),gc);
			gc.gridy++;
			JPasswordField password = new JPasswordField(new String(pass));

			jd.add(new TitledComponent("Password",password),gc);
			gc.gridy++;
			JButton jb = new JButton("OK");
			jd.add(jb,gc);
			jb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					jd.dispose();
				}
			});
			jd.pack();
			StaticUtils.center(jd);
			jd.setVisible(true);
			System.out.println("Requesting Host  : " + getRequestingHost());
			System.out.println("Requesting Port  : " + getRequestingPort());
			System.out.println("Requesting Prompt : " + getRequestingPrompt());
			System.out.println("Requesting Protocol: " + getRequestingProtocol());
			System.out.println("Requesting Scheme : " + getRequestingScheme());
			System.out.println("Requesting Site  : " + getRequestingSite());
			this.user=username.getText();
			this.pass=password.getPassword();
			return new PasswordAuthentication(this.user,this.pass);
		}
	}

	
	public static void main(String[] args) {
		Authenticator.setDefault(new MyAuthenticator());
		/*
		 * The configuration class needs to be called at least once before we
		 * can start the logger
		 */
		System.out.println("Starting GenomeView " + Configuration.version());
		/* Configure loggin */
		try {
			LogManager.getLogManager().readConfiguration(
					GenomeView.class.getResourceAsStream("/conf/logging.conf"));
			logger = Logger.getLogger(GenomeView.class.getCanonicalName());
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/* Start program */
		createNewInstance(args);

	}

	private static Set<MainWindow> running = new HashSet<MainWindow>();

	/*
	 * This will create AND SHOW the Splash screen.
	 */
	private static Splash splash = new Splash();

	/**
	 * Create a new instance with no command line parameters.
	 */
	public static void createNewInstance() {
		createNewInstance(new String[0]);
	}

	public static void createNewInstance(String args[]) {

		logger.info("Creating new instance");
		try {
			MainWindow mw = new MainWindow(args);
			mw.addInstanceObserver(new Monitor(mw));
			if (!running.add(mw)) {
				JOptionPane
						.showMessageDialog(
								null,
								"Duplicate program instances detected, save your work and quit all instances. If this problem persists, contact us.",
								"Duplicate instances!!!",
								JOptionPane.ERROR_MESSAGE);
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.warning("Couldn't create new instance.");
			logger.warning("MainWindow message: " + e.getMessage());
			// check for other instances or close
			if (running.size() == 0) {
				logger.info("Closed all models, exiting");
				System.exit(0);
			}
		}
		splash.setVisible(false);
	}

	public static void kill(MainWindow ID) {
		logger.info("Removing " + ID + " from instance manager.");
		running.remove(ID);
		if (running.size() == 0) {
			logger.info("Closed all models, exiting");
			try {
				Configuration.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			System.exit(0);
		}

	}

	public static boolean isInstancesRunning() {
		return running != null && running.size() > 0;
	}

}

class Monitor implements Observer {
	private MainWindow id;

	Monitor(MainWindow id) {
		this.id = id;
	}

	@Override
	public void update(Observable o, Object arg) {
		Model model = (Model) o;
		if (model.isExitRequested()) {
			GenomeView.kill(id);
		}
	}
}