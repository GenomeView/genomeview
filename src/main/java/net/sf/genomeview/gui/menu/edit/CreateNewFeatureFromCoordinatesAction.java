/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.NewFeatureFromCoordinatesDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class CreateNewFeatureFromCoordinatesAction extends AbstractModelAction {

	public CreateNewFeatureFromCoordinatesAction(Model model) {
		super(model.getMessageMgr()
				.getString("editmenu.create_feature_coordinates"), model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control N"));
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		NewFeatureFromCoordinatesDialog nfd = new NewFeatureFromCoordinatesDialog(
				model);
		nfd.setVisible(true);
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// TODO Auto-generated method stub

	}

}
