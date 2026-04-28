/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.ExportDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class ExportDataAction extends AbstractModelAction {

	public ExportDataAction(Model model) {
		super(MessageManager.getString("filemenu.export_data"), model);

	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		ExportDialog.display(model, false);
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignore }
	}
}
