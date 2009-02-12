/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Location;

/**
 * Action to zoom out of the chromosome view.
 * 
 * @author thpar
 *
 */
public class ChromosomeZoomOutAction extends AbstractAction {

    private static final long serialVersionUID = -3410894533162386267L;

    private Model model;

    public ChromosomeZoomOutAction(Model model) {

        super(null, new ImageIcon(model.getClass().getResource("/images/zoom_out.png")));
        this.model = model;

    }

    public void actionPerformed(ActionEvent arg0) {
        Location r=model.getChromosomeLocationVisible();
        long start = r.start();
        long end = r.end();
        
        long center = end - (end - start) / 2;
        int newStart = (int) (center - (end - start));
        int newEnd = (int) (center + (end - start));
        model.setChromosomeLocationVisible(new Location(newStart, newEnd));

    }
}
