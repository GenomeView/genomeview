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
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.tdf.TDFData;
import net.sf.samtools.SAMRecord;

import org.broad.igv.track.WindowFunction;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadProvider extends AbstractDataProvider<SAMRecord> {

	private ReadGroup source;

	private Model model;

	public ShortReadProvider(Entry e, ReadGroup source, Model model) {
		super(model);
		this.source = source;
//		/* Select default window function */
//		WindowFunction wf=WindowFunction.getWindowFunction(Configuration.get("pileup:defaultWindowFunction"));
//		System.out.println("requesting: "+Configuration.get("pileup:defaultWindowFunction")+"\t"+wf);
//		if(source.availableWindowFunctions().contains(wf))
//			source.requestWindowFunction(wf);
		this.model = model;
	}

	private ArrayList<SAMRecord> buffer = new ArrayList<SAMRecord>();
	private ArrayList<Status> status = new ArrayList<Status>();
	private int lastStart = -1;
	private int lastEnd = -1;
	// privFate float maxSummary;
//	private float maxPile;

	@Override
	public Iterable<SAMRecord> get(final int start, final int end) {
		/* Check whether request can be fulfilled by buffer */
		if (start >= lastStart && end <= lastEnd
				&& (lastEnd - lastStart) <= 2 * (end - start))
			return buffer;

		/* New request */

		// reset status
		lastStart = start;
		lastEnd = end;

//		buffer.clear();
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
				Iterable<SAMRecord> fresh = source.get(start, end);
				ArrayList<SAMRecord>tmp=new ArrayList<SAMRecord>();
				for (SAMRecord p : fresh) {
					

					tmp.add(p);
				}
				thisJob.setFinished();
				buffer=tmp;
				notifyListeners();
			}

		};
		GenomeViewScheduler.submit(t);

		// System.out.println("\tServing new request from provider");
		return buffer;
//		return new NoFailIterable<SAMRecord>(buffer);

	}

	
	
//	@Override
//	public double getMaxPile() {
//		return maxPile;
//	}

	public Iterable<Status> getStatus(int start, int end) {
		return status;
	}

//	@Override
//	public Data<Pile> getSourceData() {
//		return source;
//	}

//	@Override
//	public WindowFunction[] getWindowFunctions() {
//		return source.availableWindowFunctions().toArray(new WindowFunction[0]);
//	}
//
//	@Override
//	public void requestWindowFunction(WindowFunction wf) {
//		// System.out.println("WF in TDF: "+wf);
//		if (source.availableWindowFunctions().contains(wf)) {
//			// System.out.println("\tWe are nwo using WF: "+wf);
//			source.requestWindowFunction(wf);
//			lastStart = -1;
//			lastEnd = -1;
//			maxPile = 0;
//			buffer.clear();
//			setChanged();
//			notifyObservers();
//		}
//
//	}
//
//	@Override
//	public boolean isCurrentWindowFunction(WindowFunction wf) {
//		return source.isCurrentWindowFunction(wf);
//	}



	@Override
	public String label() {
		return source.label();
	}



	public int readLength() {
		return source.readLength();
	}



	public SAMRecord getSecondRead(SAMRecord one) {
		return source.getSecondRead(one);
	}



	public SAMRecord getFirstRead(SAMRecord one) {
		return source.getFirstRead(one);
	}

}
