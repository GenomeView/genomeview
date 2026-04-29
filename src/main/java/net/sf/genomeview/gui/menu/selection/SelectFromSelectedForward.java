/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

@SuppressWarnings("serial")
public class SelectFromSelectedForward extends AbstractModelAction {

	public SelectFromSelectedForward(Model model) {
		super(model.getMessageMgr()
				.getString("selectionmenu.move_forward_location"), model);
		model.addObserver(this);
		update(null, null);
	}

	@Override
	public void updateSafe(Observable o, Object obj) {

		boolean oneFeatureSelection = model.selectionModel()
				.getFeatureSelection() != null
				&& model.selectionModel().getFeatureSelection().size() == 1;
		boolean oneLocationSelection = model.selectionModel()
				.getLocationSelection() != null
				&& model.selectionModel().getLocationSelection().size() == 1;
		if (oneFeatureSelection && oneLocationSelection) {
			setEnabled(getNext() != null);

		} else {
			setEnabled(false);
		}

	}

	private Location getNext() {
		Feature rf = model.selectionModel().getFeatureSelection().first();
		Location rl = model.selectionModel().getLocationSelection().first();
//        ArrayList<Location> loc = new ArrayList<Location>();
//        loc.addAll(rf.location());
//        int index = loc.indexOf(rl);
		Location[] loc = rf.location();
		int index = Arrays.binarySearch(loc, rl);
		if (index >= 0 && index < loc.length - 1) {
			return loc[index + 1];// .get(index + 1);
		} else {
			return null;
		}

	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {

		Location next = getNext();
		if (next != null) {
			model.selectionModel().setLocationSelection(next);
			model.vlm.center(next.start() / 2 + next.end() / 2);
		}
	}

}
