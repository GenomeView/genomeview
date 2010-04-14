/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Blast;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;

/**
 * Opens a NCBI blast window with the sequence filled in in the boxes.
 * 
 * @author Thomas Abeel
 * 
 */
public class NCBIselectedDnaBlastAction extends AbstractModelAction {

    private static final long serialVersionUID = -10339770203425228L;

    public NCBIselectedDnaBlastAction(Model model) {
        super("Blast selected DNA at NCBI", model);
    }

    @Override
    public void update(Observable x, Object y) {
        setEnabled(model.getSelectedRegion() != null);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        Location l = model.getSelectedRegion();
        String seq = model.getSelectedEntry().sequence().getSubSequence(l.start(), l.end()+1);

        Blast.nucleotideBlast("selection",seq);

    }

}
