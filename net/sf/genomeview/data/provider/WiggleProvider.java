/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import org.broad.igv.track.WindowFunction;

import com.sun.xml.internal.bind.v2.runtime.unmarshaller.XsiNilLoader.Array;

import net.sf.genomeview.core.NoFailIterable;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class WiggleProvider extends PileProvider implements Observer {
	private PileupSummary summary = null;
	private Data<Pile> source;

	// private Model model;

	public WiggleProvider(Entry e, Data<Pile> source, Model model) {
		summary = new PileupSummary(model, e);
		summary.addObserver(this);
		this.source = source;

	}

	
	private ArrayList<Pile> buffer = new ArrayList<Pile>();
	private ArrayList<Status> status = new ArrayList<Status>();
	private int lastStart = -1;
	private int lastEnd = -1;
	// private float maxSummary;
	private float maxPile;

	@Override
	public Iterable<Pile> get(final int start, final int end) {
		/* Check whether request can be fulfilled by buffer */
		if (start >= lastStart && end <= lastEnd && (lastEnd - lastStart) <= 2 * (end - start))
			return new NoFailIterable<Pile>(buffer);

		
		/* New request */

		// reset status
		lastStart = start;
		lastEnd = end;

		buffer.clear();
		status.clear();
		
		if (end - start + 1 < PileupSummary.CHUNK) {
			
			status.add(new Status(false, true, false, start, end));
			final Status thisJob=status.get(0);
			// queue up retrieval
			Task t = new Task(new Location(start, end)) {

				@Override
				public void run() {
					// When actually running, check again whether we actually need
					// this data
					if (!(start >= lastStart && end <= lastEnd && (lastEnd - lastStart) <= 2 * (end - start)))
						return;
					thisJob.setRunning();
					// FIXME hard coded arbitrary value
//					Iterable<Pile> fresh = null;
////					if (end - start + 1 < PileupSummary.CHUNK) {
//						fresh = 
//					} else {
//						fresh = summary.get(source, start, end);
//
//					}
					for (Pile p : source.get(start, end)) {
						float val = p.getCoverage();
						// int len = p.getLength();
						// if (len > 1 && val > maxSummary)
						// maxSummary = val;
						if (val > maxPile)
							maxPile = val;

						buffer.add(p);
					}
					
					thisJob.setFinished();
					notifyListeners();
				}

			};
			GenomeViewScheduler.submit(t);
//			fresh = source.get(start, end);
		} else {
			for (Pile p : summary.get(source, start, end)) {
				float val = p.getCoverage();
				// int len = p.getLength();
				// if (len > 1 && val > maxSummary)
				// maxSummary = val;
				if (val > maxPile)
					maxPile = val;

				buffer.add(p);
			}
		}
		
		

		// System.out.println("\tServing new request from provider");

		return new NoFailIterable<Pile>(buffer);
		
		
		
		

	}

	@Override
	public double getMaxPile() {
		return maxPile;
	}

	// @Override
	// public double getMaxSummary() {
	// return maxSummary;
	// }

	public Iterable<Status> getStatus(int start, int end) {
		if (end - start < PileupSummary.CHUNK) {
			return status;
//			Status t=new Status(false,false,true,start,end);
//			ArrayList<Status>out=new ArrayList<Status>();
//			out.add(t);
//			return out; 
		}
		return summary.getStatus(start, end);
	}

	@Override
	public void update(Observable o, Object arg) {
		/* Indicates that the summary has been updated */
		/* Invalidate buffers */
		// System.out.println("\tInvalidating Wiggle Provider buffers ");
		lastStart = -1;
		lastEnd = -1;
		buffer.clear();
		setChanged();
		notifyObservers();
	}

	@Override
	public Data<Pile> getSourceData() {
		return source;
	}

	@Override
	public WindowFunction[] getWindowFunctions() {
		return new WindowFunction[] { WindowFunction.mean };
	}

	@Override
	public void requestWindowFunction(WindowFunction wf) {
		//Do nothing, it's always average!

	}

	@Override
	public boolean isCurrentWindowFunction(WindowFunction wf) {
		//Sure whatever...
		return true;
	}
}
