/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class Status {
	private int end;
	private int start;
	private boolean ready;
	private boolean queued;
	private boolean running;

	public Status(boolean running, boolean queued, boolean ready, int i, int j) {
		this.running = running;
		this.queued = queued;
		this.ready = ready;
		this.start = i;
		this.end = j;
	}

	public int start() {
		return start;
	}

	public boolean isQueued() {
		return queued;
	}

	public boolean isReady() {
		return ready;
	}

	public boolean isRunning() {
		return running;
	}

	public int end() {
		return end;
	}
}
