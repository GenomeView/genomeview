/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.help;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.Icons;
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
        super("About...");
        this.model = model;
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        String message = "GenomeView " + Configuration.version()+"\n\nAuthor:\n\tThomas Abeel, VIB\n\nContributors:\n\tThomas Van Parys, VIB\n\tMarijn Vandevoorde, VIB\n\nhttp://genomeview.org/\n\nConfiguration, logs and plugins:\n"+Configuration.getDirectory().toString()+"\n\n\nCopyright 2007-2011";
        
        JOptionPane.showMessageDialog(model.getGUIManager().getParent(), message, "GenomeView " + Configuration.version(),
                JOptionPane.INFORMATION_MESSAGE,Icons.MINILOGO_ICON);
    }

}
