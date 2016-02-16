/**
 * %HEADER%
 */
package net.sf.jannot.alignment.mfa;

import net.sf.jannot.Data;
import net.sf.jannot.MemoryListData;
import be.abeel.util.CountMap;

public class AlignmentAnnotation extends MemoryListData<Alignment> implements Data<Alignment>  {

	public String label(){
		return "Multiple alignment";
	}
	
	private static final long serialVersionUID = -5188981624665479856L;
//	private MemoryListData<Alignment> dataList = new MemoryListData<Alignment>();
	private byte[][] conservation;

	public void addAll(Iterable<Alignment> align) {
		for (Alignment a : align) {
			super.add(a);
		}
		calculateConservation();
	}

	/**
	 * Conservation is calculated on the species where the nucleotide is
	 * present.
	 */
	private void calculateConservation() {
		conservation = new byte[4][super.get(0).refLength()];
		CountMap<Character> cm = new CountMap<Character>();
		for (int i = 0; i < conservation[0].length; i++) {
			cm.clear();
			for (Alignment a : this) {
				char nt = Character.toLowerCase(a.getNucleotide(i + 1));
				if (nt != '-')
					cm.count(nt);
			}
			conservation[0][i] = cm.get('a').byteValue();
			conservation[1][i] = cm.get('c').byteValue();
			conservation[2][i] = cm.get('g').byteValue();
			conservation[3][i] = cm.get('t').byteValue();

		}

	}

	public int numAlignments() {
		return super.size();
	}

//	public Alignment getAlignment(int i) {
//		if (dataList.size() == 0)// This alignment is not loaded
//			return null;
//		return dataList.get(i);
//	}

	public int getNucleotideCount(char nt, int position) {
		if (conservation == null)
			return 0;
		switch (Character.toLowerCase(nt)) {
		case 'a':
			return conservation[0][position - 1];
		case 'c':
			return conservation[1][position - 1];
		case 'g':
			return conservation[2][position - 1];
		case 't':
			return conservation[3][position - 1];

		}
		return -1;

	}

	/**
	 * Position in the reference
	 * 
	 * This coordinate is one based
	 * 
	 * @param position
	 * @return
	 */
	public double getConservation(int position) {
		if (conservation == null)// No data loaded in this multiple alignment
			return 0;
		if (position <= conservation[0].length && position > 0) {

			double max = conservation[0][position - 1];
			double sum = max;
			for (int i = 1; i < conservation.length; i++) {
				sum += conservation[i][position - 1];
				if (conservation[i][position - 1] > max)
					max = conservation[i][position - 1];
			}
			return max / sum;
		} else
			return 0;

	}

	public double getFootprint(int position) {
		if (conservation == null)
			return 0;
		if (position <= conservation[0].length && position > 0) {

			double sum = 0;
			for (int i = 0; i < conservation.length; i++) {
				sum += conservation[i][position - 1];

			}
			return sum / numAlignments();
		} else
			return 0;
	}

	@Override
	public Iterable<Alignment> get(int start, int end) {
		//It doesn't make sense for this data implementation to be queried by range
		return get();
	}

	@Override
	public Iterable<Alignment> get() {
		return super.get();
	}

	@Override
	public boolean canSave() {

		return false;
	}

}
