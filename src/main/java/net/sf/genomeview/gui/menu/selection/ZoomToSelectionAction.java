/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ZoomToSelectionAction extends AbstractModelAction {

	public ZoomToSelectionAction(Model model) {
		super(MessageManager
				.getString("selectionmenu.zoom_to_selected_features"), model);
	}

	@Override
	public void updateSafe(Observable o, Object ob) {
		setEnabled(model.getSelectedRegion() != null);
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		model.vlm.setAnnotationLocationVisible(model.getSelectedRegion());
	}

}
