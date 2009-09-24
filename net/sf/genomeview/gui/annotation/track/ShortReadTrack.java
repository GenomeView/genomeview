/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.shortread.BAMreads;
import net.sf.jannot.shortread.ExtendedShortRead;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.shortread.ShortRead;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.wiggle.ShortReadCoverage;

public class ShortReadTrack extends Track {

	private final int graphLineHeigh = 50;

	private Tooltip tooltip = new Tooltip();

	private class Tooltip extends JWindow {

		private static final long serialVersionUID = -7416732151483650659L;

		private JLabel floater = new JLabel();

		public Tooltip() {
			floater.setBackground(Color.GRAY);
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}

		public void set(int forward, int reverse, int d, MouseEvent e) {
			StringBuffer text = new StringBuffer();
			text.append("<html>");
			text.append("Forward coverage : " + (forward < 0 ? "In progress..." : forward) + "<br />");
			text.append("Reverse coverage: " + (reverse < 0 ? "In progress..." : reverse) + "<br />");
			text.append("Total coverage : " + (d < 0 ? "In progress..." : d) + "<br />");
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

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		if (scale <= 256) {
			if (!tooltip.isVisible())
				tooltip.setVisible(true);
			ReadGroup rg = currentEntry.shortReads.getReadGroup(this.source);
			ShortReadCoverage currentBuffer = rg.getCoverage();
			int start = Convert.translateScreenToGenome(x, currentVisible, currentScreenWidth);
			int f=(int)currentBuffer.get(Strand.FORWARD,start-1);
			int r=(int)currentBuffer.get(Strand.REVERSE,start-1);
			tooltip.set(f, r, f+r, source);
		} else {
			if (tooltip.isVisible())
				tooltip.setVisible(false);
		}
		return false;
	}

	private DataSource source;

	public ShortReadTrack(Model model, DataSource source) {
		super(model, true, true);
		this.source = source;
	}

	

	private int scale = 1;
	private int scaleIndex = 0;

	private Location currentVisible;

	private Color pairingColor;

	private Color reverseColor;

	private Color forwardColor;

	private Entry currentEntry;

	private double currentScreenWidth;

	private static final double LOG2 = Math.log(2);

	private double log2(double d) {
		return Math.log(d) / LOG2;
	}

	@Override
	public int paintTrack(Graphics2D g, final Entry entry, int yOffset, double screenWidth) {
		/* Store information to be used in other methods */
		currentEntry = entry;
		currentScreenWidth = screenWidth;
		/* Configuration options */
		int maxReads = Configuration.getInt("shortread:maxReads");
		int maxRegion = Configuration.getInt("shortread:maxRegion");
		int maxStack = Configuration.getInt("shortread:maxStack");
		forwardColor = Configuration.getColor("shortread:forwardColor");
		reverseColor = Configuration.getColor("shortread:reverseColor");
		pairingColor = Configuration.getColor("shortread:pairingColor");

		currentVisible = model.getAnnotationLocationVisible();

		int originalYOffset = yOffset;

		int readLineHeight = 3;
		if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			readLineHeight = 14;
		}

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

		int start = currentVisible.start() / scale * scale;
		int end = ((currentVisible.end() / scale) + 1) * scale;

		ReadGroup rg = entry.shortReads.getReadGroup(source);
		ShortReadCoverage graph = rg.getCoverage();//.get(rg);

	
		GeneralPath conservationGP = new GeneralPath();
		GeneralPath conservationGPF = new GeneralPath();
		GeneralPath conservationGPR = new GeneralPath();
	
		float[] f = graph.get(Strand.FORWARD, start-1, end, scaleIndex);
		float[] r = graph.get(Strand.REVERSE, start-1, end, scaleIndex);
		
