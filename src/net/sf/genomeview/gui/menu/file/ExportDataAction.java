/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.ExportDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ExportDataAction extends AbstractModelAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8134920521497962449L;

	public ExportDataAction(Model model) {
		super("Export data...", model);

	}

	public void actionPerformed(ActionEvent arg0) {
		ExportDialog.display(model, false);
	}
}
