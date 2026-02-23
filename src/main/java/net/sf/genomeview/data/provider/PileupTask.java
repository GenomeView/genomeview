/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.jannot.Data;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.DoublePile;
import net.sf.jannot.pileup.Pile;
/**
 * 
 * @author Thomas Abeel
 *
 */
class PileupTask extends Task {

	private Logger log = LoggerFactory.getLogger(PileupTask.class.toString());

	private int idx;
	private Data<Pile> pw;
	private PileupSummary summary;

	private Model model;

	public PileupTask(Data<Pile> pw, int idx, PileupSummary summary,Model model) {
		super(new Location(idx * PileupSummary.CHUNK, (idx + 1) * PileupSummary.CHUNK));
		this.model=model;
		this.pw = pw;
		this.summary = summary;
		this.idx = idx;

	}

	
	private boolean cancelled=false;
	@Override
	public void cancel(){
		super.cancel();
		summary.complyCancel(idx);
		this.cancelled=true;
	}
	
	public boolean isCancelled(){
		return cancelled;
	}
	
	@Override
	public void run() {
		try {
			if(model.isExitRequested())
				return;
			if(cancelled){
				summary.complyCancel(idx);
				return;
			}
			summary.setRunning(idx);
			Iterable<Pile> piles = pw.get(idx * PileupSummary.CHUNK, (idx + 1) * PileupSummary.CHUNK);
			for (Pile p : piles) {
				if (p.start() >= idx * PileupSummary.CHUNK && p.start() < (idx + 1) * PileupSummary.CHUNK) {
					int position = (p.start() - 1) / PileupSummary.SUMMARYSIZE;
					summary.add(position, p.getValue(0),p.getValue(1));

				}
			}
			// System.out.println("Pilerequest: " + idx + " completed " +
			// maxSummary);

			summary.conditionalQueue(pw, idx + 1);
			summary.setReady(idx);

		} catch (Exception e) {
			log.error("Scheduler exception: " + pw + "\t" + idx + "\tpw.get(" + idx * PileupSummary.CHUNK + ", "
					+ (idx + 1) * PileupSummary.CHUNK + ")\n\t" + e);
		} catch (Error er) {
			log.error("Scheduler error: " + pw + "\t" + idx + "\tpw.get(" + idx * PileupSummary.CHUNK + ", "
					+ (idx + 1) * PileupSummary.CHUNK + ")\n\t" + er);
		}
	}
}
