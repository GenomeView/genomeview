/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.event.FeatureEvent;

/**
 * 
 * Features are set of characteristics which describe a genome. Lines of a .gff
 * file are example of feature. e.g. start and end position of a coding region.
 * 
 * TODO improve this documentation
 * 
 * @author Thomas Abeel
 */
public class Feature implements Comparable<Feature>, Located {

	private Logger log = Logger.getLogger(Feature.class.getCanonicalName());
	private Location[] location = null;

	private Location singleLocation = null;

	private byte[] phase = null;

	private Type type;

	private Strand strand = Strand.UNKNOWN;

	private Map<String, String> qualifiers = new HashMap<String, String>();

	/**
	 * Add a new qualifier to this Feature.
	 * 
	 * The value should not contain line-breaks.
	 * 
	 * @param key
	 *            of the qualifier
	 * @param value
	 *            value
	 */
	public void addQualifier(String key, String value) {
		if (value != null) {
			assert key != null;
			key = key.intern();

		}
		/* Remove line breaks in value */
		if (value != null)
			value = value.replaceAll("\n", "");

		if (!key.equals("score")&&qualifiers.containsKey(key))
			qualifiers.put(key, qualifiers.get(key) + "," + value);
		else
			qualifiers.put(key, value);

		if (key.equals("score"))
			scoreBuffer = false;

	}

	public void setQualifier(String key, String value) {
		if(key!=null)
			qualifiers.remove(key);
		addQualifier(key, value);
		
	
	}

	@Override
	public boolean equals(Object f) {
		return this == f;
	}

	public Type type() {
		return type;

	}

	public void setLocation(Collection<Location> tmp) {
		if (tmp.size() == 1) {
			setLocation(tmp.iterator().next());
		} else {
			singleLocation = null;
			SortedSet<Location> set = new TreeSet<Location>();
			for (Location l : tmp)
				set.add(l);
			location = new Location[set.size()];
			int idx = 0;
			for (Location l : set) {
				location[idx++] = l;

			}
			for (Location x : this.location) {
				x.setParent(this);
			}
			updatePhase();
		}
	}

	public void setLocation(Location l) {
		location = null;
		phase = null;
		singleLocation = l;
		fStart = l.start();
		fEnd = l.end();
		singleLocation.setParent(this);
	}

	public void setLocation(Location[] l) {
		if (l.length == 1)
			setLocation(l[0]);
		else {
			singleLocation = null;
			this.location = l;
			for (Location tmp : this.location) {
				tmp.setParent(this);
			}

			updatePhase();

		}

	}

	public boolean overlaps(Location otherLoc) {
		return otherLoc.overlaps(fStart, fEnd);
	}

	public boolean overlaps(Feature otherFeat) {
		Location thisLoc = new Location(fStart, fEnd);
		return thisLoc.overlaps(otherFeat.fStart, otherFeat.fEnd);
	}

	public ChangeEvent setStrand(Strand s) {
		ChangeEvent ce = new ChangeStrandEvent(this, this.strand, s);
		ce.doChange();
		if (location != null) {
			updatePhase();
		}
		return ce;

	}

	class ChangeTypeEvent extends FeatureEvent {
		private Type prev, next;

		public final Type getPrev() {
			return prev;
		}

		public final Type getNext() {
			return next;
		}

		public ChangeTypeEvent(Feature f, Type prev, Type next) {
			super(f, "set type to " + next);
			this.next = next;
			this.prev = prev;
		}

		@Override
		public void doChange() {
			type = next;

		}

		@Override
		public void undoChange() {
			assert (type == next);
			type = prev;

		}

	}

	public ChangeEvent setType(Type type) {
		ChangeEvent ce = new ChangeTypeEvent(this, this.type, type);
		ce.doChange();
		return ce;

	}

	private int fStart = -1;
	private int fEnd = -1;

	// private Location fLocation = new Location(-1, -1);

	/**
	 * Phase is not the same thing as Frame. Phase is the number of bases to
	 * skip before reading in-frame, while frame is the actual frame identifier
	 * beginning at 1.
	 */
	void updatePhase() {
		if (singleLocation == null && location == null)
			return;

		Location[] tmpLoc = location();

		phase = new byte[tmpLoc.length];

		int fStart = Integer.MAX_VALUE;
		int fEnd = 0;

		for (Location l : tmpLoc) {
			if (l.start() < fStart)
				fStart = l.start();
			if (l.end() > fEnd)
				fEnd = l.end();
		}
		this.fStart = fStart;
		this.fEnd = fEnd;

		int currentPhase = 0;
		if (strand == Strand.FORWARD) {
			for (int i = 0; i < tmpLoc.length; i++) {
				phase[i] = (byte) currentPhase;
				// phase.put(location[i], currentPhase);
				currentPhase = (tmpLoc[i].length() - currentPhase);
				currentPhase %= 3;
				currentPhase = 3 - currentPhase;
				currentPhase %= 3;
			}
		} else if (strand == Strand.REVERSE) {
			for (int i = tmpLoc.length - 1; i >= 0; i--) {
				// phase.put(tmpLoc.get(i), currentPhase);
				phase[i] = (byte) currentPhase;
				currentPhase = (tmpLoc[i].length() - currentPhase);
				currentPhase %= 3;
				currentPhase = 3 - currentPhase;
				currentPhase %= 3;
			}
		} else {
			for (int i = 0; i < tmpLoc.length; i++)
				phase[i] = 0;
			// phase.put(location.get(i), 0);
		}

	}

