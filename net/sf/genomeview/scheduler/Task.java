package net.sf.genomeview.scheduler;

public abstract class Task implements Runnable, Comparable<Task> {

	static long IDCOUNTER = Long.MAX_VALUE;
	long id = IDCOUNTER--;

	public int compareTo(Task t) {
		if (t.id == id)
			return 0;
		if (id > t.id)
			return 1;
		else
			return -1;
	}
}