/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.comparative;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.swing.JViewport;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.alignment.mfa.Alignment;
import net.sf.jannot.alignment.mfa.AlignmentAnnotation;

/**
 * The multiple alignment visualization for multi-fasta files
 */
public class MultipleAlignmentTrack extends Track {
//	private String name;
	
	
//	double LOG2 = Math.log(2);
//	int bareScale = 32;
//	int bareScaleIndex = 5;
	private Map<Alignment, float[]> pips = new HashMap<Alignment, float[]>();

//	class Buffer {
//		private Alignment a;
//
//		Buffer(Alignment a) {
//			this.a = a;
//		}
//
//		private List<float[]> buffer = new ArrayList<float[]>();
//
//		public double get(int start, int scale) {
//
//			if (scale < bareScale) {
//				double conservation = 0;
//				for (int j = 0; j < scale; j++) {
//					if (a.isAligned(start + j))
//						conservation++;
//				}
//				return conservation / scale;
//			}
//			if (buffer.size() == 0)
//				buffer.add(bare());
//
//			int index = (int) (Math.log(scale) / LOG2) - bareScaleIndex;
//
//			while (buffer.size() <= index + 1) {
//				buffer.add(merge(buffer.get(buffer.size() - 1)));
//			}
//
//			return buffer.get(index)[start / scale];
//		}
//
//		private float[] merge(float[] ds) {
//			float[] out = new float[(ds.length + 1) / 2];
//			for (int i = 0; i < ds.length - 1; i += 2) {
//				out[i / 2] = (ds[i] + ds[i + 1]) / 2;
//			}
//			out[out.length - 1] = ds[ds.length - 1];
//			return out;
//		}
//
//		private float[] bare() {
//
//			float[] out = new float[a.refLength() / bareScale + 1];
//			for (int i = 0; i < a.refLength(); i += bareScale) {
//				float conservation = 0;
//				for (int j = 0; j < bareScale; j++) {
//					if (a.isAligned(i + j))
//						conservation++;
//				}
//				conservation /= bareScale;
//				out[i / bareScale] = conservation;
//
//			}
//			return out;
//		}
//
//	}

	private MultipleAlignmentTrackModel mat = null;

	public MultipleAlignmentTrack(Model model, DataKey key) {
		super(key, model, true, false);
//		this.name = key.toString();
		AlignmentAnnotation entireAlignment = (AlignmentAnnotation) entry
				.get(dataKey);
		mat = new MultipleAlignmentTrackModel(entireAlignment);

	}

//	@Override
//	public String displayName() {
//		return "MA: " + name;
//	}

//	static class Cache {
//		private int cacheStart = -1;
//		private int cacheEnd = -1;
//		private int cacheScale = 1;
//		private double[] cacheValues = null;
//
//		public boolean hasData(int scale, int start, int end) {
//			return scale == cacheScale && start >= cacheStart
//					&& end <= cacheEnd;
//		}
//
//		public void store(int scale, int start, int end, double[] cacheValues2) {
//			this.cacheScale = scale;
//			this.cacheStart = start;
//			this.cacheEnd = end;
//			this.cacheValues = cacheValues2;
//
//		}
//
//		public double[] get() {
//			return cacheValues;
//		}
//
//		public int start() {
//			return cacheStart;
//		}
//
//		public int end() {
//			return cacheEnd;
//		}
//
//		public int scale() {
//			return cacheScale;
//		}
//
//	}

	class MultipleAlignmentTrackModel {

		private AlignmentAnnotation aa;

		private List<Alignment> ordering;

		MultipleAlignmentTrackModel(AlignmentAnnotation entireAlignment) {
			this.aa = entireAlignment;
			List<WeightedAlignment> set = new ArrayList<WeightedAlignment>();

			for (Alignment a : aa.get()) {
				double d = Configuration.getDouble("MAWEIGHT_" + a.name(), 0);
				set.add(new WeightedAlignment(d, a));
			}
			Collections.sort(set);
			ordering = new ArrayList<Alignment>();
			for (WeightedAlignment wa : set) {
				ordering.add(wa.a);
			}
		}

		public boolean hasData() {
			return aa != null;
		}

		class WeightedAlignment implements Comparable<WeightedAlignment> {
			Alignment a;
			double weight;

			public WeightedAlignment(double d, Alignment a) {
				this.weight = d;
				this.a = a;
			}

			@Override
			public int compareTo(WeightedAlignment o) {
				int d = Double.compare(weight, o.weight);
				System.out.println("Compare: " + weight + "\t" + o.weight
						+ "\t" + d);
				return Double.compare(weight, o.weight);
			}

		}

		public Iterable<Alignment> ordered() {
			return ordering;
		}

		public double getConservation(int i) {
			return aa.getConservation(i);
		}

