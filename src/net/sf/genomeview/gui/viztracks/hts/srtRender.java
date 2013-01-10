/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.hts;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import be.abeel.util.LRUCache;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.LocationTools;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.DataCallback;
import net.sf.genomeview.data.provider.ShortReadProvider;
import net.sf.genomeview.data.provider.Status;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.hts.ShortReadTrackConfig.ReadColor;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.shortread.BAMreads;
import net.sf.jannot.shortread.ShortReadTools;
import net.sf.samtools.CigarElement;
import net.sf.samtools.CigarOperator;
import net.sf.samtools.SAMRecord;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class srtRender implements Observer, DataCallback<SAMRecord> {

	
	
	private org.broad.LRUCache<SAMRecord, Integer> rowCache=new org.broad.LRUCache<SAMRecord, Integer>(50000);
	/* Keeps track of the short-read insertions */
	Map<Rectangle, ShortReadInsertion> paintedBlocks = new HashMap<Rectangle, ShortReadInsertion>();
	/*
	 * Buffer that will contain the visible reference sequence as soon as it has
	 * been used to paint mismatches
	 */
	private char[] seqBuffer = null;
	private Model model;
	private ShortReadProvider provider;
	private DataKey dataKey;

	public srtRender(Model model, ShortReadProvider provider, ShortReadTrackConfig srtc, DataKey key) {
		model.vlm.addObserver(this);
		this.model = model;
		this.dataKey = key;
		this.provider = provider;
		this.srtc = srtc;
		buffer = new BufferedImage((int) model.vlm.screenWidth(), 20, BufferedImage.TYPE_INT_ARGB);

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

	/*
	 * Mapping of all painted reads, at least in detailed mode
	 */
	HashMap<Rectangle, SAMRecord> hitMap = new HashMap<Rectangle, SAMRecord>();
	private Location currentVisible;
	private ShortReadTrackConfig srtc;
	private BufferedImage buffer;

	// class srtDataCallback implements DataCallback<SAMRecord>{

	@Override
	public void dataReady(List<SAMRecord> reads) {
		

		int maxReads = Configuration.getInt("shortread:maxReads");

		int maxStack = Configuration.getInt("shortread:maxStack");

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

		// pairingColor = Configuration.getColor();

		int yOffset = 0;
		// int originalYOffset = yOffset;
		paintedBlocks.clear();
		hitMap.clear();
		seqBuffer = null;

		Entry entry = model.vlm.getSelectedEntry();

		/*
		 * Draw individual reads when possible
		 */
		// Iterable<SAMRecord> reads = null;

		int readLength = provider.readLength();
		int pairLength = readLength;
		if (entry.get(dataKey) instanceof BAMreads)
			pairLength = ((BAMreads) entry.get(dataKey)).getPairLength();

		/* Paint reads */
		// if (reads != null) {
		double screenWidth = model.vlm.screenWidth();
		int lines = 0;
		int height = maxStack * readLineHeight;
		BufferedImage bi = new BufferedImage((int) screenWidth, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g = (Graphics2D) bi.getGraphics();
		boolean stackExceeded = false;
		boolean enablePairing = Configuration.getBoolean("shortread:enablepairing");
		lines = 0;

		TilingMatrix tilingCounter = new TilingMatrix(model.vlm.screenWidth(), currentVisible.length(), maxStack);

		int visibleReadCount = 0;
		try {
			for (SAMRecord one : reads) {

				if (enablePairing && one.getReadPairedFlag() && ShortReadTools.isSecondInPair(one)) {
					if (provider.getFirstRead(one) == null) {
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

				// int x2 = Convert.translateGenomeToScreen(one.end() +
				// 1,
				// currentVisible, screenWidth);
				// if (x2 > 0) {
				/* Find empty line */
				int pos = one.getAlignmentStart() - currentVisible.start;
				int line = tilingCounter.getFreeLine(pos);

				/* Paint read or read pair */
				// boolean differentReference=true;
				if (line < maxStack) {
					int clearStart = one.getAlignmentStart();
					int clearEnd = one.getAlignmentEnd();
					SAMRecord two = null;
					/* Modify empty space finder for paired reads */
					if (enablePairing) {
						// ShortReadTools esr = (ShortReadTools) one;
						if (ShortReadTools.isPaired(one) && ShortReadTools.isFirstInPair(one)) {
							two = provider.getSecondRead(one);

						}
						// if (two != null) {
						if (ShortReadTools.isPaired(one) && !one.getMateUnmappedFlag()
								&& one.getReferenceIndex() == one.getMateReferenceIndex() && one.getMateReferenceIndex() != -1) {
							// if (two == null)
							// System.out.println("Mate missing: " +
							// one.getMateAlignmentStart());
							// if (two.getAlignmentStart() <
							// one.getAlignmentStart()) {
							if (one.getMateAlignmentStart() < one.getAlignmentStart()) {
								pos = one.getMateAlignmentStart() - currentVisible.start;
								line = tilingCounter.getFreeLine(pos);
								if (line >= maxStack) {
									stackExceeded = true;
									continue;
								}
								clearStart = one.getMateAlignmentStart();
							} else {
								clearEnd = one.getMateAlignmentStart() + one.getReadLength();
							}
						}

					}
					/* The y-coordinate of the read */
					int yRec = line * readLineHeight;

					/* paired read - calculate connection coordinates */
					if (ShortReadTools.isPaired(one) && !one.getMateUnmappedFlag()
							&& one.getReferenceIndex() == one.getMateReferenceIndex() && one.getMateReferenceIndex() != -1) {
						int subX1, subX2;
						subX1 = Convert.translateGenomeToScreen(one.getAlignmentEnd(), currentVisible, screenWidth);
						subX2 = Convert.translateGenomeToScreen(one.getMateAlignmentStart(), currentVisible, screenWidth);

						g.setColor(srtc.color(ReadColor.PAIRING));
						g.drawLine(subX1, yRec + (readLineHeight / 2) + yOffset, subX2, yOffset + yRec + readLineHeight / 2);
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
					// FIXME this range set has to be done because this
					// will
					// determine the number of lines which will be used
					// to
					// determine the size of the track, which is needed
					// to
					// properly set the scrollbars.
					if (true || paintOne || paintTwo)
						tilingCounter.rangeSet(clearStart - pairLength - currentVisible.start, clearEnd + 4 - currentVisible.start, line);

				} else {
					stackExceeded = true;
				}
				// }
			}
		} catch (ConcurrentModificationException e) {
			System.err.println("CME!!");
			// Ignore
		}
		if (stackExceeded) {
			g.setColor(Color.RED);
			g.drawString("Max stacking depth reached!", 5, lines * readLineHeight + yOffset - 2);
			g.drawLine(0, lines * readLineHeight + yOffset, (int) screenWidth, lines * readLineHeight + yOffset);
			lines++;

		}

		
		/* Crop buffered image if not everything is needed */
		int actualHeight = lines * readLineHeight;
		buffer = bi.getSubimage(0, 0, bi.getWidth(), actualHeight);
		model.refresh();
	}

	public void requestNew(Entry entry, DataKey dataKey, ShortReadProvider provider, Location currentVisible, ShortReadTrackConfig srtc,
			double screenWidth) {
		this.currentVisible = currentVisible;
		this.srtc = srtc;

		int maxRegion = Configuration.getInt("shortread:maxRegion");

		if (currentVisible.length() > maxRegion) {
			BufferedImage bi = new BufferedImage((int) screenWidth, 20, BufferedImage.TYPE_INT_ARGB);
			// BufferedImage bi = (BufferedImage) createImage();
			Graphics2D g = bi.createGraphics();
			g.setColor(Color.BLACK);
			g.drawString("Region too big (max " + maxRegion + " nt), zoom in", (int) (screenWidth / 2), 10);
			buffer = bi;
			// yOffset += 20 + 5;
		} else {
			/* Access to BAMread is through buffer for performance! */
			provider.get(currentVisible.start, currentVisible.end, this);

		}

	}

	/* Keep track of the last x-coordinate that has been used for painting */
	private int lastX = 100;

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
	private boolean paintRead(Graphics2D g, SAMRecord rf, int yRec, double screenWidth, int readLineHeight, Entry entry,
			SAMRecord otherRead, double yOff) {
		/* If outside vertical view, return immediately */

		// if (yRec + yOff > viewRectangle.y + viewRectangle.height) {
		// return false;
		// }
		// if (yRec + yOff < viewRectangle.y - 10) {
		// double startY = viewRectangle.y - yOff;
		// // System.out.println("\t" + yRec + "\t" + yOff + ")\t" +
		// // view.getViewRect().y + "\t" + startY);
		// return false;
		// }

		// System.out.print(",");
		int subX1 = Convert.translateGenomeToScreen(rf.getAlignmentStart(), currentVisible, screenWidth);
		int subX2 = Convert.translateGenomeToScreen(rf.getAlignmentEnd() + 1, currentVisible, screenWidth);
		// System.out.println(rf.getAlignmentBlocks().size());
		// System.out.println(rf.getAlignmentBlocks().get(0).)
		// System.out.println("Start-End: "+rf.getAlignmentStart()+" "+rf.getAlignmentEnd()+"\t"+rf.getUnclippedStart()+"\t"+rf.getUnclippedEnd());
		/* If outside of screen, return immediately */
		if (subX1 > screenWidth || subX2 < 0)
			return false;

		lastX = subX2;
		ReadColor c = null;
		if (ShortReadTools.isPaired(rf) && !rf.getMateUnmappedFlag() && rf.getReferenceIndex() != rf.getMateReferenceIndex()
				&& rf.getMateReferenceIndex() != -1) {
			// System.out.println("Different indices: " + rf.getReferenceIndex()
			// + "\t"
			// + rf.getMateReferenceIndex());
			c = ReadColor.MATE_DIFFERENT_CHROMOSOME;
		} else if (rf.getReadPairedFlag()) {
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

		g.setColor(srtc.color(c));

		if (subX2 < subX1) {
			subX2 = subX1;
			// FIXME does this ever happen?
			// XXX The one time it did happen it pointed to a bug, so it may be
			// that it doesn't happen when all goes well.
			System.err.println("This happens!");
		}

		int qual = rf.getMappingQuality();
		g.setColor(srtc.gradient(c).getColor(qual));

		Rectangle r = new Rectangle(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);
		g.fill(r);
		g.setColor(srtc.color(c));
		if (rf.getReadPairedFlag() && rf.getMateUnmappedFlag())
			g.setColor(srtc.color(ReadColor.MISSING_MATE));
		g.setStroke(new BasicStroke(2));
		g.drawRect(subX1, yRec + 1, subX2 - subX1, readLineHeight - 3);
		g.setStroke(new BasicStroke(1));
		g.setColor(srtc.color(c));

		if (otherRead != null) {
			int subOtherX1 = Convert.translateGenomeToScreen(otherRead.getAlignmentStart(), currentVisible, screenWidth);
			int subOtherX2 = Convert.translateGenomeToScreen(otherRead.getAlignmentEnd() + 1, currentVisible, screenWidth);
			Location l1 = new Location(subOtherX1, subOtherX2);
			Location l2 = new Location(subX1, subX2);

			if (l1.overlaps(subX1, subX2)) {
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
						g.setColor(srtc.color(ReadColor.PAIRING));
						g.fillRect((int) tx1, yRec + 4, (int) (tx2 - tx1), readLineHeight - 8 - 1);
					}
					if (readNt != '_' && this.currentVisible.length() < 100) {
						g.setColor(srtc.color(c));
						Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + readNt, g);
						g.drawString("" + readNt, (int) (tx1 + ((tx2 - tx1) / 2 - stringSize.getWidth() / 2)), yRec + readLineHeight - 3);

					}

				}
			}

			// if (rf instanceof ShortReadTools) {
			// ShortReadTools esr = (ShortReadTools) rf;
			int pos = 0;
			// int esrPos = 0;
			Set<CigarOperator> skip = new HashSet<CigarOperator>();
			skip.add(CigarOperator.HARD_CLIP);
			skip.add(CigarOperator.SOFT_CLIP);
			skip.add(CigarOperator.H);
			skip.add(CigarOperator.S);
			for (CigarElement ce : rf.getCigar().getCigarElements()) {
				if (ce.getOperator() == CigarOperator.I) {
					double tx1 = Convert.translateGenomeToScreen(rf.getAlignmentStart() + pos, currentVisible, screenWidth);
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
					in.start = pos;
					in.len = ce.getLength();
					paintedBlocks.put(rec, in);
				} else {
					if (!skip.contains(ce.getOperator()))
						pos += ce.getLength();
				}
				// if(!skip.contains(ce.getOperator()))
				// esrPos += ce.getLength();
			}
		} else {

			int[][] locs = splice(rf);
			for (int i = 0; i < locs[0].length; i++) {
				int lx1 = Convert.translateGenomeToScreen(rf.getAlignmentStart() + locs[0][i], currentVisible, screenWidth);
				int lx2 = Convert.translateGenomeToScreen(rf.getAlignmentStart() + locs[1][i], currentVisible, screenWidth);
				g.setColor(srtc.color(ReadColor.PAIRING));
				g.fillRect(lx1, yRec, lx2 - lx1, readLineHeight - 1);
			}
		}
		return true;
		// }

	}

	public BufferedImage buffer() {
		assert (buffer != null);
		return buffer;
	}

	private Location prevVisible = null;

	private boolean same(Location loc) {
		boolean same = loc.equals(prevVisible);
		prevVisible = loc;
		return same;
	}

	@Override
	public void update(Observable o, Object arg) {
		/**
		 * If new location -> request new render
		 */

		Location currentVisible = model.vlm.getAnnotationLocationVisible();

		if (!same(currentVisible)) {
			requestNew(model.vlm.getVisibleEntry(), dataKey, provider, currentVisible, srtc, model.vlm.screenWidth());
			System.out.println("Requesting new SRT render");
		} else {
			System.out.println("Using previous render");
		}

	}

}
