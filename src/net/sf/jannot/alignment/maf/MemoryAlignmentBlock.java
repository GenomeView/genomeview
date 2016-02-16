/**
 * %HEADER%
 */
package net.sf.jannot.alignment.maf;

import java.util.ArrayList;
import java.util.Iterator;

import net.sf.jannot.Location;

/**
 * @author Thomas Abeel
 * 
 */
public class MemoryAlignmentBlock extends AbstractAlignmentBlock {

	public MemoryAlignmentBlock(int start, int end) {
		super(start, end);
	}

	private ArrayList<AbstractAlignmentSequence> list = new ArrayList<AbstractAlignmentSequence>();
	/**
	 * 
	 */
	private static final long serialVersionUID = -8021564812568122679L;

	@Override
	public void add(AbstractAlignmentSequence as) {
		// if (list.size() == 0) {
		// //this.loc = new Location(as.start(), as.end());
		// initPosition(as);
		// }
		list.add(as);
	}

	/**
	 * @param entry
	 */
	@Override
	public AbstractAlignmentSequence getAlignmentSequence(int i) {
		return list.get(i);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<AbstractAlignmentSequence> iterator() {
		return list.iterator();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.alignment.maf.AbstractAlignmentBlock#size()
	 */
	@Override
	public int size() {
		return list.size();
	}

}
