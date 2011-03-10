/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
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
	private Model model;

	public WiggleProvider(Entry e, Data<Pile> source, Model model) {
		summary = new PileupSummary(model, e);
		this.model=model;
		summary.addObserver(this);
		this.source = source;

	}

	private ArrayList<Pile> buffer = new ArrayList<Pile>();

	private int lastStart = -1;
	private int lastEnd = -1;
	private int maxSummary;
	private int maxPile;

	@Override
	public Iterable<Pile> get(int start, int end) {
		/* Check whether request can be fulfilled by buffer */
		if (start >= lastStart && end <= lastEnd && (lastEnd - lastStart) <= 2 * (end - start))
			return buffer;

		/* New request */

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

	public Iterable<Status> getStatus(int start, int end) {
		return summary.getStatus(start, end);
	}

	@Override
	public void update(Observable o, Object arg) {
		/* Indicates that the summary has been updated */
		/* Invalidate buffers */
		lastStart = -1;
		lastEnd = -1;
		buffer.clear();
		model.refresh();
	}
}
