/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.BitSet;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.scheduler.GenomeViewScheduler;
import net.sf.genomeview.scheduler.Task;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.tabix.PileupWrapper;

public class PileupTrack extends Track {

	public PileupTrack(DataKey key, Model model) {
		super(key, model, true, true);
	}

	private Tooltip tooltip = new Tooltip();

	private class Tooltip extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public Tooltip() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

	}

	@Override
	public boolean mouseExited(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		return false;
	}

	// private int currentYOffset = 0;

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		return false;
	}

	/* Queue in blocks of CHUNK */
	private BitSet queued = null;
	/* Queue in blocks of CHUNK */
	private BitSet ready = null;
	private int[] summary;
	private int maxPile = 0;
	private int maxSummary = 0;

	private static final int CHUNK = 2000;
	private static final int SUMMARYSIZE = 100;

	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}

	private Logger log = Logger.getLogger(PileupTrack.class.toString());

	class PileupTask extends Task {
		private int idx;
		private PileupWrapper pw;

		public PileupTask(PileupWrapper pw, int idx) {
			super(new Location(idx * CHUNK, (idx + 1) * CHUNK));
			this.pw = pw;
			this.idx = idx;

		}

		@Override
		public void run() {
			try {
				Iterable<Pile> piles = pw.get(idx * CHUNK, (idx + 1) * CHUNK);
				for (Pile p : piles) {
					if (p.getPos() >= idx * CHUNK && p.getPos() < (idx + 1) * CHUNK) {
						summary[(p.getPos() - 1) / SUMMARYSIZE] += p.getCoverage();
						if (p.getCoverage() > maxPile) {
							maxPile = p.getCoverage();
						}
						if (summary[(p.getPos() - 1) / SUMMARYSIZE] > maxSummary)
							maxSummary = summary[(p.getPos() - 1) / SUMMARYSIZE];
					}
				}
				System.out.println("Pilerequest: " + idx + " completed " + maxSummary);
				ready.set(idx);
				if (!queued.get(idx + 1)) {
					if ((idx + 1) * CHUNK < entry.getMaximumLength()) {
						queued.set(idx + 1);
						GenomeViewScheduler.submit(new PileupTask(pw, idx + 1));
					}
				}
				model.refresh();
			} catch (Exception e) {
				log.severe("Scheduler exception: " + pw + "\t" + idx + "\tpw.get(" + idx * CHUNK + ", " + (idx + 1)
						* CHUNK + ")\n\t" + e);
			} catch (Error er) {
				log.severe("Scheduler error: " + pw + "\t" + idx + "\tpw.get(" + idx * CHUNK + ", " + (idx + 1) * CHUNK
						+ ")\n\t" + er);
			}
		}
	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth) {
		if (summary == null || summary.length <= 1) {
			reset();
		}

		/* Get configuration */
		boolean logScaling = Configuration.getBoolean("shortread:logScaling");
		double bottomValue = Configuration.getDouble("shortread:bottomValue");
		// double topValue = Configuration.getDouble("shortread:topValue");

		int graphLineHeigh = Configuration.getInt("shortread:graphLineHeight");

		final PileupWrapper pw = (PileupWrapper) entry.get(super.dataKey);

		// System.out.println("PW=" + pw);
		Location visible = model.getAnnotationLocationVisible();
		if (visible.length() < CHUNK) {
			// System.out.println("Show individual");
			Iterable<Pile> piles = pw.get(visible.start, visible.end);
			g.setColor(Color.GRAY);
			int width = (int) Math.ceil(screenWidth / visible.length());
			for (Pile p : piles) {

				int pos = p.getLocation().start;
				int coverage = p.getCoverage();
				if (coverage > maxPile)
					maxPile = coverage;
				double frac = coverage / (double) maxPile;
				int size = (int) (frac * graphLineHeigh);

				// System.out.println(size);
				int screenX = Convert.translateGenomeToScreen(pos, visible, screenWidth);
				g.fillRect(screenX, yOffset + graphLineHeigh - size, width, size);

			}

		} else {
			/* Queue data retrieval */
			int startChunk = visible.start / CHUNK;
			int endChunk = visible.end / CHUNK;
			int stepChunk = (endChunk - startChunk) / 20 + 1;
			// System.out.println(startChunk+"\t"+endChunk+"\t"+stepChunk);
			for (int i = visible.start / CHUNK; i < visible.end / CHUNK + 1; i += stepChunk) {
				final int idx = i;
				if (!queued.get(idx)) {
					queued.set(idx);
					GenomeViewScheduler.submit(new PileupTask(pw, idx));

				}

			}
			/* Draw status */
			int chunkWidth = (int) Math.ceil(CHUNK * screenWidth / visible.length());
			for (int i = visible.start; i < visible.end; i += CHUNK) {
				int x = Convert.translateGenomeToScreen((i / CHUNK) * CHUNK, visible, screenWidth);
				if (queued.get(i / CHUNK))
					g.setColor(Color.ORANGE);
				if (ready.get(i / CHUNK))
					g.setColor(Color.GREEN);
				if (queued.get(i / CHUNK) || ready.get(i / CHUNK))
					g.fillRect(x, yOffset, chunkWidth, graphLineHeigh);
			}

			/* Paint coverage plot */
			// ShortReadCoverage graph = rg.getCoverage();// .get(rg);
			// if (topValue < 0)
			// topValue = graph.max();

			GeneralPath conservationGP = new GeneralPath();
			// GeneralPath conservationGPF = new GeneralPath();
			// GeneralPath conservationGPR = new GeneralPath();

			// float[] f = graph.get(Strand.FORWARD, start - 1, end,
			// scaleIndex);
			// float[] r = graph.get(Strand.REVERSE, start - 1, end,
			// scaleIndex);

			// double range = topValue - bottomValue;

			int vs = visible.start / SUMMARYSIZE * SUMMARYSIZE + SUMMARYSIZE / 2;
			double topValue = maxSummary;
			double range = topValue - bottomValue;

			conservationGP.moveTo(-5, yOffset + graphLineHeigh);

			for (int i = vs; i < visible.end + SUMMARYSIZE; i += SUMMARYSIZE) {
				if (!ready.get(i / CHUNK))
					continue;
				int x = Convert.translateGenomeToScreen(i, visible, screenWidth);

				// double valF = f[i] - 1;
				// double valR = r[i] - 1;
				// System.out.println("P-" + (i / SUMMARYSIZE) + " " + summary[i
				// / SUMMARYSIZE]);
				// try {
				int idx = i / SUMMARYSIZE;
				if (idx >= summary.length) {
					// System.err.println(idx);
					idx = summary.length - 1;
				}
				double val = summary[idx];// /
				// (double)maxSummary;//

				// /f[i] +

				// r[i] - 2;
				/* Cap value */
				// if (valF > topValue)
				// valF = topValue;
				// if (valR > topValue)
				// valR = topValue;
				// if (val > topValue)
				// val = topValue;

				// if (valF < bottomValue)
				// valF = bottomValue;
				// valR = bottomValue;
				if (val < bottomValue)
					val = bottomValue;

				/* Translate for bottom point */
				// valF -= bottomValue;
				// valR -= bottomValue;
				val -= bottomValue;
				/* Logaritmic scaling */
				if (logScaling) {
					// valF = log2(valF + 1);
					// valF /= log2(range);
					// valR = log2(valR + 1);
					// valR /= log2(range);
					val = log2(val + 1);
					val /= log2(range);
					/* Regular scaling */
				} else {
					// valF /= range;
					// valR /= range;
					val /= range;
				}
				// System.out.println("VAL: " + val);
				/* Draw lines */
				// if (i == vs) {
				// conservationGP.moveTo(x - 1, yOffset + (1 - val) *
				// (graphLineHeigh - 4) + 2);
				// // conservationGPF.moveTo(x - 1, yOffset + (1 - valF) *
				// // (graphLineHeigh - 4) + 2);
				// // conservationGPR.moveTo(x - 1, yOffset + (1 - valR) *
				// // (graphLineHeigh - 4) + 2);
				// }

				conservationGP.lineTo(x, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
				// conservationGPF.lineTo(x, yOffset + (1 - valF) *
				// (graphLineHeigh - 4) + 2);
				// conservationGPR.lineTo(x, yOffset + (1 - valR) *
				// (graphLineHeigh - 4) + 2);
				// } catch (ArrayIndexOutOfBoundsException e) {
				// System.err.println(e);
				// System.err.println(summary.length);
				// System.out.println(entry.getMaximumLength());
				// }
			}
			// for (int i = 0; i < f.length; i++) {
			// int x = Convert.translateGenomeToScreen(start + i * scale,
			// currentVisible, screenWidth);
			// /* Coverage is stored +1 in SRC, needs correcting here */
			// double valF = f[i] - 1;
			// double valR = r[i] - 1;
			// double val = f[i] + r[i] - 2;

			// }

			/* Draw coverage lines */
			// g.setColor(forwardColor);
			// g.draw(conservationGPF);
			//
			// g.setColor(reverseColor);
			// g.draw(conservationGPR);
			g.setColor(Color.BLACK);
			conservationGP.lineTo(screenWidth + 5, yOffset + graphLineHeigh);
			g.draw(conservationGP);

		}

		g.setColor(Color.BLACK);
		g.drawString(StaticUtils.shortify(super.dataKey.toString()), 10, yOffset + 24 - 2);
		return graphLineHeigh;

	}

	private void reset() {
		// System.out.println(entry);
		summary = new int[entry.getMaximumLength() / SUMMARYSIZE + 1];
		// System.out.println("Piluptrack: "+summary.length);
		ready = new BitSet();
		queued = new BitSet();

	}

	@Override
	public String displayName() {
		return "Pileup: " + super.dataKey;
	}
}