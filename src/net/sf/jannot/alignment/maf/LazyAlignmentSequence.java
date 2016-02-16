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
public class LazyAlignmentSequence extends AbstractAlignmentSequence {

	private LazyAlignmentBlock parent;

	/**
	 * @param selectedChrom
	 * @param nucStart
	 * @param alignmentLength
	 * @param strand2
	 */
	public LazyAlignmentSequence(String id, int nucStart, int alignmentLength, Strand strand, LazyAlignmentBlock parent) {
		super(id, nucStart, alignmentLength, strand);
		this.parent = parent;
	}

	/* This field needs to be set by the lazy load method of the parent */
	private Sequence seq = null;

	@Override
	public Sequence seq() {
		if (seq == null) {
			parent.lazyLoad();

		}

		return seq;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.alignment.maf.AbstractAlignmentSequence#end()
	 */
	@Override
	public int end() {
		if (seq == null) {
			parent.lazyLoad();
		}
		return super.end();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.alignment.maf.AbstractAlignmentSequence#start()
	 */
	@Override
	public int start() {
		if (seq == null)
			parent.lazyLoad();
		return super.start();
	}

	/**
	 * @param seq2
	 */
	public void setSeq(Sequence seq) {
		this.seq = seq;

	}

}
