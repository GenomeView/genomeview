/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JViewport;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.ColorGradient;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.LocationTools;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.shortread.BAMreads;
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
		super(key, model, true, false);
	}

	private InsertionTooltip tooltip = new InsertionTooltip();
	private ReadInfo readinfo = new ReadInfo();

	private class InsertionTooltip extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public InsertionTooltip() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(MouseEvent e, ShortReadInsertion sri) {
			if (sri == null)
				return;
			StringBuffer text = new StringBuffer();
			text.append("<html>");

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

	private class ReadInfo extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public ReadInfo() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(MouseEvent e, SAMRecord sr) {
			if (sr == null)
				return;
			StringBuffer text = new StringBuffer();
			text.append("<html>");

			if (sr != null) {
				text.append("Name: " + sr.getReadName() + "<br/>");
				text.append("Len: " + sr.getReadLength() + "<br/>");
				text.append("Cigar: " + sr.getCigarString() + "<br/>");
				text.append("Sequence: " + sr.getReadString() + "<br/>");
				text.append("Paired: " + sr.getReadPairedFlag() + "<br/>");
				if (sr.getReadPairedFlag()) {
					if (!sr.getMateUnmappedFlag())
						text.append("Mate: " + sr.getMateReferenceName() + ":" + sr.getMateAlignmentStart() + "<br/>");
					else
						text.append("Mate missing" + "<br/>");
					text.append("Second: " + sr.getFirstOfPairFlag());
				}
				// text.append("<br/>");
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
		readinfo.setVisible(false);
		return false;
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent source) {
		// System.out.println("Click: " + x + " " + y);
		for (java.util.Map.Entry<Rectangle, SAMRecord> e : hitMap.entrySet()) {
			if (e.getKey().contains(x, y)) {
				System.out.println("Click: " + e.getValue());
				model.selectionModel();
			}
		}

		return false;
	}

	public boolean mouseDragged(int x, int y, MouseEvent source) {
		tooltip.setVisible(false);
		readinfo.setVisible(false);
		return false;

	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			ShortReadInsertion sri = null;
			for (java.util.Map.Entry<Rectangle, ShortReadInsertion> e : paintedBlocks.entrySet()) {
				if (e.getKey().contains(x, y)) {
					sri = e.getValue();
					break;
				}
			}

			if (sri != null) {
				if (!tooltip.isVisible())
					tooltip.setVisible(true);
				tooltip.set(source, sri);
			} else {
				if (tooltip.isVisible())
					tooltip.setVisible(false);
			}
			//
			// System.out.println("Moved: " + x + " " + y);
			for (java.util.Map.Entry<Rectangle, SAMRecord> e : hitMap.entrySet()) {
				if (e.getKey().contains(x, y)) {
					// System.out.println("Prijs: " + e.getValue());
					readinfo.set(source, e.getValue());
				}
			}
			//
			return false;

		} else {
			if (tooltip.isVisible())
				tooltip.setVisible(false);
		}
		return false;
	}

	private Location currentVisible;

	private Color pairingColor;

	private JViewport view;

	/* Keep track of the last x-coordinate that has been used for painting */
	private int lastX = 100;

	class TilingMatrix {
		/* One place Bitset per pixel column on screen */
		private BitSet[] tc;
		private int visibleLength;

		/* Get a free line for the tranlated genomic coordinate */
		int getFreeLine(int startX) {
			int transX = translate(startX);
			if (transX >= 0 && transX < tc.length)
				return tc[transX].nextClearBit(0);
			else
				return tc[0].nextClearBit(0);

		}

		private int translate(int startX) {
			return (int) (startX * ((double) tc.length / (double) visibleLength));

		}

		public TilingMatrix(double screenWidth, int visibleLength, int maxStack) {
			this.visibleLength = visibleLength;
			tc = new BitSet[(int) screenWidth + 1];
			for (int i = 0; i < tc.length; i++) {
				tc[i] = new BitSet(maxStack);
			}
		}

		public int length() {
			return tc.length;
		}

		/*
		 * set from all x [from,to[
		 */
		public void rangeSet(int fromX, int toX, int y) {
			fromX = translate(fromX);
			toX = translate(toX);
			if (fromX < 0)
				fromX = 0;
			if (toX > tc.length)
				toX = tc.length;
			for (int i = fromX; i < toX; i++) {
				if (i >= 0 && i < tc.length)
					tc[i].set(y);

			}

		}

	}

	enum ReadColor {
		FORWARD_SENSE("shortread:forwardColor"), FORWARD_ANTISENSE("shortread:forwardAntiColor"), REVERSE_SENSE(
				"shortread:reverseColor"), REVERSE_ANTISENSE("shortread:reverseAntiColor");

		Color c;
		ColorGradient cg;
		private String cfg;

		private ReadColor(String cfg) {
			this.cfg = cfg;
			reset();

		}

		static void resetAll() {
			for (ReadColor rc : values())
				rc.reset();
		}

		private void reset() {
			c = Configuration.getColor(cfg);
			cg = new ColorGradient();
			cg.addPoint(Color.WHITE);
			cg.addPoint(c);
			cg.createGradient(100);

		}

	}

	/*
	 * Mapping of all painted reads, at least in detailed mode
	 */
	private HashMap<Rectangle, SAMRecord> hitMap = new HashMap<Rectangle, SAMRecord>();

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth, JViewport view) {
		paintedBlocks.clear();
		hitMap.clear();

		this.view = view;
		// /* Store information to be used in other methods */
		// currentEntry = entry;
		// currentScreenWidth = screenWidth;
		seqBuffer = null;
		// this.currentYOffset = yOffset;
		/* Configuration options */
		int maxReads = Configuration.getInt("shortread:maxReads");
		int maxRegion = Configuration.getInt("shortread:maxRegion");
		int maxStack = Configuration.getInt("shortread:maxStack");

		pairingColor = Configuration.getColor("shortread:pairingColor");

		currentVisible = model.getAnnotationLocationVisible();

		int originalYOffset = yOffset;

		ReadGroup rg = (ReadGroup) entry.get(dataKey);
		if (rg == null)
			return 0;

		/*
		 * Draw individual reads when possible
		 */
		Iterable<SAMRecord> reads = null;

		int readLength = ((ReadGroup) entry.get(dataKey)).readLength();
		int pairLength = readLength;
		if (entry.get(dataKey) instanceof BAMreads)
			pairLength = ((BAMreads) entry.get(dataKey)).getPairLength();

		if (!isCollapsed() && (currentVisible.length() > maxRegion)) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big (max " + maxRegion + " nt), zoom in", (int) (screenWidth / 2), yOffset + 10);
			yOffset += 20 + 5;
		} else if (!isCollapsed()) {
			/* Access to BAMread is through buffer for performance! */
			reads = rg.get(currentVisible.start, currentVisible.end);

		}

		int lines = 0;
		boolean stackExceeded = false;
		boolean enablePairing = Configuration.getBoolean("shortread:enablepairing");

		int readLineHeight = 3;
		if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			/*
			 * Make some room for the SNP track. Although it's painted last, it
			 * needs to be drawn above the reads
			 */
			readLineHeight = 14;
			// yOffset += snpTrackHeight;
			// nc.init(currentVisible.length());
		}

		if (reads != null) {

			lines = 0;

			TilingMatrix tilingCounter = new TilingMatrix(screenWidth, currentVisible.length(), maxStack);

			int visibleReadCount = 0;
			try {
				for (SAMRecord one : reads) {

					if (enablePairing && one.getReadPairedFlag() && ShortReadTools.isSecondInPair(one)) {
						if (rg.getFirstRead(one) == null) {
							// System.out.println("First read not found");
						} else if (!one.getMateUnmappedFlag()) {

							continue;
						}

					}

					if (visibleReadCount > maxReads) {
						String msg = "Too many short reads to display, only first " + maxReads + " are displayed ";
						FontMetrics metrics = g.getFontMetrics();
						int hgt = metrics.getHeight();
						int adv = metrics.stringWidth(msg);

						int h = 25;
						if ((lines + 1) * readLineHeight + 5 > h)
							h = (lines + 1) * readLineHeight + 5;

						g.setColor(new Color(255, 0, 0, 100));
						g.fillRect(lastX, yOffset, (int) screenWidth, h);

						g.setColor(Color.WHITE);
						g.fillRect((int) (screenWidth / 2 - adv / 2), yOffset + h / 2 - hgt, adv + 2, hgt + 2);
						g.setColor(Color.RED);
						g.drawRect((int) (screenWidth / 2 - adv / 2), yOffset + h / 2 - hgt, adv + 2, hgt + 2);
						g.drawString(msg, (int) (screenWidth / 2 - adv / 2) + 2, yOffset + h / 2 - 2);

						break;
					}

					// int x2 = Convert.translateGenomeToScreen(one.end() + 1,
					// currentVisible, screenWidth);
					// if (x2 > 0) {
					/* Find empty line */
					int pos = one.getAlignmentStart() - currentVisible.start;
					int line = tilingCounter.getFreeLine(pos);

					/* Paint read or read pair */
					if (line < maxStack) {
						int clearStart = one.getAlignmentStart();
						int clearEnd = one.getAlignmentEnd();
						SAMRecord two = null;
						/* Modify empty space finder for paired reads */
						if (enablePairing) {
							// ShortReadTools esr = (ShortReadTools) one;
							if (ShortReadTools.isPaired(one) && ShortReadTools.isFirstInPair(one)) {
								two = rg.getSecondRead(one);
								if(!one.getMateUnmappedFlag()&&one.getReferenceIndex()!=one.getMateReferenceIndex()){
									System.out.println("Different indices: "+one.getReferenceIndex()+"\t"+one.getMateReferenceIndex());
								}
							
//								if (two !=null&& one.getReferenceIndex() != two.getReferenceIndex()) {
//									System.out.println("Different indices: "+one.getReadName()+"\t"+two.getReferenceName());
//									
//								}
							}
							if (two != null) {
								if (two.getAlignmentStart() < one.getAlignmentStart()) {

									pos = two.getAlignmentStart() - currentVisible.start;
									line = tilingCounter.getFreeLine(pos);
									if (line >= maxStack) {
										stackExceeded = true;
										continue;
									}
									clearStart = two.getAlignmentStart();
								} else {
									clearEnd = two.getAlignmentEnd();
								}
							}

						}
						/* The y-coordinate of the read */
						int yRec = line * readLineHeight;

						/* paired read - calculate connection coordinates */
						if (two != null) {

							int subX1, subX2;
							if (one.getAlignmentStart() < two.getAlignmentStart()) {

								subX1 = Convert.translateGenomeToScreen(one.getAlignmentEnd(), currentVisible,
										screenWidth);
								subX2 = Convert.translateGenomeToScreen(two.getAlignmentStart(), currentVisible,
										screenWidth);

							} else {

								subX1 = Convert.translateGenomeToScreen(one.getAlignmentStart(), currentVisible,
										screenWidth);
								subX2 = Convert.translateGenomeToScreen(two.getAlignmentEnd(), currentVisible,
										screenWidth);

							}

							g.setColor(pairingColor);
							g.drawLine(subX1, yRec + (readLineHeight / 2) + yOffset, subX2, yOffset + yRec
									+ readLineHeight / 2);
						}
						if (line > lines)
							lines = line;
						g.translate(0, yOffset);

						boolean paintOne = paintRead(g, one, yRec, screenWidth, readLineHeight, entry, null, yOffset);
						boolean paintTwo = false;
						if (paintOne)
							visibleReadCount++;
						if (two != null) {
							paintTwo = paintRead(g, two, yRec, screenWidth, readLineHeight, entry, one, yOffset);
							if (paintTwo)
								visibleReadCount++;
						}
						g.translate(0, -yOffset);
						/* Carve space out of hitmap */
						// FIXME this range set has to be done because this will
						// determine the number of lines which will be used to
						// determine the size of the track, which is needed to
						// properly set the scrollbars.
						if (true || paintOne || paintTwo)
							tilingCounter.rangeSet(clearStart - pairLength - currentVisible.start, clearEnd + 4
									- currentVisible.start, line);

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
				g.drawString("Max stacking depth reached!", 5, lines * readLineHeight + yOffset - 2);
				g.drawLine(0, lines * readLineHeight + yOffset, (int) screenWidth, lines * readLineHeight + yOffset);
				lines++;

			}
			if ((lines + 1) * readLineHeight + 5 > 25)
				yOffset += (lines + 1) * readLineHeight + 5;
			else
				yOffset += 25;

		}

		/* Draw label */
		String name = StaticUtils.shortify(super.dataKey.toString());
		FontMetrics metrics = g.getFontMetrics();
		int hgt = metrics.getHeight();
		int adv = metrics.stringWidth(name);

		g.setColor(Color.WHITE);
		g.fillRect(10, originalYOffset, adv + 2, hgt + 2);

		g.setColor(Color.BLUE);
		g.drawString(name, 10, originalYOffset + hgt - 2);

		return yOffset - originalYOffset;
	}

	/* Keeps track of the short-read insertions */
	private Map<Rectangle, ShortReadInsertion> paintedBlocks = new HashMap<Rectangle, ShortReadInsertion>();
	/*
	 * Buffer that will contain the visible reference sequence as soon as it has
	 * been used to paint mismatches
	 */
	private char[] seqBuffer = null;

	/**
	 * Returns true if the read was actually painted.
	 * 
	 * @param g
	 * @param rf
	 * @param yRec
	 * @param screenWidth
	 * @param readLineHeight
	 * @param entry
	 * @param otherRead
	 *            the other read in the pair, this should only be set for the
	 *            second read, and should be null for the first read.
	 * @return Returns true if the read was actually painted, false if it wasn't
	 */
	private boolean paintRead(Graphics2D g, SAMRecord rf, int yRec, double screenWidth, int readLineHeight,
			Entry entry, SAMRecord otherRead, double yOff) {
		/* If outside vertical view, return immediately */

		if (yRec + yOff > view.getViewRect().y + view.getViewRect().height) {
			return false;
		}
		if (yRec + yOff < view.getViewRect().y - 10) {
			double startY = view.getViewRect().y - yOff;
			// System.out.println("\t" + yRec + "\t" + yOff + ")\t" +
			// view.getViewRect().y + "\t" + startY);
			return false;
		}

		// System.out.print(",");
		int subX1 = Convert.translateGenomeToScreen(rf.getAlignmentStart(), currentVisible, screenWidth);
		int subX2 = Convert.translateGenomeToScreen(rf.getAlignmentEnd() + 1, currentVisible, screenWidth);

		/* If outside of screen, return immediately */
		if (subX1 > screenWidth || subX2 < 0)
			return false;

		lastX = subX2;
		ReadColor c = null;
		if (rf.getReadPairedFlag()) {
			if (rf.getFirstOfPairFlag()) {
				if (rf.getReadNegativeStrandFlag()) {
					c = ReadColor.REVERSE_SENSE;
				} else {
					c = ReadColor.FORWARD_ANTISENSE;
				}

			} else {
				if (rf.getReadNegativeStrandFlag()) {
					c = ReadColor.REVERSE_ANTISENSE;
				} else {
					c = ReadColor.FORWARD_SENSE;

				}
			}
		} else {
			if (rf.getReadNegativeStrandFlag()) {
				c = ReadColor.REVERSE_SENSE;
			} else {
				c = ReadColor.FORWARD_SENSE;

			}
		}

		g.setColor(c.c);

		if (subX2 < subX1) {
			subX2 = subX1;
			// FIXME does this ever happen?
			// XXX The one time it did happen it pointed to a bug, so it may be
			// that it doesn't happen when all goes well.
			System.err.println("This happens!");
		}

		int qual = rf.getMappingQuality();
		g.setColor(c.cg.getColor(qual));

		Rectangle r = new Rectangle(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);
		g.fill(r);
		g.setColor(c.c);
		if (rf.getReadPairedFlag() && rf.getMateUnmappedFlag())
			g.setColor(Color.RED);
		g.setStroke(new BasicStroke(2));
		g.drawRect(subX1, yRec + 1, subX2 - subX1, readLineHeight - 3);
		g.setStroke(new BasicStroke(1));
		g.setColor(c.c);

		if (otherRead != null) {
			int subOtherX1 = Convert
					.translateGenomeToScreen(otherRead.getAlignmentStart(), currentVisible, screenWidth);
			int subOtherX2 = Convert.translateGenomeToScreen(otherRead.getAlignmentEnd() + 1, currentVisible,
					screenWidth);
			Location l1 = new Location(subOtherX1, subOtherX2);
			Location l2 = new Location(subX1, subX2);

			if (l1.overlaps(l2)) {
				// System.out.println("L1: "+l1);
				// System.out.println("L2: "+l2);
				Location l = LocationTools.getOverlap(l1, l2);
				// System.out.println("L="+l);
				// int x1 = Convert.translateGenomeToScreen(l.start,
				// currentVisible, screenWidth);
				// int x2 = Convert.translateGenomeToScreen(l.end+ 1,
				// currentVisible, screenWidth);
				g.setColor(Color.BLACK);
				g.fillRect(l.start, yRec, l.length(), readLineHeight - 1);

			}

		}

		/* Check mismatches */
		if (entry.sequence().size() == 0)
			return true;
		/*
		 * == Detailed mode ==
		 */
		if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {

			hitMap.put(r, rf);
			/* If there is no sequence, return immediately */
			if (entry.sequence().size() == 0)
				return true;
			if (seqBuffer == null) {

				Iterable<Character> bufferedSeq = entry.sequence().get(currentVisible.start, currentVisible.end + 1);

				seqBuffer = new char[currentVisible.length() + 1];
				int idx = 0;
				for (char cc : bufferedSeq) {
					seqBuffer[idx++] = cc;
				}

			}
			byte[] readNts = ShortReadTools.construct(rf);
			for (int j = rf.getAlignmentStart(); j <= rf.getAlignmentEnd(); j++) {
				if (j > currentVisible.end || j < currentVisible.start)
					continue;
				// FIXME Speed-up by putting code here...
				// char readNt = ShortReadTools.getNucleotide(rf, j
				// - rf.getAlignmentStart() + 1);
				char readNt = (char) readNts[j - rf.getAlignmentStart()];
				// char refNt = entry.sequence.getNucleotide(j);

				char refNt = seqBuffer[j - currentVisible.start];
				double tx1 = Convert.translateGenomeToScreen(j, currentVisible, screenWidth);
				double tx2 = Convert.translateGenomeToScreen(j + 1, currentVisible, screenWidth);

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
						// nc.count(readNt, j - currentVisible.start);
						break;
					}
					int width = (int) (tx2 - tx1);
					if (width < 1)
						width = 1;
					g.fillRect((int) tx1, yRec, width, readLineHeight - 1);
					/*
					 * For spliced alignments, the connection is blanked with a
					 * white box, now put some color back
					 */
					if (readNt == '_') {
						g.setColor(pairingColor);
						g.fillRect((int) tx1, yRec + 4, (int) (tx2 - tx1), readLineHeight - 8 - 1);
					}
					if (readNt != '_' && model.getAnnotationLocationVisible().length() < 100) {
						g.setColor(c.c);
						Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + readNt, g);
						g.drawString("" + readNt, (int) (tx1 + ((tx2 - tx1) / 2 - stringSize.getWidth() / 2)), yRec
								+ readLineHeight - 3);

					}

				}
			}

			// if (rf instanceof ShortReadTools) {
			// ShortReadTools esr = (ShortReadTools) rf;
			int pos = 0;
			int esrPos = 0;
			for (CigarElement ce : rf.getCigar().getCigarElements()) {
				if (ce.getOperator() == CigarOperator.I) {
					double tx1 = Convert.translateGenomeToScreen(rf.getAlignmentStart() + pos, currentVisible,
							screenWidth);
					if (ce.getLength() % 3 == 0)
						g.setColor(Color.GRAY);
					else
						g.setColor(Color.BLACK);
					Rectangle rec = new Rectangle((int) (tx1 - 1), yRec, 2, readLineHeight - 1);
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
		} else {

			int[][] locs = splice(rf);
			for (int i = 0; i < locs[0].length; i++) {
				int lx1 = Convert.translateGenomeToScreen(rf.getAlignmentStart() + locs[0][i], currentVisible,
						screenWidth);
				int lx2 = Convert.translateGenomeToScreen(rf.getAlignmentStart() + locs[1][i], currentVisible,
						screenWidth);
				g.setColor(pairingColor);
				g.fillRect(lx1, yRec, lx2 - lx1, readLineHeight - 1);
			}
		}
		return true;
		// }

	}

	private int[][] splice(SAMRecord rf) {
		List<CigarElement> list = rf.getCigar().getCigarElements();
		int len = 0;
		for (CigarElement ce : list) {
			if (ce.getOperator() == CigarOperator.N) {
				len++;
			}

		}
		int[][] out = new int[2][len];
		int idx = 0;
		int pos = 0;
		for (CigarElement ce : list) {
			switch (ce.getOperator()) {
			case I:
				// pos+=ce.getLength();
				// System.out.println("I: "+pos);
				break;
			case N:
				out[0][idx] = pos;
				pos += +ce.getLength();
				out[1][idx] = pos;
				idx++;
				break;
			case D:
				// System.out.println("D: "+pos);
				pos += ce.getLength();
				break;
			case M:
				pos += ce.getLength();
				break;
			case S:
				// //out[pos] = readBases[superPos];
				// //pos++;
				// superPos++;
				break;
			case H:
				// i++;
				break;
			default:
			}
		}
		return out;
	}

	private class ShortReadInsertion {
		SAMRecord esr;
		int start, len;
	}

	@Override
	public String displayName() {
		return "Short reads: " + super.dataKey;
	}

}
