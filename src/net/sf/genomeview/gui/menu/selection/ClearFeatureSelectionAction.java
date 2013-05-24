/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class ClearFeatureSelectionAction extends AbstractModelAction implements Observer {

    private static final long serialVersionUID = 3091007204195190317L;

    public ClearFeatureSelectionAction(Model model) {
        super(MessageManager.getString("selectionmenu.clear_feature"), model);
    }

    public void actionPerformed(ActionEvent e) {
        model.selectionModel().clearLocationSelection();

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.selectionModel().getFeatureSelection().size() > 0);

    }
}