		public Integer getNucleotideCount(char c, int i) {
			return aa.getNucleotideCount(c, i);
		}

		public double getFootprint(int i) {
			return aa.getFootprint(i);
		}

		public int numAlignments() {
			return aa.numAlignments();
		}
	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth,
			JViewport view,TrackCommunicationModel tcm) {
		Location r = model.getAnnotationLocationVisible();
		int lineHeigh = 20;

		if (mat.hasData()) {
			if (r.length() > 10000000) {
				g.setColor(Color.BLACK);
				g.drawString(
						"Too much data in alignment, zoom in to see details",
						5, yOffset + lineHeigh - 2);
				return lineHeigh;
			}
			if (r.length() < 1000) {

				for (Alignment alignment : mat.ordered()) {
					double width = screenWidth / (double) r.length();
					int grouping = (int) Math.ceil(1.0 / width);
					for (int i = r.start(); i <= r.end(); i += grouping) {
						char nt = ' ';
						double conservation = 0;
						boolean dash = false;
						for (int j = 0; j < grouping; j++) {
							nt = alignment.getNucleotide(i + j);
							conservation += mat.getConservation(i + j);
							if (nt == '-')
								dash = true;

						}
						conservation /= grouping;
						if ((int) conservation == 1) {
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

						g.fillRect((int) ((i - r.start()) * width), yOffset,
								(int) (width * grouping) + 1, lineHeigh);
						if (alignment.sizeGapAfter(i) > 0) {
							g.setColor(Color.ORANGE);
							g.fillRect(
									(int) ((i - r.start()) * width + width * 3 / 4),
									yOffset, (int) (width * grouping) / 2 + 1,
									lineHeigh);
						}
						if (model.getAnnotationLocationVisible().length() < 100) {
							Rectangle2D stringSize = g.getFontMetrics()
									.getStringBounds("" + nt, g);
							if (conservation > 0.75) {
								g.setColor(Color.WHITE);
							} else
								g.setColor(Color.BLACK);
							g.drawString(
									"" + nt,
									(int) (((i - r.start()) * width - stringSize
											.getWidth() / 2) + (width / 2)),
									yOffset + lineHeigh - 2);
						}
					}

					FontMetrics metrics = g.getFontMetrics();
					int hgt = metrics.getHeight();
					int adv = metrics.stringWidth(alignment.name());

					g.setColor(Color.WHITE);
					g.fillRect(10, yOffset + lineHeigh - hgt, adv + 2, hgt + 2);

					g.setColor(Color.BLUE);
					// if (model.getAnnotationLocationVisible().length() >= 100)
					g.drawString(alignment.name(), 10, yOffset + lineHeigh - 2);
					yOffset += lineHeigh;

				}

			} else {

				for (Alignment alignment : mat.ordered()) {
//					double width = screenWidth / (double) r.length() / 5.0;

//					int scale = 1;
//					while (scale < (int) Math.ceil(1.0 / width))
//						scale *= 2;

//					GeneralPath conservationGP = new GeneralPath();
//					conservationGP.moveTo(0, yOffset);
//
//					int start = r.start() / scale * scale;
//					int end = ((r.end() / scale) + 1) * scale;

					// /* Plot whatever is in the cache */
					if (!pips.containsKey(alignment))
						pips.put(alignment, percentIdentify(alignment));
					float[] b = pips.get(alignment);

					int stepSize=(int)Math.max(1,Math.floor((r.length()/screenWidth)/2));
					
					for (int i = r.start; i <=r.end; i+=stepSize) {
						int x = Convert.translateGenomeToScreen(i, r, screenWidth);
						int width= Math.max(1,Convert.translateGenomeToScreen(i+stepSize, r, screenWidth)-x);
						float min=1;
						float max=0;
						for(int j=0;j<stepSize;j++){
							float val=b[i+j-1];
							if(val>max)
								max=val;
							if(val<min)
								min=val;
								
						}
						int height=(int)Math.max(1,lineHeigh*(max-min));
						int y = (int)(max*lineHeigh);
						g.setColor(Color.BLACK);
//						System.out.println("Plotting pips: "+x+"\t"+y);
						g.fillRect(x, yOffset+lineHeigh-y,width, height);
//						conservationGP.lineTo(x,
//								yOffset + (1 - b.get(start + i * scale, scale))
//										* (lineHeigh - 4) + 2);
					}
					
//					g.draw(conservationGP);
//					g.setColor(Color.BLUE);
//					g.drawString(alignment.name() + " (" + scale + ")", 10,
//							yOffset + lineHeigh - 2);
					yOffset += lineHeigh;
				}

			}
			int logoLineHeight = 40;
			
			
			if (model.getAnnotationLocationVisible().length() < 100) {
				double width = screenWidth / (double) r.length();
				int grouping = (int) Math.ceil(1.0 / width);
				for (int i = r.start(); i <= r.end(); i += grouping) {
					// TODO do something with zoom-out

					SortedMap<Integer, String> map = new TreeMap<Integer, String>(
							Collections.reverseOrder());

					map.put(mat.getNucleotideCount('a', i), "A");

					if (map.containsKey(mat.getNucleotideCount('c', i))) {
						map.put(mat.getNucleotideCount('c', i),
								map.get(mat.getNucleotideCount('c', i)) + "C");
					} else {
						map.put(mat.getNucleotideCount('c', i), "C");
					}
					if (map.containsKey(mat.getNucleotideCount('g', i))) {
						map.put(mat.getNucleotideCount('g', i),
								map.get(mat.getNucleotideCount('g', i)) + "G");
					} else {
						map.put(mat.getNucleotideCount('g', i), "G");
					}
					if (map.containsKey(mat.getNucleotideCount('t', i))) {
						map.put(mat.getNucleotideCount('t', i),
								map.get(mat.getNucleotideCount('t', i)) + "T");
					} else {
						map.put(mat.getNucleotideCount('t', i), "T");
					}
					draw(map, g, mat.numAlignments(), i, logoLineHeight, model,
							width, yOffset);
				}
			} else {
				double width = screenWidth / (double) r.length() / 10.0;
				int grouping = (int) Math.ceil(1.0 / width);

				GeneralPath conservationGP = new GeneralPath();
				GeneralPath footprintGP = new GeneralPath();
				conservationGP.moveTo(0, yOffset);
				footprintGP.moveTo(0, yOffset);
				for (int i = r.start(); i <= r.end() + grouping; i += grouping) {

					double conservation = 0;
					double footprint = 0;
					for (int j = 0; j < grouping; j++) {
						conservation += mat.getConservation(i + j);
						footprint += mat.getFootprint(i + j);
					}
					conservation /= grouping;
					footprint /= grouping;
					conservationGP.lineTo((int) ((i - r.start()) * width * 10),
							yOffset + (1 - conservation) * lineHeigh);
					footprintGP.lineTo((int) ((i - r.start()) * width * 10),
							yOffset + (1 - footprint) * logoLineHeight);

				}
				g.setColor(Color.BLUE);
				g.draw(conservationGP);
				g.setColor(Color.RED);
				g.draw(footprintGP);
				g.drawString(this.config.displayName() + " (" + grouping + ")", 10,
						yOffset + logoLineHeight - 2);
				// return 3 * lineHeigh;
			}
			return mat.numAlignments() * lineHeigh + logoLineHeight;

		}
		return 0;
	}

	private float[] percentIdentify(Alignment alignment) {
		BitSet rollingBuffer=new BitSet();
		
		int reflen=alignment.refLength();
		for(int i=1;i<=reflen;i++){
			if(alignment.getReferenceNucleotide(i)==alignment.getNucleotide(i))
				rollingBuffer.set(i-1);
		}
		float[] out=new float[reflen];
		for(int i=0;i<reflen;i++){
			int count=0;
			for(int j=-50;j<=50;j++){
				/* wrap index */
				int idx=(i-j);
				if(idx<0)
					idx+=reflen;
				if(idx>=reflen)
					idx-=reflen;
				if(rollingBuffer.get(idx))
					count++;
				
			}
			
			out[i]=(float)(count/101.0);
			if(count!=101)
			System.out.println("calculate: "+count+"\t"+out[i]);
		}
		return out;
		
		
	}

	@Override
	protected void paintDisplayName(Graphics2D g, int yOffset){
		//Do nothing
	}
	
	private void draw(Map<Integer, String> map, Graphics2D g, int numAlign,
			int position, int lineHeight, Model m, double width, int yOffset) {

		int left = lineHeight;
		for (int key : map.keySet()) {
			for (char c : map.get(key).toCharArray()) {
				Color ntColor = Configuration.getNucleotideColor(c);
				// System.out.println(c + "\t" + key);
				double fraction = key / (double) numAlign;

				Font font = new Font("Sans serif", 1, lineHeight);

				Font font2 = font.deriveFont(AffineTransform.getScaleInstance(
						width / lineHeight * 1.2, fraction * 1.4));

				GlyphVector glyphvector = font2.createGlyphVector(
						g.getFontRenderContext(), "" + c);
				//
				Rectangle2D stringSize = font2.getStringBounds("" + c,
						g.getFontRenderContext());
				int x = (int) (((position - model
						.getAnnotationLocationVisible().start()) * width) + (width - stringSize
						.getWidth()) / 2);
				int y = (int) (yOffset + left);
				g.translate(x, y);
				left -= fraction * lineHeight;
				java.awt.Shape shape = glyphvector.getGlyphOutline(0);
				g.setColor(ntColor);
				g.fill(shape);
				g.translate(-x, -y);

			}
		}
	}
}
