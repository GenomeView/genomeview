/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.pileup.Pile;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class WiggleProvider extends PileProvider {
	private PileupSummary summary = null;
	private Data<Pile> source;

	public WiggleProvider(Entry e, Data<Pile> source, Model model) {
		summary = new PileupSummary(model, e);
		this.source = source;
	}

	private ArrayList<Pile> buffer = new ArrayList<Pile>();

	private int lastStart = -1;
	private int lastEnd = -1;
	private int maxSummary;
	private int maxPile;

	@Override
	public Iterable<Pile> get(int start, int end) {

		if (start >= lastStart && end <= lastEnd && (lastEnd - lastStart) <= 2 * (end - start))
			return buffer;

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
			int val = p.getCoverage();
			int len = p.getLength();
			if (len > 1 && val > maxSummary)
				maxSummary = val;
			if (len == 1 && val > maxPile)
				maxPile = val;

			buffer.add(p);
		}
		return buffer;

	}

	@Override
	public double getMaxPile() {
		return maxPile;
	}

	@Override
	public double getMaxSummary() {
		return maxSummary;
	}
}
