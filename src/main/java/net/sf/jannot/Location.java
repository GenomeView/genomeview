/**
 * %HEADER%
 */
package net.sf.jannot;

import net.sf.jannot.event.ChangeEvent;

public class Location implements Comparable<Location> {

	/**
	 * These fields are public for efficient getter access. If you want to set these fields, please use the proper setters.
	 */
	public int start, end;

	private boolean fuzzyEnd;

	private boolean fuzzyStart;

	/**
	 * Create a location from a String that comes out of the toString method
	 * 
	 * @param s
	 * @return
	 */
	public static Location fromString(String s){
		String[]arr=s.replace('<', ' ').replace('>', ' ').trim().split("..");
		return new Location(Integer.parseInt(arr[0]),Integer.parseInt(arr[1]),s.startsWith("<"),s.endsWith(">"));
	}
	
	@Override
	public String toString() {
		return (fuzzyStart ? "<" : "") + start + ".." + (fuzzyEnd ? ">" : "")
				+ end;
	}

	public Location(int start, int end, boolean fuzzyStart, boolean fuzzyEnd) {
		if (end > start) {
			this.start = start;
			this.end = end;
		} else {
			this.start = end;
			this.end = start;
		}
		this.fuzzyStart = fuzzyStart;
		this.fuzzyEnd = fuzzyEnd;
	}

	public Location(int x, int y) {
		this(x, y, false, false);
	}

	public final int start() {
		return start;
	}

	public final ChangeEvent setStart(int start) {
		ChangeEvent e = new SetStartEvent(this, this.start, start);
		e.doChange();
		return e;
	}

	public final int end() {
		return end;
	}

	public final ChangeEvent setEnd(int end) {
		ChangeEvent e = new SetEndEvent(this, this.end, end);
		e.doChange();
		return e;
	}

	@Override
	public int compareTo(Location arg0) {
		int comp = new Integer(start).compareTo(arg0.start());
		if (comp == 0)
			comp = new Integer(this.end).compareTo(arg0.end());
		
		return comp;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + end;
		result = prime * result + (fuzzyEnd ? 1231 : 1237);
		result = prime * result + (fuzzyStart ? 1231 : 1237);
		result = prime * result + start;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Location other = (Location) obj;
		if (end != other.end)
			return false;
		if (fuzzyEnd != other.fuzzyEnd)
			return false;
		if (fuzzyStart != other.fuzzyStart)
			return false;
		if (start != other.start)
			return false;
		return true;
	}

	public int length() {
		return end - start + 1;
	}

	public boolean overlaps(int lStart,int lEnd) {
		if (start >= lStart && start <= lEnd)
			return true;
		if (end >= lStart && end <= lEnd)
			return true;
		if (lStart >= start && lEnd <= end) {
			return true;
		}
		return false;
	}

	/*
	 * A location can belong to a feature, but the feature is responsible for
	 * notifying the location that it belongs to that feature
	 * 
	 * This change is not recorded using a ChangeEvent.
	 */
	private Feature parent = null;

	void setParent(Feature f) {
		this.parent = f;
	}

	public Feature getParent() {
		return parent;
	}

	class SetEndEvent implements ChangeEvent {
		/* New position */
		private int to;

		/* Original position */
		private int from;

		private Location l;

		public SetEndEvent(Location l, int orig, int newpos) {
			this.l = l;
			this.from = orig;
			this.to = newpos;
		}

		@Override
		public void doChange() {
			l.end = to;
			if (parent != null)
				parent.updatePhase();

		}

		@Override
		public void undoChange() {
			assert (l.end == to);
			l.end = from;
			if (parent != null)
				parent.updatePhase();

		}

		@Override
		public String toString() {
			return new String("Set end from " + from + " to " + to);
		}
	}

	class SetStartEvent implements ChangeEvent {

		private int to;

		private int from;

		private Location l;

		public SetStartEvent(Location l, int originalPosition, int newPosition) {
			this.l = l;
			this.from = originalPosition;
			this.to = newPosition;
		}

		@Override
		public void doChange() {
			l.start = to;
			if (parent != null)
				parent.updatePhase();

		}

		@Override
		public void undoChange() {
			assert (l.start == to);
			l.start = from;
			if (parent != null)
				parent.updatePhase();
		}

		@Override
		public String toString() {
			return new String("Set start from " + from + " to " + to);
		}
	}

	public Location copy() {
		return new Location(start, end, fuzzyStart, fuzzyEnd);
	}
}
