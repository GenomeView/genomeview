/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

/**
 * 
 * @author Thomas Abeel
 *
 */
class NucCounter {
	int[][] counter;

	private boolean didCount = false;

	public NucCounter(int len) {
		counter = new int[6][len];
	}

	public boolean hasData() {
		return counter != null && didCount;
	}

	/**
	 * 
	 * @param nuc a char 'a','c','g','t' etc
	 * @param pos the position index
	 * @return totals for this
	 * @throws IndexOutOfBoundsException if nuc or pos not in range
	 */
	public int getCount(char nuc, int pos) {
		return counter[ix(nuc)][pos];
	}

	public int getTotalCount(int pos) {
		if (pos < 0 || pos >= counter[0].length) {
			return -1;

		}
		return counter[0][pos] + counter[1][pos] + counter[2][pos]
				+ counter[3][pos] + counter[5][pos];
	}

	private int ix(char nuc) {
		switch (nuc) {
		case 'a':
		case 'A':
			return 0;
		case 'T':
		case 't':
			return 1;
		case 'c':
		case 'C':
			return 2;
		case 'g':
		case 'G':
			return 3;
		case 'n':
		case 'N':
			return 4;
		case '.':
		case ',':
			return 5;
		default:
			return -1;
		}
	}

	public void count(char nuc, int pos) {
		didCount = true;
		int ix = ix(nuc);
		if (ix >= 0 && pos >= 0 && pos < counter[0].length) {
			counter[ix][pos]++;
		}
	}
}