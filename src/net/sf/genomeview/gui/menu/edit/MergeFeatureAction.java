/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.MergeFeatureDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class MergeFeatureAction extends AbstractModelAction {

    /**
     * 
     */
    private static final long serialVersionUID = -3265609839659200956L;

    public MergeFeatureAction(Model model) {
        super(MessageManager.getString("editmenu.merge_feature"), model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
    }

    public void actionPerformed(ActionEvent e) {
        if (isEnabled())
        	new MergeFeatureDialog(model);

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() > 1);

    }

}
