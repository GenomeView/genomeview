/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.EventQueue;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class DataExplorerManager implements Observer {
	DataExplorer bg;
	private Model model;

	private boolean autoMode;

	public DataExplorerManager(Model model) {
		this.model = model;
		this.autoMode = model.getConfiguration()
				.getBoolean("general:enableGenomeExplorer");
		bg = new DataExplorer(model);

		model.getGUIManager().registerGenomeExplorer(this);
		model.addObserver(this);
		model.getWorkerManager().addObserver(this);
	}

	public void setVisible(final boolean vis) {
		if (vis) {
			autoMode = model.getConfiguration()
					.getBoolean("general:enableGenomeExplorer");
		} else {
			autoMode = false;
		}
		visi(vis);

	}

	/**
	 * If set to visible and network not available, throw a message to the user.
	 * 
	 * @param vis true iff data explorer becomes visible now.
	 */
	private void visi(final boolean vis) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (vis && model.getConnectionMonitor().offline()) {
					autoMode = false;
					JOptionPane.showMessageDialog(
							model.getGUIManager().getMainWindow(),
							MessageManager
									.getString("explorermanager.offline_warn"),
							MessageManager.getString("explorermanager.offline"),
							JOptionPane.WARNING_MESSAGE);
				} else {
					bg.setVisible(vis);
				}
			}
		});

//		if (vis && firstUse) {
//			firstUse = false;
//			EventQueue.invokeLater(new Runnable() {
//
//				@Override
//				public void run() {
//					bg.scollToTop();
//				}
//			});
//
//		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!autoMode) {
			return;
		}

		if (model.getWorkerManager().runningJobs() > 0
				|| model.entries().size() > 0) {
			if (bg.isVisible()) {
				visi(false);
			}
		} else {
			if (!bg.isVisible()) {
				visi(true);

			}
		}

	}
}
