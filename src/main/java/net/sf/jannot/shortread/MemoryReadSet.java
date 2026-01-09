/**
 * %HEADER%
 */
package net.sf.jannot.shortread;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jannot.Data;
import net.sf.jannot.Location;
import net.sf.samtools.SAMRecord;
import be.abeel.util.FrequencyMap;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class MemoryReadSet extends ReadGroup {
	
	public String label(){
		return "Memory readset";
	}
	
	private SortedSet<SAMRecord> set = new TreeSet<SAMRecord>();
	private int maxPos = 0;

	private int maxLength = 0;

	/* Maps for paired reads, these will be empty for non-paired data sets */
	private HashMap<String, SAMRecord> first = new HashMap<String, SAMRecord>();
	private HashMap<String, SAMRecord> second = new HashMap<String, SAMRecord>();
	private int maxPairedLenght;

	private FrequencyMap fm = new FrequencyMap();

	private void addQuiet(SAMRecord g) {
		set.add(g);
		if (g.getAlignmentEnd() > maxPos)
			maxPos = g.getAlignmentEnd();
		if ((g.getAlignmentEnd()-g.getAlignmentStart()+1) > maxLength)
			maxLength = (g.getAlignmentEnd()-g.getAlignmentStart()+1);
		/* Keep track of paired reads */
//		if (g instanceof ShortReadTools) {
//			ShortReadTools esr = (ShortReadTools) g;
//			String name = esr.record().getReadName();
//			if (esr.isPaired() && esr.isFirstInPair()) {
//				first.put(name, esr);
//				if (second.containsKey(name)) {
//					int len = Math.max(first.get(name).end() - second.get(name).start() + 1, second.get(name).end()
//							- first.get(name).start() + 1);
//					fm.count(len);
//					if (len > maxPairedLenght)
//						maxPairedLenght = len;
//				}
//			}
//			if (esr.isPaired() && esr.isSecondInPair()) {
//
//				second.put(name, esr);
//				if (first.containsKey(name)) {
//					int len = Math.max(first.get(name).end() - second.get(name).start() + 1, second.get(name).end()
//							- first.get(name).start() + 1);
//					fm.count(len);
//					if (len > maxPairedLenght)
//						maxPairedLenght = len;
//				}
//
//			}
//		}

		// updatePileup(g);
	}

	// @Override
	public void add(SAMRecord g) {
		addQuiet(g);
		// setChanged();
		// notifyObservers();
	}

	// @Override
	public void addAll(Data<SAMRecord> t) {

		for (SAMRecord s : t.get()) {
			add(s);
		}

	}

	// @Override
	// public void addAll(Iterable<ShortRead> list) {
	// for (ShortRead sr : list) {
	// addQuiet(sr);
	// }
	// setChanged();
	// notifyObservers();
	// }

	// private void updatePileup(ShortRead sr) {
	// if (pileupForward.size() <= sr.end()) {
	// pileupForward.setSize(sr.end() + 1);
	// pileupReverse.setSize(sr.end() + 1);
	// }
	// for (int i = sr.start() - 1; i < sr.end(); i++) {
	// if (sr.strand() == Strand.FORWARD) {
	// int val = pileupForward.get(i) + 1;
	// pileupForward.set(i, val);
	//
	// } else {
	// int val = pileupReverse.get(i) + 1;
	// pileupReverse.set(i, val);
	//
	// }
	// int pile = pileupForward.get(i) + pileupReverse.get(i);
	// if (pile > maxPile)
	// maxPile = pile;
	// }
	//
	// }

	// @Override
	private Iterable<SAMRecord> get(Location l) {
		try {
			return set;//.subSet(ShortRead.getQuery(l.start() - maxLength), ShortRead.getQuery(l.end() + maxLength));
		} catch (ConcurrentModificationException e) {
			return new ArrayList<SAMRecord>();
		}
	}

	public int size() {
		return set.size();
	}

	// @Override
	// public List<ShortRead> getAll() {
	// return new ArrayList<ShortRead>(set);
	// }

	// @Override
	// public Iterator<ShortRead> iterator() {
	// return set.iterator();
	// }

//	@Override
//	protected int maxPos() {
//		return maxPos;
//	}

	@Override
	public int readLength() {
		return maxLength;
	}

	public void clear() {
		set.clear();
		maxLength = 0;
		maxPos = 0;
		// maxPile = 0;
		// setChanged();
		// notifyObservers();

	}

//	public ShortReadTools getSecond(ShortReadTools sr) {
//		return second.get(sr.record().getReadName());
//	}
//
//	public ShortReadTools getFirst(ShortReadTools sr) {
//		return first.get(sr.record().getReadName());
//	}

	public int getPairLength() {
		return maxPairedLenght;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Data#get(int, int)
	 */
	@Override
	public Iterable<SAMRecord> get(int start, int end) {
		return get(new Location(start, end));
	}

	@Override
	public Iterable<SAMRecord> get() {
		return set;
	}

	/* (non-Javadoc)
	 * @see net.sf.jannot.shortread.ReadGroup#getSecondRead(net.sf.samtools.SAMRecord)
	 */
	@Override
	public SAMRecord getSecondRead(SAMRecord one) {
		
		//XXX This is broken
		//FIXME  This is broken
		// TODO Auto-generated method stub
		return null;
	}
	
	/* (non-Javadoc)
	 * @see net.sf.jannot.shortread.ReadGroup#getSecondRead(net.sf.samtools.SAMRecord)
	 */
	@Override
	public SAMRecord getFirstRead(SAMRecord one) {
		
		//XXX This is broken
		//FIXME  This is broken
		// TODO Auto-generated method stub
		return null;
	}
}