/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

@SuppressWarnings("serial")
public class ZoomToSelectedLocationAction extends AbstractModelAction
		implements Observer {

	public ZoomToSelectedLocationAction(Model model) {
		super(MessageManager.getString("selectionmenu.zoom_to_location"),
				model);
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		Set<Location> selected = model.selectionModel().getLocationSelection();
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (Location f : selected) {
			if (min > f.start()) {
				min = f.start();
			}
			if (max < f.end()) {
				max = f.end();
			}

		}
		double margin = (max - min) * 0.05;
		model.vlm.setAnnotationLocationVisible(
				new Location((int) (min - margin), (int) (max + margin)));

	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		setEnabled(model.selectionModel().getFeatureSelection().size() > 0);

	}
}
