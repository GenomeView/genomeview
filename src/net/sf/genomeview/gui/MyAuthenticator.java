/**
 * This file is part of GenomeView, a genome browser and annotation curator
 * 
 * Copyright (C) 2007-2013 Thomas Abeel and contributors
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see http://www.gnu.org/licenses/.
 *
 * Project: http://genomeview.org/
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

		Logger logger = LoggerFactory.getLogger(MyAuthenticator.class.getCanonicalName());
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

		jd.setTitle(MessageManager.getString("authenticator.enter_password"));
		jd.setModal(true);
		jd.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.fill = GridBagConstraints.BOTH;

		JLabel jl = new JLabel(MessageManager.getString("authenticator.enter_details")+ " "+ getRequestingPrompt() + " at "
				+ getRequestingHost());
		jd.add(jl, gc);
		gc.gridy++;
		JTextField username = new JTextField();
		username.addActionListener(dps);
		jd.add(new TitledComponent(MessageManager.getString("authenticator.user_name"), username), gc);
		gc.gridy++;
		JPasswordField password = new JPasswordField();
		password.addActionListener(dps);
		jd.add(new TitledComponent(MessageManager.getString("authenticator.password"), password), gc);
		gc.gridy++;
		JButton jb = new JButton(MessageManager.getString("button.ok"));
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
