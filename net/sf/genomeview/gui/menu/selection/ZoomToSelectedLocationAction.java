/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

public class ZoomToSelectedLocationAction extends AbstractModelAction implements Observer {

    /**
     * 
     */
    private static final long serialVersionUID = 8764835346246357318L;

    public ZoomToSelectedLocationAction(Model model) {
        super("Zoom to location selection", model);
    }

    public void actionPerformed(ActionEvent e) {
        Set<Location> selected = model.selectionModel().getLocationSelection();
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (Location f : selected) {
            if (min > f.start())
                min = f.start();
            if (max < f.end())
                max = f.end();

        }
        double margin = (max - min) * 0.05;
        model.setAnnotationLocationVisible(new Location((int) (min - margin), (int) (max + margin)));

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.selectionModel().getFeatureSelection().size() > 0);

    }
}
