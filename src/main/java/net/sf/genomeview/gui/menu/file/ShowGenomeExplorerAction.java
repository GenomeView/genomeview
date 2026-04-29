/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class ShowGenomeExplorerAction extends AbstractAction {

	private static final long serialVersionUID = 7365143224046185361L;
	private Model model;

	public ShowGenomeExplorerAction(Model model) {
		super(MessageManager.getString("filemenu.show_genome_explorer"));
		this.model = model;
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control W"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		model.getGUIManager().getGenomeExplorer().setVisible(true);
	}
}
