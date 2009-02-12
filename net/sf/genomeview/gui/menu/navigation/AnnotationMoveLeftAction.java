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
 * Moves the view of the annotation panel to the left.
 * 
 * @author Thomas Abeel
 * 
 */
public class AnnotationMoveLeftAction extends AbstractModelAction {

    @Override
    public void update(Observable o, Object obj) {
        setEnabled(model.getAnnotationLocationVisible().start() > 1);
    }

    /**
     * 
     */
    private static final long serialVersionUID = -151723517814284940L;

    public AnnotationMoveLeftAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/arrow_left.png")), model);
        
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getAnnotationLocationVisible();
        int halve = r.length() / 4;
        model.setAnnotationLocationVisible(new Location(r.start() - halve, r.end() - halve));

    }

}
