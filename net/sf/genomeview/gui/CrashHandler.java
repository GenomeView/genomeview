/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Desktop;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

/**
 * Use this class to terminate GenomeView when an unexpected error has occured
 * from which it is impossible to recover.
 * 
 * @author Thomas Abeel
 * 
 */
public class CrashHandler {

	/* For testing purposes only */
	public static void main(String[] args) {
		new CrashHandler();
	}

	private CrashHandler() {

		final JFrame window = new JFrame("GenomeView ERROR!");
		JTextArea ll = new JTextArea(10, 30);
		ll
				.setText("GenomeView has encountered an error from which it cannot recover and will be terminated.\n\nPlease report this problem.\n\nWhen reporting this issue, make sure to include the a description of what you were doing. Please also include the most recent log file. You can find error logs in the .genomeview folder in your home directory.");
		ll.setWrapStyleWord(true);
		ll.setLineWrap(true);
		ll.setEditable(false);
		window.setLayout(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.weighty=1;
		gc.fill=GridBagConstraints.BOTH;
		
		// gc.grid
		window.add(ll, gc);
		JButton open = new JButton("Open folder with logs");
		open.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					/* We should not use the configuration class as it may be the source of the crash */
					String s = System.getProperty("user.home");
					File confDir = new File(s + "/.genomeview");
					Desktop.getDesktop().open(confDir);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(window, "Could not open log folder");
				}

			}
		});
		window.add(open, gc);
		
		JButton report = new JButton("Report this problem");
		report.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("https://sourceforge.net/tracker/?func=browse&group_id=208107&atid=1004368"));
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(window, "Could not open bugtracker website");
				}

			}
		});
		window.add(report, gc);
		
		JButton close = new JButton("Close this window!");
		close.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(-1);

			}
		});
		window.add(close, gc);

		window.pack();
		StaticUtils.center(window);
		window.setVisible(true);
		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}
	private static Logger log=Logger.getLogger(CrashHandler.class.getCanonicalName());

	public static void crash(Level severe, String string, Throwable ex) {
		log.log(severe, string, ex);
		log.severe("GenomeView is dead, initializing post-mortem.");
		new CrashHandler();
		
	}
}
