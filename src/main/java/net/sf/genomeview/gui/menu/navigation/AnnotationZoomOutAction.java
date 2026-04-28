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

@SuppressWarnings("serial")
public class AnnotationZoomOutAction extends AbstractModelAction {

	public AnnotationZoomOutAction(Model model) {
		super(null,
				new ImageIcon(
						model.getClass().getResource("/images/zoom_out.png")),
				model);

	}

	@Override
	public void actionPerformedSafe(ActionEvent arg0) {
		Location r = model.vlm.getAnnotationLocationVisible();
		double start = r.start();
		double end = r.end();
		double center = end - (end - start) / 2;
		int newStart = (int) (center - (end - start) * 0.75);
		int newEnd = (int) (center + (end - start) * 0.75);
		if (enabled) {
			model.vlm.setAnnotationLocationVisible(
					new Location(newStart, newEnd));
		}

	}

	@Override
	public void updateSafe(Observable o, Object obj) {
		// ignore

	}

}
