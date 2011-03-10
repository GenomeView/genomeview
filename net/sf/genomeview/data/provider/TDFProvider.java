/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.tdf.TDFData;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class TDFProvider extends PileProvider {
	
	//FIXME this class may expose the window functions and so on
	
	private TDFData source;
//	private Model model;

	public TDFProvider(Entry e, TDFData source, Model model) {
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

		Iterable<Pile> fresh = source.get(start, end);
		
		for (Pile p : fresh) {
			float val = p.getCoverage();

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


	public Iterable<Status> getStatus(int start, int end) {
		return new ArrayList<Status>();
	}

	

	@Override
	public Data<Pile> getSourceData() {
		return source;
	}

	
}
