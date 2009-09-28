/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class ClearRegionSelectionAction extends AbstractModelAction implements Observer {

    private static final long serialVersionUID = 3091007204195190317L;

    public ClearRegionSelectionAction(Model model) {
        super("Clear region selection", model);
        model.addObserver(this);
        setEnabled(model.getSelectedRegion()!=null);
    }

    public void actionPerformed(ActionEvent e) {
        model.setSelectedRegion(null);

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.getSelectedRegion()!=null);
        
    }
}
