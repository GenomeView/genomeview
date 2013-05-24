/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class EditStructureAction extends AbstractModelAction {

    private static final long serialVersionUID = -2032099327942679205L;

    public EditStructureAction(Model model) {
        super(MessageManager.getString("editmenu.edit_structure"), model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        StaticUtils.getEditStructure(model).setVisible(true);

    }

    @Override
    public void update(Observable o, Object arg) {
        setEnabled(model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() == 1);

    }

}
