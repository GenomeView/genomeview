/**
 * %HEADER%
 */
package net.sf.jannot.tabix;

/**
 * Pair of longs
 * 
 * @author Thomas Abeel, Thomas Van Parys
 * 
 */
public class Pair64 implements Comparable<Pair64>{
	/**
	 * Pair value
	 */
	public long start, end;

	public Pair64(long u, long v) {
		this.start = u;
		this.end = v;
	}

	@Override
	public int compareTo(Pair64 o) {
		long a = this.start;
		long b = o.start;
		if (a < b)
			return -1;
		else if (a == b)
			return 0;
		else
			return 1;
	}

	@Override
	public String toString() {
		final long compressedOffset = start >> 16;
		final int uncompressedOffset = (int) (start & 0xFFFF);
		final long compressedOffsetE = end >> 16;
		final int uncompressedOffsetE = (int) (end & 0xFFFF);
		return compressedOffset + "," + uncompressedOffset + "\t" + compressedOffsetE + "," + uncompressedOffsetE;
	}

}

