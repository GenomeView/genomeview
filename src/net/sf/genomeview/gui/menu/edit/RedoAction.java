/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;


/**
 * Action to zoom in on the chromosome view.
 * 
 * @author thpar
 *
 */
public class RedoAction extends AbstractModelAction{


    private static final long serialVersionUID = -1318894389028565654L;

    public RedoAction(Model model) {
        super("Redo", new ImageIcon(model.getClass().getResource("/images/redo.png")), model);
    }

    public void actionPerformed(ActionEvent arg0) {
        model.redo();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        setEnabled(model.hasRedo());
        super.putValue(SHORT_DESCRIPTION, model.getRedoDescription());
    }
}
