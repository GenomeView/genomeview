/**
 * %HEADER%
 */
package net.sf.jannot.alignment.maf;

import net.sf.jannot.Strand;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

/**
 * @author Thomas Abeel
 * 
 */
public class MemoryAlignmentSequence extends AbstractAlignmentSequence {
	private Sequence seq;

	/**
	 * @param e
	 * @param parseInt
	 * @param fromSymbol
	 * @param string
	 */
	public MemoryAlignmentSequence(String id, int start, int noNucleotides, int srcSize, Strand fromSymbol, Sequence seq) {
		super(id,fromSymbol == Strand.FORWARD?start: srcSize - start - noNucleotides,noNucleotides,fromSymbol);
		/* Overwrite start as well!! */
		if (fromSymbol == Strand.FORWARD) {
			this.seq = seq;
		} else {
			this.seq = SequenceTools.reverseComplement(seq);
		}
		this.noNucleotides = noNucleotides;
		this.strand = fromSymbol;

	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.alignment.maf.AbstractAlignmentSequence#seq()
	 */
	@Override
	public Sequence seq() {
		return seq;
	}
}
