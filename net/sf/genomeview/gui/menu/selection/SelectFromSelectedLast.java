/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;

public class SelectFromSelectedLast extends AbstractModelAction {

    

    /**
     * 
     */
    private static final long serialVersionUID = 944182722570469980L;

    public SelectFromSelectedLast(Model model) {
        super("Select last location", model);
        model.addObserver(this);
        update(null,null);
    }

    @Override
    public void update(Observable o, Object obj) {
       setEnabled(model.getFeatureSelection()!=null&&model.getFeatureSelection().size()==1);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Feature rf=model.getFeatureSelection().iterator().next();
        model.setLocationSelection(rf.location().last());
        model.center(rf.location().last().start() / 2 + rf.location().last().end() / 2);
    }

}
