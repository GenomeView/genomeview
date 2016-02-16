/**
 * %HEADER%
 */

package net.sf.jannot.alignment;

import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;

/**
 * Sequence that can be used as reference for multiple alignments.
 * 
 * @author tabeel
 * 
 */
public class ReferenceSequence extends MemorySequence{
	private int refGapCount;
	private int[] mapping = null;

	public ReferenceSequence(StringBuffer sequence) {
		super(sequence);
		calculateMapping();
	}
	
	public ReferenceSequence(MemorySequence sequence) {
		super(sequence);
		calculateMapping();
	}

	

//	private int countGaps() {
//		int gaps = 0;
//		for (int i = 1; i <= this.size(); i++) {
//			if (this.getNucleotide(i) == '-')
//				gaps++;
//
//		}
//		return gaps;
//	}

	private void calculateMapping() {
		mapping = new int[this.size()];
		refGapCount=0;
		int index = 0;
		int pos = 0;
		for (int i = 0; i < this.size(); i++) {

			pos++;
			if (this.getNucleotide(i + 1) != '-') {
				mapping[index++] = pos;
			}else{
				refGapCount++;
			}

		}
		for (int i = index; i < this.size(); i++) {
			mapping[i] = pos + 1;
		}
//		refGapCount = countGaps();

	}

	@Override
	public void setSequence(String sequence) {
		super.setSequence(sequence);
		calculateMapping();
	}

	@Override
	public void addSequence(String seq) {
		super.addSequence(seq);
		calculateMapping();
	}

	/**
	 * Returns the alignment position if you know the position in the reference
	 * sequence. Suppose you have the following reference sequence
	 * 
	 * <pre>
	 * AAAATTTT
	 * </pre>
	 * 
	 * The corresponding alignment to some other sequence of this reference is
	 * 
	 * <pre>
	 * AAAA----TTTT
	 * </pre>
	 * 
	 * Then this method allows you the query which position in the original
	 * reference sequence corresponds to which position in the reference
	 * alignment.
	 * 
	 * For example 5 in the reference sequence maps to 9 in the reference
	 * alignment. And 4 maps to 4.
	 * 
	 * Both coordinates are 1 based!
	 * 
	 */
	public int ref2aln(int position) {
		if(position>mapping.length){
			return mapping[mapping.length-1];
//			throw new IllegalArgumentException((position-1)+"\t"+mapping.length);
		}
		if (mapping[position - 1] == 0)
			System.err.println("ref2aln: " + (position - 1) + "\t" + mapping[position - 1]);
		return mapping[position - 1];
	}

	public int getRefGapCount() {
		return refGapCount;

	}

}