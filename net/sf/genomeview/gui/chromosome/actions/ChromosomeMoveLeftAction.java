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

public class ChromosomeMoveLeftAction extends AbstractModelAction {

    private static final long serialVersionUID = -1179136343253345035L;

    public ChromosomeMoveLeftAction(Model model) {
        super(null, new ImageIcon(model.getClass().getResource("/images/arrow_left.png")), model);

    }

    @Override
    public void update(Observable o, Object obj) {
        setEnabled(model.getChromosomeLocationVisible().start() != 1);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Location r = model.getChromosomeLocationVisible();
        int halve = r.length() / 2;
        model.setChromosomeLocationVisible(new Location(r.start() - halve, r.start() + halve));
    }

}
