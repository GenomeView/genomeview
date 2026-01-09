/**
 * %HEADER%
 */
package net.sf.jannot.alignment.maf;

import net.sf.jannot.Located;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.refseq.Sequence;

/**
 * @author Thomas Abeel
 * 
 */
public abstract class AbstractAlignmentBlock implements Comparable<AbstractAlignmentBlock>, Located,
		Iterable<AbstractAlignmentSequence> {
	/* Buffers for finding nucleotides */
	// protected int[] position = null;
	// private Entry ref = null;
	private Location loc = new Location(0, 0);

	public AbstractAlignmentBlock(int start, int end) {
		this.loc = new Location(start, end);
	}

	// public void setRef(Entry entry) {
	// this.ref = entry;
	// update(entry);
	// }

	// /**
	// * @param reference
	// */
	// private void update(Entry reference) {
	// this.ref = reference;
	//
	// for (AlignmentSequence as : this) {
	// System.out.println(as);
	// if (as.entry() == ref) {
	//
	// return;
	// }
	// }
	//
	// }

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(AbstractAlignmentBlock o) {
		int thisVal = this.hashCode();
		int anotherVal = o.hashCode();
		return (thisVal < anotherVal ? -1 : (thisVal == anotherVal ? 0 : 1));
	}

	//
	// /*
	// * (non-Javadoc)
	// *
	// * @see net.sf.jannot.Located#getLocation()
	// */
	// @Override
	// public Location getLocation() {
	// return loc;
	// }
	@Override
	public int start() {
		return loc.start;
	}

	@Override
	public int end() {
		return loc.end;
	}

	public int length() {
		return loc.length();
	}

	/**
	 * @param as
	 * @return
	 */
	public abstract void add(AbstractAlignmentSequence as);

	/**
	 * @param i
	 * @return
	 */
	public abstract AbstractAlignmentSequence getAlignmentSequence(int i);

	/**
	 * @return
	 */
	public abstract int size();

}
