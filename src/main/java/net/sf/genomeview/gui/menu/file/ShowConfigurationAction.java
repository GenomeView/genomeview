/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.config.ConfigurationDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ShowConfigurationAction extends AbstractModelAction {

	public ShowConfigurationAction(Model model) {
		super(model.getMessageMgr().getString("filemenu.configuration"), model);
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		ConfigurationDialog.showConfigurationDialog(model);
		model.refresh();
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignored
	}

}
