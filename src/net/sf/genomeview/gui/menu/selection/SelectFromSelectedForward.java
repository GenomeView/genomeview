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

public class SelectFromSelectedForward extends AbstractModelAction {

    /**
	 * 
	 */
    private static final long serialVersionUID = 3877028636520770303L;

    public SelectFromSelectedForward(Model model) {
        super("Move location selection forward", model);
        model.addObserver(this);
        update(null, null);
    }

    @Override
    public void update(Observable o, Object obj) {

        boolean oneFeatureSelection = model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() == 1;
        boolean oneLocationSelection = model.selectionModel().getLocationSelection() != null && model.selectionModel().getLocationSelection().size() == 1;
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
        Location[]loc=rf.location();
        int index=Arrays.binarySearch(loc, rl);
        if (index >= 0 && index < loc.length - 1)
            return loc[index+1];//.get(index + 1);
        else
            return null;

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Location next = getNext();
        if (next != null) {
            model.selectionModel().setLocationSelection(next);
            model.center(next.start()/2+next.end()/2);
        }
    }

}
