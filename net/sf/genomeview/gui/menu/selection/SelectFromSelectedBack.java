/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
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
            boolean oneFeatureSelection = model.getFeatureSelection() != null
                    && model.getFeatureSelection().size() == 1;
            boolean oneLocationSelection = model.getLocationSelection() != null
                    && model.getLocationSelection().size() == 1;
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
        Feature rf = model.getFeatureSelection().first();
        Location rl = model.getLocationSelection().first();
        ArrayList<Location> loc = new ArrayList<Location>();
        loc.addAll(rf.location());
        int index = loc.indexOf(rl);
        if (index > 0)
            return loc.get(index - 1);
        else
            return null;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Location prev = getPrev();
        if (prev != null) {
            model.setLocationSelection(prev);
            model.center(prev.start() / 2 + prev.end() / 2);
        }
    }

}
