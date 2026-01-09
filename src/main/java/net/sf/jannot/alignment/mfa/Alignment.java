/**
 * %HEADER%
 */
package net.sf.jannot.alignment.mfa;

import java.util.BitSet;

import net.sf.jannot.Located;
import net.sf.jannot.alignment.ReferenceSequence;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;

public class Alignment implements Located {

	private String name;
	private MemorySequence alignment;
	private ReferenceSequence reference;

	/**
	 * Returns the expanded query sequence including gaps
	 * 
	 * @return expanded query sequence
	 */
	public Sequence getExpandedQuerySequence() {
		return alignment;
	}

	/**
	 * Returns the expanded reference sequence including gaps.
	 * 
	 * @return expanded reference sequence
	 */
	public Sequence getExpandedReferenceSequence() {
		return reference;
	}

	public Alignment(String name, MemorySequence sequence, ReferenceSequence reference) {
		this.name = name;
		this.alignment = sequence;
		this.reference = reference;
		aligned = new BitSet();

		/* Only positions in reference sequence are cached */
		for (int i = 1; i < this.refLength(); i++) {
			char inf = getNucleotide(i);
			char ref = getReferenceNucleotide(i);
			if (inf != '-' && ref != '-' && Character.toLowerCase(inf) == Character.toLowerCase(ref))
				aligned.set(i);
		}

	}

	/**
	 * Returns the name of this alignment
	 * 
	 * @return
	 */
	public String name() {
		return name;
	}

	/**
	 * Gives the nucleotide that appears at the given position in the alignment.
	 * The coordinates are in the expanded reference sequence.
	 * 
	 * @param pos
	 *            position the get nucleotide
	 * @return nucleotide at provided position
	 */
	public char getNucleotide(int pos) {
		return alignment.getNucleotide(reference.ref2aln(pos));
	}

	/**
	 * Gives the nucleotide that appears at the given position in the reference
	 * sequence. The coordinates are in the expanded reference sequence.
	 * 
	 * @param pos
	 *            position the get nucleotide
	 * @return nucleotide at provided position
	 */
	public char getReferenceNucleotide(int pos) {
		return reference.getNucleotide(reference.ref2aln(pos));
	}

	private BitSet aligned;

	/**
	 * Returns whether the supplied position is aligned. Two sequences are
	 * aligned in a position if the share the same nucleotide.
	 * 
	 * @param pos
	 * @return
	 */
	public boolean isAligned(int pos) {
		return aligned.get(pos);
	}

	/**
	 * Returns the length of the reference sequence. This is the length of the
	 * reference alignment minus all gaps.
	 * 
	 * @return
	 */
	public int refLength() {
		return reference.size() - reference.getRefGapCount();
	}

	/*
	 * Returns true if there is extra data between this position and the next
	 * one.
	 */
	public int sizeGapAfter(int i) {
		return reference.ref2aln(i + 1) - reference.ref2aln(i) - 1;

	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int start() {
		return 1;

	}

	@Override
	public int end() {
		return refLength();
	}

}
