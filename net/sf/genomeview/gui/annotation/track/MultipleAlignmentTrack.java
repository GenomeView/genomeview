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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.alignment.Alignment;

public class MultipleAlignmentTrack extends Track {
	private int index;
	private String name;
	double LOG2 = Math.log(2);
	int bareScale = 32;
	int bareScaleIndex = 5;
	private Map<Entry, Buffer> buffers = new HashMap<Entry, Buffer>();

	class Buffer {
		private Alignment a;

		Buffer(Alignment a) {
			this.a = a;
		}

		private List<float[]> buffer = new ArrayList<float[]>();

		public double get(int start, int scale) {

			if (scale < bareScale) {
				double conservation = 0;
				for (int j = 0; j < scale; j++) {
					if (a.isAligned(start + j))
						conservation++;
				}
				return conservation / scale;
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

			float[] out = new float[a.refLength() / bareScale + 1];
			for (int i = 0; i < a.refLength(); i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale; j++) {
					if (a.isAligned(i + j))
						conservation++;
				}
				conservation /= bareScale;
				out[i / bareScale] = conservation;

			}
			return out;
		}

	}

	public MultipleAlignmentTrack(String name, int index, Model model, boolean b) {
		super(model, b, false);
		this.index = index;
		this.name = name;

	}

	@Override
	public String displayName() {
		return "MA: " + name;
	}

	static class Cache {
		private int cacheStart = -1;
		private int cacheEnd = -1;
		private int cacheScale = 1;
		private double[] cacheValues = null;

		public boolean hasData(int scale, int start, int end) {
			return scale == cacheScale && start >= cacheStart && end <= cacheEnd;
		}

		public void store(int scale, int start, int end, double[] cacheValues2) {
			this.cacheScale = scale;
			this.cacheStart = start;
			this.cacheEnd = end;
			this.cacheValues = cacheValues2;

		}

		public double[] get() {
			return cacheValues;
		}

		public int start() {
			return cacheStart;
		}

		public int end() {
			return cacheEnd;
		}

		public int scale() {
			return cacheScale;
		}

	}

	@Override
	public int paintTrack(Graphics2D g, Entry e, int yOffset, double screenWidth) {
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh = 20;

		Alignment align = e.alignment.getAlignment(index);

		if (align != null) {
			if (r.length() > 10000000) {
				g.setColor(Color.BLACK);
				g.drawString("Too much data in alignment, zoom in to see details", 5, yOffset + lineHeigh - 2);
				return lineHeigh;
			}
			if (r.length() < 1000) {
				double width = screenWidth / (double) r.length();
				int grouping = (int) Math.ceil(1.0 / width);
				for (int i = r.start(); i <= r.end(); i += grouping) {
					char nt = ' ';
					double conservation = 0;
					boolean dash = false;
					for (int j = 0; j < grouping; j++) {
						nt = align.getNucleotide(i + j);
						conservation += e.alignment.getConservation(i + j);
						if (nt == '-')
							dash = true;

					}
					conservation /= grouping;
					if ((int)conservation == 1) {
						// g.setColor(new Color(0x00,0x00,0x33));/*blue */
						// g.setColor(new Color(0x00,0x33,0x00));/*green */
						g.setColor(Color.BLACK);
					} else if (conservation > 0.75) {
						// g.setColor(new Color(0x00,0x66,0x00));/*green */
						// g.setColor(new Color(0x00,0x00,0x66));/*blue*/
						g.setColor(Color.DARK_GRAY);
					} else if (conservation > 0.5) {
						// g.setColor(new Color(0x00,0x00,0xff));/*blue*/
						// g.setColor(new Color(0x00,0xff,0x00));/*green */
						g.setColor(Color.LIGHT_GRAY);
					} else
						// g.setColor(new Color(0xcc,0xff,0x00));
						g.setColor(Color.WHITE);
					if (dash) {
						g.setColor(Color.RED);
					}

					g.fillRect((int) ((i - r.start()) * width), yOffset, (int) (width * grouping) + 1, lineHeigh);
					if (align.sizeGapAfter(i) > 0) {
						g.setColor(Color.ORANGE);
						g.fillRect((int) ((i - r.start()) * width + width * 3 / 4), yOffset, (int) (width * grouping) / 2 + 1, lineHeigh);
					}
					if (model.getAnnotationLocationVisible().length() < 100) {
						Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + nt, g);
						if (conservation > 0.75) {
							g.setColor(Color.WHITE);
						} else
							g.setColor(Color.BLACK);
						g.drawString("" + nt, (int) (((i - r.start()) * width - stringSize.getWidth() / 2) + (width / 2)), yOffset + lineHeigh - 2);
					}
				}
			
				FontMetrics metrics = g.getFontMetrics();
				int hgt = metrics.getHeight();
				int adv = metrics.stringWidth(this.displayName());
				
				g.setColor(Color.WHITE);
				g.fillRect(10, yOffset + lineHeigh-hgt, adv+2, hgt+2);
				
				g.setColor(Color.BLUE);
				// if (model.getAnnotationLocationVisible().length() >= 100)
				g.drawString(this.displayName(), 10, yOffset + lineHeigh - 2);
				return lineHeigh;
			} else {

				double width = screenWidth / (double) r.length() / 5.0;

				int scale = 1;
				while (scale < (int) Math.ceil(1.0 / width))
					scale *= 2;

				GeneralPath conservationGP = new GeneralPath();
				conservationGP.moveTo(0, yOffset);

				int start = r.start() / scale * scale;
				int end = ((r.end() / scale) + 1) * scale;
				// if (!cache.hasData(scale, r.start(), r.end())) {
				// double[] cacheValues = new double[(end - start) / scale];
				// Buffer b=buffers.get(e);
				// for (int i = 0; i < cacheValues.length; i++) {
				// double conservation = 0;
				// conservation = b.get(start + i * scale, scale);
				// cacheValues[i] = conservation;
				// }
				// cache.store(scale, start, end, cacheValues);
				// }
				// /* Plot whatever is in the cache */
				if (!buffers.containsKey(e))
					buffers.put(e, new Buffer(e.alignment.getAlignment(index)));
				Buffer b = buffers.get(e);
				// double[] cValues = cache.get();
				// int cStart = cache.start();
				// int cScale = cache.scale();
				for (int i = 0; i < (end - start) / scale; i++) {
					int x = Convert.translateGenomeToScreen(start + i * scale, r, screenWidth) + 5;
					// conservationGP.lineTo(x, yOffset + (1 - cValues[i]) *
					// (lineHeigh - 4) + 2);
					conservationGP.lineTo(x, yOffset + (1 - b.get(start + i * scale, scale)) * (lineHeigh - 4) + 2);
				}
				g.setColor(Color.BLACK);
				g.draw(conservationGP);
				g.setColor(Color.BLUE);
				g.drawString(this.displayName() + " (" + scale + ")", 10, yOffset + lineHeigh - 2);
				return lineHeigh;

			}

		}
		return 0;
	}

	public int getIndex() {
		return index;
	}
}
