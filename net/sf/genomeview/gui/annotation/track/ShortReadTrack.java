/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;

import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.ShortRead;
import net.sf.jannot.Strand;

public class ShortReadTrack extends Track {

	public ShortReadTrack(Model model) {
		super(model, true, false);
	}

	class Buffer {
		double LOG2 = Math.log(2);
		int bareScale = 32;
		int bareScaleIndex = 5;
		private short[] counts;
		private int maxCount;

		public Buffer(short[] counts, int maxCount) {
			this.counts = counts;
			this.maxCount = maxCount;
		}

		private List<float[]> buffer = new ArrayList<float[]>();

		public double get(int start, int scale) {

			if (scale < bareScale) {
				double conservation = 0;
				for (int j = 0; j < scale; j++) {
					// if (a.isAligned(start + j))
					// FIXME out of bounds when scrolling completely to the
					// right
					conservation += counts[start + j];
				}
				return conservation / (scale * maxCount);
			}
			if (buffer.size() == 0)
				buffer.add(bare());

			int index = (int) (Math.log(scale) / LOG2) - bareScaleIndex;

			while (buffer.size() <= index + 1) {
				buffer.add(merge(buffer.get(buffer.size() - 1)));
			}

			return buffer.get(index)[start / scale];
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

			float[] out = new float[counts.length / bareScale + 1];
			for (int i = 0; i < counts.length; i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale && i + j < counts.length; j++) {
					conservation += counts[i + j];
				}
				conservation /= bareScale * maxCount;
				out[i / bareScale] = conservation;

			}
			return out;
		}

	}

	private Map<Entry, Buffer> buffers = new HashMap<Entry, Buffer>();
	private Map<Entry, Buffer> buffersA = new HashMap<Entry, Buffer>();
	private Map<Entry, Buffer> buffersC = new HashMap<Entry, Buffer>();
	private Map<Entry, Buffer> buffersT = new HashMap<Entry, Buffer>();
	private Map<Entry, Buffer> buffersG = new HashMap<Entry, Buffer>();

	private Location cachedLocation = null;
	private List<Rectangle> cachedRectangles = new ArrayList<Rectangle>();
	private List<Color> cachedColors = new ArrayList<Color>();

	private List<Integer> cachedIndices = new ArrayList<Integer>();
	private int lines = 0;

	@Override
	public int paint(Graphics gg, Entry entry, int yOffset, double screenWidth) {
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
		if (!buffers.containsKey(entry)) {
			buffers.put(entry, new Buffer(entry.shortReads.counts(), entry.shortReads.maxCount()));
			buffersA.put(entry, new Buffer(entry.shortReads.aCounts(), 10000));
			buffersC.put(entry, new Buffer(entry.shortReads.cCounts(), 10000));
			buffersG.put(entry, new Buffer(entry.shortReads.gCounts(), 10000));
			buffersT.put(entry, new Buffer(entry.shortReads.tCounts(), 10000));
			// write(entry.shortReads.counts());
		}
		Buffer b = buffers.get(entry);

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
		// System.out.println("CV="+checkValue);
		g.setColor(Color.BLACK);
		g.draw(conservationGP);
		g.setColor(Color.BLUE);
		g.drawString(this.displayName() + " (" + scale + ")", 10, yOffset + graphLineHeigh - 2);
		yOffset += graphLineHeigh;

		/*
		 * Draw line plot of nucleotide frequencies
		 */
		Buffer bA = buffersA.get(entry);
		Buffer bC = buffersC.get(entry);
		Buffer bG = buffersG.get(entry);
		Buffer bT = buffersT.get(entry);
		GeneralPath conservationGPA = new GeneralPath();
		GeneralPath conservationGPC = new GeneralPath();
		GeneralPath conservationGPG = new GeneralPath();
		GeneralPath conservationGPT = new GeneralPath();
		for (int i = 0; i < (end - start) / scale; i++) {
			int x = Convert.translateGenomeToScreen(start + i * scale + 1, r, screenWidth) + 5;
			// conservationGP.lineTo(x, yOffset + (1 - cValues[i]) *
			// (lineHeigh - 4) + 2);

			double vA = bA.get(start + i * scale, scale);
			double vC = bC.get(start + i * scale, scale);
			double vG = bG.get(start + i * scale, scale);
			double vT = bT.get(start + i * scale, scale);
			// if(i==100)
			// checkValue=v;
			if (i == 0) {

				conservationGPA.moveTo(x - 1, yOffset + (1 - vA) * (graphLineHeigh - 4) + 2);
				conservationGPC.moveTo(x - 1, yOffset + (1 - vC) * (graphLineHeigh - 4) + 2);
				conservationGPG.moveTo(x - 1, yOffset + (1 - vG) * (graphLineHeigh - 4) + 2);
				conservationGPT.moveTo(x - 1, yOffset + (1 - vT) * (graphLineHeigh - 4) + 2);

			}

			conservationGPA.lineTo(x, yOffset + (1 - vA) * (graphLineHeigh - 4) + 2);
			conservationGPC.lineTo(x, yOffset + (1 - vC) * (graphLineHeigh - 4) + 2);
			conservationGPG.lineTo(x, yOffset + (1 - vG) * (graphLineHeigh - 4) + 2);
			conservationGPT.lineTo(x, yOffset + (1 - vT) * (graphLineHeigh - 4) + 2);
		}
		
		g.setColor(Configuration.getNucleotideColor('a'));
		g.draw(conservationGPA);
		g.setColor(Configuration.getNucleotideColor('c'));
		g.draw(conservationGPC);
		g.setColor(Configuration.getNucleotideColor('g'));
		g.draw(conservationGPG);
		g.setColor(Configuration.getNucleotideColor('t'));
		g.draw(conservationGPT);
		g.setColor(Color.BLUE);
		g.drawString("Nuc. freq. (" + scale + ")", 10, yOffset + graphLineHeigh - 2);
		yOffset += graphLineHeigh;
		/*
		 * Draw individual reads when possible
		 */
		List<ShortRead> reads = null;
		if (r.length() > 1000000) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big, zoom in", 10, yOffset + 10);
			yOffset += 20 + 5;
		} else {
			reads = entry.shortReads.get(model.getAnnotationLocationVisible());
		}
		if (reads != null) {
			int limit = 100 * Configuration.getInt("annotationview:maximumNoVisibleFeatures");
			if (reads.size() > limit) {
				g.setColor(Color.BLACK);
				g.drawString("Too many short reads to display, zoom in, " + reads.size() + " reads, limit=" + limit, 10, yOffset + 10);
				yOffset += 20 + 5;
			} else {
				// System.out.println("Painting: " + reads.size() + " reads");
				if (cachedLocation != null && r.start() == cachedLocation.start() && r.end() == cachedLocation.end()) {
					// Same place, everything is cached
				} else {
					cachedLocation = r;
					cachedColors.clear();
					cachedRectangles.clear();
					cachedIndices.clear();
					lines = 0;
					int readLength = entry.shortReads.readLength();
					BitSet[] tilingCounter = new BitSet[r.length()];
					for (int i = 0; i < tilingCounter.length; i++) {
						tilingCounter[i] = new BitSet();
					}
					int readIndex = -1;
					for (ShortRead rf : reads) {
						readIndex++;
						Color c = Color.GRAY;

						if (rf.strand() == Strand.FORWARD)
							c = Color.BLUE;
						else
							c = new Color(0x00, 0x99, 0x00);
						cachedColors.add(c);
						// int x1 = Convert.translateGenomeToScreen(rf.start(),
						// model.getAnnotationLocationVisible(), screenWidth);
						int x2 = Convert.translateGenomeToScreen(rf.end() + 1, model.getAnnotationLocationVisible(), screenWidth);

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

							int subX1 = Convert.translateGenomeToScreen(rf.start(), model.getAnnotationLocationVisible(), screenWidth);
							int subX2 = Convert.translateGenomeToScreen(rf.end() + 1, model.getAnnotationLocationVisible(), screenWidth);
							Rectangle rec = new Rectangle(subX1, line * readLineHeight + yOffset, subX2 - subX1, readLineHeight - 1);
							cachedRectangles.add(rec);
							cachedIndices.add(readIndex);

						}
					}

				}

				/* Actual painting */
				for (int i = 0; i < cachedRectangles.size(); i++) {
					g.setColor(cachedColors.get(i));
					Rectangle rec = cachedRectangles.get(i);
					g.fillRect(rec.x, rec.y, rec.width, rec.height);

					/* Check mismatches */
					if (r.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
						ShortRead rf = reads.get(cachedIndices.get(i));
						for (int j = rf.start(); j <= rf.end(); j++) {
							char readNt = rf.getNucleotide(j - rf.start() + 1);
							char refNt;
							if (rf.strand() == Strand.FORWARD) {
								refNt = Character.toUpperCase(entry.sequence.getNucleotide(j));
							} else
								refNt = Character.toUpperCase(entry.sequence.getReverseNucleotide(j));
							int x1 = Convert.translateGenomeToScreen(j, model.getAnnotationLocationVisible(), screenWidth);
							int x2 = Convert.translateGenomeToScreen(j + 1, model.getAnnotationLocationVisible(), screenWidth);

							if (readNt != refNt) {
								g.setColor(Color.ORANGE);
								g.fillRect(x1, rec.y, x2 - x1, rec.height);
								if (model.getAnnotationLocationVisible().length() < 100) {
									g.setColor(cachedColors.get(i));
									Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + readNt, g);
									// g.setColor(Color.black);
									g.drawString("" + readNt, (int) (x1 + ((x2 - x1) / 2 - stringSize.getWidth() / 2)), rec.y + readLineHeight - 3);
									// g.drawChars(new char[] { readNt }, 0, 1,
									// x1, rec.y);
								}
							}

							// g.setColor(Color.CYAN);
							// g.drawRect(x1, rec.y, x2 - x1, rec.height);

						}
					}

				}
				if (r.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
					System.out.println("Reads visible: " + reads.size());
				}

				yOffset += (lines + 1) * readLineHeight + 5;

			}
		}

		return yOffset - originalYOffset;
	}

	

	@Override
	public String displayName() {
		return "Short reads";
	}
}
