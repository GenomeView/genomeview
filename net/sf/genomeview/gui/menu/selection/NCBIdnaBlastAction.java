/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Blast;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.utils.SequenceTools;

/**
 * Opens a NCBI blast window with the sequence filled in in the boxes.
 * 
 * @author Thomas Abeel
 * 
 */
public class NCBIdnaBlastAction extends AbstractModelAction {

    private static final long serialVersionUID = -10339770203425228L;

    public NCBIdnaBlastAction(Model model) {
        super("Blast DNA at NCBI", model);
    }

    @Override
    public void update(Observable x, Object y) {
        setEnabled(model.getFeatureSelection() != null && model.getFeatureSelection().size() == 1);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {

        Feature rf = model.getLocationSelection().iterator().next().getParent();
        String seq = SequenceTools.extractSequence(model.getSelectedEntry().sequence, rf);
        Blast.nucleotideBlast(""+rf.toString(),seq);

    }

}
