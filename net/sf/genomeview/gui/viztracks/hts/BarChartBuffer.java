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
import net.sf.genomeview.data.provider.Status;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.viztracks.Track;
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
	private double[][] detailedRects = null;

	/* Actual coverage values for barchart */
	// private double[][] covValues = null;
	private Location visible;
	private double localMaxPile = 0;
	private NucCounter nc;
	private PileupTrackModel ptm;
	private Logger log = Logger.getLogger(BarChartBuffer.class.toString());
	private PileProvider provider;

	private int pileWidth = 1;
	private boolean exact = false;

	private double MAX_WIDTH = 2000;
	private Iterable<Status> status;

	public BarChartBuffer(Location visible, PileProvider provider, PileupTrackModel ptm) {
		this.visible = visible;
		this.provider = provider;
		this.ptm = ptm;

		double factor = MAX_WIDTH / visible.length();
		// System.out.println("Factor: "+factor);
		if (visible.length() < MAX_WIDTH) {
			detailedRects = new double[2][visible.length()];
			exact = true;
		} else {
			detailedRects = new double[2][(int) MAX_WIDTH];
		}
		// covValues = new double[2][(int) MAX_WIDTH];
		/* Variables for SNP track */
		if (visible.length() < MAX_WIDTH)
			nc = new NucCounter(visible.length());
		else
			nc = null;
		status = provider.getStatus(visible.start, visible.end);

		// g.setColor(Color.GRAY);
		// System.out.println("Building rects.");
		// System.err.println(provider);
		Iterable<Pile> itt = provider.get(visible.start, visible.end + 1);
		// System.out.println(itt);
		for (Pile p : itt) {

			if (p == null) {
				System.out.println("Null pile");
				continue;
			}
			if (p.getPos() + p.getLength() < visible.start || p.getPos() > visible.end)
				continue;

			if (nc != null && p.getBases() != null) {
				count(nc, p, visible);

			}

			pileWidth = p.getLength();
			int startPos = p.start();
			int endPos = p.end();

			int startIdx = (int) ((startPos - visible.start) * factor);
			int endIdx = (int) ((endPos - visible.start) * factor);
			if (exact) {
				startIdx = startPos - visible.start;
				endIdx = endPos - visible.start;
			}
			// System.out.println("Idex: "+startPos+"\t"+startIdx);
			// System.out.println("\t"+endPos+"\t"+endIdx);
			for (int i = startIdx; i <= endIdx; i++) {
				if (i >= 0 && i < detailedRects[0].length) {
					float fcov = p.getFCoverage();
					float rcov = p.getRCoverage();
					// int pos=i - visible.start;

					detailedRects[0][i] = fcov;
					detailedRects[1][i] = rcov;
					// covValues[0][i] = fcov;
					// covValues[1][i] = rcov;

					double coverage = fcov + rcov;
					ptm.getTrackCommunication().updateLocalPileupMax(coverage, visible);

					if (coverage > localMaxPile)
						localMaxPile = coverage;
				}
			}

		}
	}

	private void count(NucCounter nc, Pile p, Location visible) {
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
				if (i < reads.length) {
					nc.count((char) reads[i], p.getPos() - visible.start);
				}
			} catch (NumberFormatException ne) {
				log.log(Level.WARNING, "Pileup parser failed on line: " + new String(reads), ne);
				System.err.println("Pileup parser failed on line: " + new String(reads));
			}
		}

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
		// int snpTrackHeight =
		// Configuration.getInt("shortread:snpTrackHeight");
		int snpTrackMinimumCoverage = Configuration.getInt("shortread:snpTrackMinimumCoverage");

		// System.out.println("Drawing bar charts with " + pCount +
		// " piles.");
		int lastX = -1;
		double div = provider.getMaxPile();
		if (ptm.isDynamicScaling())
			div = localMaxPile;
		if (ptm.isCrossTrackScaling()) {
			// System.out.println("Using TCM.. " +
			// ptm.getTrackCommunication().getLocalPileupMax());
			div = ptm.getTrackCommunication().getLocalPileupMax();
		}

		if (ptm.maxValue() > 0) {
			div = ptm.maxValue();
		}

		// System.out.println("Using " + div + ", dynamic: " +
		// ptm.isDynamicScaling());
		// System.out.println("\tvalue 20: "+detailedRects[0][20]+"\t"+detailedRects[1][20]);
		// int width = (int) Math.ceil(screenWidth / detailedRects[0].length);
		// System.out.println("Draw width:"+width);
		for (int i = 0; i < detailedRects[0].length; i++) {
			// int snpOffset = yOffset;
			double fcov = detailedRects[0][i];
			double rcov = detailedRects[1][i];
			double coverage = fcov + rcov;
			/* Max value set, truncate */
			//
			// if (ptm.maxValue() > 0) {
			// div = ptm.maxValue();
			if (coverage > div)
				coverage = div;
			if (rcov > div)
				rcov = div;
			if (fcov > div)
				fcov = div;

			// }
			double frac = coverage / div;
			int size = (int) (frac * graphLineHeigh);
			double ffrac = fcov / div;
			int fsize = (int) (ffrac * graphLineHeigh);
			double rfrac = rcov / div;
			int rsize = (int) (rfrac * graphLineHeigh);
			double factor = MAX_WIDTH / visible.length();
			int sLoc = (int) ((i / factor) + visible.start);
			int eLoc = (int) (((i + 1) / factor) + 1 + visible.start);
			if(exact){
				sLoc = (int) ((i) + visible.start);
				eLoc = (int) (((i)) + 1 + visible.start);
			}
			// System.out.println("LOC: "+sLoc+"\t"+eLoc);
			int screenX1 = Convert.translateGenomeToScreen(sLoc, visible, screenWidth);
			int screenX2 = Convert.translateGenomeToScreen(eLoc, visible, screenWidth);
			// System.out.println("Screen: "+screenX1+"\t"+screenX2);
			// System.out.println(frac+"\t"+ffrac+"\t"+rfrac);
			if (screenX1 > lastX) {
				lastX = screenX1;
				g.setColor(Color.ORANGE);
				g.fillRect(screenX1, yOffset + graphLineHeigh - size, screenX2 - screenX1 + 1, 2 * size);

				g.setColor(forwardColor);
				g.fillRect(screenX1, yOffset + graphLineHeigh - fsize, screenX2 - screenX1 + 1, fsize);

				g.setColor(reverseColor);
				g.fillRect(screenX1, yOffset + graphLineHeigh, screenX2 - screenX1 + 1, rsize);
			}
			// System.out.println("Show individual");

			g.setColor(Color.GRAY);

		}

		g.setColor(Color.BLACK);
		for (Line line : ptm.getLines()) {
			// g.fillRect(screenX1, yOffset + graphLineHeigh - size, screenX2 -
			// screenX1 + 1, 2 * size);
			if (line.value() < div) {
				int tY = (int) ((line.value() / div) * graphLineHeigh);
				g.drawLine(0, yOffset + graphLineHeigh - tY, (int) screenWidth, yOffset + graphLineHeigh - tY);
				g.drawLine(0, yOffset + graphLineHeigh + tY, (int) screenWidth, yOffset + graphLineHeigh + tY);
			}
		}

		yOffset += 2 * graphLineHeigh;

		int snpTrackHeight = 0;
		if (visible.length() < MAX_WIDTH) {
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
			if (nc != null && nc.hasData() && seqBuffer != null) {
				snpTrackHeight = Configuration.getInt("shortread:snpTrackHeight");
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
					// System.out.println("it: "+i);
					int x1 = Convert.translateGenomeToScreen(i, visible, screenWidth);
					double total = nc.getTotalCount(i - visible.start);
					char refNt = seqBuffer[i - visible.start];
					double done = 0;// Fraction gone to previous nucs
					// System.out.println(seqBuffer[i -
					// visible.start]+" "+total);
					// System.out.println("total: "+(i -
					// visible.start)+"\t"+total);
					if (total > snpTrackMinimumCoverage) {
						for (int j = 0; j < 4; j++) {
							if (nucs[j] != refNt) {
								double fraction = nc.getCount(nucs[j], i - visible.start) / total;
								fraction *= snpTrackHeight;
								// totl++;
								// if (fraction > 0.05) {
								g.setColor(color[j]);
								// System.out.println("filling: "+x1+"\t"+(int)
								// (yOffset + snpTrackHeight - fraction -
								// done));
								g.fillRect(x1, (int) (yOffset + snpTrackHeight - fraction - done), nucWidth,
										(int) (Math.ceil(fraction)));
								// }
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
		}

		/* Draw tick labels on coverage plot */
		g.setColor(Color.BLACK);
		g.drawLine(0, yOffset, 5, yOffset);
		g.drawLine(0, yOffset - graphLineHeigh, 5, yOffset - graphLineHeigh);
		g.drawLine(0, yOffset - 2 * graphLineHeigh, 5, yOffset - 2 * graphLineHeigh);

		g.drawString("" + nrReg.format(div), 10, yOffset);
		g.drawString("0" + "", 10, yOffset - graphLineHeigh + 5);
		g.drawString("" + nrReg.format(div), 10, yOffset - 2 * graphLineHeigh + 10);
		// if (dataKey != null)
		// g.drawString(StaticUtils.shortify(dataKey.toString()), 10, yOffset -
		// graphLineHeigh + 24 - 2);
		// else

		int returnTrackHeight = 2 * graphLineHeigh + snpTrackHeight;
		Track.paintStatus(g, status, yOffset - 2 * graphLineHeigh, returnTrackHeight, visible, screenWidth);

		return returnTrackHeight;

	}

	private String format(int count, int total) {
		if (total > 0)
			return count + " (" + nf.format(count / (double) total) + ")";
		else
			return "" + count;
	}

	private NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);
	private NumberFormat nrReg = NumberFormat.getInstance(Locale.US);

	@Override
	public String getTooltip(int mouseX) {
		double factor = MAX_WIDTH / visible.length();

		nf.setMaximumFractionDigits(1);
		nrReg.setMaximumFractionDigits(1);
		StringBuffer text = new StringBuffer();

		text.append("<html>");
		text.append("<strong>Window length: </strong>" + pileWidth + "<br/>");
		int ntPosition = Convert.translateScreenToGenome(mouseX, visible, ptm.getScreenWidth()) - visible.start;// track.translateFromMouse(e.getX());

		if (nc != null) {

			int total = nc.getTotalCount(ntPosition);

			if (nc.hasData()) {

				text.append("<strong>Matches:</strong> " + format(nc.getCount('.', ntPosition), total) + "<br/>");
				text.append("<strong>Mismatches:</strong><br/>");
				text.append("A: " + format(nc.getCount('A', ntPosition), total));
				text.append("<br/>");
				text.append("T: " + format(nc.getCount('T', ntPosition), total));
				text.append("<br/>");
				text.append("G: " + format(nc.getCount('G', ntPosition), total));
				text.append("<br/>");
				text.append("C: " + format(nc.getCount('C', ntPosition), total));
				text.append("<br/>");
			}

		}
		int effectivePosition = (int) (factor * (Convert.translateScreenToGenome(mouseX, visible, ptm.getScreenWidth()) - visible.start));// track.translateFromMouse(e.getX());
		if (exact)
			effectivePosition = Convert.translateScreenToGenome(mouseX, visible, ptm.getScreenWidth()) - visible.start;

		text.append("<strong>" + (pileWidth > 1 ? "Average" : "") + "Coverage:</strong> "
				+ nrReg.format(detailedRects[0][effectivePosition] + detailedRects[1][effectivePosition]) + "<br/>");
		text.append("Forward: " + nrReg.format(detailedRects[0][effectivePosition]) + "<br/>");
		text.append("Reverse: " + nrReg.format(detailedRects[1][effectivePosition]) + "<br/>");
		text.append("</html>");
		return text.toString();
	}
}
