/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import org.broad.igv.track.WindowFunction;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.pileup.Pile;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class WiggleProvider extends PileProvider implements Observer {
	private PileupSummary summary = null;
	private Data<Pile> source;
//	private Model model;

	public WiggleProvider(Entry e, Data<Pile> source, Model model) {
		summary = new PileupSummary(model, e);
		summary.addObserver(this);
		this.source = source;

	}

	private ArrayList<Pile> buffer = new ArrayList<Pile>();

	private int lastStart = -1;
	private int lastEnd = -1;
//	private float maxSummary;
	private float maxPile;

	@Override
	public Iterable<Pile> get(int start, int end) {
		/* Check whether request can be fulfilled by buffer */
		if (start >= lastStart && end <= lastEnd && (lastEnd - lastStart) <= 2 * (end - start))
			return buffer;

		/* New request */
		//System.out.println("\tServing new request from provider");
		lastStart = start;
		lastEnd = end;

		buffer.clear();
		// FIXME hard coded arbitrary value
		Iterable<Pile> fresh = null;
		if (end - start + 1 < 32000) {
			fresh = source.get(start, end);
		} else {
			fresh = summary.get(source, start, end);

		}
		for (Pile p : fresh) {
			float val = p.getCoverage();
//			int len = p.getLength();
//			if (len > 1 && val > maxSummary)
//				maxSummary = val;
			if (val > maxPile)
				maxPile = val;

			buffer.add(p);
		}
		return buffer;

	}

	@Override
	public double getMaxPile() {
		return maxPile;
	}

//	@Override
//	public double getMaxSummary() {
//		return maxSummary;
//	}

	public Iterable<Status> getStatus(int start, int end) {
		return summary.getStatus(start, end);
	}

	@Override
	public void update(Observable o, Object arg) {
		/* Indicates that the summary has been updated */
		/* Invalidate buffers */
		//System.out.println("\tInvalidating Wiggle Provider buffers ");
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
		return new WindowFunction[]{WindowFunction.mean};
	}

	@Override
	public void requestWindowFunction(WindowFunction wf) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isCurrentWindowFunction(WindowFunction wf) {
		// TODO Auto-generated method stub
		return true;
	}
}
