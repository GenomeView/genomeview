/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.help;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;

/**
 * Action to show the about dialog.
 * 
 * @author Thomas Abeel
 * @author thpar
 * 
 */
public class ShowAboutDialogAction extends AbstractAction {

	private Model model;

	private static final long serialVersionUID = 4182067300462615334L;

	public ShowAboutDialogAction(Model model) {
		super(MessageManager.getString("helpmenu.about"));
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		String message = MessageManager.formatMessage("helpmenu.message",
				new Object[] { model.getConfiguration().version(),
						model.getConfiguration().getDirectory().toString() });

		JOptionPane.showMessageDialog(model.getGUIManager().getMainWindow(),
				message,
				MessageManager.formatMessage("helpmenu.version",
						new Object[] { model.getConfiguration().version() }),
				JOptionPane.INFORMATION_MESSAGE, Icons.MINILOGO_ICON);
	}

}
