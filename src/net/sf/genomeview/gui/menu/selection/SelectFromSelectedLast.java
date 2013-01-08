/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

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
       setEnabled(model.selectionModel().getFeatureSelection()!=null&&model.selectionModel().getFeatureSelection().size()==1);

    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Feature rf=model.selectionModel().getFeatureSelection().iterator().next();
        Location[]loc=rf.location();
        model.selectionModel().setLocationSelection(loc[loc.length-1]);
        model.vlm.center(loc[loc.length-1].start() / 2 + loc[loc.length-1].end() / 2);
    }

}
