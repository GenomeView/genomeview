/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;

@SuppressWarnings("serial")
public class SelectFromSelectedFirst extends AbstractModelAction {

	public SelectFromSelectedFirst(Model model) {
		super(model.getMessageMgr()
				.getString("selectionmenu.select_first_location"), model);
		model.addObserver(this);
		update(null, null);
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		setEnabled(model.selectionModel().getFeatureSelection() != null
				&& model.selectionModel().getFeatureSelection().size() == 1);

	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		Feature rf = model.selectionModel().getFeatureSelection().iterator()
				.next();
		model.selectionModel().setLocationSelection(rf.location()[0]);
		model.vlm.center(
				rf.location()[0].start() / 2 + rf.location()[0].end() / 2);
	}

}
