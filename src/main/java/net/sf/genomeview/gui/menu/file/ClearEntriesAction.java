/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class ClearEntriesAction extends AbstractAction {

	private static final long serialVersionUID = 1349866639638385199L;
	private Model model;

	public ClearEntriesAction(Model model) {
		super(model.getMessageMgr().getString("filemenu.unload_all_data"));
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final MessageManager mm = model.getMessageMgr();
		int result = JOptionPane.showConfirmDialog(
				model.getGUIManager().getMainWindow(),
				mm.getString("filemenu.clear_all_loaded_warn"),
				mm.getString("filemenu.clear_entries"),
				JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION) {
			model.clearEntries();
			GenomeViewScheduler.submit(Task.GC);
		}

	}

}
