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
        setEnabled(model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() == 1);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Feature rf = model.selectionModel().getFeatureSelection().iterator().next();
        model.selectionModel().setLocationSelection(rf.location()[0]);
        model.center(rf.location()[0].start() / 2 + rf.location()[0].end() / 2);
    }

}
