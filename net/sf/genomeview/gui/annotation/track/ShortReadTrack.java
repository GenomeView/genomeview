/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.shortread.BAMreads;
import net.sf.jannot.shortread.MemoryReadSet;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.shortread.ShortRead;
import net.sf.jannot.source.DataSource;

public class ShortReadTrack extends Track {

	private DataSource source;
	private ReadBuffer readBuffer;

	public ShortReadTrack(Model model, DataSource source) {
		super(model, true, true);
		this.source = source;
		this.readBuffer = new ReadBuffer(source);
	}

	class ReadBuffer {

		class Runner implements Runnable {

			private Entry localEntry;

			public Runner(Entry e) {
				this.localEntry = e;
			}

			@Override
			public void run() {
				buffer.clear();
				System.out.println("Start buffering: " + localEntry);
				for (ShortRead sr : localEntry.shortReads.getReadGroup(source)) {
					if (localEntry != entry) {
						return;
					} else {
						buffer.add(sr);
					}
					if(buffer.size()>2000000){
						buffer.clear();
						System.out.println("Buffering is not working out, sorry!");
						JOptionPane.showMessageDialog(null, "Buffering is not working out, too much data.","Warning!",JOptionPane.WARNING_MESSAGE);
						return;
					}

				}
				System.out.println("Buffering done: " + localEntry);
				ready = true;

			}
		}

		private DataSource source;
		private Entry entry = null;
		private boolean ready = false;
		private MemoryReadSet buffer = new MemoryReadSet();

		public ReadBuffer(DataSource source) {
			this.source = source;
		}

		public synchronized Iterable<ShortRead> get(Entry e, Location r) {
			if (entry == e) {
				if (ready)
					return buffer.get(r);
				else
					return e.shortReads.getReadGroup(source).get(r);
			} else {
				entry = e;
				ready = false;
				new Thread(new Runner(entry)).start();
				return e.shortReads.getReadGroup(source).get(r);
			}

		}
	}

	/**
	 * Keeps an eye on a read group
	 * 
	 * @author tabeel
	 * 
	 */
	static class GraphBuffer implements Observer {

		private double log(int val) {
			if (val > 0)
				return Math.log(val);
			else
				return 0;
		}

		double LOG2 = Math.log(2);
		int bareScale = 32;
		int bareScaleIndex = 5;

		private ReadGroup rg;

		public GraphBuffer(ReadGroup rg) {
			rg.addObserver(this);
			this.rg = rg;
		}

		private List<float[]> buffer = new ArrayList<float[]>();

		public synchronized double get(int start, int scale) {
			if (start + scale >= rg.getPileUp().size())
				return 0;
			if (scale < bareScale) {
				double conservation = 0;
				for (int j = 0; j < scale; j++) {
					conservation += log(rg.getPileUp().get(start + j));
				}
				return conservation / (scale * log(rg.getMaxPile()));
			}
			if (buffer.size() == 0)
				buffer.add(bare());

			int index = (int) (Math.log(scale) / LOG2) - bareScaleIndex;

			while (buffer.size() <= index + 1) {
				buffer.add(merge(buffer.get(buffer.size() - 1)));
			}

			if (start / scale < buffer.get(index).length)
				return buffer.get(index)[start / scale];
			else
				return -0.02;
		}

		private float[] merge(float[] ds) {
			float[] out = new float[(ds.length + 1) / 2];
			double max = 0;
			for (int i = 0; i < ds.length - 1; i += 2) {
				out[i / 2] = (ds[i] + ds[i + 1]) / 2;
				if (out[i / 2] > max)
					max = out[i / 2];
			}
			out[out.length - 1] = ds[ds.length - 1];
			for (int i = 0; i < out.length; i++)
				out[i] /= max;
			return out;
		}

		private float[] bare() {
			int size = rg.getPileUp().size();
			float[] out = new float[size / bareScale + 1];
			for (int i = 0; i < size; i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale && i + j < size; j++) {
					conservation += log(rg.getPileUp().get(i + j));
				}
				conservation /= bareScale * log(rg.getMaxPile());
				out[i / bareScale] = conservation;

			}
			return out;
		}

