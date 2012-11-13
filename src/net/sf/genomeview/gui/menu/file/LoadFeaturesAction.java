/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.OpenDialog;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class LoadFeaturesAction extends AbstractAction {

	private static final long serialVersionUID = 4601582100774593419L;

	private Model model;

	public LoadFeaturesAction(Model model) {
		super("Load data...");
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
		this.model = model;

	}

	public void actionPerformed(ActionEvent arg0) {
		new OpenDialog(model.getGUIManager().getParent(), model);

	}
}
