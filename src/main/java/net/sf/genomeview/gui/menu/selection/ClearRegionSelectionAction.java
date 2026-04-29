/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ClearRegionSelectionAction extends AbstractModelAction
		implements Observer {

	public ClearRegionSelectionAction(Model model) {
		super(model.getMessageMgr().getString("selectionmenu.clear_region"),
				model);
		model.addObserver(this);
		setEnabled(model.getSelectedRegion() != null);
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		model.selectionModel().setSelectedRegion(null);

	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		setEnabled(model.getSelectedRegion() != null);

	}
}
