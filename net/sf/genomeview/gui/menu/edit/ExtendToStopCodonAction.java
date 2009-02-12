/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Sequence;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.utils.SequenceTools;

public class ExtendToStopCodonAction extends AbstractModelAction {

    public ExtendToStopCodonAction(Model model) {
        super("Extend to next stop codon", model);
        model.addObserver(this);
        update(null, null);

    }

    /**
     * 
     */
    private static final long serialVersionUID = 2143874687832094430L;

    @Override
    public void update(Observable o, Object obj) {
        if (model.getFeatureSelection().size() == 1 && model.getFeatureSelection().first().type() == Type.get("CDS")) {
            setEnabled(SequenceTools.hasMissingStopCodon(model.getSelectedEntry().sequence, model.getFeatureSelection()
                    .first()));
        } else
            setEnabled(false);

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        assert (model.getFeatureSelection() != null);
        assert (model.getFeatureSelection().size() == 1);
        Feature rf = model.getFeatureSelection().iterator().next();
        Sequence seq = model.getSelectedEntry().sequence;
        String nt = SequenceTools.extractSequence(seq, rf);
        int rest = nt.length() % 3;
        assert (SequenceTools.hasMissingStopCodon(model.getSelectedEntry().sequence, rf));
        if (rf.strand() == Strand.FORWARD) {
            int start = rf.end() - rest + 1;
            while (model.getSelectedEntry().sequence.getAminoAcid(start) != '*') {
                start += 3;
            }
            start += 2;
            rf.location().last().setEnd(start);

        } else if (rf.strand() == Strand.REVERSE) {
            int start = rf.start() + rest;
            System.out.println(start);
            while (model.getSelectedEntry().sequence.getReverseAminoAcid(start) != '*') {

                start -= 3;
            }
            rf.location().first().setStart(start);

        }

    }
}
