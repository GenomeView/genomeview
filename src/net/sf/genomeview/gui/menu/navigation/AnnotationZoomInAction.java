/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.ImageIcon;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

public class AnnotationZoomInAction extends AbstractModelAction {

    private static final long serialVersionUID = -1125623416282373487L;

    public AnnotationZoomInAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/zoom_in.png")), model);

    }

    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getAnnotationLocationVisible();
        double start = r.start();
        double end = r.end();
        double center = end - (end - start) / 2;
        int newStart = (int) (center - (end - start) * 0.25);
        int newEnd = (int) (center + (end - start) * 0.25);
        if(enabled)
        	model.setAnnotationLocationVisible(new Location(newStart, newEnd));

    }

    @Override
    public void update(Observable arg0, Object arg1) {
        setEnabled(model.getAnnotationLocationVisible().length() > Configuration.getInt("minimumNucleotides"));

    }

}
