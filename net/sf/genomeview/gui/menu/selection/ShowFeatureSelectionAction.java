/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;

public class ShowFeatureSelectionAction extends AbstractModelAction implements Observer {

    /**
     * 
     */
    private static final long serialVersionUID = 3678046253049598077L;

    public ShowFeatureSelectionAction(Model model) {
        super("Show feature selection", model);
    }

    public void actionPerformed(ActionEvent e) {
        Set<Feature> selected = model.getFeatureSelection();
        for (Feature f : selected)
            model.setFeatureVisible(f, true);

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.getFeatureSelection().size() > 0);

    }
}
