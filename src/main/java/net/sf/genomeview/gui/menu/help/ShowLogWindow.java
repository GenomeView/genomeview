/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.help;

import java.awt.event.ActionEvent;
import java.util.logging.Level;

import javax.swing.AbstractAction;

import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.LogWindow;

/**
 * Action to show the log window
 * 
 */
@SuppressWarnings("serial")
public class ShowLogWindow extends AbstractAction {

	private Model model;

	public ShowLogWindow(Model model) {
		super(MessageManager.getString("helpmenu.logwindow"));
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// special log to make the log window visible
		model.getLog().log(Level.FINEST, LogWindow.MAKE_LOG_VISIBLE_REQUEST);
	}

}
