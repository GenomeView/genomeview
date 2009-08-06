/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
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

	private Location cachedLocation = null;
	private List<Rectangle> cachedRectangles = new ArrayList<Rectangle>();
	private List<Color> cachedColors = new ArrayList<Color>();
	private int lines = 0;

	@Override
	public int paint(Graphics gg, Entry entry, int yOffset, double screenWidth) {
		Location r = model.getAnnotationLocationVisible();

		int originalYOffset = yOffset;
		Graphics2D g = (Graphics2D) gg;

		int graphLineHeigh = 50;
		int readLineHeight = 3;
		/*
		 * Draw line plot
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
			write(entry.shortReads.counts());
		}
		Buffer b = buffers.get(entry);
		// double[] cValues = cache.get();
		// int cStart = cache.start();
		// int cScale = cache.scale();

		// System.out.println("SRsteps= "+((end - start) / scale));
		// double checkValue=-1;
		GeneralPath conservationGP = new GeneralPath();

		for (int i = 0; i < (end - start) / scale; i++) {
			int x = Convert.translateGenomeToScreen(start + i * scale, r, screenWidth) + 5;
			// conservationGP.lineTo(x, yOffset + (1 - cValues[i]) *
			// (lineHeigh - 4) + 2);
			double v = b.get(start + i * scale, scale);
			// if(i==100)
			// checkValue=v;
			if (i == 0)
				conservationGP.moveTo(x - 1, yOffset + (1 - v) * (graphLineHeigh - 4) + 2);
			conservationGP.lineTo(x, yOffset + (1 - v) * (graphLineHeigh - 4) + 2);
		}
		// System.out.println("CV="+checkValue);
		g.setColor(Color.BLACK);
		g.draw(conservationGP);
		g.setColor(Color.BLUE);
		g.drawString(this.displayName() + " (" + scale + ")", 10, yOffset + graphLineHeigh - 2);
		yOffset += graphLineHeigh;
		/*
		 * Draw individual reads when possible
		 */
		List<ShortRead> reads = null;
		if (r.length() > 250000) {
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
//				System.out.println("Painting: " + reads.size() + " reads");
				if (cachedLocation != null && r.start() == cachedLocation.start() && r.end() == cachedLocation.end()) {
					// Same place, everything is cached
				} else {
					cachedLocation = r;
					cachedColors.clear();
					cachedRectangles.clear();
					lines=0;

					BitSet[] tilingCounter = new BitSet[r.length()];
					for (int i = 0; i < tilingCounter.length; i++) {
						tilingCounter[i] = new BitSet();
					}
					for (ShortRead rf : reads) {

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
							boolean found = false;
							int line = 0;
							while (!found) {
								boolean hit = false;
								for (int i = rf.start(); i <= rf.end(); i++) {
									int pos = i - r.start();
									if (pos >= 0 && pos < tilingCounter.length && tilingCounter[pos].get(line)) {
										hit = true;
										break;
									}
								}
								if (!hit) {
									found = true;
									for (int i = rf.start() - 1; i <= rf.end() + 1; i++) {
										int pos = i - r.start();
										if (pos >= 0 && pos < tilingCounter.length)
											tilingCounter[pos].set(line);
									}
								} else {
									line++;
								}
							}
							if (line > lines)
								lines = line;

							int subX1 = Convert.translateGenomeToScreen(rf.start(), model.getAnnotationLocationVisible(), screenWidth);
							int subX2 = Convert.translateGenomeToScreen(rf.end() + 1, model.getAnnotationLocationVisible(), screenWidth);
							Rectangle rec = new Rectangle(subX1, line * readLineHeight + yOffset, subX2 - subX1, readLineHeight - 1);
							cachedRectangles.add(rec);

						}
					}

				}

				/* Actual painting */
				for (int i = 0; i < cachedRectangles.size(); i++) {
					g.setColor(cachedColors.get(i));
					Rectangle rec = cachedRectangles.get(i);
					g.fillRect(rec.x, rec.y, rec.width, rec.height);
				}
				yOffset += (lines + 1) * readLineHeight + 5;

			}
		}

		return yOffset - originalYOffset;
	}

	private void write(short[] counts) {
		PrintWriter out;
		try {
			out = new PrintWriter("counts.log");
			out.println(Arrays.toString(counts));
			out.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public String displayName() {
		return "Short reads";
	}
}
