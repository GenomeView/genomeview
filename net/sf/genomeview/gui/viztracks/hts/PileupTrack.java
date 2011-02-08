/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.tabix.PileupWrapper;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class PileupTrack extends Track {

	private NumberFormat nf = NumberFormat.getInstance(Locale.US);
	private PileupSummary summary = null;

	public PileupTrack(DataKey key, Model model) {
		super(key, model, true, false);
		ptm = new PileupTrackModel(model);
		tooltip = new PileupTooltip(ptm);
		
		nf.setMaximumFractionDigits(0);
		summary = new PileupSummary(model);
	}

	
	private PileupTrackModel ptm ;
	private PileupTooltip tooltip ;

	@Override
	public boolean mouseExited(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		return false;
	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		tooltip.set(source);
		return false;
	}

	/*
	 * Keeps track of the maximum value in detailed mode per data query, this
	 * value will be reset each time we retrieve new data.
	 */
	private double localMaxPile = 0;

	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}

	private Logger log = Logger.getLogger(PileupTrack.class.toString());

	

	

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth, JViewport view) {
		if (summary == null || summary.length() <= 1) {
			summary.reset(entry);
		}
		
		ptm.setScreenWidth(screenWidth);
		
		Location visible = model.getAnnotationLocationVisible();
		/* Only retrieve data when location changed */
		if (ptm.lastQuery == null || !ptm.lastQuery.equals(model.getAnnotationLocationVisible())) {
			localMaxPile = 0;
			final Data<Pile> pw = (Data<Pile>) entry.get(super.dataKey);
			/* Data for detailed panel */
			if (pw != null)
				ptm.lastQuery = model.getAnnotationLocationVisible();
			if (ptm.isDetailed()) {
				/* Draw individual piles */
				// update data...
				Iterable<Pile> piles = pw.get(visible.start, visible.end);
				ptm.detailedRects = new int[2][visible.length()];
				/* Variables for SNP track */
				ptm.nc = new NucCounter(visible.length());
				g.setColor(Color.GRAY);
				for (Pile p : piles) {
					if (p.getPos() < visible.start || p.getPos() > visible.end)
						continue;
					if (p.getBases() != null) {
						count(ptm.nc, p, visible);

					}

					int pos = p.getLocation().start;

					int fcov = p.getFCoverage();
					int rcov = p.getRCoverage();

					ptm.detailedRects[0][pos - visible.start] = fcov;
					ptm.detailedRects[1][pos - visible.start] = rcov;

					double coverage = fcov + rcov;
					if (coverage > summary.getMaxPile())
						summary.setMaxPile(coverage);
					if (coverage > localMaxPile)
						localMaxPile = coverage;

				}
			} else {
				/* Queue data retrieval */
				int startChunk = visible.start / PileupSummary.CHUNK;
				int endChunk = visible.end / PileupSummary.CHUNK;
				int stepChunk = (endChunk - startChunk) / 20 + 1;
				// System.out.println(startChunk+"\t"+endChunk+"\t"+stepChunk);
				for (int i = visible.start / PileupSummary.CHUNK; i < visible.end / PileupSummary.CHUNK + 1; i += stepChunk) {
					final int idx = i;
					summary.conditionalQueue(pw, idx);

					// }

				}
			}

		}

		/* Get data for overview panel */

		/* Get information from configuration */
		Color forwardColor = Configuration.getColor("shortread:forwardColor");
		Color reverseColor = Configuration.getColor("shortread:reverseColor");
		int graphLineHeigh = Configuration.getInt("shortread:graphLineHeight");
		int snpTrackHeight = Configuration.getInt("shortread:snpTrackHeight");
		int snpTrackMinimumCoverage = Configuration.getInt("shortread:snpTrackMinimumCoverage");

		
		/**
		 * Draw detailed coverage plot
		 */
		if (ptm.isDetailed()) {
			int lastX = -1;
			double div = summary.getMaxPile();
			if (ptm.isDynamicScaling())
				div = localMaxPile;
			int width = (int) Math.ceil(screenWidth / visible.length());
			for (int i = 0; i < ptm.detailedRects[0].length; i++) {
				// int snpOffset = yOffset;
				double fcov = ptm.detailedRects[0][i];
				double rcov = ptm.detailedRects[1][i];
				double coverage = fcov + rcov;
				/* Max value set, truncate */

				if (maxValue > 0) {
					div = maxValue;
					if (coverage > maxValue)
						coverage = maxValue;
					if (rcov > maxValue)
						rcov = maxValue;
					if (fcov > maxValue)
						fcov = maxValue;

				}
				double frac = coverage / div;
				int size = (int) (frac * graphLineHeigh);
				double ffrac = fcov / div;
				int fsize = (int) (ffrac * graphLineHeigh);
				double rfrac = rcov / div;
				int rsize = (int) (rfrac * graphLineHeigh);
				int screenX = Convert.translateGenomeToScreen(i + visible.start, visible, screenWidth);

				if (screenX > lastX) {
					lastX = screenX;
					g.setColor(Color.ORANGE);
					g.fillRect(screenX, yOffset + graphLineHeigh - size, width, 2 * size);

					g.setColor(forwardColor);
					g.fillRect(screenX, yOffset + graphLineHeigh - fsize, width, fsize);

					g.setColor(reverseColor);
					g.fillRect(screenX, yOffset + graphLineHeigh, width, rsize);
				}
				// System.out.println("Show individual");

				g.setColor(Color.GRAY);

			}

			g.setColor(Color.BLACK);

			yOffset += 2 * graphLineHeigh;

			// System.out.println("DD:"+Arrays.deepToString(nc.counter));

			/*
			 * Draw SNP track if possible. This depends on drawing the
			 * individual reads first as during the iteration over all reads, we
			 * store the polymorphisms.
			 */

			Sequence sb = entry.sequence().subsequence(visible.start, visible.end + 1);
			char[] seqBuffer = new char[visible.length()];
			int idx = 0;
			for (char cc : sb.get()) {
				seqBuffer[idx++] = cc;
			}
			// System.out.println("Array: "+Arrays.toString(seqBuffer));

			char[] nucs = new char[] { 'A', 'T', 'G', 'C' };
			Color[] color = new Color[4];
			for (int i = 0; i < 4; i++)
				color[i] = Configuration.getNucleotideColor(nucs[i]);
			int nucWidth = (int) (Math.ceil(screenWidth / visible.length()));
			if (ptm.nc.hasData() && seqBuffer != null) {

				g.setColor(Colors.LIGHEST_GRAY);
				g.fillRect(0, yOffset, (int) screenWidth, snpTrackHeight);
				g.setColor(Color.LIGHT_GRAY);
				g.drawLine(0, yOffset + snpTrackHeight / 2, (int) screenWidth, yOffset + snpTrackHeight / 2);
				g.setColor(Color.BLACK);
				g.drawString("SNPs", 5, yOffset + snpTrackHeight - 4);
				// System.out.println("Drawing snps: "+visible);
				// int skipped = 0;
				// int totl=0;
				for (int i = visible.start; i <= visible.end; i++) {
					int x1 = Convert.translateGenomeToScreen(i, visible, screenWidth);
					double total = ptm.nc.getTotalCount(i - visible.start);
					char refNt = seqBuffer[i - visible.start];
					double done = 0;// Fraction gone to previous nucs
					// System.out.println(seqBuffer[i -
					// visible.start]+" "+total);
					if (total > snpTrackMinimumCoverage) {
						for (int j = 0; j < 4; j++) {
							if (nucs[j] != refNt) {
								double fraction = ptm.nc.getCount(nucs[j], i - visible.start) / total;
								fraction *= snpTrackHeight;
								// totl++;
								if (fraction > 0.05) {
									g.setColor(color[j]);
									g.fillRect(x1, (int) (yOffset + snpTrackHeight - fraction - done), nucWidth,
											(int) (Math.ceil(fraction)));
								}
								// else {
								// skipped++;
								// }
								done += fraction;
							}
						}
					}

				}
				// System.out.println("Skipped: "+skipped +"/"+totl);
			}
			/* Draw tick labels on coverage plot */
			g.setColor(Color.BLACK);
			g.drawLine(0, yOffset, 5, yOffset);
			g.drawLine(0, yOffset - graphLineHeigh, 5, yOffset - graphLineHeigh);
			g.drawLine(0, yOffset - 2 * graphLineHeigh, 5, yOffset - 2 * graphLineHeigh);

			g.drawString("" + div, 10, yOffset);
			g.drawString("0" + "", 10, yOffset - graphLineHeigh + 5);
			g.drawString("" + div, 10, yOffset - 2 * graphLineHeigh + 10);

			g.drawString(StaticUtils.shortify(super.dataKey.toString()), 10, yOffset - graphLineHeigh + 24 - 2);

			return 2 * graphLineHeigh + snpTrackHeight;
		} else {/* Draw coverage lines */

			/* Draw status */
			int chunkWidth = (int) Math.ceil(PileupSummary.CHUNK * screenWidth / visible.length());
			for (int i = visible.start; i < visible.end + PileupSummary.CHUNK; i += PileupSummary.CHUNK) {
				int x = Convert.translateGenomeToScreen((i / PileupSummary.CHUNK) * PileupSummary.CHUNK, visible,
						screenWidth);
				g.setColor(Color.red);
				if (summary.isQueued(i / PileupSummary.CHUNK))
					g.setColor(Color.ORANGE);
				if (summary.isRunning(i / PileupSummary.CHUNK))
					g.setColor(Color.GREEN);
				if (!summary.isReady(i / PileupSummary.CHUNK))
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

			int vs = visible.start / PileupSummary.SUMMARYSIZE * PileupSummary.SUMMARYSIZE + PileupSummary.SUMMARYSIZE
					/ 2;
			double topValue = summary.getMaxSummary();
			// double range = topValue - bottomValue;

			conservationGP.moveTo(-5, yOffset + graphLineHeigh);

			for (int i = vs; i < visible.end + PileupSummary.SUMMARYSIZE; i += PileupSummary.SUMMARYSIZE) {
				if (!summary.isReady(i / PileupSummary.CHUNK))
					continue;
				int x = Convert.translateGenomeToScreen(i, visible, screenWidth);

				// double valF = f[i] - 1;
				// double valR = r[i] - 1;
				// System.out.println("P-" + (i / SUMMARYSIZE) + " " + summary[i
				// / SUMMARYSIZE]);
				// try {
				int idx = i / PileupSummary.SUMMARYSIZE;
				if (idx >= summary.length()) {
					// System.err.println(idx);
					idx = summary.length() - 1;
				}
				double val = summary.getValue(idx);// /
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
				// // valR = bottomValue;
				// if (val < bottomValue)
				// val = bottomValue;

				/* Translate for bottom point */
				// valF -= bottomValue;
				// valR -= bottomValue;
				// val -= bottomValue;
				/* Logaritmic scaling */
				if (ptm.isLogscaling()) {
					val = log2(val + 1);
					val /= log2(summary.getMaxSummary());
					/* Regular scaling */
				} else {
					val /= summary.getMaxSummary();
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
			g.setColor(Color.BLACK);

			/* Draw tick labels */
			g.drawLine(0, yOffset, 5, yOffset);

			g.drawString("" + nf.format(summary.getMaxSummary() / (double) PileupSummary.SUMMARYSIZE), 10, yOffset + 10);
			g.drawString("0", 10, yOffset + graphLineHeigh);

			g.drawString(StaticUtils.shortify(super.dataKey.toString()), 10, yOffset + 24 - 2);
			return graphLineHeigh;

		}

	}

	private void count(NucCounter nc, Pile p, Location visible) {
		// System.out.print("P: "+p.getPos());
		byte[] reads = p.getBases();
		for (int i = 0; i < reads.length; i++) {
			try {
				char c = (char) reads[i];
				if (c == '^') {
					i++;
					continue;
				} else if (c == '-' || c == '+') {
					int jump = reads[++i];
					i += Integer.parseInt("" + (char) jump);
					continue;
				}
				/* Might have jumped past the end */
				if (i < reads.length)
					nc.count((char) reads[i], p.getPos() - visible.start);
			} catch (NumberFormatException ne) {
				log.log(Level.WARNING, "Pileup parser failed on line: " + new String(reads), ne);
				System.err.println("Pileup parser failed on line: " + new String(reads));
			}
		}
		// .out.println();

	}

	/* User settable maximum value for charts, use negative for unlimited */
	
	// FIXME move to track model
	private double maxValue = -1;

	@Override
	public List<JMenuItem> getMenuItems() {
		ArrayList<JMenuItem> out = new ArrayList<JMenuItem>();
		
		/* Use global settings */
		final JCheckBoxMenuItem itemGlobal = new JCheckBoxMenuItem();
		itemGlobal.setSelected(ptm.isGlobalSettings());
		itemGlobal.setAction(new AbstractAction("Use global settings for scaling") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ptm.setGlobalSettings(itemGlobal.isSelected());

			}

		});
		out.add(itemGlobal);
		
		/* Log scaling of line graph */
		final JCheckBoxMenuItem item = new JCheckBoxMenuItem();
		item.setSelected(ptm.isLogscaling());
		item.setAction(new AbstractAction("Use log scaling for line graph") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ptm.setLogscaling(item.isSelected());

			}

		});
		out.add(item);

		/* Dynamic scaling for both plots */
		final JCheckBoxMenuItem item3 = new JCheckBoxMenuItem();
		item3.setSelected(ptm.isDynamicScaling());
		item3.setAction(new AbstractAction("Use dynamic scaling for plots") {
			@Override
			public void actionPerformed(ActionEvent e) {
				ptm.setDynamicScaling(item3.isSelected());

			}

		});
		out.add(item3);

		/* Maximum value */
		final JMenuItem item2 = new JMenuItem(new AbstractAction("Set maximum value") {

			@Override
			public void actionPerformed(ActionEvent e) {
				String in = JOptionPane.showInputDialog(model.getGUIManager().getParent(),
						"Input the maximum value, choose a negative number for unlimited", "Input maximum value",
						JOptionPane.QUESTION_MESSAGE);
				if (in != null) {
					try {
						Double d = Double.parseDouble(in);
						maxValue = d;
					} catch (Exception ex) {
						log.log(Level.WARNING, "Unparseble value for maximum in PileupTrack: " + in, ex);
					}
				}

			}
		});
		out.add(item2);
		return out;
	}

	@Override
	public String displayName() {
		return "Pileup: " + super.dataKey;
	}
}