	public Location[] location() {
		if (location == null) {
			assert singleLocation != null;
			if(singleLocation==null)
				return new Location[0];
			else 
				return new Location[] { singleLocation };
		} else
			return location;

	}

	public int length() {
		return fEnd - fStart + 1;
	}

	@Override
	public int compareTo(Feature o) {

		int comp = new Integer(fStart).compareTo(o.fStart);
		if (comp == 0)
			comp = new Integer(fEnd).compareTo(o.fEnd);

		if (comp == 0)
			return new Integer(hashCode()).compareTo(o.hashCode());
		else
			return comp;
	}

	public Strand strand() {
		return strand;
	}

	public void removeQualifier(String key){
		qualifiers.remove(key);
	}
	
	public String qualifier(String key) {
		return qualifiers.get(key);

	}

	public Set<String> getQualifiersKeys() {
		return qualifiers.keySet();
	}

	/**
	 * Creates a deep copy of this feature.
	 * 
	 * @return
	 */
	public Feature copy() {
		Feature out = new Feature();
		SortedSet<Location> loc = new TreeSet<Location>();
		for (Location l : this.location()) {
			loc.add(l.copy());
		}
		out.setLocation(loc);
		out.setStrand(this.strand());

		for (String key : qualifiers.keySet())
			out.addQualifier(key, qualifiers.get(key));
		out.type = this.type();
		return out;
	}

	// private double bufferedScore = Double.NaN;
	@Deprecated
	public void setScore(double score) {
		// if(score!=bufferedScore){
		setQualifier("score", "" + score);
		scoreBuffer=false;
		// bufferedScore=score;
		// }
		// this.score = score;
	
	}

	private boolean scoreBuffer = false;
	private double score = Double.NaN;

	public double getScore() {
		if (scoreBuffer) {
			return score;
		} else {
			String val = qualifier("score");
			if (val == null)
				return 0;
			else {
				scoreBuffer = true;
				double tmpScore = 0;
				try {
					tmpScore = Double.parseDouble(val);
				} catch (Exception e) {
					log.log(Level.WARNING, "Could not parse score: " + val, e);
				}
				score=tmpScore;
				return score;
			}
		}
	}

	@Override
	public String toString() {
		if (type != null)
			return type.toString() + " [" + new Location(fStart, fEnd) + "]";
		else
			return "[" + new Location(fStart, fEnd).toString() + "]";
	}

	public int getFrame() {
		int frame;
		if (location == null) {
			if (strand == Strand.REVERSE)
				frame = fEnd % 3;
			else
				frame = fStart % 3;
		} else {
			if (strand == Strand.REVERSE)
				frame = (location[location.length - 1].end()) % 3;
			else
				frame = (location[0].start()) % 3;
		}
		return frame == 0 ? 3 : frame;

	}

	public int getPhase(int idx) {
		if (location == null)
			return 0;
		else
			return phase[idx];

	}

	class ChangeStrandEvent extends FeatureEvent {
		private Strand from, to;

		public ChangeStrandEvent(Feature f, Strand from, Strand to) {
			super(f, "Change strand from " + from.symbol() + " to " + to.symbol());
			this.from = from;
			this.to = to;
		}

		@Override
		public void doChange() {
			super.getFeature().strand = to;

		}

		@Override
		public void undoChange() {
			super.getFeature().strand = from;

		}

	}

	/**
	 * @return
	 */
	public String getColor() {
		String notes = this.qualifier("colour");
		if (notes == null)
			notes = this.qualifier("color");
		return notes;
	}

	/**
	 * 
	 */
	public void clearQualifiers() {
		qualifiers.clear();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Located#start()
	 */
	@Override
	public int start() {
		return fStart;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.sf.jannot.Located#end()
	 */
	@Override
	public int end() {
		return fEnd;
	}

	/**
	 * @param l
	 */
	public void addLocation(Location l) {
		List<Location> arr = new ArrayList<Location>();
		if (singleLocation != null)
			arr.add(singleLocation);
		if (location != null) {
			for (Location ll : location)
				arr.add(ll);
		}
		arr.add(l);
		setLocation(arr);
	}

	/**
	 * @param rf
	 */
	public void removeLocation(Location rf) {
		List<Location> arr = new ArrayList<Location>();
		if (singleLocation != null)
			throw new RuntimeException("Can not remove the last location!!!");
		if (location != null) {
			for (Location ll : location)
				if (!ll.equals(rf))
					arr.add(ll);
		}
		setLocation(arr);

	}

}
