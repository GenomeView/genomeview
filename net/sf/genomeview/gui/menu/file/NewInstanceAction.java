/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.GenomeView;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class NewInstanceAction extends AbstractModelAction {

    private static final long serialVersionUID = -7088480852250688472L;

    public NewInstanceAction(Model model) {
        super("New program instance", model);

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
      GenomeView.single.newActivation(new String[0]);
    }

}
