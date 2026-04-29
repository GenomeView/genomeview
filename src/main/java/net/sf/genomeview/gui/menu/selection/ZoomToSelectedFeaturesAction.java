/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

@SuppressWarnings("serial")
public class ZoomToSelectedFeaturesAction extends AbstractModelAction
		implements Observer {

	public ZoomToSelectedFeaturesAction(Model model) {
		super(model.getMessageMgr().getString("selectionmenu.zoom_to_feature"),
				model);
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		Set<Feature> selected = model.selectionModel().getFeatureSelection();
		int min = Integer.MAX_VALUE;
		int max = 0;
		for (Feature f : selected) {
			if (min > f.start()) {
				min = f.start();
			}
			if (max < f.end()) {
				max = f.start();
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
