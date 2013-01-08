/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.event.ActionEvent;
import java.util.Observable;

import net.sf.genomeview.core.AnalyzedFeature;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Feature;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

/**
 * 
 * @author Thomas Abeel
 * 
 */
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
		if (model.selectionModel().getFeatureSelection().size() == 1
				&& model.selectionModel().getFeatureSelection().first().type() == Type.get("CDS")) {
			AnalyzedFeature af = new AnalyzedFeature(model.vlm.getSelectedEntry().sequence(), model.selectionModel()
					.getFeatureSelection().first(), model.getAAMapping());

			setEnabled(af.hasMissingStopCodon());
		} else
			setEnabled(false);

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		assert (model.selectionModel().getFeatureSelection() != null);
		assert (model.selectionModel().getFeatureSelection().size() == 1);
		Feature rf = model.selectionModel().getFeatureSelection().iterator().next();
		Sequence seq = model.vlm.getSelectedEntry().sequence();
		String nt = SequenceTools.extractSequence(seq, rf).stringRepresentation();
		int rest = nt.length() % 3;
		AnalyzedFeature af = new AnalyzedFeature(model.vlm.getSelectedEntry().sequence(), rf, model.getAAMapping());
		assert (af.hasMissingStopCodon());
		if (rf.strand() == Strand.FORWARD) {
			int start = rf.end() - rest + 1;
			while (model.getAAMapping().get(
					model.vlm.getSelectedEntry().sequence().subsequence(start, start + 3).stringRepresentation()) != '*') {
				start += 3;
			}
			start += 2;
			rf.location()[rf.location().length-1].setEnd(start);

		} else if (rf.strand() == Strand.REVERSE) {
			int start = rf.start() + rest;
			System.out.println(start);
			String codon = SequenceTools.reverseComplement(
					model.vlm.getSelectedEntry().sequence().subsequence(start, start + 3))
					.stringRepresentation();
			while (model.getAAMapping().get(codon) != '*') {

				start -= 3;
				codon = SequenceTools.reverseComplement(
						model.vlm.getSelectedEntry().sequence().subsequence(start, start + 3))
						.stringRepresentation();
			}
			rf.location()[0].setStart(start);

		}

	}
}
