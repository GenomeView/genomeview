/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.MergeFeatureDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * 
 * @author Thomas Abeel
 *
 */
@SuppressWarnings("serial")
public class MergeFeatureAction extends AbstractModelAction {

	public MergeFeatureAction(Model model) {
		super(MessageManager.getString("editmenu.merge_feature"), model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control M"));
	}

	@Override
	public void actionPerformedSafe(ActionEvent e) {
		if (isEnabled()) {
			new MergeFeatureDialog(model);
		}

	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		setEnabled(model.selectionModel().getFeatureSelection() != null
				&& model.selectionModel().getFeatureSelection().size() > 1);

	}

}
