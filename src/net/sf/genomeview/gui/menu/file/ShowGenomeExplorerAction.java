/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

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
		super("Show Genome Explorer");
			this.model = model;
		}

		private static Logger log = Logger.getLogger(LoadSessionAction.class.getCanonicalName());

		@Override
		public void actionPerformed(ActionEvent e) {
			model.getGUIManager().getGenomeExplorer().setVisible(true);
		}
}
