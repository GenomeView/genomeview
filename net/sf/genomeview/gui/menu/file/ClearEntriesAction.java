/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class ClearEntriesAction extends AbstractAction {

	private static final long serialVersionUID = 1349866639638385199L;
	private Model model;

	public ClearEntriesAction(Model model) {
		super("Unload all data");
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		int result = JOptionPane.showConfirmDialog(model.getParent(),
				"Do you really want to clear all loaded data?",
				"Clear entries?", JOptionPane.YES_NO_OPTION);
		if (result == JOptionPane.YES_OPTION)
			model.clearEntries();

	}

}
