/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.NoFailIterable;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.tdf.TDFData;

import org.broad.igv.track.WindowFunction;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TDFProvider extends PileProvider {

	private TDFData source;


	public TDFProvider(Entry e, TDFData source, Model model) {
//		super(model);
		this.source = source;
		/* Select default window function */
		WindowFunction wf=WindowFunction.getWindowFunction(Configuration.get("pileup:defaultWindowFunction"));
		System.out.println("requesting: "+Configuration.get("pileup:defaultWindowFunction")+"\t"+wf);
		if(source.availableWindowFunctions().contains(wf))
			source.requestWindowFunction(wf);
		

	}

	private ArrayList<Pile> buffer = new ArrayList<Pile>();
	private ArrayList<Status> status = new ArrayList<Status>();
	private int lastStart = -1;
	private int lastEnd = -1;
	// privFate float maxSummary;
	private float maxPile;

	@Override
	public void get(final int start, final int end,final DataCallback<Pile>cb) {
		/* Check whether request can be fulfilled by buffer */
		if (start >= lastStart && end <= lastEnd
				&& (lastEnd - lastStart) <= 2 * (end - start))
//			return new NoFailIterable<Pile>(buffer);
			cb.dataReady(new Location(start,end),buffer);

		/* New request */

		// reset status
		lastStart = start;
		lastEnd = end;

		buffer.clear();
		status.clear();
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
				Iterable<Pile> fresh = source.get(start, end+1);
				
				for (Pile p : fresh) {
					float val = p.getTotal();

					if (val > maxPile)
						maxPile = val;

					buffer.add(p);
				}
				thisJob.setFinished();
//				notifyListeners();
				cb.dataReady(new Location(start,end),buffer);
			}

		};
		GenomeViewScheduler.submit(t);

		// System.out.println("\tServing new request from provider");

//		return new NoFailIterable<Pile>(buffer);

	}

	
	
	@Override
	public double getMaxPile() {
		return maxPile;
	}

	public Iterable<Status> getStatus(int start, int end) {
		return status;
	}

//	@Override
//	public Data<Pile> getSourceData() {
//		return source;
//	}

	@Override
	public WindowFunction[] getWindowFunctions() {
		return source.availableWindowFunctions().toArray(new WindowFunction[0]);
	}

	@Override
	public void requestWindowFunction(WindowFunction wf) {
		// System.out.println("WF in TDF: "+wf);
		if (source.availableWindowFunctions().contains(wf)) {
			// System.out.println("\tWe are nwo using WF: "+wf);
			source.requestWindowFunction(wf);
			lastStart = -1;
			lastEnd = -1;
			maxPile = 0;
			buffer.clear();
//			setChanged();
//			notifyObservers();
		}

	}

	@Override
	public boolean isCurrentWindowFunction(WindowFunction wf) {
		return source.isCurrentWindowFunction(wf);
	}



//	@Override
//	public String label() {
//		return source.label();
//	}

}
