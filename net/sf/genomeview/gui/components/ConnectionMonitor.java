package net.sf.genomeview.gui.components;

import java.awt.Dimension;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import net.sf.genomeview.core.Icons;
import be.abeel.io.LineIterator;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ConnectionMonitor extends Observable {

	private static ImageIcon online = Icons.get("network_connect.png");
	private static ImageIcon offline = Icons.get("network_disconnect.png");

	private boolean webstartOnline = false;
	private boolean reposOnline = false;
	private Logger log = Logger.getLogger(ConnectionMonitor.class.getCanonicalName());

	public static final ConnectionMonitor instance = new ConnectionMonitor();

	public JLabel webLabel = new JLabel();
	public JLabel reposLabel = new JLabel();

	private ConnectionMonitor() {
		webLabel.setPreferredSize(new Dimension(online.getIconWidth(), online.getIconHeight()));
		reposLabel.setPreferredSize(new Dimension(online.getIconWidth(), online.getIconHeight()));
		this.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				if (webstartOnline) {
					webLabel.setIcon(online);
					webLabel.setToolTipText("GenomeView online");
				} else {
					webLabel.setIcon(offline);
					webLabel.setToolTipText("Cannot connect to genomeview.org");
				}
				if (reposOnline) {
					reposLabel.setIcon(online);
					reposLabel.setToolTipText("Data repository online");
				} else {
					reposLabel.setIcon(offline);
					reposLabel.setToolTipText("Cannot connect to data repository");
				}
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
						LineIterator it = new LineIterator("http://genomeview.org/online.php");
						log.info("Reply from web: " + it.next());
						it.close();
						webstartOnline = true;
					} catch (Exception e) {
						// Failed, try again later;
					}
					setChanged();
					notifyObservers();
					try {
						/* Check once every 5 minutes whether we can still get online */
						Thread.sleep(5*60 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
						LineIterator it = new LineIterator(
								"http://www.broadinstitute.org/software/genomeview/online.php");
						log.info("Reply from repository: " + it.next());
						it.close();
						reposOnline = true;
					} catch (Exception e) {
						// Failed, try again later;
					}
					setChanged();
					notifyObservers();
					try {
						/* Check once every 15 seconds whether we can still access the main data repository*/
						Thread.sleep(15 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

			}
		});
		t1.setDaemon(true);
		t2.setDaemon(true);
		t1.start();
		t2.start();
	}

}
