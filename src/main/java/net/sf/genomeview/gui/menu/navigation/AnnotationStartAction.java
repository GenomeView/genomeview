/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

/**
 * Moves the view of the annotation panel to the beginning of the sequence.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class AnnotationStartAction extends AbstractModelAction {

	public AnnotationStartAction(Model model) {
		super(null, null, model);

	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		Location r = model.vlm.getAnnotationLocationVisible();
		model.vlm.setAnnotationLocationVisible(new Location(1, r.length()));

	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignore
	}

}
