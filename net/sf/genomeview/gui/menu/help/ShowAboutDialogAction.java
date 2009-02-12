/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.help;

import java.awt.event.ActionEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

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

        String message = "GenomeView " + Configuration.version()+"\n\nAuthors:\n\tThomas Abeel\n\tThomas Van Parys\n\nhttp://genomeview.sourceforge.net/\n\nConfiguration, logs and plugins:\n"+Configuration.getDirectory().toString()+"\n\n\nCopyright 2007-2009";
        
        JOptionPane.showMessageDialog(model.getParent(), message, "GenomeView " + Configuration.version(),
                JOptionPane.INFORMATION_MESSAGE);
    }

}
