/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * Action to zoom in on the chromosome view.
 * 
 * @author thpar
 *
 */
@SuppressWarnings("serial")
public class RedoAction extends AbstractModelAction {

	public RedoAction(Model model) {
		super(model.getMessageMgr().getString("editmenu.redo"),
				new ImageIcon(model.getClass().getResource("/images/redo.png")),
				model);
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		model.redo();
	}

	@Override
	public void updateSafe(Observable o, Object arg) {
		setEnabled(model.hasRedo());
		super.putValue(SHORT_DESCRIPTION, model.getRedoDescription());
	}
}
