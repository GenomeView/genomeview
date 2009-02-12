/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;

public class SelectFromSelectedFirst extends AbstractModelAction {

    /**
     * 
     */
    private static final long serialVersionUID = 3658234266201763591L;

    public SelectFromSelectedFirst(Model model) {
        super("Select first location", model);
        model.addObserver(this);
        update(null, null);
    }

    @Override
    public void update(Observable o, Object obj) {
        setEnabled(model.getFeatureSelection() != null && model.getFeatureSelection().size() == 1);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Feature rf = model.getFeatureSelection().iterator().next();
        model.setLocationSelection(rf.location().first());
        model.center(rf.location().first().start() / 2 + rf.location().first().end() / 2);
    }

}
