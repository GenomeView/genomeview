/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.SaveDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * 
 * @author Thomas Abeel
 *
 */
@SuppressWarnings("serial")
public class SaveAction extends AbstractModelAction {

	public SaveAction(Model model) {
		super(MessageManager.getString("filemenu.save_annotation"), model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		new SaveDialog(model);
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignored

	}

}
