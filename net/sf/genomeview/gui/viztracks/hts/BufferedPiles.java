/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.util.HashMap;
import java.util.List;

import net.sf.genomeview.data.provider.Status;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.pileup.Pile;

/**
 * Buffer class for pile up stuff that does not support buffering natively.
 * 
 * @author Thomas Abeel
 * 
 */
class BufferedPiles /*implements Data<Pile> */{
//	private static Entry currentEntry = null;
//	private static HashMap<DataKey, BufferedPiles> source = new HashMap<DataKey, BufferedPiles>();
//	private Data<Pile> data;
//
//	public BufferedPiles(Data<Pile> data) {
//		this.data = data;
//
//	}
//
//	public List<Status> status(int start, int end) {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	/**
//	 * Has to return immediately, but can miss some data
//	 */
//	@Override
//	public Iterable<Pile> get(int start, int end) {
//		return data.get(start, end);
//	}
//
//	@Override
//	public Iterable<Pile> get() {
//		return data.get();
//	}
//
//	@Override
//	public boolean canSave() {
//		return false;
//	}
//
//	/**
//	 * Can return null if this entry does not exists, or that entry does not
//	 * have the associated data key.
//	 * 
//	 * @param entry
//	 * @param dataKey
//	 * @return
//	 */
//	public static BufferedPiles get(Entry entry, DataKey dataKey) {
//		if (entry != currentEntry) {
//			source.clear();
//			currentEntry = entry;
//
//		}
//		if (!source.containsKey(dataKey)) {
//			if (entry.get(dataKey) != null)
//				source.put(dataKey, new BufferedPiles((Data<Pile>) entry.get(dataKey)));
//
//		}
//		return source.get(dataKey);
//	}

}