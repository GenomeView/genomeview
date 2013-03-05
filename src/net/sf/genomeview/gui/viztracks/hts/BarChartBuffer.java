/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.NoFailIterable;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.DataCallback;
import net.sf.genomeview.data.provider.PileProvider;
import net.sf.genomeview.data.provider.Status;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.tdf.ReadType;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class BarChartBuffer implements VizBuffer,DataCallback<Pile> {
	/* Data for pileupgraph barchart */
	private double[][] detailedRects = null;

	/* Actual coverage values for barchart */
	// private double[][] covValues = null;
	private Location visible;
	private double localMaxPile = 0;
	private double localMinPile = 0;
	private NucCounter nc;
	private PileupTrackConfig ptm;
	private Logger log = Logger.getLogger(BarChartBuffer.class.toString());
	private PileProvider provider;

	private int pileWidth = 1;
	private boolean exact = false;

	private double MAX_WIDTH = 2000;
	private Iterable<Status> status;

	
	
	public BarChartBuffer(Model model,Location visible, PileProvider provider, PileupTrackConfig ptm) {
		this.visible = visible;
		this.provider = provider;
		this.ptm = ptm;
		this.model=model;

		
//		status = provider.getStatus(visible.start, visible.end);

		/*Iterable<Pile> itt =*/ provider.get(visible.start, visible.end + 1,this);

		
	//	System.out.println("Halt!");
	}

	private void initArray(Pile p, int length) {
		if (visible.length() < MAX_WIDTH) {
			exact = true;
			detailedRects = new double[p.getValueCount()][visible.length()];
		} else {
			detailedRects = new double[p.getValueCount()][(int) MAX_WIDTH];
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
					nc.count((char) reads[i], p.start() - visible.start);
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

		int graphLineHeigh = Configuration.getInt("shortread:graphLineHeight");
		// int snpTrackHeight =
		// Configuration.getInt("shortread:snpTrackHeight");
		int snpTrackMinimumCoverage = Configuration.getInt("shortread:snpTrackMinimumCoverage");

		double range = provider.getMaxPile() - localMinPile;
		// System.out.println("provider max="+provider.getMaxPile());
		// System.out.println("local minpile ="+localMinPile);
		// System.out.println("local maxpile="+localMaxPile);
		// System.out.println("DIV:"+range);
		if (ptm.isDynamicScaling())
			range = localMaxPile - localMinPile;
		if (ptm.isCrossTrackScaling()) {
			// System.out.println("Using TCM.. " +
			// ptm.getTrackCommunication().getLocalPileupMax());
			range = ptm.getTrackCommunication().getLocalPileupMax() - localMinPile;
		}

		if (ptm.maxValue() > 0) {
			range = ptm.maxValue() - localMinPile;
		}

		if (detailedRects != null) {
			switch (detailedRects.length) {
			case 4:
				// TODO should this be 2*range?
				drawFour(g, range, graphLineHeigh, screenWidth, yOffset);
				break;
			case 2:
				drawTwo(g, range, graphLineHeigh, screenWidth, yOffset);
				break;
			default:
				drawOne(g, range, graphLineHeigh, screenWidth, yOffset);
				break;

			}
		}

		// }

		yOffset += 2 * graphLineHeigh;

		int snpTrackHeight = 0;
		if (visible.length() < MAX_WIDTH) {
			Sequence sb = ptm.sequence().subsequence(visible.start, visible.end + 1);
			char[] seqBuffer = new char[visible.length()];
			int idx = 0;
			for (char cc : sb.get()) {
				seqBuffer[idx++] = cc;
			}

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

				for (int i = visible.start; i <= visible.end; i++) {
					// System.out.println("it: "+i);
					int x1 = Convert.translateGenomeToScreen(i, visible, screenWidth);
					double total = nc.getTotalCount(i - visible.start);
					char refNt = seqBuffer[i - visible.start];
					double done = 0;// Fraction gone to previous nucs

					if (total > snpTrackMinimumCoverage) {
						for (int j = 0; j < 4; j++) {
							if (nucs[j] != refNt) {
								double fraction = nc.getCount(nucs[j], i - visible.start) / total;
								fraction *= snpTrackHeight;

								g.setColor(color[j]);

								g.fillRect(x1, (int) (yOffset + snpTrackHeight - fraction - done), nucWidth, (int) (Math.ceil(fraction)));

								done += fraction;
							}
						}
					}

				}

			}
		}

		int returnTrackHeight = 2 * graphLineHeigh + snpTrackHeight;
//		Track.paintStatus(g, status, yOffset - 2 * graphLineHeigh, returnTrackHeight, visible, screenWidth);

		return returnTrackHeight;

	}

	private void drawFour(Graphics2D g, double range, int graphLineHeigh, double screenWidth, int yOffset) {
		//range = 2 * range;
		Color forwardColor = Configuration.getColor("shortread:forwardColor");
		Color reverseColor = Configuration.getColor("shortread:reverseColor");
		Color forwardAntiColor = Configuration.getColor("shortread:forwardAntiColor");
		Color reverseAntiColor = Configuration.getColor("shortread:reverseAntiColor");

		int lastX = -10;

		if (ptm.isLogscaling()) {
			range = log2(range);
			range*=2;

		}
		for (int i = 0; detailedRects != null && i < detailedRects[0].length; i++) {

			double f1cov = detailedRects[ReadType.FIRSTREADFORWARDMAP.ordinal()][i] - localMinPile;
			double f2cov = detailedRects[ReadType.SECONDREADFORWARDMAP.ordinal()][i] - localMinPile;
			double r1cov = detailedRects[ReadType.FIRSTREADREVERSEMAP.ordinal()][i] - localMinPile;
			double r2cov = detailedRects[ReadType.SECONDREADREVERSEMAP.ordinal()][i] - localMinPile;
			if (ptm.isLogscaling()) {
				if (f1cov >= 1)
					f1cov = log2(f1cov);
				else
					f1cov=0;
				if (f2cov >=1)
					f2cov = log2(f2cov);
				else
					f2cov=0;
				if (r1cov >= 1)
					r1cov = log2(r1cov);
				else
					r1cov=0;
				if (r2cov >= 1)
					r2cov = log2(r2cov);
				else
					r2cov=0;
			}

			double coverage = f1cov + r1cov + f2cov + r2cov;
			/* Max value set, truncate */
			//
			// if (ptm.maxValue() > 0) {
			// div = ptm.maxValue();
			if (coverage > range)
				coverage = range;
			if (r1cov > range)
				r1cov = range;
			if (f1cov > range)
				f1cov = range;
			if (r2cov > range)
				r2cov = range;
			if (f2cov > range)
				f2cov = range;
			// }
			double frac = coverage / range;
			int size = (int) (frac * graphLineHeigh);

			double f1frac = f1cov / range;
			int f1size = (int) (f1frac * graphLineHeigh);
			double r1frac = r1cov / range;
			int r1size = (int) (r1frac * graphLineHeigh);

			double f2frac = f2cov / range;
			int f2size = (int) (f2frac * graphLineHeigh);
			double r2frac = r2cov / range;
			int r2size = (int) (r2frac * graphLineHeigh);

			double factor = MAX_WIDTH / visible.length();
			int sLoc = (int) ((i / factor) + visible.start);
			int eLoc = (int) (((i + 1) / factor) + 1 + visible.start);
			if (exact) {
				sLoc = (int) ((i) + visible.start);
				eLoc = (int) (((i)) + 1 + visible.start);
			}
//			if(f2cov+r1cov+(f1cov+r2cov)>0)
//				System.out.println("LOC: "+sLoc+"\t"+eLoc+"\t"+(f2cov+r1cov)+"\t"+(f1cov+r2cov));
			int screenX1 = Convert.translateGenomeToScreen(sLoc, visible, screenWidth);
			int screenX2 = Convert.translateGenomeToScreen(eLoc, visible, screenWidth);
			// System.out.println("Screen: "+screenX1+"\t"+screenX2);
			// System.out.println(frac+"\t"+ffrac+"\t"+rfrac);
			if (screenX1 > lastX) {
				lastX = screenX1;
				g.setColor(Colors.LIGHEST_GRAY);
				g.fillRect(screenX1, yOffset + graphLineHeigh - size, screenX2 - screenX1 + 1, 2 * size);

				/* --- */
				g.setColor(forwardAntiColor);
				g.fillRect(screenX1, yOffset + graphLineHeigh, screenX2 - screenX1 + 1, f1size);

				g.setColor(reverseAntiColor);
				g.fillRect(screenX1, yOffset + graphLineHeigh + f1size, screenX2 - screenX1 + 1, r2size);

				/* --- */
				g.setColor(reverseColor);
				g.fillRect(screenX1, yOffset + graphLineHeigh - r1size, screenX2 - screenX1 + 1, r1size);

				g.setColor(forwardColor);
				g.fillRect(screenX1, yOffset + graphLineHeigh - r1size - f2size, screenX2 - screenX1 + 1, f2size);

			}
			// System.out.println("Show individual");

			g.setColor(Color.GRAY);
		}

		g.setColor(Color.BLACK);
		for (Line line : ptm.getLines()) {
			// g.fillRect(screenX1, yOffset + graphLineHeigh - size, screenX2 -
			// screenX1 + 1, 2 * size);
			if (line.value() - localMinPile < range) {
				int tY = (int) (((line.value() - localMinPile) / range) * graphLineHeigh);
				g.drawLine(0, yOffset + graphLineHeigh - tY, (int) screenWidth, yOffset + graphLineHeigh - tY);
				g.drawLine(0, yOffset + graphLineHeigh + tY, (int) screenWidth, yOffset + graphLineHeigh + tY);
			}
		}

		yOffset += 2 * graphLineHeigh;
		/* Draw tick labels on coverage plot */

		g.setColor(Color.BLACK);
		g.drawLine(0, yOffset, 5, yOffset);
		g.drawLine(0, yOffset - graphLineHeigh, 5, yOffset - graphLineHeigh);
		g.drawLine(0, yOffset - 2 * graphLineHeigh, 5, yOffset - 2 * graphLineHeigh);

		g.drawString("" + nrReg.format(range + localMinPile), 10, yOffset);
		g.drawString("" + nrReg.format(localMinPile), 10, yOffset - graphLineHeigh + 5);
		g.drawString("" + nrReg.format(range + localMinPile), 10, yOffset - 2 * graphLineHeigh + 10);

	}

	private void drawOne(Graphics2D g, double range, int graphLineHeigh, double screenWidth, int yOffset) {

		int lastX = -10;
		double lastFrac = 0;
		if (ptm.isLogscaling()) {
			// val = log2(val + 1);
			range = log2(range);
			// val /= log2(provider.getMaxPile());
			/* Regular scaling */
		}
		for (int i = 0; detailedRects != null && i < detailedRects[0].length; i++) {

			/* Val is normalized to baseline zero with localMinPile */
			double val = 0;
			for (int j = 0; j < detailedRects.length; j++)
				val += detailedRects[j][i] - localMinPile;

			if (ptm.isLogscaling())
				val = log2(val);

			if (val > range)
				val = range;

			double frac = val / range;
			int size = (int) (frac * graphLineHeigh);

			double factor = MAX_WIDTH / visible.length();
			int sLoc = (int) ((i / factor) + visible.start);
			int eLoc = (int) (((i + 1) / factor) + 1 + visible.start);
			if (exact) {
				// System.out.println("Exact!");
				sLoc = (int) ((i) + visible.start);
				eLoc = (int) (((i)) + 1 + visible.start);
			}
			// System.out.println("LOC: "+sLoc+"\t"+eLoc);
			int screenX1 = Convert.translateGenomeToScreen(sLoc, visible, screenWidth);
			int screenX2 = Convert.translateGenomeToScreen(eLoc, visible, screenWidth);
			if (screenX2 == screenX1)
				screenX2 = screenX1 + 1;
			// if(frac>0)
			// System.out.println("Screen: "+screenX1+"\t"+screenX2+"\t"+frac);
			// System.out.println(frac+"\t"+ffrac+"\t"+rfrac);
			if (screenX1 > lastX || frac > lastFrac) {
				lastX = screenX1;
				lastFrac = frac;
				g.setColor(Color.ORANGE);
				g.fillRect(screenX1, yOffset + 2 * graphLineHeigh - 2 * size, screenX2 - screenX1 + 1, 2 * size);

			}
			g.setColor(Color.GRAY);
		}

		yOffset += 2 * graphLineHeigh;

		g.setColor(Color.BLACK);
		for (Line line : ptm.getLines()) {
			// g.fillRect(screenX1, yOffset + graphLineHeigh - size, screenX2 -
			// screenX1 + 1, 2 * size);
			if (line.value() - localMinPile < range) {
				int tY = (int) (((line.value() - localMinPile) / range) * 2 * graphLineHeigh);
				g.drawLine(0, yOffset - tY, (int) screenWidth, yOffset - tY);
				// g.drawLine(0, yOffset + graphLineHeigh + tY, (int)
				// screenWidth, yOffset + graphLineHeigh + tY);
			}
		}

		g.setColor(Color.BLACK);
		g.drawLine(0, yOffset, 5, yOffset);
		g.drawLine(0, yOffset - graphLineHeigh, 5, yOffset - graphLineHeigh);
		g.drawLine(0, yOffset - 2 * graphLineHeigh, 5, yOffset - 2 * graphLineHeigh);

		g.drawString(nrReg.format(localMinPile), 10, yOffset);
		// g.drawString("0" + "", 10, yOffset - graphLineHeigh + 5);
		g.drawString(nrReg.format(range + localMinPile), 10, yOffset - 2 * graphLineHeigh + 10);
	}

	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}

	private void drawTwo(Graphics g, double range, int graphLineHeigh, double screenWidth, int yOffset) {
		Color forwardColor = Configuration.getColor("shortread:forwardColor");
		Color reverseColor = Configuration.getColor("shortread:reverseColor");
		int lastX = -10;

		if (ptm.isLogscaling()) {
			// val = log2(val + 1);
			range = log2(range);
			// val /= log2(provider.getMaxPile());
			/* Regular scaling */
		}
		for (int i = 0; detailedRects != null && i < detailedRects[0].length; i++) {
			// int snpOffset = yOffset;
			double fcov = detailedRects[0][i] - localMinPile;
			double rcov = detailedRects[1][i] - localMinPile;
			if (ptm.isLogscaling()) {
				if (fcov > 0)
					fcov = log2(fcov);
				if (rcov > 0)
					rcov = log2(rcov);
			}

			double coverage = fcov + rcov;
			/* Max value set, truncate */
			//
			// if (ptm.maxValue() > 0) {
			// div = ptm.maxValue();
			if (coverage > range)
				coverage = range;
			if (rcov > range)
				rcov = range;
			if (fcov > range)
				fcov = range;

			// }
			double frac = coverage / range;
			int size = (int) (frac * graphLineHeigh);
			double ffrac = fcov / range;
			int fsize = (int) (ffrac * graphLineHeigh);
			double rfrac = rcov / range;
			int rsize = (int) (rfrac * graphLineHeigh);
			double factor = MAX_WIDTH / visible.length();
			int sLoc = (int) ((i / factor) + visible.start);
			int eLoc = (int) (((i + 1) / factor) + 1 + visible.start);
			if (exact) {
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
			if (line.value() - localMinPile < range) {
				int tY = (int) (((line.value() - localMinPile) / range) * graphLineHeigh);
				g.drawLine(0, yOffset + graphLineHeigh - tY, (int) screenWidth, yOffset + graphLineHeigh - tY);
				g.drawLine(0, yOffset + graphLineHeigh + tY, (int) screenWidth, yOffset + graphLineHeigh + tY);
			}
		}

		yOffset += 2 * graphLineHeigh;
		/* Draw tick labels on coverage plot */

		g.setColor(Color.BLACK);
		g.drawLine(0, yOffset, 5, yOffset);
		g.drawLine(0, yOffset - graphLineHeigh, 5, yOffset - graphLineHeigh);
		g.drawLine(0, yOffset - 2 * graphLineHeigh, 5, yOffset - 2 * graphLineHeigh);

		g.drawString("" + nrReg.format(range + localMinPile), 10, yOffset);
		g.drawString("" + nrReg.format(localMinPile), 10, yOffset - graphLineHeigh + 5);
		g.drawString("" + nrReg.format(range + localMinPile), 10, yOffset - 2 * graphLineHeigh + 10);

	}

	private String format(int count, int total) {
		if (total > 0)
			return count + " (" + nf.format(count / (double) total) + ")";
		else
			return "" + count;
	}

	private NumberFormat nf = NumberFormat.getPercentInstance(Locale.US);
	private NumberFormat nrReg = NumberFormat.getInstance(Locale.US);

	private Model model;

	@Override
	public String getTooltip(int mouseX) {
		double factor = MAX_WIDTH / visible.length();
		nf.setMinimumFractionDigits(0);
		nf.setMaximumFractionDigits(3);
		nrReg.setMinimumFractionDigits(0);
		nrReg.setMaximumFractionDigits(3);
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
		if (detailedRects != null) {
			text.append("<strong>" + (pileWidth > 1 ? "Average " : "") + "Coverage:</strong> ");
			if (detailedRects.length == 4) {
				double f1cov = detailedRects[ReadType.FIRSTREADFORWARDMAP.ordinal()][effectivePosition];
				double f2cov = detailedRects[ReadType.SECONDREADFORWARDMAP.ordinal()][effectivePosition];
				double r1cov = detailedRects[ReadType.FIRSTREADREVERSEMAP.ordinal()][effectivePosition];
				double r2cov = detailedRects[ReadType.SECONDREADREVERSEMAP.ordinal()][effectivePosition];
				double sense = f2cov + r1cov;
				double antisense = r2cov + f1cov;
				double reverse=r1cov+r2cov;
				double forward=f1cov+f2cov;
				text.append(nrReg.format(sense+antisense) + "<br/>");
				text.append("Forward transcript: " + nrReg.format(sense)+" ("+nrReg.format(r1cov)+" - "+nrReg.format(f2cov)+")" + "<br/>");
				text.append("Reverse transcript: " + nrReg.format(antisense) +" ("+nrReg.format(f1cov)+" - "+nrReg.format(r2cov)+")" + "<br/>");
//				text.append("Forward mapping: " + nrReg.format(forward) + "<br/>");
//				text.append("Reverse mapping: " + nrReg.format(reverse) + "<br/>");
				

			} else if (detailedRects.length == 2) {
				text.append(nrReg.format(detailedRects[0][effectivePosition] + detailedRects[1][effectivePosition]) + "<br/>");
				text.append("Forward: " + nrReg.format(detailedRects[0][effectivePosition]) + "<br/>");
				text.append("Reverse: " + nrReg.format(detailedRects[1][effectivePosition]) + "<br/>");
			} else {
				double val = 0;
				for (int j = 0; j < detailedRects.length; j++)
					val += detailedRects[j][effectivePosition];
				text.append(nrReg.format(val) + "<br/>");
			}
		} else {
			text.append("<strong>No coverage in this region</strong> ");
		}
		text.append("</html>");
		return text.toString();
	}

	@Override
	public void dataReady(Location dataLocation, List<Pile> itt) {
		//FIXME add check on dataLocation to make sure this is the right data
		double factor = MAX_WIDTH / visible.length();
		if (exact)
			factor = 1;

		/* Variables for SNP track */
		if (visible.length() < MAX_WIDTH)
			nc = new NucCounter(visible.length());
		else
			nc = null;
		
		for (Pile p : new NoFailIterable<Pile>(itt)) {
			if (p == null) {
				System.out.println("Null pile");
				continue;
			}
			if (detailedRects == null)
				initArray(p, visible.length());

			if (p.start() + p.getLength() < visible.start || p.start() > visible.end) {

				continue;
			}

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

//			if(p.getTotal()>0)
//				System.out.println("Pile: \t"+startPos+"\t"+startIdx+"\t"+endPos+"\t"+endIdx+"\t"+p.getTotal());
			for (int i = startIdx; i <= endIdx; i++) {
				if (i >= 0 && i < detailedRects[0].length) {
					// float fcov = p.getFCoverage();
					// float rcov = p.getRCoverage();
					// int pos=i - visible.start;
					for (int j = 0; j < p.getValueCount(); j++) {
						double val = p.getValue(j);
						if (ptm.isNormalizationAvailable() && ptm.isNormalizeMean() && ptm.normalizationEngine.value() != null) {
							val /= ptm.normalizationEngine.value()[j];
						}

						if (val > detailedRects[j][i])
							detailedRects[j][i] = val;
					}
					// detailedRects[1][i] = rcov;
					// covValues[0][i] = fcov;
					// covValues[1][i] = rcov;

					double coverage = p.getTotal();
					if (ptm.isNormalizationAvailable() && ptm.isNormalizeMean() && ptm.normalizationEngine.value() != null) {
						coverage /= ptm.normalizationEngine.value()[0];
					}
					ptm.getTrackCommunication().updateLocalPileupMax(coverage, visible);

					if (coverage > localMaxPile)
						localMaxPile = coverage;
					if (coverage < localMinPile)
						localMinPile = coverage;
				}
			}
			
		}
		model.refresh();
	}
}
