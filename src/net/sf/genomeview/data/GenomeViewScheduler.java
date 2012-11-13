package net.sf.genomeview.data;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sf.jannot.Location;
import be.abeel.concurrency.DaemonThread;
import be.abeel.concurrency.DaemonThreadFactory;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GenomeViewScheduler {

	static PriorityBlockingQueue<Runnable> gvs;
	static {
		gvs = new PriorityBlockingQueue<Runnable>();
	}

	private static ExecutorService worker = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, gvs,new DaemonThreadFactory());

	public static void submit(Task t) {
		worker.execute(t);
		// System.out.println("Queuesize: " + gvs.size());

	}

	private static void boost(Location visible) {
		for (Runnable r : gvs) {
			Task t = (Task) r;
			gvs.remove(t);
			Location l=t.getLocation();
			if (l!=null&&l.overlaps(visible.start,visible.end)) {
				t.boost();
			} else
				t.cancel();
			if(!t.isCancelled())
				gvs.add(t);

		}
	}

	public static void start(final Model model) {
		/* Scheduler booster thread */
		new DaemonThread(new Runnable() {

			@Override
			public void run() {
				while (true && !model.isExitRequested()) {
					GenomeViewScheduler.boost(model.getAnnotationLocationVisible());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						/* This happens when restarting the Applet */
						return;
					}
				}

			}
		}).start();
		
	}

}
