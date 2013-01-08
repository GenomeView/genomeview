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
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

public class ZoomToSelectedFeaturesAction extends AbstractModelAction implements Observer {

    private static final long serialVersionUID = 2073453052082133190L;

    public ZoomToSelectedFeaturesAction(Model model) {
        super("Zoom to feature selection", model);
    }

    public void actionPerformed(ActionEvent e) {
        Set<Feature> selected = model.selectionModel().getFeatureSelection();
        int min = Integer.MAX_VALUE;
        int max = 0;
        for (Feature f : selected) {
            if (min > f.start())
                min = f.start();
            if (max < f.end())
                max = f.start();

        }
        double margin = (max - min) * 0.05;
        model.vlm.setAnnotationLocationVisible(new Location((int) (min - margin), (int) (max + margin)));

    }

    public void update(Observable o, Object arg) {
        setEnabled(model.selectionModel().getFeatureSelection().size() > 0);

    }
}
