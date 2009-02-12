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
 * Action to zoom in on the chromosome view.
 * 
 * @author thpar
 *
 */
public class ChromosomeOverviewAction extends AbstractAction {

    /**
     * 
     */
    private static final long serialVersionUID = -1318894389028565654L;

    private Model model;

    public ChromosomeOverviewAction(Model model) {

        super("Overview", new ImageIcon(model.getClass().getResource("/images/preview.png")));
        this.model = model;

    }

    public void actionPerformed(ActionEvent arg0) {
        model.setChromosomeLocationVisible(new Location(1, model.getLongestChromosomeLength()));

    }
}
