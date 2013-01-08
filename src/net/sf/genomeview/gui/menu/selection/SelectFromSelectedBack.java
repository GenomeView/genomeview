/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

public class SelectFromSelectedBack extends AbstractModelAction {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3698699205405201460L;

    public SelectFromSelectedBack(Model model) {
        super("Move location selection back", model);

    }

    @Override
    public void update(Observable o, Object obj) {
        try {
            boolean oneFeatureSelection = model.selectionModel().getFeatureSelection() != null
                    && model.selectionModel().getFeatureSelection().size() == 1;
            boolean oneLocationSelection = model.selectionModel().getLocationSelection() != null
                    && model.selectionModel().getLocationSelection().size() == 1;
            if (oneFeatureSelection && oneLocationSelection) {
                setEnabled(getPrev() != null);

            } else {
                setEnabled(false);
            }
        } catch (Exception e) {
            System.err.println("SelectFromSelectedBack update exception...");

        }

    }

    private Location getPrev() {
        Feature rf = model.selectionModel().getFeatureSelection().first();
        Location rl = model.selectionModel().getLocationSelection().first();
//        ArrayList<Location> loc = new ArrayList<Location>();
//        loc.addAll(rf.location());
        int index=Arrays.binarySearch(rf.location(), rl);
//        int index = .indexOf(rl);
        if (index > 0)
        	return rf.location()[index-1];
//            return loc.get(index - 1);
        else
            return null;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Location prev = getPrev();
        if (prev != null) {
            model.selectionModel().setLocationSelection(prev);
            model.vlm.center(prev.start() / 2 + prev.end() / 2);
        }
    }

}
