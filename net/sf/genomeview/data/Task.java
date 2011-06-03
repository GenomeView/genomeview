package net.sf.genomeview.data;

import net.sf.jannot.Location;

public abstract class Task implements Runnable, Comparable<Task> {

	// static long IDCOUNTER = 0;
	long id = 0;
	private Location location;

	public Task(Location l) {
		this.location = l;
	}

	public Location getLocation() {
		return location;
	}

	public void boost() {
		id++;
	}

	public int compareTo(Task t) {
		if (t.id == id)
			return 0;
		if (id > t.id)
			return -1;
		else
			return 1;
	}

	/**
	 * Try to cancel this task. 
	 * 
	 * Default implementation will not cancel the task, but reduce it's priority to zero.
	 * 
	 */
	public void cancel() {
		id=0;
		

	}
}