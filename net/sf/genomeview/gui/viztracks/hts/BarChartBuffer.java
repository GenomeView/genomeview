/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.provider.PileProvider;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.refseq.Sequence;
/**
 * 
 * @author Thomas Abeel
 *
 */
class BarChartBuffer implements VizBuffer {
	/* Data for pileupgraph barchart */
	private int[][] detailedRects = null;
	private Location visible;
	private double localMaxPile = 0;
	private NucCounter nc;
	private PileupTrackModel ptm;
	private Logger log = Logger.getLogger(BarChartBuffer.class.toString());
	private PileProvider provider;
	
	
	public BarChartBuffer(Location visible, PileProvider provider, PileupTrackModel ptm) {
		this.visible = visible;
		this.provider=provider;
		this.ptm=ptm;
		
		
		detailedRects = new int[2][visible.length()];
		/* Variables for SNP track */
		nc = new NucCounter(visible.length());
		// g.setColor(Color.GRAY);
		// System.out.println("Building rects.");
		for (Pile p : provider.get(visible.start, visible.end)) {
			if (p.getPos() + p.getLength() < visible.start || p.getPos() > visible.end)
				continue;

			if (p.getBases() != null) {
				count(nc, p, visible);

			}
			

			int pos = p.getLocation().start;

			if (pos - visible.start >= 0) {
				int fcov = p.getFCoverage();
				int rcov = p.getRCoverage();

				detailedRects[0][pos - visible.start] = fcov;
				detailedRects[1][pos - visible.start] = rcov;

				double coverage = fcov + rcov;

				if (coverage > localMaxPile)
					localMaxPile = coverage;
			}

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

	@Override
	public int draw(Graphics2D g, int yOffset /*
											 * FIXME remove, should be done with
											 * g.translate
											 */, double screenWidth) {
		/* Get information from configuration */
		Color forwardColor = Configuration.getColor("shortread:forwardColor");
		Color reverseColor = Configuration.getColor("shortread:reverseColor");
		int graphLineHeigh = Configuration.getInt("shortread:graphLineHeight");
		int snpTrackHeight = Configuration.getInt("shortread:snpTrackHeight");
		int snpTrackMinimumCoverage = Configuration.getInt("shortread:snpTrackMinimumCoverage");

		// System.out.println("Drawing bar charts with " + pCount +
		// " piles.");
		int lastX = -1;
		double div = provider.getMaxPile();
		if (ptm.isDynamicScaling())
			div = localMaxPile;
//		System.out.println("Using " + div + ", dynamic: " + ptm.isDynamicScaling());
//		System.out.println("\tvalue 20: "+detailedRects[0][20]+"\t"+detailedRects[1][20]);
		int width = (int) Math.ceil(screenWidth / visible.length());
		for (int i = 0; i < detailedRects[0].length; i++) {
			// int snpOffset = yOffset;
			double fcov = detailedRects[0][i];
			double rcov = detailedRects[1][i];
			double coverage = fcov + rcov;
			/* Max value set, truncate */

			if (ptm.maxValue() > 0) {
				div = ptm.maxValue();
				if (coverage > div)
					coverage = div;
				if (rcov > div)
					rcov = div;
				if (fcov > div)
					fcov = div;

			}
			double frac = coverage / div;
			int size = (int) (frac * graphLineHeigh);
			double ffrac = fcov / div;
			int fsize = (int) (ffrac * graphLineHeigh);
			double rfrac = rcov / div;
			int rsize = (int) (rfrac * graphLineHeigh);
			int screenX = Convert.translateGenomeToScreen(i + visible.start, visible, screenWidth);
			// System.out.println(frac+"\t"+ffrac+"\t"+rfrac);
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
		 * Draw SNP track if possible. This depends on drawing the individual
		 * reads first as during the iteration over all reads, we store the
		 * polymorphisms.
		 */

		Sequence sb = ptm.sequence().subsequence(visible.start, visible.end + 1);
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
		if (nc.hasData() && seqBuffer != null) {

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
				double total = nc.getTotalCount(i - visible.start);
				char refNt = seqBuffer[i - visible.start];
				double done = 0;// Fraction gone to previous nucs
				// System.out.println(seqBuffer[i -
				// visible.start]+" "+total);
				if (total > snpTrackMinimumCoverage) {
					for (int j = 0; j < 4; j++) {
						if (nucs[j] != refNt) {
							double fraction = nc.getCount(nucs[j], i - visible.start) / total;
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
//		if (dataKey != null)
//			g.drawString(StaticUtils.shortify(dataKey.toString()), 10, yOffset - graphLineHeigh + 24 - 2);
//		else
			g.drawString(StaticUtils.shortify("no name"), 10, yOffset - graphLineHeigh + 24 - 2);

		return 2 * graphLineHeigh + snpTrackHeight;

	}

	private String format(int count, int total) {
		if (total > 0)
			return count + " (" + nf.format(count / (double) total) + ")";
		else
			return "" + count;
	}

	private NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);

	@Override
	public String getTooltip(int mouseX) {
		nf.setMaximumFractionDigits(1);
		StringBuffer text = new StringBuffer();

		int effectivePosition = Convert.translateScreenToGenome(mouseX, visible, ptm.getScreenWidth()) - visible.start;// track.translateFromMouse(e.getX());
		text.append("<html>");
		int total = nc.getTotalCount(effectivePosition);

		if (nc.hasData()) {
			text.append("<strong>Matches:</strong> " + format(nc.getCount('.', effectivePosition), total) + "<br/>");
			text.append("<strong>Mismatches:</strong><br/>");
			text.append("A: " + format(nc.getCount('A', effectivePosition), total));
			text.append("<br/>");
			text.append("T: " + format(nc.getCount('T', effectivePosition), total));
			text.append("<br/>");
			text.append("G: " + format(nc.getCount('G', effectivePosition), total));
			text.append("<br/>");
			text.append("C: " + format(nc.getCount('C', effectivePosition), total));
			text.append("<br/>");
		}
		text.append("<strong>Coverage:</strong> "
				+ (detailedRects[0][effectivePosition] + detailedRects[1][effectivePosition]) + "<br/>");
		text.append("Forward: " + detailedRects[0][effectivePosition] + "<br/>");
		text.append("Reverse: " + detailedRects[1][effectivePosition] + "<br/>");

		text.append("</html>");
		return text.toString();
	}

}