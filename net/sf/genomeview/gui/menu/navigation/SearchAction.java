/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.SearchDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class SearchAction extends AbstractModelAction {

    /**
     * 
     */
    private static final long serialVersionUID = 8047658085913988768L;

    public SearchAction(Model model) {
        super("Search ...", model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        SearchDialog.showDialog(super.model);

    }
}
