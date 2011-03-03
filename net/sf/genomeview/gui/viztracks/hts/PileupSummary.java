/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.util.BitSet;

import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Data;
import net.sf.jannot.Entry;
import net.sf.jannot.pileup.Pile;
/**
 * 
 * @author Thomas Abeel
 *
 */
class PileupSummary {

	static final int CHUNK = 32000;
	static final int SUMMARYSIZE = 100;

	/* Queue in blocks of CHUNK */
	private BitSet queued = null;
	/* Queue in blocks of CHUNK */
	private BitSet ready = null;
	private BitSet running = null;
	private int[] summary;

	/* Keeps track of the maximum value in detailed mode */
	private double maxPile = 0;

	/* Keeps track of the maximum value in summary graph mode */
	private double maxSummary = 0;
	private Model model;

	public int length() {
		if(summary==null)
			return 0;
		return summary.length;
	}

	public PileupSummary(Model model) {
		this.model = model;
	}

	public void conditionalQueue(Data<Pile> pw, int idx) {
		if (!queued.get(idx)) {
			if (idx < summary.length) {
				/* Only queue additional chunks in visible region */
				if ((idx) * PileupSummary.CHUNK < model.getAnnotationLocationVisible().end
						&& (idx + 1) * PileupSummary.CHUNK > model.getAnnotationLocationVisible().start) {

					queued.set(idx);
					GenomeViewScheduler.submit(new PileupTask(pw, idx, this,model));
				}
			}

		}

	}

	void reset(Entry entry) {
		// System.out.println(entry);
		summary = new int[entry.getMaximumLength() / SUMMARYSIZE + 1];
		// System.out.println("Piluptrack: "+summary.length);
		ready = new BitSet();
		queued = new BitSet();
		running = new BitSet();

	}

	public boolean isReady(int i) {
		return ready.get(i);
	}

	public boolean isRunning(int i) {
		return running.get(i);
	}

	public boolean isQueued(int i) {
		return queued.get(i);
	}

	public double getValue(int idx) {
		return summary[idx];
	}

	public void setRunning(int idx) {
		running.set(idx);

	}

	

	public double getMaxPile() {
		return maxPile;
	}

	

	public double getMaxSummary() {
		return maxSummary;
	}

	public void add(int idx, int coverage) {
		summary[idx]+=coverage;
		if(coverage>maxPile)
			maxPile=coverage;
		if(summary[idx]>maxSummary)
			maxSummary=summary[idx];

	}

	public void setReady(int idx) {
		ready.set(idx);
		model.refresh();

	}

	public void setMaxPile(double coverage) {
		this.maxPile=coverage;
		
	}

}
