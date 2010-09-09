/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.help;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;


/**
 * Action to show the about dialog.
 * 
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

        String message = "GenomeView " + Configuration.version()+"\n\nAuthors:\n\tThomas Abeel\n\tThomas Van Parys\n\nhttp://genomeview.sourceforge.net/\n\nConfiguration, logs and plugins:\n"+Configuration.getDirectory().toString()+"\n\n\nCopyright 2007-2010";
        
        JOptionPane.showMessageDialog(model.getGUIManager().getParent(), message, "GenomeView " + Configuration.version(),
                JOptionPane.INFORMATION_MESSAGE);
    }

}
