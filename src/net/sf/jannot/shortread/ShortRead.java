/**
 * %HEADER%
 */
package net.sf.jannot.shortread;

import net.sf.jannot.Strand;
/**
 * 
 * @author Thomas Abeel
 *
 */
public abstract class ShortRead implements Comparable<ShortRead> {

	public static ShortRead getQuery(int pos) {
		return new BasicShortRead(new byte[0], pos, true);
	}

	/*
	 * These should be one-based coordinates
	 */
	public abstract char getNucleotide(int pos);

	@Override
	public int compareTo(ShortRead o) {
		if (this == o)
			return 0;
		int comp = new Integer(this.start()).compareTo(new Integer(o.start()));
		if (comp == 0)
			comp++;
		return comp;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}

	public abstract int start();

	public abstract int end();

	public abstract Strand strand();

	public abstract int length();

	// @Override
	// public String toString() {
	//
	// }

	public boolean isFirstInPair() {
		return true;
	}

	

	public boolean isPaired() {
		return false;
	}
}
