/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.dialog.SaveDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;

/**
 * 
 * @author Thomas Abeel
 *
 */
public class SaveAction extends AbstractModelAction {

    private static final long serialVersionUID = 5192584831566138816L;

    public SaveAction(Model model) {
        super(MessageManager.getString("filemenu.save_annotation"), model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
    }

    public void actionPerformed(ActionEvent arg0) {
    	new SaveDialog(model);
    }

}
