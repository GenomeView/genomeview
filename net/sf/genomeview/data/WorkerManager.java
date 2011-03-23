/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.HashSet;
import java.util.Observable;

/**
 * Keeps track of running worker threads. Threads need to register themselves
 * and unregister when finished.
 * 
 * @author Thomas Abeel
 * 
 */
public class WorkerManager extends Observable{

	private HashSet<DataSourceWorker> running = new HashSet<DataSourceWorker>();

	
	public synchronized void start(DataSourceWorker ds) {
		running.add(ds);
		setChanged();
		notifyObservers();
	}

	public synchronized void done(DataSourceWorker ds) {
		running.remove(ds);
		setChanged();
		notifyObservers();
	}

	public synchronized  int runningJobs() {
		return running.size();
	}
}
