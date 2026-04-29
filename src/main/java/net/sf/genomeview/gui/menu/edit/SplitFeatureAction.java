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

@SuppressWarnings("serial")
public class SplitFeatureAction extends AbstractModelAction {

	public SplitFeatureAction(Model model) {
		super(model.getMessageMgr().getString("editmenu.split_feature"), model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		if (isEnabled()) {
			StaticUtils.splitFeature(model).setVisible(true);
		}

	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		setEnabled(model.selectionModel().getFeatureSelection() != null
				&& model.selectionModel().getFeatureSelection().size() == 1
				&& model.selectionModel().getLocationSelection() != null
				&& model.selectionModel().getLocationSelection().size() == 2);

	}

}
