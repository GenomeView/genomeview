/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ClearFeatureSelectionAction extends AbstractModelAction
		implements Observer {

	public ClearFeatureSelectionAction(Model model) {
		super(MessageManager.getString("selectionmenu.clear_feature"), model);
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		model.selectionModel().clearLocationSelection();

	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		setEnabled(model.selectionModel().getFeatureSelection().size() > 0);

	}
}
