/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.nameservice.NameService;

/**
 * Each Entry contains information about a specific genome/chromossomes or any
 * chunck of DNA.
 * 
 * With the chunk we associate a sequence, feature annotation, a description,
 * graph annotation and alignment annotation.
 * 
 * In order to load data to an Entry set, you should use the class Filesource.
 * 
 * e.g.: EntrySet set = new FileSource(new File("sequence.fasta")).read();
 * 
 * will read a sequence from a fasta to Entryset set.
 * 
 * new FileSource(new File("sequence.gff")).read(set);
 * 
 * will read notations from the .gff file to Entryset set.
 * 
 * 
 * 
 * 
 * @author Thomas Abeel
 * 
 */
public class Entry implements Comparable<Entry>, Iterable<DataKey> {

//	static final NameService ns=new NameService();
	
	private static final StringKey seqKey = new StringKey("SEQ*(^#%(@#%)@#^@#^))^)@#)^(@#%^*()SEQ");

	private static final Logger log=Logger.getLogger(Entry.class.getCanonicalName());
	
	final public Description description = new Description();

	private Map<DataKey, Data<?>> data = new HashMap<DataKey, Data<?>>();

	/**
	 * Returns the highest position for which there is data
	 * 
	 * @return
	 */
	public int getMaximumLength() {
		// FIXME think of a more efficient way
		long maxSize = 0;
		for (DataKey dk : this) {
			Data<?> newData = data.get(dk);
			/* Update maximum size if applicable */
			long s = 0;
			if (newData instanceof Sequence)
				s = ((Sequence) newData).size();

			if (newData instanceof FeatureAnnotation)
				s = ((FeatureAnnotation) newData).getMaximumCoordinate();

			// System.out.println("s update: " + s);
			if (s > maxSize)
				maxSize = s;
		}
		return (int) maxSize;
	}

	// public AlignmentAnnotation align = null;

	public void add(DataKey key, Data<?> newData) {
		
		if (!data.containsKey(key)) {
			data.put(key, newData);
			// if (newData instanceof AlignmentAnnotation)
			// align = (AlignmentAnnotation) newData;
		} else {
			// FIXME implement for feature data */
			log.severe("Already here, you lose!!!");

		}
		
	}

	/**
	 * @param dataKey
	 * @return
	 */
	public Data<?> get(DataKey dataKey) {
		return data.get(dataKey);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<DataKey> iterator() {
		return data.keySet().iterator();
	}

	/**
	 * @return
	 */
	public Iterable<ReadGroup> shortReads() {
		ArrayList<ReadGroup> out = new ArrayList<ReadGroup>();
		for (DataKey key : data.keySet()) {
			Data<?> x = data.get(key);
			if (x instanceof ReadGroup)
				out.add((ReadGroup) x);
		}
		return out;
	}

	/**
	 * @param type
	 * @return
	 */
	public MemoryFeatureAnnotation getMemoryAnnotation(DataKey type) {
		if (!data.containsKey(type))
			this.add(type, new MemoryFeatureAnnotation());
		Data<?> tmp = this.get(type);
		if (tmp instanceof MemoryFeatureAnnotation)
			return (MemoryFeatureAnnotation) tmp;
		else
			return null;

	}

	private final String id;

	public Sequence sequence() {
		if (!data.containsKey(seqKey)) {
			data.put(seqKey, new MemorySequence());
		}
		return (Sequence) data.get(seqKey);
	}

	public Entry(String id) {
		id=NameService.getPrimaryName(id);
		if (id == null)
			throw new RuntimeException("Should never be null!");
		this.id = id;

		
	}	

	@Override
	public String toString() {
		return id;
	}

	/**
	 * Shortcut to access the id
	 * 
	 * @return
	 */
	public String getID() {
		return id;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Entry o) {
		return this.getID().compareTo(o.getID());
	}

	/**
	 * @param dataKey
	 * @return
	 */
	public boolean contains(DataKey dataKey) {
		return data.containsKey(dataKey);
	}

	public void setSequence(Sequence seq) {
		data.put(seqKey, seq);
	}

	/**
	 * @param dataKey
	 */
	public void remove(DataKey dataKey) {
		data.remove(dataKey);

	}

}