		@Override
		public synchronized void update(Observable o, Object arg) {
			buffer.clear();
		}

	}

	private Map<Entry, GraphBuffer> buffers = new HashMap<Entry, GraphBuffer>();

	@Override
	public int paint(Graphics gg, final Entry entry, int yOffset, double screenWidth) {

		/* Configuration options */
		int maxReads = Configuration.getInt("shortread:maxReads");
		int maxRegion = Configuration.getInt("shortread:maxRegion");
		int maxStack = Configuration.getInt("shortread:maxStack");

		Location r = model.getAnnotationLocationVisible();

		int originalYOffset = yOffset;
		Graphics2D g = (Graphics2D) gg;

		int graphLineHeigh = 50;
		int readLineHeight = 3;
		if (r.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			readLineHeight = 14;
		}

		/*
		 * Draw line plot of coverage
		 */
		g.setColor( new Color(204, 238, 255, 100));
		g.fillRect(0, yOffset, (int)screenWidth, graphLineHeigh);
		double width = screenWidth / (double) r.length() / 2.0;

		int scale = 1;
		while (scale < (int) Math.ceil(1.0 / width))
			scale *= 2;

		int start = r.start() / scale * scale;
		int end = ((r.end() / scale) + 1) * scale;

		ReadGroup rg = entry.shortReads.getReadGroup(source);
		// System.out.println(entry.shortReads+"\t"+rg+"\t"+source);
		if (rg != null) {
			if (!buffers.containsKey(entry)) {
				// System.out.println("Construction size: " + tmp);
				buffers.put(entry, new GraphBuffer(rg));

			}
			// write(entry.shortReads.counts());
		} else {
			return 0; /* Not yet ready */
		}
		GraphBuffer b = buffers.get(entry);
		if (b != null) {
			GeneralPath conservationGP = new GeneralPath();
			for (int i = 0; i < (end - start) / scale; i++) {
				int x = Convert.translateGenomeToScreen(start + i * scale + 1, r, screenWidth) + 5;
				// conservationGP.lineTo(x, yOffset + (1 - cValues[i]) *
				// (lineHeigh - 4) + 2);
				double v = b.get(start + i * scale, scale);

				// if(i==100)
				// checkValue=v;
				if (i == 0) {
					conservationGP.moveTo(x - 1, yOffset + (1 - v) * (graphLineHeigh - 4) + 2);

				}
				conservationGP.lineTo(x, yOffset + (1 - v) * (graphLineHeigh - 4) + 2);

			}
			g.setColor(Color.BLACK);
			g.draw(conservationGP);
			g.setColor(Color.BLUE);
			g.drawString(source.toString() + " (" + scale + ")", 10, yOffset + 12 - 2);
			yOffset += graphLineHeigh;
		}

		// /*
		// * Draw line plot of nucleotide frequencies
		// */
		// Buffer bA = buffersA.get(entry);
		// Buffer bC = buffersC.get(entry);
		// Buffer bG = buffersG.get(entry);
		// Buffer bT = buffersT.get(entry);
		// if (bA != null && bC != null && bG != null && bT != null) {
		// GeneralPath conservationGPA = new GeneralPath();
		// GeneralPath conservationGPC = new GeneralPath();
		// GeneralPath conservationGPG = new GeneralPath();
		// GeneralPath conservationGPT = new GeneralPath();
		// for (int i = 0; i < (end - start) / scale; i++) {
		// int x = Convert.translateGenomeToScreen(start + i * scale + 1, r,
		// screenWidth) + 5;
		// // conservationGP.lineTo(x, yOffset + (1 - cValues[i]) *
		// // (lineHeigh - 4) + 2);
		//
		// double vA = bA.get(start + i * scale, scale);
		// double vC = bC.get(start + i * scale, scale);
		// double vG = bG.get(start + i * scale, scale);
		// double vT = bT.get(start + i * scale, scale);
		// // if(i==100)
		// // checkValue=v;
		// if (i == 0) {
		//
		// conservationGPA.moveTo(x - 1, yOffset + (1 - vA) * (graphLineHeigh -
		// 4) + 2);
		// conservationGPC.moveTo(x - 1, yOffset + (1 - vC) * (graphLineHeigh -
		// 4) + 2);
		// conservationGPG.moveTo(x - 1, yOffset + (1 - vG) * (graphLineHeigh -
		// 4) + 2);
		// conservationGPT.moveTo(x - 1, yOffset + (1 - vT) * (graphLineHeigh -
		// 4) + 2);
		//
		// }
		//
		// conservationGPA.lineTo(x, yOffset + (1 - vA) * (graphLineHeigh - 4) +
		// 2);
		// conservationGPC.lineTo(x, yOffset + (1 - vC) * (graphLineHeigh - 4) +
		// 2);
		// conservationGPG.lineTo(x, yOffset + (1 - vG) * (graphLineHeigh - 4) +
		// 2);
		// conservationGPT.lineTo(x, yOffset + (1 - vT) * (graphLineHeigh - 4) +
		// 2);
		// }
		//
		// g.setColor(Configuration.getNucleotideColor('a'));
		// g.draw(conservationGPA);
		// g.setColor(Configuration.getNucleotideColor('c'));
		// g.draw(conservationGPC);
		// g.setColor(Configuration.getNucleotideColor('g'));
		// g.draw(conservationGPG);
		// g.setColor(Configuration.getNucleotideColor('t'));
		// g.draw(conservationGPT);
		// g.setColor(Color.BLUE);
		// g.drawString("Nuc. freq. (" + scale + ")", 10, yOffset +
		// graphLineHeigh - 2);
		// yOffset += graphLineHeigh;
		// }
		/*
		 * Draw individual reads when possible
		 */
		Iterable<ShortRead> reads = null;
		if (!isCollapsed() && (r.length() > maxRegion)) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big (max " + maxRegion + " nt), zoom in", 10, yOffset + 10);
			yOffset += 20 + 5;
		} else if (!isCollapsed()) {
			/* Access to BAMread is through buffer for performance! */
			if (rg instanceof BAMreads) {
				reads = readBuffer.get(entry, r);
			} else {
				reads = rg.get(r);
			}
		}

		int lines = 0;
		boolean stackExceeded = false;
		if (reads != null) {
			lines = 0;
			int readLength = entry.shortReads.getReadGroup(source).readLength();
			BitSet[] tilingCounter = new BitSet[r.length()];
			for (int i = 0; i < tilingCounter.length; i++) {
				tilingCounter[i] = new BitSet();
			}
			int visibleReadCount = 0;
			try {
				for (ShortRead rf : reads) {

					if (visibleReadCount > maxReads) {

						String msg = "Too many short reads to display, only first " + maxReads + " are displayed ";
						FontMetrics metrics = g.getFontMetrics();
						int hgt = metrics.getHeight();
						int adv = metrics.stringWidth(msg);
						g.setColor(Color.WHITE);
						g.fillRect(10, yOffset + 20 - hgt, adv + 2, hgt + 2);
						g.setColor(Color.RED);
						g.drawString(msg, 10, yOffset + 18);
						break;
					}
					Color c = Color.GRAY;

					if (rf.strand() == Strand.FORWARD)
						c = Color.BLUE;
					else
						c = new Color(0x00, 0x99, 0x00);

					int x2 = Convert.translateGenomeToScreen(rf.end() + 1, r, screenWidth);
					if (x2 > 0) {
						/* Find empty line */
						int line = 0;
						int pos = rf.start() - r.start();
						if (pos >= 0 && pos < tilingCounter.length)
							line = tilingCounter[rf.start() - r.start()].nextClearBit(line);
						else
							line = tilingCounter[0].nextClearBit(line);

						/*
						 * FIXME add additional checks for Extended reads that
						 * may cover other reads
						 */

						if (line < maxStack) {
							for (int i = rf.start() - 1 - readLength; i <= rf.end() + 1; i++) {
								pos = i - r.start();
								if (pos >= 0 && pos < tilingCounter.length)
									tilingCounter[pos].set(line);
							}

							if (line > lines)
								lines = line;

							int subX1 = Convert.translateGenomeToScreen(rf.start(), r, screenWidth);
							int subX2 = Convert.translateGenomeToScreen(rf.end() + 1, r, screenWidth);
							if (subX2 < subX1) {
								subX2 = subX1;
							}

							if (visibleReadCount + 100 > maxReads)
								g.setColor(Color.RED);
							else
								g.setColor(c);

							int yRec = line * readLineHeight + yOffset;
							g.fillRect(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);
							visibleReadCount++;
							/* Check mismatches */
							if (r.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
								for (int j = rf.start(); j <= rf.end(); j++) {
									char readNt = rf.getNucleotide(j - rf.start() + 1);
									char refNt = Character.toUpperCase(entry.sequence.getNucleotide(j));
									double tx1 = Convert.translateGenomeToScreen(j, r, screenWidth);
									double tx2 = Convert.translateGenomeToScreen(j + 1, r, screenWidth);

									if (readNt != refNt) {
										if (readNt == '-'){
//											System.out.println("RED");
											g.setColor(Color.RED);
										}else
											g.setColor(Color.ORANGE);
										g.fillRect((int) tx1, yRec, (int) (tx2 - tx1), readLineHeight - 1);
										if (model.getAnnotationLocationVisible().length() < 100) {
											g.setColor(c);
											Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + readNt, g);
											g.drawString("" + readNt, (int) (tx1 + ((tx2 - tx1) / 2 - stringSize.getWidth() / 2)), yRec + readLineHeight - 3);

										}
									}
								}
							}
						} else {
							stackExceeded = true;
						}
					}
				}
			} catch (ConcurrentModificationException e) {
				// Ignore
			}
			if (stackExceeded) {
				g.setColor(Color.RED);
				g.drawString("Max stacking depth reached!", 5, lines * readLineHeight + yOffset - 2);
				g.drawLine(0, lines * readLineHeight + yOffset, (int) screenWidth, lines * readLineHeight + yOffset);
				lines++;

			}
			yOffset += (lines + 1) * readLineHeight + 5;

		}
		return yOffset - originalYOffset;
	}

	@Override
	public String displayName() {
		return "Short reads: " + source.toString();
	}

	public DataSource source() {
		return source;
	}
}
