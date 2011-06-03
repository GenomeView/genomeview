/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.util.concurrent.ThreadFactory;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class DaemonThreadFactory implements ThreadFactory {
	public Thread newThread(Runnable r) {
		Thread thread = new Thread(r);
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.setDaemon(true);
		return thread;
	}
}