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

import cern.colt.list.ShortArrayList;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.shortread.BAMreads;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.shortread.ShortRead;
import net.sf.jannot.source.DataSource;

public class ShortReadTrack extends Track {

	private DataSource source;

	public ShortReadTrack(Model model, DataSource source) {
		super(model, true, true);
		this.source = source;
	}

	/**
	 * Keeps an eye on a read group
	 * 
	 * @author tabeel
	 * 
	 */
	static class Buffer implements Observer {
		double LOG2 = Math.log(2);
		int bareScale = 32;
		int bareScaleIndex = 5;

		private ReadGroup rg;

		public Buffer(ReadGroup rg) {
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
					conservation += rg.getPileUp().get(start + j);
				}
				return conservation / (scale * rg.getMaxPile());
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
				return -1;
		}

		private float[] merge(float[] ds) {
			float[] out = new float[(ds.length + 1) / 2];
			for (int i = 0; i < ds.length - 1; i += 2) {
				out[i / 2] = (ds[i] + ds[i + 1]) / 2;
			}
			out[out.length - 1] = ds[ds.length - 1];
			return out;
		}

		private float[] bare() {
			int size = rg.getPileUp().size();
			float[] out = new float[size / bareScale + 1];
			for (int i = 0; i < size; i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale && i + j < size; j++) {
					conservation += rg.getPileUp().get(i + j);
				}
				conservation /= bareScale * rg.getMaxPile();
				out[i / bareScale] = conservation;

			}
			return out;
		}

		@Override
		public synchronized void update(Observable o, Object arg) {
			buffer.clear();
		}

	}

	private Map<Entry, Buffer> buffers = new HashMap<Entry, Buffer>();

	@Override
	public int paint(Graphics gg, final Entry entry, int yOffset, double screenWidth) {
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
		double width = screenWidth / (double) r.length() / 2.0;

		int scale = 1;
		while (scale < (int) Math.ceil(1.0 / width))
			scale *= 2;

		int start = r.start() / scale * scale;
		int end = ((r.end() / scale) + 1) * scale;

		// /* Plot whatever is in the cache */

		ReadGroup rg = entry.shortReads.getReadGroup(source);
		// System.out.println(entry.shortReads+"\t"+rg+"\t"+source);
		if (rg != null) {
			if (!buffers.containsKey(entry)) {
				// System.out.println("Construction size: " + tmp);
				buffers.put(entry, new Buffer(rg));

			}
			// write(entry.shortReads.counts());
		} else {
			return 0; /* Not yet ready */
		}
		Buffer b = buffers.get(entry);
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
			g.drawString(this.displayName() + " (" + scale + ")", 10, yOffset + 12 - 2);
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
		if (!isCollapsed()&&(r.length() > 1000000 || (rg instanceof BAMreads && r.length() > 25000))) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big, zoom in", 10, yOffset + 10);
			yOffset += 20 + 5;
		} else if (!isCollapsed()) {
			reads = rg.get(r);
		}
		// Location cachedLocation = null;
		// List<Rectangle> cachedRectangles = new ArrayList<Rectangle>();
		// List<Color> cachedColors = new ArrayList<Color>();
		//
		// List<Integer> cachedIndices = new ArrayList<Integer>();
		int lines = 0;
		if (reads != null) {
			int limit = 100 * Configuration.getInt("annotationview:maximumNoVisibleFeatures");

			lines = 0;
			int readLength = entry.shortReads.getReadGroup(source).readLength();
			BitSet[] tilingCounter = new BitSet[r.length()];
			for (int i = 0; i < tilingCounter.length; i++) {
				tilingCounter[i] = new BitSet();
			}
			int readIndex = -1;
			try {
				for (ShortRead rf : reads) {
					readIndex++;
					if (readIndex > limit) {

						String msg = "Too many short reads to display, only first " + limit + " are displayed ";
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

					// int x1 = Convert.translateGenomeToScreen(rf.start(),
					// model.getAnnotationLocationVisible(), screenWidth);
					int x2 = Convert.translateGenomeToScreen(rf.end() + 1, r, screenWidth);

					// TODO is this not always the case?
					if (x2 > 0) {
						/* Find empty line */
						int line = 0;
						int pos = rf.start() - r.start();
						if (pos >= 0 && pos < tilingCounter.length)
							line = tilingCounter[rf.start() - r.start()].nextClearBit(line);
						else
							line = tilingCounter[0].nextClearBit(line);
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

						if (readIndex + 100 > limit)
							g.setColor(Color.RED);
						else
							g.setColor(c);

						int yRec = line * readLineHeight + yOffset;
						g.fillRect(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);

						/* Check mismatches */
						if (r.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
							// ShortRead rf =
							// reads.get(cachedIndices.get(i));
							for (int j = rf.start(); j <= rf.end(); j++) {
								char readNt = rf.getNucleotide(j - rf.start() + 1);
								char refNt = Character.toUpperCase(entry.sequence.getNucleotide(j));
								double tx1 = Convert.translateGenomeToScreen(j, r, screenWidth);
								double tx2 = Convert.translateGenomeToScreen(j + 1, r, screenWidth);

								if (readNt != refNt) {
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
					}
				}
			} catch (ConcurrentModificationException e) {
				// Ignore
			}

			yOffset += (lines + 1) * readLineHeight + 5;

		}
		return yOffset - originalYOffset;
	}

	@Override
	public String displayName() {
		return "Short reads from " + source;
	}

	public DataSource source() {
		return source;
	}
}
