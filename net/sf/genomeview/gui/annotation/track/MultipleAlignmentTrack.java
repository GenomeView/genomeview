/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Alignment;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;

public class MultipleAlignmentTrack extends Track {
	private int index;
	private String name;

	public MultipleAlignmentTrack(String name, int index, Model model, boolean b) {
		super(model, b);
		this.index = index;
		this.name = name;
	}

	@Override
	public String displayName() {
		return "MA: " + name;
	}

	class Cache {
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

	private Cache cache = new Cache();

	@Override
	public int paint(Graphics g1, Entry e, int yOffset, double screenWidth) {
		Graphics2D g = (Graphics2D) g1;
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh = 20;

		Alignment align = e.alignment.getAlignment(index);

		if (align != null) {
			if (r.length() > 1000000) {
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
					if (conservation == 1) {
						g.setColor(Color.BLACK);
					} else if (conservation > 0.75) {
						g.setColor(Color.DARK_GRAY);
					} else if (conservation > 0.5) {
						g.setColor(Color.LIGHT_GRAY);
					} else
						g.setColor(Color.WHITE);
					if (dash) {
						g.setColor(Color.RED);
					}

					g.fillRect((int) ((i - r.start()) * width), yOffset, (int) (width * grouping) + 1, lineHeigh);
					if (model.getAnnotationLocationVisible().length() < 100) {
						Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + nt, g);
						if (conservation == 1) {
							g.setColor(Color.WHITE);
						} else if (conservation > 0.75) {
							g.setColor(Color.WHITE);
						} else if (conservation > 0.5) {
							g.setColor(Color.BLACK);
						} else
							g.setColor(Color.BLACK);
						g.drawString("" + nt, (int) (((i - r.start()) * width - stringSize.getWidth() / 2) + (width / 2)), yOffset + lineHeigh - 2);
					}
				}
				g.setColor(Color.GREEN);
				if (model.getAnnotationLocationVisible().length() >= 100)
					g.drawString(this.displayName(), 10, yOffset + lineHeigh - 2);
				return lineHeigh;
			} else {

				double width = screenWidth / (double) r.length() / 5.0;

				int scale = 1;
				while (scale < (int) Math.ceil(1.0 / width))
					scale *= 2;

				GeneralPath conservationGP = new GeneralPath();
				conservationGP.moveTo(0, yOffset);
				Alignment alg = e.alignment.getAlignment(index);

				int start = r.start() / scale * scale;
				int end = ((r.end() / scale) + 1) * scale;
				if (!cache.hasData(scale, r.start(), r.end())) {
					double[] cacheValues = new double[(end - start) / scale];
					for (int i = 0; i < cacheValues.length; i++) {
						double conservation = 0;
						for (int j = 0; j < scale; j++) {
							if (alg.isAligned(start + i * scale + j))
								conservation++;
						}
						conservation /= scale;
						cacheValues[i] = conservation;
					}
					cache.store(scale, start, end, cacheValues);
				}
				/* Plot whatever is in the cache */
				double[] cValues = cache.get();
				int cStart = cache.start();
				int cScale = cache.scale();
				for (int i = 0; i < cValues.length; i++) {
					int x = Convert.translateGenomeToScreen(cStart + i * cScale, r, screenWidth) + 5;
					conservationGP.lineTo(x, yOffset + (1 - cValues[i]) * (lineHeigh - 4) + 2);
				}
				g.setColor(Color.BLACK);
				g.draw(conservationGP);
				g.setColor(Color.BLUE);
				g.drawString(this.displayName() + " (" + cScale + ")", 10, yOffset + lineHeigh - 2);
				return lineHeigh;

			}

		}
		return 0;
	}

	public int getIndex() {
		return index;
	}
}
