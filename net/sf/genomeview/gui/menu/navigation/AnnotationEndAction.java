/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

/**
 * Moves the view of the annotation panel to the end of the sequence.
 * 
 * @author Thomas Abeel
 * 
 */
public class AnnotationEndAction extends AbstractModelAction {

	private static final long serialVersionUID = -8869862710635018773L;

	public AnnotationEndAction(Model model) {
        super(null, null, model);
        
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getAnnotationLocationVisible();
        model.setAnnotationLocationVisible(new Location(model.getSelectedEntry().size()-r.length(),model.getSelectedEntry().size()));

    }

}