		for (int i = 0; i < f.length; i++) {
			int x = Convert.translateGenomeToScreen(start + i * scale, currentVisible, screenWidth);
			double valF = f[i];
			double valR = r[i];
			double val = f[i] + r[i];
			/* Cap value */
			if (valF > graph.max())
				valF = graph.max();
			if (valR > graph.max())
				valR = graph.max();
			if (val > graph.max())
				val = graph.max();
			/* Logaritmic scaling */
			valF = log2(valF);
			valF /= log2(graph.max());
			valR = log2(valR);
			valR /= log2(graph.max());
			val = log2(val);
			val /= log2(graph.max());

			/* Draw lines */
			if (i == 0) {
				conservationGP.moveTo(x - 1, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
				conservationGPF.moveTo(x - 1, yOffset + (1 - valF) * (graphLineHeigh - 4) + 2);
				conservationGPR.moveTo(x - 1, yOffset + (1 - valR) * (graphLineHeigh - 4) + 2);
			}

			conservationGP.lineTo(x, yOffset + (1 - val) * (graphLineHeigh - 4) + 2);
			conservationGPF.lineTo(x, yOffset + (1 - valF) * (graphLineHeigh - 4) + 2);
			conservationGPR.lineTo(x, yOffset + (1 - valR) * (graphLineHeigh - 4) + 2);

		}

		/* Draw coverage lines */
		g.setColor(forwardColor);
		g.draw(conservationGPF);

		g.setColor(reverseColor);
		g.draw(conservationGPR);
		g.setColor(Color.BLACK);
		g.draw(conservationGP);

		g.setColor(Color.BLUE);
		g.drawString(source.toString() + " (" + scale + ")", 10, yOffset + 12 - 2);
		yOffset += graphLineHeigh;
		// }

		/*
		 * Draw individual reads when possible
		 */
		Iterable<ShortRead> reads = null;
		boolean timeout = false;
		int readLength = entry.shortReads.getReadGroup(source).readLength();
		if (!isCollapsed() && (currentVisible.length() > maxRegion)) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big (max " + maxRegion + " nt), zoom in", 10, yOffset + 10);
			yOffset += 20 + 5;
		} else if (!isCollapsed()) {
			/* Access to BAMread is through buffer for performance! */
			reads = rg.get(currentVisible);
			if (rg instanceof BAMreads) {
				/* Update readLength for paired reads */
				readLength = ((BAMreads) rg).getPairLength();

			} else {
				reads = rg.get(currentVisible);
			}
		}
		int maxPairingDistance = Configuration.getInt("shortread:maximumPairing");
		if (readLength > maxPairingDistance)
			readLength = maxPairingDistance;
		int lines = 0;
		boolean stackExceeded = false;
		boolean enablePairing = Configuration.getBoolean("shortread:enablepairing");
		if (reads != null) {
			lines = 0;

			BitSet[] tilingCounter = new BitSet[currentVisible.length()];
			for (int i = 0; i < tilingCounter.length; i++) {
				tilingCounter[i] = new BitSet();
			}
			int visibleReadCount = 0;
			try {
				for (ShortRead one : reads) {

					if (enablePairing && one.isPaired() && one.isSecondInPair())
						continue;

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

					int x2 = Convert.translateGenomeToScreen(one.end() + 1, currentVisible, screenWidth);
					// if (x2 > 0) {
					/* Find empty line */
					int pos = one.start() - currentVisible.start();
					int line = line(one, pos, tilingCounter);
					if (line > maxStack) {
						stackExceeded = true;
						continue;

					}
					int clearStart = one.start();
					int clearEnd = one.end();
					ExtendedShortRead two = null;
					/* Modify empty space finder for paired reads */
					if (enablePairing && one instanceof ExtendedShortRead) {
						ExtendedShortRead esr = (ExtendedShortRead) one;
						if (esr.isPaired() && esr.isFirstInPair()) {
							two = ((BAMreads) rg).getSecondRead(esr);
						}
						if (two != null) {
							if (two.start() < one.start()) {

								pos = two.start() - currentVisible.start();
								line = line(two, pos, tilingCounter);
								clearStart = two.start();
							} else {
								clearEnd = two.end();
							}
						}

					}
					/* Carve space out of hitmap */
					for (int i = clearStart - readLength; i <= clearEnd + 3; i++) {
						pos = i - currentVisible.start();
						if (pos >= 0 && pos < tilingCounter.length)
							tilingCounter[pos].set(line);
					}

					int yRec = line * readLineHeight + yOffset;

					/* Paint read or read pair */
					if (line < maxStack) {
						/* paired read - calculate connection coordinates */
						if (two != null) {

							int subX1, subX2;
							if (one.start() < two.start()) {

								subX1 = Convert.translateGenomeToScreen(one.end(), currentVisible, screenWidth);
								subX2 = Convert.translateGenomeToScreen(two.start(), currentVisible, screenWidth);

							} else {

								subX1 = Convert.translateGenomeToScreen(one.start(), currentVisible, screenWidth);
								subX2 = Convert.translateGenomeToScreen(two.end(), currentVisible, screenWidth);

							}

							g.setColor(pairingColor);
							g.drawLine(subX1, yRec + readLineHeight / 2, subX2, yRec + readLineHeight / 2);
						}
						if (line > lines)
							lines = line;

						paintRead(g, one, yRec, screenWidth, readLineHeight, entry);
						visibleReadCount++;
						if (two != null) {

							paintRead(g, two, yRec, screenWidth, readLineHeight, entry);
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
				g.drawString("Max stacking depth reached!", 5, lines * readLineHeight + yOffset - 2);
				g.drawLine(0, lines * readLineHeight + yOffset, (int) screenWidth, lines * readLineHeight + yOffset);
				lines++;

			}
			yOffset += (lines + 1) * readLineHeight + 5;

		}
		return yOffset - originalYOffset;
	}

	private int line(ShortRead one, int pos, BitSet[] tilingCounter) {
		if (pos >= 0 && pos < tilingCounter.length)
			return tilingCounter[one.start() - currentVisible.start()].nextClearBit(0);
		else
			return tilingCounter[0].nextClearBit(0);

	}

	private void paintRead(Graphics2D g, ShortRead rf, int yRec, double screenWidth, int readLineHeight, Entry entry) {
		Color c = Color.GRAY;
		if (rf.strand() == Strand.FORWARD)
			c = forwardColor;
		else
			c = reverseColor;
		g.setColor(c);
		int subX1 = Convert.translateGenomeToScreen(rf.start(), currentVisible, screenWidth);
		int subX2 = Convert.translateGenomeToScreen(rf.end() + 1, currentVisible, screenWidth);
		if (subX2 < subX1) {
			subX2 = subX1;
			// FIXME does this ever happen?
			// XXX The one time it did happen it pointed to a bug, so it may be
			// that it doesn't happen when all goes well.
			System.err.println("This happens!");
		}
		g.fillRect(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);

		/* Check mismatches */
		if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			for (int j = rf.start(); j <= rf.end(); j++) {
				char readNt = rf.getNucleotide(j - rf.start() + 1);
				char refNt = Character.toUpperCase(entry.sequence.getNucleotide(j));
				double tx1 = Convert.translateGenomeToScreen(j, currentVisible, screenWidth);
				double tx2 = Convert.translateGenomeToScreen(j + 1, currentVisible, screenWidth);

				if (readNt != refNt) {
					if (readNt == '-') {
						g.setColor(Color.RED);
					} else
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

	@Override
	public String displayName() {
		return "Short reads: " + source.toString();
	}

	public DataSource source() {
		return source;
	}
}
