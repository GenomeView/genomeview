package net.sf.genomeview.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import be.abeel.gui.TitledComponent;

public class MyAuthenticator extends Authenticator {
	private String user = "";
	private char[] pass = new char[0];

	protected PasswordAuthentication getPasswordAuthentication() {
		final JDialog jd = new JDialog();
		ActionListener dps = new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				jd.dispose();

			}
		};

		jd.setTitle("Enter password");
		jd.setModal(true);
		jd.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;

		JLabel jl = new JLabel("Please enter login details for: " + getRequestingPrompt() + " at "
				+ getRequestingHost());
		jd.add(jl, gc);
		gc.gridy++;
		JTextField username = new JTextField(user);
		username.addActionListener(dps);
		jd.add(new TitledComponent("User name", username), gc);
		gc.gridy++;
		JPasswordField password = new JPasswordField(new String(pass));
		password.addActionListener(dps);
		jd.add(new TitledComponent("Password", password), gc);
		gc.gridy++;
		JButton jb = new JButton("OK");
		jb.addActionListener(dps);
		jd.add(jb, gc);

		jd.pack();
		StaticUtils.center(jd);
		jd.setVisible(true);
		
		Logger logger = Logger.getLogger(MyAuthenticator.class.getCanonicalName());
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
