/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.logging.Logger;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.jannot.Data;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
/**
 * 
 * @author Thomas Abeel
 *
 */
class PileupTask extends Task {

	private Logger log = Logger.getLogger(PileupTask.class.toString());

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

	@Override
	public void run() {
		try {
			if(model.isExitRequested())
				return;
			summary.setRunning(idx);
			Iterable<Pile> piles = pw.get(idx * PileupSummary.CHUNK, (idx + 1) * PileupSummary.CHUNK);
			for (Pile p : piles) {
				if (p.getPos() >= idx * PileupSummary.CHUNK && p.getPos() < (idx + 1) * PileupSummary.CHUNK) {
					int position = (p.getPos() - 1) / PileupSummary.SUMMARYSIZE;
					summary.add(position, p.getCoverage());

				}
			}
			// System.out.println("Pilerequest: " + idx + " completed " +
			// maxSummary);

			summary.conditionalQueue(pw, idx + 1);
			summary.setReady(idx);

		} catch (Exception e) {
			log.severe("Scheduler exception: " + pw + "\t" + idx + "\tpw.get(" + idx * PileupSummary.CHUNK + ", "
					+ (idx + 1) * PileupSummary.CHUNK + ")\n\t" + e);
		} catch (Error er) {
			log.severe("Scheduler error: " + pw + "\t" + idx + "\tpw.get(" + idx * PileupSummary.CHUNK + ", "
					+ (idx + 1) * PileupSummary.CHUNK + ")\n\t" + er);
		}
	}
}