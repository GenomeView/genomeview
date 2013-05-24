/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class ExitAction extends AbstractModelAction {

    private static final long serialVersionUID = 5781369935758205711L;

    public ExitAction(Model model) {
        super(MessageManager.getString("filemenu.exit"), model);
    }

    public void actionPerformed(ActionEvent arg0) {
        model.exit();
    }

}
