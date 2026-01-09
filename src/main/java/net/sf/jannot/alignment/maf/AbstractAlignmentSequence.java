/**
 * %HEADER%
 */
package net.sf.jannot.alignment.maf;

import net.sf.jannot.Strand;
import net.sf.jannot.refseq.Sequence;

/**
 * @author Thomas Abeel
 * 
 */
public abstract class AbstractAlignmentSequence {

	/**
	 * @param id2
	 * @param start
	 *            the real start, not some weird UCSC start, this is ZERO based
	 * @param noNucleotides2
	 * @param srcSize
	 * @param fromSymbol
	 */
	public AbstractAlignmentSequence(String id, int start, int noNucleotides, Strand fromSymbol) {
		this.id = id;
		this.start = start;
		this.noNucleotides = noNucleotides;
		this.strand = fromSymbol;
	}

	protected String id;

	@Override
	public String toString() {
		return id + " " + start() + " " + end();
	}

	// private Entry ref;
	protected int start;
	protected int noNucleotides;
	protected Strand strand;

	public abstract Sequence seq();

	/**
	 * @return
	 */
	public int end() {
		return start + noNucleotides + 1;
	}

	/**
	 * One based coordinate of this multiple alignment block
	 * 
	 * @return
	 */
	public int start() {
		return start + 1;
	}

	public String getName() {
		return id;
	}

	// /**
	// * @return
	// */
	// public Entry entry() {
	// return ref;
	// }

	/**
	 * @return
	 */
	public Strand strand() {
		return strand;
	}

}
