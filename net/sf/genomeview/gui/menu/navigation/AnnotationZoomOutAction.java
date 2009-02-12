/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

public class AnnotationZoomOutAction extends AbstractModelAction {

    private static final long serialVersionUID = 3458337359897530470L;

    public AnnotationZoomOutAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/zoom_out.png")), model);

    }

    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getAnnotationLocationVisible();
        double start = r.start();
        double end = r.end();
        double center = end - (end - start) / 2;
        int newStart = (int) (center - (end - start) * 0.75);
        int newEnd = (int) (center + (end - start) * 0.75);
        model.setAnnotationLocationVisible(new Location(newStart, newEnd));

    }

}
