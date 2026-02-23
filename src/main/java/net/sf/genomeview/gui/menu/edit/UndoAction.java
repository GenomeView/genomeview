/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.menu.AbstractModelAction;


/**
 * Action to zoom in on the chromosome view.
 * 
 * @author thpar
 *
 */
public class UndoAction extends AbstractModelAction{


    private static final long serialVersionUID = -1318894389028565654L;


    public UndoAction(Model model2) {
        super(MessageManager.getString("editmenu.undo"), new ImageIcon(model2.getClass().getResource("/images/undo.png")), model2);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
    }

    public void actionPerformed(ActionEvent arg0) {
    	model.undo();
    }
    
    @Override
    public void update(Observable o, Object arg) {
        setEnabled(model.hasUndo());
        super.putValue(SHORT_DESCRIPTION, model.getUndoDescription());
    }
    
}
