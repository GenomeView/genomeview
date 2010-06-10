package net.sf.genomeview.scheduler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeViewScheduler {

	private static ExecutorService worker = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS,
			new PriorityBlockingQueue<Runnable>());

	public static void submit(Task t) {
		worker.execute(t);

	}

}
