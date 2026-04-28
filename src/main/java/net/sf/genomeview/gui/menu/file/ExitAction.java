/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class ExitAction extends AbstractModelAction {

	public ExitAction(Model model) {
		super(MessageManager.getString("filemenu.exit"), model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		model.exit();
	}

	@Override
	public void updateSafe(Observable o, Object obj) {
//ignore		
	}

}
