/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.EventQueue;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.components.ConnectionMonitor;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeExplorerManager implements Observer {
	GenomeExplorer bg;
	private Model model;

	private boolean autoMode = Configuration.getBoolean("general:enableGenomeExplorer");

	public GenomeExplorerManager(Model model) {
		bg = new GenomeExplorer(model);
		this.model = model;
		model.getGUIManager().registerGenomeExplorer(this);
		model.addObserver(this);
		model.getWorkerManager().addObserver(this);
	}

	public void setVisible(final boolean vis) {
		if(vis)
			autoMode = Configuration.getBoolean("general:enableGenomeExplorer");
		else
			autoMode = false;
		visi(vis);

	}

	private boolean firstUse = true;

	private void visi(final boolean vis) {
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				if(vis&&ConnectionMonitor.instance.offline()){
					autoMode=false;
					JOptionPane.showMessageDialog(model.getGUIManager().getParent(), MessageManager.getString("explorermanager.offline_warn"),MessageManager.getString("explorermanager.offline"),JOptionPane.WARNING_MESSAGE);
				}else{
					bg.setVisible(vis);
				}
			}
		});

		if (vis && firstUse) {
			firstUse = false;
			EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					bg.scollToTop();
				}
			});

		}
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!autoMode)
			return;

		if (model.getWorkerManager().runningJobs()>0||model.entries().size() > 0) {
			if (bg.isVisible())
				visi(false);
		} else {
			if (!bg.isVisible()) {
				visi(true);

			}
		}

	}
}
