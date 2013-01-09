/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.provider.PileProvider;
import net.sf.genomeview.data.provider.Status;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Location;
import net.sf.jannot.pileup.DoublePile;
import net.sf.jannot.pileup.Pile;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class LineChartBuffer{
//	implements VizBuffer {
//
//
//	// private Iterable<Status> status;
//	// private Iterable<Pile> piles;
//	private Location visible;
//	private PileProvider provider;
//	private PileupTrackConfig ptm;
//
//	public LineChartBuffer(Location visible, PileProvider provider, PileupTrackConfig ptm) {
//		this.visible = visible;
//		this.provider = provider;
//		this.ptm = ptm;
//	}
//	private static final double LOG2 = Math.log(2);
//	private double log2(double d) {
//		return Math.log(d) / LOG2;
//	}
//
//	@Override
//	public int draw(Graphics2D g, int yOffset, double screenWidth) {
//		/* Get information from configuration */
////		Color forwardColor = Configuration.getColor("shortread:forwardColor");
////		Color reverseColor = Configuration.getColor("shortread:reverseColor");
//		int graphLineHeigh = Configuration.getInt("shortread:graphLineHeight");
////		int snpTrackHeight = Configuration.getInt("shortread:snpTrackHeight");
////		int snpTrackMinimumCoverage = Configuration.getInt("shortread:snpTrackMinimumCoverage");
//
//		/* Draw status */
//		// int chunkWidth = (int) Math.ceil(PileupSummary.CHUNK *
//		// screenWidth / visible.length());
//		for (Status t : provider.getStatus(visible.start, visible.end)) {
//			// for (int i = visible.start; i < visible.end +
//			// PileupSummary.CHUNK; i += PileupSummary.CHUNK) {
//			// int x = Convert.translateGenomeToScreen((i /
//			// PileupSummary.CHUNK) * PileupSummary.CHUNK, visible,
//			// screenWidth);
//			int x1 = Convert.translateGenomeToScreen(t.start(), visible, screenWidth);
//			int x2 = Convert.translateGenomeToScreen(t.end(), visible, screenWidth);
//
//			g.setColor(Color.red);
//			if (t.isQueued())
//				g.setColor(Color.ORANGE);
//			if (t.isRunning())
//				g.setColor(Color.GREEN);
//			if (!t.isReady())
//				g.fillRect(x1, yOffset, x2 - x1 + 1, graphLineHeigh);
//		}
//
//		/* Paint coverage plot */
//		// ShortReadCoverage graph = rg.getCoverage();// .get(rg);
//		// if (topValue < 0)
//		// topValue = graph.max();
//
//		GeneralPath conservationGP = new GeneralPath();
//		// GeneralPath conservationGPF = new GeneralPath();
//		// GeneralPath conservationGPR = new GeneralPath();
//
//		// float[] f = graph.get(Strand.FORWARD, start - 1, end,
//		// scaleIndex);
//		// float[] r = graph.get(Strand.REVERSE, start - 1, end,
//		// scaleIndex);
//
//		// double range = topValue - bottomValue;
//
//		// int vs = visible.start / PileupSummary.SUMMARYSIZE *
//		// PileupSummary.SUMMARYSIZE + PileupSummary.SUMMARYSIZE
//		// / 2;
//	//	double topValue = provider.getMaxPile();
//		// double range = topValue - bottomValue;
//
//		conservationGP.moveTo(-5, yOffset + graphLineHeigh);
//
//		// for (int i = vs; i < visible.end + PileupSummary.SUMMARYSIZE; i
//		// += PileupSummary.SUMMARYSIZE) {
//		// if (!summary.isReady(i / PileupSummary.CHUNK))
//		// continue;
//		
//		double div=provider.getMaxPile();
//		
//		/* Logaritmic scaling */
//		if (ptm.isLogscaling()) {
////			val = log2(val + 1);
//			div=log2(provider.getMaxPile());
////			val /= log2(provider.getMaxPile());
//			/* Regular scaling */
//		} 
//		
//		for (Pile p : provider.get(visible.start, visible.end)) {
//			//System.out.println("Pile: "+p.getPos()+"\t"+p.getLength()+"\t"+p.getCoverage());
//			int x1 = Convert.translateGenomeToScreen(p.start(), visible, screenWidth);
//			int x2 = Convert.translateGenomeToScreen(p.start() + p.getLength(), visible, screenWidth);
//			//System.out.println("\t"+x1+"\t"+x2);
//			// double valF = f[i] - 1;
//			// double valR = r[i] - 1;
//			// System.out.println("P-" + (i / SUMMARYSIZE) + " " + summary[i
//			// / SUMMARYSIZE]);
//			// try {
//			// int idx = i / PileupSummary.SUMMARYSIZE;
//			// if (idx >= summary.length()) {
//			// // System.err.println(idx);
//			// idx = summary.length() - 1;
//			// }
//			double val = p.getTotal();// .getValue(idx);// /
//			if (ptm.isLogscaling()) {
//				val = log2(val + 1);
//			}
//			// (double)maxSummary;//
//
//			// /f[i] +
//
//			// r[i] - 2;
//			/* Cap value */
//			// if (valF > topValue)
//			// valF = topValue;
//			// if (valR > topValue)
//			// valR = topValue;
//			// if (val > topValue)
//			// val = topValue;
//
//			// if (valF < bottomValue)
//			// valF = bottomValue;
//			// // valR = bottomValue;
//			// if (val < bottomValue)
//			// val = bottomValue;
//
//			/* Translate for bottom point */
//			// valF -= bottomValue;
//			// valR -= bottomValue;
//			// val -= bottomValue;
//			
//			
//			
//			val /= div;
//			// System.out.println("VAL: " + val);
//			/* Draw lines */
//			// if (i == vs) {
//			// conservationGP.moveTo(x - 1, yOffset + (1 - val) *
//			// (graphLineHeigh - 4) + 2);
//			// // conservationGPF.moveTo(x - 1, yOffset + (1 - valF) *
//			// // (graphLineHeigh - 4) + 2);
//			// // conservationGPR.moveTo(x - 1, yOffset + (1 - valR) *
//			// // (graphLineHeigh - 4) + 2);
//			// }
//
//			conservationGP.lineTo((x2 + x1) / 2, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
//			// conservationGPF.lineTo(x, yOffset + (1 - valF) *
//			// (graphLineHeigh - 4) + 2);
//			// conservationGPR.lineTo(x, yOffset + (1 - valR) *
//			// (graphLineHeigh - 4) + 2);
//			// } catch (ArrayIndexOutOfBoundsException e) {
//			// System.err.println(e);
//			// System.err.println(summary.length);
//			// System.out.println(entry.getMaximumLength());
//			// }
//		}
//		// for (int i = 0; i < f.length; i++) {
//		// int x = Convert.translateGenomeToScreen(start + i * scale,
//		// currentVisible, screenWidth);
//		// /* Coverage is stored +1 in SRC, needs correcting here */
//		// double valF = f[i] - 1;
//		// double valR = r[i] - 1;
//		// double val = f[i] + r[i] - 2;
//
//		// }
//
//		/* Draw coverage lines */
//		// g.setColor(forwardColor);
//		// g.draw(conservationGPF);
//		//
//		// g.setColor(reverseColor);
//		// g.draw(conservationGPR);
//		g.setColor(Color.BLACK);
//		conservationGP.lineTo(screenWidth + 5, yOffset + graphLineHeigh);
//		g.draw(conservationGP);
//		g.setColor(Color.BLACK);
//
//		/* Draw tick labels */
//		g.drawLine(0, yOffset, 5, yOffset);
//
//		g.drawString(""+div, 10, yOffset + 10);// +
//															// nf.format(provider.getMaxSummary()
//															// / (double)
//															// PileupSummary.SUMMARYSIZE),
//															// 10, yOffset +
//															// 10);
//		g.drawString("0", 10, yOffset + graphLineHeigh);
//
//		// if (dataKey != null)
//		// g.drawString(StaticUtils.shortify(dataKey.toString()), 10, yOffset +
//		// 24 - 2);
//		// else
//		
//		return graphLineHeigh;
//	}
//
//	@Override
//	public String getTooltip(int mouseX) {
//		// TODO Auto-generated method stub
//		return null;
//	}

}