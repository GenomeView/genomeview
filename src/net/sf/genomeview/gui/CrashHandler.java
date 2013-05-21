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

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import be.abeel.net.URIFactory;

/**
 * Use this class to terminate GenomeView when an unexpected error has occured
 * from which it is impossible to recover.
 * 
 * @author Thomas Abeel
 * 
 */
public class CrashHandler {

	private static Logger log = Logger.getLogger(CrashHandler.class.getCanonicalName());

	public static void showErrorMessage(String message, Throwable cause) {
		log.log(Level.SEVERE, message, cause);
		JOptionPane.showMessageDialog(null, message + "\n\n" + MessageManager.getString("crashhandler.error_logged"),
				MessageManager.getString("crashhandler.error "), JOptionPane.ERROR_MESSAGE);

	}

	private CrashHandler() {
		try {
			final JFrame window = new JFrame(MessageManager.getString("crashhandler.error"));
			window.setAlwaysOnTop(true);
			window.setResizable(false);
			JTextArea ll = new JTextArea(10, 30);
			ll.setText(MessageManager.getString("crashhandler.unrecoverable_error"));
			ll.setWrapStyleWord(true);
			ll.setLineWrap(true);
			ll.setEditable(false);
			window.setLayout(new GridBagLayout());
			GridBagConstraints gc = new GridBagConstraints();
			gc.gridx = 0;
			gc.weighty = 1;
			gc.fill = GridBagConstraints.BOTH;

			// gc.grid
			window.add(ll, gc);
			JButton open = new JButton(MessageManager.getString("crashhandler.open_logs"));
			open.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						/*
						 * We should not use the configuration class as it may
						 * be the source of the crash
						 */
						String s = System.getProperty("user.home");
						File confDir = new File(s + "/.genomeview");
						Desktop.getDesktop().open(confDir);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(window, MessageManager.getString("crashhandler.couldnt_open_log_folder"));
					}

				}
			});
			window.add(open, gc);

			JButton report = new JButton(MessageManager.getString("crashhandler.report"));
			report.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					try {
						Desktop.getDesktop().browse(URIFactory.uri("https://sourceforge.net/tracker/?func=add&group_id=208107&atid=1004368"));
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(window, MessageManager.getString("crashhandler.couldnt_bugtrack"));
					}

				}
			});
			window.add(report, gc);

			JButton close = new JButton(MessageManager.getString("crashhandler.close_window"));
			close.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					System.exit(-1);

				}
			});
			window.add(close, gc);

			window.pack();
			StaticUtils.center(null, window);
			window.setVisible(true);
			window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		} catch (Throwable e) {

			log.log(Level.SEVERE, "GenomeView could not execute post-mortem", e);
			System.exit(-2);

		}
	}

	public static void crash(Level logLevel, String logMessage, Throwable ex) {
		log.log(logLevel, logMessage, ex);
		log.severe("GenomeView is dead, initializing post-mortem.");

		new CrashHandler();

	}
}
