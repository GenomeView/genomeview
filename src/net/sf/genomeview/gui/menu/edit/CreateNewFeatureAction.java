/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.NewFeatureDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;



public class CreateNewFeatureAction extends AbstractModelAction{

    private static final long serialVersionUID = 4521376746707912717L;

    public CreateNewFeatureAction(Model model) {
        super(MessageManager.getString("editmenu.create_new_feature"), model);
      
    }

    public void actionPerformed(ActionEvent e) {
        NewFeatureDialog nfd = new NewFeatureDialog(model);
        nfd.setVisible(true);
    }

    public void update(Observable o, Object arg) {
        setEnabled(model.getSelectedRegion() != null);

    }

}
