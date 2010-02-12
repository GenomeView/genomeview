/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class SplitFeatureAction extends AbstractModelAction {

    /**
     * 
     */
    private static final long serialVersionUID = -3265609839659200956L;

    public SplitFeatureAction(Model model) {
        super("Split feature between two selected locations", model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
    }

    public void actionPerformed(ActionEvent e) {
        if (isEnabled())
            StaticUtils.splitFeature(model).setVisible(true);

    }

    @Override
    public void update(Observable o, Object arg) {
        setEnabled(model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() == 1
                && model.selectionModel().getLocationSelection() != null && model.selectionModel().getLocationSelection().size() == 2);

    }

}
