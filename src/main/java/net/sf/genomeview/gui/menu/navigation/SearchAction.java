/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.genomeview.gui.search.SearchDialog;

/**
 * 
 * @author Thomas Abeel
 *
 */
@SuppressWarnings("serial")
public class SearchAction extends AbstractModelAction {

	public SearchAction(Model model) {
		super(MessageManager.getString("navigationmenu.search"), model);
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));

	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		SearchDialog.showDialog(super.model);

	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignored

	}
}
