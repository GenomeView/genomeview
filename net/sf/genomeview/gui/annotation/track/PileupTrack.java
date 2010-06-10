/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.util.BitSet;

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
import net.sf.jannot.Strand;
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

	/* Queue in blocks of 1000 */
	private BitSet queued = new BitSet();
	private int[] summary;
	private int maxPile = 0;

	private static final int CHUNK = 2000;
	private static final int SUMMARYSIZE = 100;

	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}

	public synchronized void queue(final int idx, final PileupWrapper pw) {
		final PileupTrack pt=this;
		if (!queued.get(idx)) {
			queued.set(idx);
			GenomeViewScheduler.submit(new Task() {

				@Override
				public void run() {
					Iterable<Pile> piles = pw.get(idx * CHUNK, (idx + 1) * CHUNK);
					for (Pile p : piles) {
						if (p.getPos() >= idx * CHUNK && p.getPos() < (idx + 1) * CHUNK) {
							summary[(p.getPos() - 1) / SUMMARYSIZE] += p.getCoverage();
//							System.out.println((p.getPos() - 1) / SUMMARYSIZE + "-"
//									+ summary[(p.getPos() - 1) / SUMMARYSIZE]);
							if (p.getCoverage() > maxPile) {
								maxPile = p.getCoverage();
							}
						}
					}
					System.out.println("Pilerequest: " + idx + " completed");
					pt.model.refresh();
				}

			});

			System.out.println("Queue: " + idx);

		}
	}

	@Override
	public int paintTrack(Graphics2D g, final Entry entry, int yOffset, double screenWidth) {
		if (summary == null)
			summary = new int[entry.getMaximumLength() / SUMMARYSIZE];

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
			for (Pile p : piles) {

				int pos = p.getLocation().start;
				int coverage = p.getCoverage();
				if (coverage > maxPile)
					maxPile = coverage;
				double frac = coverage / (double) maxPile;
				int size = (int) (frac * 50);
				int width = (int) Math.ceil(screenWidth / visible.length());
				// System.out.println(size);
				int screenX = Convert.translateGenomeToScreen(pos, visible, screenWidth);
				g.fillRect(screenX, yOffset + 50 - size, width, size);

			}

		} else {
			/* Queue data retrieval */
			for (int i = visible.start / CHUNK; i < visible.end / CHUNK + 1; i++) {
				final int idx = i;
				queue(idx, pw);

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
			double topValue = maxPile;
			double range = topValue - bottomValue;
			for (int i = vs; i < visible.end + SUMMARYSIZE; i += SUMMARYSIZE) {
				int x = Convert.translateGenomeToScreen(i, visible, screenWidth);

				// double valF = f[i] - 1;
				// double valR = r[i] - 1;
				//System.out.println("P-" + (i / SUMMARYSIZE) + " " + summary[i / SUMMARYSIZE]);
				double val = summary[i / SUMMARYSIZE] / SUMMARYSIZE;// /f[i] +
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
				//System.out.println("VAL: " + val);
				/* Draw lines */
				if (i == vs) {
					conservationGP.moveTo(x - 1, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
					// conservationGPF.moveTo(x - 1, yOffset + (1 - valF) *
					// (graphLineHeigh - 4) + 2);
					// conservationGPR.moveTo(x - 1, yOffset + (1 - valR) *
					// (graphLineHeigh - 4) + 2);
				}

				conservationGP.lineTo(x, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
				// conservationGPF.lineTo(x, yOffset + (1 - valF) *
				// (graphLineHeigh - 4) + 2);
				// conservationGPR.lineTo(x, yOffset + (1 - valR) *
				// (graphLineHeigh - 4) + 2);

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
			g.draw(conservationGP);

		}
		g.setColor(Color.BLACK);
		g.drawString(StaticUtils.shortify(super.dataKey.toString()), 10, yOffset + 24 - 2);
		return graphLineHeigh;

	}

	@Override
	public String displayName() {
		return "Pileup: " + super.dataKey;
	}
}