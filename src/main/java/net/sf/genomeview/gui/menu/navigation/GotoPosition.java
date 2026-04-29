/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;

@SuppressWarnings("serial")
public class GotoPosition extends AbstractModelAction {

	public GotoPosition(Model model) {
		super(model.getMessageMgr().getString("navigationmenu.goto_position"),
				model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control G"));
	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		String input = JOptionPane.showInputDialog(model.getMessageMgr()
				.getString("navigationmenu.provide_coordination"));
		if (input != null && input.trim().length() > 0) {
			try {
				int i = Integer.parseInt(input.trim());
				super.model.vlm.center(i);
			} catch (NumberFormatException e) {
				model.setPosition(input.trim());
			}

		}

	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// TODO Auto-generated method stub

	}

}
