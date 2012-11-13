/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class ZoomToSelectionAction extends AbstractModelAction {
    static final long serialVersionUID = -330839317356311971L;

    public ZoomToSelectionAction(Model model) {
        super("Zoom to selected features", model);
    }

    @Override
    public void update(Observable o, Object ob) {
        setEnabled(model.getSelectedRegion()!=null);
    }

    public void actionPerformed(ActionEvent arg0) {
        model.setAnnotationLocationVisible(model.getSelectedRegion());
    }

}
