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


public class MergeFeatureAction extends AbstractModelAction {

    /**
     * 
     */
    private static final long serialVersionUID = -3265609839659200956L;

    public MergeFeatureAction(Model model) {
        super("Merge selected features", model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
    }

    public void actionPerformed(ActionEvent e) {
        if (isEnabled())
            StaticUtils.getMergeFeature(model).setVisible(true);

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.getFeatureSelection() != null && model.getFeatureSelection().size() > 1);

    }

}
