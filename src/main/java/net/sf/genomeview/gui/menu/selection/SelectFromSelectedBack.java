/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Observable;
import java.util.logging.Level;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

@SuppressWarnings("serial")
public class SelectFromSelectedBack extends AbstractModelAction {

	public SelectFromSelectedBack(Model model) {
		super(model.getMessageMgr()
				.getString("selectionmenu.move_back_location"), model);

	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		StaticUtils.run(() -> {
			boolean oneFeatureSelection = model.selectionModel()
					.getFeatureSelection() != null
					&& model.selectionModel().getFeatureSelection().size() == 1;
			boolean oneLocationSelection = model.selectionModel()
					.getLocationSelection() != null
					&& model.selectionModel().getLocationSelection()
							.size() == 1;
			if (oneFeatureSelection && oneLocationSelection) {
				setEnabled(getPrev() != null);

			} else {
				setEnabled(false);
			}
		}, model.getLog(), Level.WARNING,
				"SelectFromSelectedBack update failed");
	}

	private Location getPrev() {
		Feature rf = model.selectionModel().getFeatureSelection().first();
		Location rl = model.selectionModel().getLocationSelection().first();
		int index = Arrays.binarySearch(rf.location(), rl);
		if (index > 0) {
			return rf.location()[index - 1];
		} else {
			return null;
		}

	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {

		Location prev = getPrev();
		if (prev != null) {
			model.selectionModel().setLocationSelection(prev);
			model.vlm.center(prev.start() / 2 + prev.end() / 2);
		}
	}

}
