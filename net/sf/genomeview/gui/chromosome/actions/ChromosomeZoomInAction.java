/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome.actions;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

/**
 * Action to zoom in on the chromosome view.
 * 
 * @author thpar
 * 
 */
public class ChromosomeZoomInAction extends AbstractModelAction {

    private static final long serialVersionUID = -1313894389028565654L;

    public ChromosomeZoomInAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/zoom_in.png")), model);
    }

    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getChromosomeLocationVisible();
        long start = r.start();
        long end = r.end();
        long center = end - (end - start) / 2;
        int newStart = (int) (center - (end - start) / 4);
        int newEnd = (int) (center + (end - start) / 4);
        model.setChromosomeLocationVisible(new Location(newStart, newEnd));
    }

    @Override
    public void update(Observable arg0, Object arg1) {
        if (model.getLongestChromosomeLength() > 5000){
        	setEnabled(model.getChromosomeLocationVisible().length() > 1000);
        } else {
        	setEnabled(model.getChromosomeLocationVisible().length() > 10); 
        }

    }
}
