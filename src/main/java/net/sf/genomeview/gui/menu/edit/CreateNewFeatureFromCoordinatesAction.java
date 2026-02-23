/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.NewFeatureFromCoordinatesDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class CreateNewFeatureFromCoordinatesAction extends AbstractModelAction {

    private static final long serialVersionUID = 4521376746707912717L;

    public CreateNewFeatureFromCoordinatesAction(Model model) {
        super(MessageManager.getString("editmenu.create_feature_coordinates"), model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
    }

    public void actionPerformed(ActionEvent e) {
        NewFeatureFromCoordinatesDialog nfd = new NewFeatureFromCoordinatesDialog(model);
        nfd.setVisible(true);
    }

}
