/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

/**
 * Moves the view of the annotation view to the right.
 * 
 * @author Thomas Abeel
 * 
 */
public class AnnotationMoveRightAction extends AbstractModelAction {

    @Override
    public void update(Observable o, Object obj) {
        setEnabled(model.vlm.getAnnotationLocationVisible().end() < model.vlm.getSelectedEntry().getMaximumLength());
    }

    /**
     * 
     */
    private static final long serialVersionUID = 8307496875031190561L;

    public AnnotationMoveRightAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/arrow_right.png")), model);

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Location r = model.vlm.getAnnotationLocationVisible();
        int halve = r.length() / 4;
        if(enabled)
        	model.vlm.setAnnotationLocationVisible(new Location(r.start() + halve, r.end() + halve));

    }
    
    public static void perform(Model model){
    	AnnotationMoveRightAction action = new AnnotationMoveRightAction(model);
    	action.actionPerformed(null);
    }

}
