package net.sf.genomeview.gui.components;

import java.awt.Dimension;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import be.abeel.concurrency.DaemonThread;
import be.abeel.io.LineIterator;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
import net.sf.genomeview.gui.MessageManager;
import tudelft.utilities.logging.Reporter;

/**
 * Monitors whether the network connection can connect to the main data
 * repositories. Note, this runs 3 threads that don't terminate ever
 * 
 * @author Thomas Abeel
 * 
 */
public class ConnectionMonitor extends Observable {

	private static ImageIcon online = Icons.get("network_connect.png");
	private static ImageIcon offline = Icons.get("network_disconnect.png");

	private boolean webstartOnline = false;
	private boolean reposOnline = false;
	private boolean networkInterface = false;

	public JLabel networkLabel = new JLabel();
	public JLabel webLabel = new JLabel();
	public JLabel reposLabel = new JLabel();
	private final Reporter log;

	/**
	 * One instance of this is created in the Model at startup. No need to
	 * create more instances.
	 * 
	 * @param log the {@link Reporter} to log issues to
	 */
	public ConnectionMonitor(Reporter log) {
		this.log = log;
		// FIXME this monitor should not manipulate GUI labels?
		networkLabel.setPreferredSize(
				new Dimension(online.getIconWidth(), online.getIconHeight()));
		webLabel.setPreferredSize(
				new Dimension(online.getIconWidth(), online.getIconHeight()));
		reposLabel.setPreferredSize(
				new Dimension(online.getIconWidth(), online.getIconHeight()));
		this.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				if (webstartOnline) {
					webLabel.setIcon(online);
					webLabel.setToolTipText(MessageManager
							.getString("connectionmonitor.genomeview_online"));
				} else {
					webLabel.setIcon(offline);
					webLabel.setToolTipText(MessageManager.getString(
							"connectionmonitor.cannot_connect_genomeview"));
				}
				if (reposOnline) {
					reposLabel.setIcon(online);
					reposLabel.setToolTipText(MessageManager.getString(
							"connectionmonitor.data_repository_online"));
				} else {
					reposLabel.setIcon(offline);
					reposLabel.setToolTipText(MessageManager.getString(
							"connectionmonitor.cannot_connect_data_repository"));
				}

				StringBuffer text = new StringBuffer();
				try {
					Enumeration<NetworkInterface> nets = NetworkInterface
							.getNetworkInterfaces();
					for (NetworkInterface netint : Collections.list(nets))
						if (!netint.isLoopback()) {
							text.append((netint.isUp() ? "online" : "offline")
									+ " - " + netint.getName() + " - "
									+ netint.getInterfaceAddresses().toString()
									+ "<br/>");

						}
					networkLabel.setToolTipText(
							"<html>" + text.toString() + "</html>");
				} catch (Exception e) {
					log.log(Level.WARNING, "update of network label failed", e);
				}

				if (networkInterface) {
					networkLabel.setIcon(online);
				} else {
					networkLabel.setIcon(offline);

				}

				networkLabel.repaint();
				webLabel.repaint();
				reposLabel.repaint();

			}
		});

		Thread t1 = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					webstartOnline = false;

					try {
						if (Configuration.instance()
								.getBoolean("general:monitorConnection")) {
							LineIterator it = new LineIterator(
									"http://genomeview.org/online.php");
							// log.info("Reply from web: " + it.next());
							it.close();

						}
						webstartOnline = true;
					} catch (Exception e) {
						// Failed, try again later;
						// don't even log, this is very irrelevant
					}
					setChanged();
					notifyObservers();
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						// don't even log, this is very irrelevant
					}

				}

			}
		});
		Thread t2 = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					reposOnline = false;
					try {
						if (Configuration.instance()
								.getBoolean("general:monitorConnection")) {
							LineIterator it = new LineIterator(
									"http://www.broadinstitute.org/software/genomeview/online.php");
							// log.info("Reply from repository: " + it.next());
							it.close();
						}
						reposOnline = true;
					} catch (Exception e) {
						// Failed, try again later;
						// don't even log, this is very irrelevant
					}
					setChanged();
					notifyObservers();
					try {
						Thread.sleep(10 * 1000);
					} catch (InterruptedException e) {
						// don't even log, this is very irrelevant
					}
				}

			}
		});
		Thread t3 = new DaemonThread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					boolean previous = networkInterface;

					networkInterface = false;

					try {
						Enumeration<NetworkInterface> nets = NetworkInterface
								.getNetworkInterfaces();
						for (NetworkInterface netint : Collections.list(nets))
							if (!netint.isLoopback()) {
								if (netint.isUp())
									networkInterface = true;

							}

					} catch (Exception e) {
						e.printStackTrace();
						networkInterface = false;
					}
					if (previous != networkInterface) {
						setChanged();
						notifyObservers();
					}
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// don't even log, this is very irrelevant
					}

				}

			}
		});

		t1.setDaemon(true);
		t2.setDaemon(true);
		t1.start();
		t2.start();
		t3.start();
	}

	public boolean offline() {
		return !reposOnline;
	}

}
