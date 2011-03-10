/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Observable;

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
class PileupSummary extends Observable{

	static final int CHUNK = 32000;
	static final int SUMMARYSIZE = 100;

	/* Queue in blocks of CHUNK */
	private BitSet queued = null;
	/* Queue in blocks of CHUNK */
	private BitSet ready = null;
	private BitSet running = null;
	private int[] forwardSummary;
	private int[] reverseSummary;

	/* Keeps track of the maximum value in detailed mode */
	private double maxPile = 0;

//	/* Keeps track of the maximum value in summary graph mode */
//	private double maxSummary = 0;
	private Model model;
//	private Entry entry;

	public int length() {
		if (forwardSummary == null)
			return 0;
		return forwardSummary.length;
	}

	public PileupSummary(Model model,Entry e) {
		this.model = model;
		//this.entry=e;
		reset(e);
		// System.out.println(entry);
	
	}
	
	private void reset(Entry e){
		forwardSummary = new int[e.getMaximumLength() / SUMMARYSIZE + 1];
		reverseSummary = new int[e.getMaximumLength() / SUMMARYSIZE + 1];
		ready = new BitSet();
		queued = new BitSet();
		running = new BitSet();
	}

	void conditionalQueue(Data<Pile> pw, int idx) {
		if (!queued.get(idx)) {
			if (idx < forwardSummary.length) {
				/* Only queue additional chunks in visible region */
				if ((idx) * PileupSummary.CHUNK < model.getAnnotationLocationVisible().end
						&& (idx + 1) * PileupSummary.CHUNK > model.getAnnotationLocationVisible().start) {

					queued.set(idx);
					GenomeViewScheduler.submit(new PileupTask(pw, idx, this, model));
				}
			}

		}

	}

	
	private boolean isReady(int i) {
		return ready.get(i);
	}

	private boolean isRunning(int i) {
		return running.get(i);
	}

	private boolean isQueued(int i) {
		return queued.get(i);
	}

	private double getFValue(int idx) {
		return forwardSummary[idx];
	}
	private double getRValue(int idx) {
		return reverseSummary[idx];
	}
	
	void setRunning(int idx) {
		running.set(idx);

	}
//
//	private double getMaxPile() {
//		return maxPile;
//	}

//	private double getMaxSummary() {
//		return maxSummary;
//	}

	void add(int idx, float fcov,float rcov) {
		forwardSummary[idx] += fcov;
		reverseSummary[idx] += rcov;
		if (fcov > maxPile)
			maxPile = fcov;
		if (rcov> maxPile)
			maxPile = rcov;
//		if (summary[idx] > maxPile)
//			maxPile = summary[idx];

	}

	void setReady(int idx) {
		ready.set(idx);
		System.out.println("Completed "+idx);
		lastStart=-1;
		lastEnd=-1;
		setChanged();
		notifyObservers();

	}

//	private void setMaxPile(double coverage) {
//		this.maxPile = coverage;
//
//	}

	private ArrayList<Pile>buffer=new ArrayList<Pile>();
	private int lastEnd=0;
	private int lastStart=0;
	public Iterable<Pile> get(Data<Pile> source, int start, int end) {
		
		
		/* Queue data retrieval */
		int startChunk = start / PileupSummary.CHUNK;
		int endChunk = end / PileupSummary.CHUNK;
		int stepChunk = (endChunk - startChunk) / 20 + 1;
		for (int i = start / PileupSummary.CHUNK; i < end / PileupSummary.CHUNK + 1; i += stepChunk) {
			final int idx = i;
			conditionalQueue(source, idx);

		}
		int vs = start / PileupSummary.SUMMARYSIZE * PileupSummary.SUMMARYSIZE;// + PileupSummary.SUMMARYSIZE / 2;
		//double topValue = maxPile;
		// double range = topValue - bottomValue;

		//conservationGP.moveTo(-5, yOffset + graphLineHeigh);

		if (start >= lastStart && end <= lastEnd)
			return buffer;
		
		lastStart=start;
		lastEnd=end;

		buffer.clear();
		for (int i = vs; i < end + PileupSummary.SUMMARYSIZE; i += PileupSummary.SUMMARYSIZE) {
			if (!isReady(i / PileupSummary.CHUNK))
				continue;
			else{
				int idx = i / PileupSummary.SUMMARYSIZE;
				if (idx >= length()) {
					// System.err.println(idx);
					idx = length() - 1;
				}
				float fval = (float)getFValue(idx)/SUMMARYSIZE;// /
				float rval = (float)getRValue(idx)/SUMMARYSIZE;// /
				Pile tmp=new Pile(i,fval,rval,null);
				tmp.setLen(PileupSummary.SUMMARYSIZE);
				buffer.add(tmp);
			}
		}
		return buffer;
	}

	public Iterable<Status> getStatus(int start, int end) {
		ArrayList<Status>out=new ArrayList<Status>();
		
		int vs = start / PileupSummary.CHUNK * PileupSummary.CHUNK;// + PileupSummary.SUMMARYSIZE / 2;
		
		for (int i = vs; i < end + PileupSummary.CHUNK; i += PileupSummary.CHUNK) {
			out.add(getStatus(i/PileupSummary.CHUNK));
		}
		return out;
		
	}

	private Status getStatus(int i) {
		return new Status(isRunning(i),isQueued(i),isReady(i),i*CHUNK,(i+1)*CHUNK);
			
	}

}
