/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
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
		}

		private static Logger log = LoggerFactory.getLogger(LoadSessionAction.class.getCanonicalName());

		@Override
		public void actionPerformed(ActionEvent e) {
			model.getGUIManager().getGenomeExplorer().setVisible(true);
		}
}
