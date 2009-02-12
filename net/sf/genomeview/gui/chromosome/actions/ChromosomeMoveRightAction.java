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

public class ChromosomeMoveRightAction extends AbstractModelAction {

    private static final long serialVersionUID = -4436890402785275133L;

    public ChromosomeMoveRightAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/arrow_right.png")), model);

    }

    @Override
    public void update(Observable o, Object obj) {
        setEnabled(model.getChromosomeLocationVisible().end() < model.getLongestChromosomeLength());
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getChromosomeLocationVisible();
        int halve = r.length() / 2;
        model.setChromosomeLocationVisible(new Location(r.end() - halve, r.end() + halve));
    }

}
