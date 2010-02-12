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
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

public class RemoveLocationAction extends AbstractModelAction implements Observer {

    private static final long serialVersionUID = -5857913546086864524L;

    public RemoveLocationAction(Model model) {
        super("Remove selected location", model);
    }

    public void actionPerformed(ActionEvent arg0) {

        Set<Location> toRemove = new HashSet<Location>();
        toRemove.addAll(model.selectionModel().getLocationSelection());
        for (Location rf : toRemove)
            rf.getParent().removeLocation(rf);

    }

    public void update(Observable o, Object arg) {
        SortedSet<Location> set = model.selectionModel().getLocationSelection();
        setEnabled(set.size() > 0);

    }

}
