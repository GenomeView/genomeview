/**
 * %HEADER%
 */
package net.sf.genomeview.gui.explorer;

import java.awt.EventQueue;
import java.util.Observable;
import java.util.Observer;

import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeExplorerManager implements Observer {
	GenomeExplorer bg;
	private Model model;

	private boolean autoMode = true;

	public GenomeExplorerManager(Model model) {
		bg = new GenomeExplorer(model);
		this.model = model;
		model.getGUIManager().registerGenomeExplorer(this);
		model.addObserver(this);
	}

	public void setVisible(boolean vis) {
		autoMode = vis;
		bg.setVisible(vis);
		if (bg.isVisible())
			bg.requestFocus();
		
		
	}

	@Override
	public void update(Observable o, Object arg) {
		if (!autoMode)
			return;
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				if (model.entries().size() > 0) {
					if (bg.isVisible())
						bg.setVisible(false);
				} else {
					if (!bg.isVisible()) {
						bg.setVisible(true);

					}
					bg.requestFocus();
				}

			}
		});

	}
}
