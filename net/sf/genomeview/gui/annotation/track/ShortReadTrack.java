/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.core.Colors;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.shortread.ShortReadTools;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class ShortReadTrack extends Track {

	public ShortReadTrack(DataKey key, Model model) {
		super(key, model, true, true);
	}

	private Tooltip tooltip = new Tooltip();

	private class Tooltip extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public Tooltip() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder,
					emptyBorder));
			add(floater);
			pack();
		}

		public void set( MouseEvent e,
				ShortReadInsertion sri) {
			StringBuffer text = new StringBuffer();
			text.append("<html>");
//			text.append("Forward coverage : "
//					+ (forward < 0 ? "In progress..." : forward) + "<br />");
//			text.append("Reverse coverage: "
//					+ (reverse < 0 ? "In progress..." : reverse) + "<br />");
//			text.append("Total coverage : " + (d < 0 ? "In progress..." : d)
//					+ "<br />");
			if (sri != null) {
				text.append("Insertion: ");
				byte[] bases = sri.esr.getReadBases();
				for (int i = sri.start; i < sri.start + sri.len; i++) {
					text.append((char) bases[i]);
				}
				text.append("<br/>");
			}
			text.append("</html>");
			if (!text.toString().equals(floater.getText())) {
				floater.setText(text.toString());
				this.pack();
			}
			setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);

			if (!isVisible()) {
				setVisible(true);
			}

		}

	}

	@Override
	public boolean mouseExited(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		return false;
	}

	private int currentYOffset = 0;

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		if (scale <= 256) {
			if (!tooltip.isVisible())
				tooltip.setVisible(true);
			// ReadGroup rg = currentEntry.shortReads.getReadGroup(this.source);
//			ReadGroup rg = (ReadGroup) currentEntry.get(dataKey);
			// ShortReadCoverage currentBuffer = rg.getCoverage();
//			int start = Convert.translateScreenToGenome(x, currentVisible,
//					currentScreenWidth);
//			 int f = (int) currentBuffer.get(Strand.FORWARD, start - 1);
//			 int r = (int) currentBuffer.get(Strand.REVERSE, start - 1);

			ShortReadInsertion sri = null;
			for (java.util.Map.Entry<Rectangle, ShortReadInsertion> e : paintedBlocks
					.entrySet()) {
				if (e.getKey().contains(x, y + currentYOffset)) {
					sri = e.getValue();
					break;
				}
			}
			tooltip.set(source, sri);
		} else {
			if (tooltip.isVisible())
				tooltip.setVisible(false);
		}
		return false;
	}

	// private DataSource source;

	private int scale = 1;
	private int scaleIndex = 0;

	private Location currentVisible;

	private Color pairingColor;

	private Color reverseColor;

	private Color forwardColor;

//	private Entry currentEntry;
//
//	private double currentScreenWidth;
	private ColorGradient forwardGradient;
	private ColorGradient reverseGradient;
	private JViewport view;

//	private static final double LOG2 = Math.log(2);

//	private double log2(double d) {
//		return Math.log(d) / LOG2;
//	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth,JViewport view) {
		paintedBlocks.clear();
		this.view=view;
//		/* Store information to be used in other methods */
//		currentEntry = entry;
//		currentScreenWidth = screenWidth;
		seqBuffer = null;
		this.currentYOffset = yOffset;
		/* Configuration options */
		int maxReads = Configuration.getInt("shortread:maxReads");
		int maxRegion = Configuration.getInt("shortread:maxRegion");
		int maxStack = Configuration.getInt("shortread:maxStack");
		forwardColor = Configuration.getColor("shortread:forwardColor");
		reverseColor = Configuration.getColor("shortread:reverseColor");
		pairingColor = Configuration.getColor("shortread:pairingColor");

		/* Create color gradient for forward reads */
		forwardGradient = new ColorGradient();
		forwardGradient.addPoint(Color.WHITE);
		forwardGradient.addPoint(forwardColor);
		forwardGradient.createGradient(100);

		/* Create color gradient for reverse reads */
		reverseGradient = new ColorGradient();
		reverseGradient.addPoint(Color.WHITE);
		reverseGradient.addPoint(reverseColor);
		reverseGradient.createGradient(100);

