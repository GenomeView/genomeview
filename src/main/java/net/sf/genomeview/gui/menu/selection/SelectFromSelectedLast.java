/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

@SuppressWarnings("serial")
public class SelectFromSelectedLast extends AbstractModelAction {

	public SelectFromSelectedLast(Model model) {
		super(MessageManager.getString("selectionmenu.select_last_location"),
				model);
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
		Location[] loc = rf.location();
		model.selectionModel().setLocationSelection(loc[loc.length - 1]);
		model.vlm.center(loc[loc.length - 1].start() / 2
				+ loc[loc.length - 1].end() / 2);
	}

}
