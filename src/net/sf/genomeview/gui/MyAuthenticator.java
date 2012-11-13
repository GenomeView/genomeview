/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import be.abeel.gui.TitledComponent;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class MyAuthenticator extends Authenticator {

	private static HashMap<String, PasswordAuthentication> mapping = new HashMap<String, PasswordAuthentication>();

	private static void addURL(URL url) {
		System.out.println(url.getUserInfo());
		if (url.getUserInfo() != null) {
			String[] arr = url.getUserInfo().split(":");
			mapping.put(url.toString(), new PasswordAuthentication(arr[0], arr[1].toCharArray()));
			System.out.println(mapping);
		}
	}

	protected PasswordAuthentication getPasswordAuthentication() {

		Logger logger = Logger.getLogger(MyAuthenticator.class.getCanonicalName());
		logger.info("Requesting Host  : " + getRequestingHost());
		logger.info("Requesting Port  : " + getRequestingPort());
		logger.info("Requesting Prompt : " + getRequestingPrompt());
		logger.info("Requesting Protocol: " + getRequestingProtocol());
		logger.info("Requesting Scheme : " + getRequestingScheme());
		logger.info("Requesting Site  : " + getRequestingSite());

		URL ru = getRequestingURL();
		addURL(ru);

		String reqURL = getRequestingURL().toString();
		String shortReq = reqURL.substring(0, reqURL.lastIndexOf('.'));
		logger.info("Requesting URL : " + reqURL);
		if (mapping.containsKey(reqURL))
			return mapping.get(reqURL);
		if (mapping.containsKey(shortReq))
			return mapping.get(shortReq);

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
		JTextField username = new JTextField();
		username.addActionListener(dps);
		jd.add(new TitledComponent("User name", username), gc);
		gc.gridy++;
		JPasswordField password = new JPasswordField();
		password.addActionListener(dps);
		jd.add(new TitledComponent("Password", password), gc);
		gc.gridy++;
		JButton jb = new JButton("OK");
		jb.addActionListener(dps);
		jd.add(jb, gc);

		jd.pack();
		StaticUtils.center(null,jd);
		jd.setVisible(true);
		if (username.getText().length() > 0 || password.getPassword().length > 0)
			return new PasswordAuthentication(username.getText(), password.getPassword());
		else
			return null;
	}
}