//		boolean logScaling = Configuration.getBoolean("shortread:logScaling");
		double bottomValue = Configuration.getDouble("shortread:bottomValue");
		// double topValue = Configuration.getDouble("shortread:topValue");

		int graphLineHeigh = Configuration.getInt("shortread:graphLineHeight");

		currentVisible = model.getAnnotationLocationVisible();

		int originalYOffset = yOffset;

		/*
		 * Draw line plot of coverage
		 */

		double width = screenWidth / (double) currentVisible.length() / 2.0;

		scale = 1;
		scaleIndex = 0;
		while (scale < (int) Math.ceil(1.0 / width)) {
			scale *= 2;
			scaleIndex++;
		}

//		int start = currentVisible.start / scale * scale;
//		int end = ((currentVisible.end / scale) + 1) * scale;

		ReadGroup rg = (ReadGroup) entry.get(dataKey);// entry.shortReads.
														// getReadGroup(source);
		if (rg == null)
			return 0;
		// ShortReadCoverage graph = rg.getCoverage();// .get(rg);
		// if (topValue < 0)
		// topValue = graph.max();

		// GeneralPath conservationGP = new GeneralPath();
		// GeneralPath conservationGPF = new GeneralPath();
		// GeneralPath conservationGPR = new GeneralPath();

		// float[] f = graph.get(Strand.FORWARD, start - 1, end, scaleIndex);
		// float[] r = graph.get(Strand.REVERSE, start - 1, end, scaleIndex);
		//
		// double range = topValue - bottomValue;

		// for (int i = 0; i < f.length; i++) {
		// int x = Convert.translateGenomeToScreen(start + i * scale,
		// currentVisible, screenWidth);
		// /* Coverage is stored +1 in SRC, needs correcting here */
		// double valF = f[i] - 1;
		// double valR = r[i] - 1;
		// double val = f[i] + r[i] - 2;
		// /* Cap value */
		// if (valF > topValue)
		// valF = topValue;
		// if (valR > topValue)
		// valR = topValue;
		// if (val > topValue)
		// val = topValue;
		//
		// if (valF < bottomValue)
		// valF = bottomValue;
		// valR = bottomValue;
		// if (val < bottomValue)
		// val = bottomValue;
		//
		// /* Translate for bottom point */
		// valF -= bottomValue;
		// valR -= bottomValue;
		// val -= bottomValue;
		// /* Logaritmic scaling */
		// if (logScaling) {
		// valF = log2(valF + 1);
		// valF /= log2(range);
		// valR = log2(valR + 1);
		// valR /= log2(range);
		// val = log2(val + 1);
		// val /= log2(range);
		// /* Regular scaling */
		// } else {
		// valF /= range;
		// valR /= range;
		// val /= range;
		// }
		//
		// /* Draw lines */
		// if (i == 0) {
		// conservationGP.moveTo(x - 1, yOffset + (1 - val) * (graphLineHeigh -
		// 4) + 2);
		// conservationGPF.moveTo(x - 1, yOffset + (1 - valF) * (graphLineHeigh
		// - 4) + 2);
		// conservationGPR.moveTo(x - 1, yOffset + (1 - valR) * (graphLineHeigh
		// - 4) + 2);
		// }
		//
		// conservationGP.lineTo(x, yOffset + (1 - val) * (graphLineHeigh - 4) +
		// 2);
		// conservationGPF.lineTo(x, yOffset + (1 - valF) * (graphLineHeigh - 4)
		// + 2);
		// conservationGPR.lineTo(x, yOffset + (1 - valR) * (graphLineHeigh - 4)
		// + 2);
		//
		// }

		// /* Draw coverage lines */
		// g.setColor(forwardColor);
		// g.draw(conservationGPF);
		//
		// g.setColor(reverseColor);
		// g.draw(conservationGPR);
		// g.setColor(Color.BLACK);
		// g.draw(conservationGP);

		g.setColor(Color.BLUE);
		g.drawLine(0, yOffset, 5, yOffset);
		// g.drawString("" + topValue, 10, yOffset + 12 - 2);
		g.drawString(StaticUtils.shortify(super.dataKey.toString()) + " ("
				+ scale + ")", 10, yOffset + 24 - 2);
		yOffset += graphLineHeigh;
		g.drawLine(0, yOffset, 5, yOffset);
		g.drawString("" + bottomValue, 10, yOffset - 2);
		// }

		/*
		 * Draw individual reads when possible
		 */
		Iterable<SAMRecord> reads = null;

		int readLength = ((ReadGroup) entry.get(dataKey)).readLength();// entry.
																		// shortReads
																		// .
																		// getReadGroup
																		// (
																		// source
																		// ).
																		// readLength
																		// ();
		if (!isCollapsed() && (currentVisible.length() > maxRegion)) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big (max " + maxRegion + " nt), zoom in",
					10, yOffset + 10);
			yOffset += 20 + 5;
		} else if (!isCollapsed()) {
			/* Access to BAMread is through buffer for performance! */
			reads = rg.get(currentVisible.start, currentVisible.end);
			// if (rg instanceof BAMreads) {
			// /* Update readLength for paired reads */
			// readLength = ((BAMreads) rg).getPairLength();
			//
			// }
		}
		int maxPairingDistance = Configuration
				.getInt("shortread:maximumPairing");
		if (readLength > maxPairingDistance)
			readLength = maxPairingDistance;
		int lines = 0;
		boolean stackExceeded = false;
		boolean enablePairing = Configuration
				.getBoolean("shortread:enablepairing");

		/* Variables for SNP track */
		NucCounter nc = new NucCounter();
		int snpOffset = yOffset;
		int snpTrackHeight = Configuration.getInt("shortread:snpTrackHeight");
		int snpTrackMinimumCoverage = Configuration
				.getInt("shortread:snpTrackMinimumCoverage");

		int readLineHeight = 3;
		if (currentVisible.length() < Configuration
				.getInt("geneStructureNucleotideWindow")) {
			/*
			 * Make some room for the SNP track. Although it's painted last, it
			 * needs to be drawn above the reads
			 */
			readLineHeight = 14;
			yOffset += snpTrackHeight;
			nc.init(currentVisible.length());
		}

		if (reads != null) {

			lines = 0;

			BitSet[] tilingCounter = new BitSet[currentVisible.length()];
			for (int i = 0; i < tilingCounter.length; i++) {
				tilingCounter[i] = new BitSet(maxStack);
			}
			int visibleReadCount = 0;
			try {
				for (SAMRecord one : reads) {

					if (enablePairing && one.getReadPairedFlag()
							&& ShortReadTools.isSecondInPair(one))
						continue;

					if (visibleReadCount > maxReads) {
						String msg = "Too many short reads to display, only first "
								+ maxReads + " are displayed ";
						FontMetrics metrics = g.getFontMetrics();
						int hgt = metrics.getHeight();
						int adv = metrics.stringWidth(msg);
						g.setColor(Color.WHITE);
						g.fillRect(10, yOffset + 20 - hgt, adv + 2, hgt + 2);
						g.setColor(Color.RED);
						g.drawString(msg, 10, yOffset + 18);
						break;
					}

					// int x2 = Convert.translateGenomeToScreen(one.end() + 1,
					// currentVisible, screenWidth);
					// if (x2 > 0) {
					/* Find empty line */
					int pos = one.getAlignmentStart() - currentVisible.start;
					int line = line(one, pos, tilingCounter);
					if (line > maxStack) {
						stackExceeded = true;
						continue;

					}
					int clearStart = one.getAlignmentStart();
					int clearEnd = one.getAlignmentEnd();
					SAMRecord two = null;
					/* Modify empty space finder for paired reads */
					if (enablePairing && one instanceof SAMRecord) {
						// ShortReadTools esr = (ShortReadTools) one;
						if (ShortReadTools.isPaired(one)
								&& ShortReadTools.isFirstInPair(one)) {
							two = rg.getSecondRead(one);
						}
						if (two != null) {
							if (two.getAlignmentEnd() < one.getAlignmentStart()) {

								pos = two.getAlignmentStart()
										- currentVisible.start;
								line = line(two, pos, tilingCounter);
								clearStart = two.getAlignmentStart();
							} else {
								clearEnd = two.getAlignmentEnd();
							}
						}

					}
					/* Carve space out of hitmap */
					for (int i = clearStart - readLength; i <= clearEnd + 3; i++) {
						pos = i - currentVisible.start;
						if (pos >= 0 && pos < tilingCounter.length)
							tilingCounter[pos].set(line);

					}
					/* The y-coordinate of the read */
					int yRec = line * readLineHeight + yOffset;

					/* Paint read or read pair */
					if (line < maxStack) {
						/* paired read - calculate connection coordinates */
						if (two != null) {

							int subX1, subX2;
							if (one.getAlignmentStart() < two
									.getAlignmentStart()) {

								subX1 = Convert.translateGenomeToScreen(one
										.getAlignmentEnd(), currentVisible,
										screenWidth);
								subX2 = Convert.translateGenomeToScreen(two
										.getAlignmentStart(), currentVisible,
										screenWidth);

							} else {

								subX1 = Convert.translateGenomeToScreen(one
										.getAlignmentStart(), currentVisible,
										screenWidth);
								subX2 = Convert.translateGenomeToScreen(two
										.getAlignmentEnd(), currentVisible,
										screenWidth);

							}

							g.setColor(pairingColor);
							g.drawLine(subX1, yRec + readLineHeight / 2, subX2,
									yRec + readLineHeight / 2);
						}
						if (line > lines)
							lines = line;

						paintRead(g, one, yRec, screenWidth, readLineHeight,
								entry, nc);
						visibleReadCount++;
						if (two != null) {

							paintRead(g, two, yRec, screenWidth,
									readLineHeight, entry, nc);
							visibleReadCount++;
						}
					} else {
						stackExceeded = true;
					}
					// }
				}
			} catch (ConcurrentModificationException e) {
				// Ignore
			}
			if (stackExceeded) {
				g.setColor(Color.RED);
				g.drawString("Max stacking depth reached!", 5, lines
						* readLineHeight + yOffset - 2);
				g.drawLine(0, lines * readLineHeight + yOffset,
						(int) screenWidth, lines * readLineHeight + yOffset);
				lines++;

			}
			yOffset += (lines + 1) * readLineHeight + 5;

		}
		/*
		 * Draw SNP track if possible. This depends on drawing the individual
		 * reads first as during the iteration over all reads, we store the
		 * polymorphisms.
		 */

		char[] nucs = new char[] { 'A', 'T', 'G', 'C' };
		Color[] color = new Color[4];
		for (int i = 0; i < 4; i++)
			color[i] = Configuration.getNucleotideColor(nucs[i]);
		int nucWidth = (int) (Math.ceil(screenWidth / currentVisible.length()));
		if (true && nc.hasData() && seqBuffer != null) {
			g.setColor(Colors.LIGHEST_GRAY);
			g.fillRect(0, snpOffset, (int) screenWidth, snpTrackHeight);
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(0, snpOffset + snpTrackHeight / 2, (int) screenWidth,
					snpOffset + snpTrackHeight / 2);
			g.setColor(Color.BLACK);
			g.drawString("SNPs", 5, snpOffset + snpTrackHeight - 4);
			for (int i = currentVisible.start; i <= currentVisible.end; i++) {
				int x1 = Convert.translateGenomeToScreen(i, currentVisible,
						screenWidth);
				double total = nc.getTotalCount(i - currentVisible.start);
				char refNt = seqBuffer[i - currentVisible.start];
				double done = 0;// Fraction gone to previous nucs
				if (total > snpTrackMinimumCoverage) {
					for (int j = 0; j < 4; j++) {
						if (nucs[j] != refNt) {
							double fraction = nc.getCount(nucs[j], i
									- currentVisible.start)
									/ total;
							fraction *= snpTrackHeight;
							g.setColor(color[j]);
							g.fillRect(x1, (int) (snpOffset + snpTrackHeight
									- fraction - done), nucWidth, (int) (Math
									.ceil(fraction)));
							done += fraction;
						}
					}
				}

			}
		}

		return yOffset - originalYOffset;
	}

	private int line(SAMRecord one, int pos, BitSet[] tilingCounter) {
		if (pos >= 0 && pos < tilingCounter.length)
			return tilingCounter[one.getAlignmentStart() - currentVisible.start]
					.nextClearBit(0);
		else
			return tilingCounter[0].nextClearBit(0);

	}

	class NucCounter {
		int[][] counter;

		public NucCounter() {
			// TODO Auto-generated constructor stub
		}

		public boolean hasData() {
			return counter != null;
		}

		public void init(int len) {
			if (counter == null)
				counter = new int[4][len];

		}

		public int getCount(char nuc, int pos) {
			return counter[ix(nuc)][pos];
		}

		public int getTotalCount(int pos) {
			return counter[0][pos] + counter[1][pos] + counter[2][pos]
					+ counter[3][pos];
		}

		private int ix(char nuc) {
			switch (nuc) {
			case 'a':
			case 'A':
				return 0;
			case 'T':
			case 't':
				return 1;
			case 'c':
			case 'C':
				return 2;
			case 'g':
			case 'G':
				return 3;
			default:
				return -1;
			}
		}

		public void count(char nuc, int pos) {
			counter[ix(nuc)][pos]++;
		}
	}

	/* Keeps track of the short-read insertions */
	private Map<Rectangle, ShortReadInsertion> paintedBlocks = new HashMap<Rectangle, ShortReadInsertion>();
	/*
	 * Buffer that will contain the visible reference sequence as soon as it has
	 * been used to paint mismatches
	 */
	private char[] seqBuffer = null;

	private void paintRead(Graphics2D g, SAMRecord rf, int yRec,
			double screenWidth, int readLineHeight, Entry entry, NucCounter nc) {
		if(yRec<view.getViewRect().y||yRec>view.getViewRect().y+view.getViewRect().height){
			//System.out.println("Skipping");
			return;
		}
		
		Color c = Color.GRAY;
		if (ShortReadTools.strand(rf) == Strand.FORWARD) {
			c = forwardColor;
		} else
			c = reverseColor;
		g.setColor(c);

		int subX1 = Convert.translateGenomeToScreen(rf.getAlignmentStart(),
				currentVisible, screenWidth);
		int subX2 = Convert.translateGenomeToScreen(rf.getAlignmentEnd() + 1,
				currentVisible, screenWidth);
		if (subX2 < subX1) {
			subX2 = subX1;
			// FIXME does this ever happen?
			// XXX The one time it did happen it pointed to a bug, so it may be
			// that it doesn't happen when all goes well.
			System.err.println("This happens!");
		}
		// /* BAM read, has quality information */
		// if (rf instanceof ShortReadTools) {
		// ShortReadTools esr = (ShortReadTools) rf;
		int qual = rf.getMappingQuality();
		if (c == forwardColor)
			g.setColor(forwardGradient.getColor(qual));
		else
			g.setColor(reverseGradient.getColor(qual));
		// }
		g.fillRect(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);
		g.setColor(c);
		g.drawRect(subX1, yRec, subX2 - subX1, readLineHeight - 2);

		/* Check mismatches */
		if (entry.sequence().size() == 0)
			return;
		if (currentVisible.length() < Configuration
				.getInt("geneStructureNucleotideWindow")) {
			/* If there is no sequence, return immediately */
			if (entry.sequence().size() == 0)
				return;
			if (seqBuffer == null) {
				// seqBuffer =
				// entry.sequence().getSubSequence(currentVisible.start,
				// currentVisible.end + 1).toCharArray();
				
				// FIXME Why is this +1?
				Iterable<Character> bufferedSeq = entry.sequence().get(
						currentVisible.start, currentVisible.end+1);
				

				seqBuffer = new char[currentVisible.length()];
				int idx = 0;
				for (char cc : bufferedSeq) {
					seqBuffer[idx++] = cc;
				}
				//System.out.println("SS:"+new String(seqBuffer));

			}
			byte[]readNts=ShortReadTools.construct(rf);
			for (int j = rf.getAlignmentStart(); j <= rf.getAlignmentEnd(); j++) {
				if (j > currentVisible.end || j < currentVisible.start)
					continue;
				// FIXME Speed-up by putting code here...
//				char readNt = ShortReadTools.getNucleotide(rf, j
//						- rf.getAlignmentStart() + 1);
				char readNt=(char)readNts[j-rf.getAlignmentStart()];
				// char refNt = entry.sequence.getNucleotide(j);

				char refNt = seqBuffer[j - currentVisible.start];
				double tx1 = Convert.translateGenomeToScreen(j, currentVisible,
						screenWidth);
				double tx2 = Convert.translateGenomeToScreen(j + 1,
						currentVisible, screenWidth);

				if (readNt != refNt) {
					// if (readNt != '_') {
					switch (readNt) {
					case '-':/* Gap */
						g.setColor(Color.RED);
						break;
					case '_':/* Spliced alignment */
						g.setColor(Color.WHITE);
						break;
					default:/* Mismatch */
						g.setColor(Color.ORANGE);
						nc.count(readNt, j - currentVisible.start);
						break;
					}
					int width=(int) (tx2 - tx1);
					if(width<1)
						width=1;
					g.fillRect((int) tx1, yRec, width,
							readLineHeight - 1);
					/*
					 * For spliced alignments, the connection is blanked with a
					 * white box, now put some color back
					 */
					if (readNt == '_') {
						g.setColor(pairingColor);
						g.fillRect((int) tx1, yRec + 4, (int) (tx2 - tx1),
								readLineHeight - 8 - 1);
					}
					if (readNt != '_'
							&& model.getAnnotationLocationVisible().length() < 100) {
						g.setColor(c);
						Rectangle2D stringSize = g.getFontMetrics()
								.getStringBounds("" + readNt, g);
						g.drawString("" + readNt,
								(int) (tx1 + ((tx2 - tx1) / 2 - stringSize
										.getWidth() / 2)), yRec
										+ readLineHeight - 3);

					}

				} else {
					nc.count(readNt, j - currentVisible.start);
				}
			}

//			if (rf instanceof ShortReadTools) {
//				ShortReadTools esr = (ShortReadTools) rf;
				int pos = 0;
				int esrPos = 0;
				for (CigarElement ce : rf.getCigar().getCigarElements()) {
					if (ce.getOperator() == CigarOperator.I) {
						double tx1 = Convert.translateGenomeToScreen(rf.getAlignmentStart()
								+ pos, currentVisible, screenWidth);
						if (ce.getLength() % 3 == 0)
							g.setColor(Color.GRAY);
						else
							g.setColor(Color.BLACK);
						Rectangle rec = new Rectangle((int) (tx1 - 1), yRec, 2,
								readLineHeight - 1);
						g.fill(rec);
						/* Make it easier to hit with the mouse */
						rec.x--;
						rec.width += 2;
						ShortReadInsertion in = new ShortReadInsertion();
						in.esr = rf;
						in.start = esrPos;
						in.len = ce.getLength();
						paintedBlocks.put(rec, in);
					} else {
						pos += ce.getLength();
					}
					esrPos += ce.getLength();
				}
			}
//		}

	}

	private class ShortReadInsertion {
		SAMRecord esr;
		int start, len;
	}

	@Override
	public String displayName() {
		return "Short reads: " + super.dataKey;
	}

	// public DataSource source() {
	// return source;
	// }
}
