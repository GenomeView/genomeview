/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Data;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;

/**
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class RemoveLocationAction extends AbstractModelAction
		implements Observer {

	public RemoveLocationAction(Model model) {
		super(MessageManager.getString("editmenu.remove_location"), model);
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {

		Set<Location> toRemove = new HashSet<Location>();
		toRemove.addAll(model.selectionModel().getLocationSelection());
		model.selectionModel().clearLocationSelection();
		for (Location rf : toRemove) {
			Feature f = rf.getParent();
			/* If there are more locations, remove the selected one */
			if (f.location().length > 1) {
				f.removeLocation(rf);
			} else {
				Data<?> d = model.vlm.getVisibleEntry().get(f.type());
				if (d instanceof MemoryFeatureAnnotation) {
					MemoryFeatureAnnotation mf = (MemoryFeatureAnnotation) d;
					mf.remove(f);
				}

			}

		}

	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		SortedSet<Location> set = model.selectionModel().getLocationSelection();
		setEnabled(set.size() > 0);

	}

}
