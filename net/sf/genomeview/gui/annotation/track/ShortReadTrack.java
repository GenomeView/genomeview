/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.border.Border;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.dialog.GVProgressBar;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.shortread.BAMreads;
import net.sf.jannot.shortread.ExtendedShortRead;
import net.sf.jannot.shortread.MemoryReadSet;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.shortread.ShortRead;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.SAMDataSource;
import net.sf.samtools.SAMFileReader;
import net.sf.samtools.SAMRecord;
import net.sf.samtools.util.CloseableIterator;

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

		public void set(int forward, int reverse, int d,MouseEvent e) {
			StringBuffer text = new StringBuffer();
			text.append("<html>");
			text.append("Forward coverage : " + forward + "<br />");
			text.append("Reverse coverage: " +reverse + "<br />");
			text.append("Total coverage : " + d + "<br />");
			text.append("</html>");
			if (!text.toString().equals(floater.getText())) {
				floater.setText(text.toString());
				setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);
				this.pack();
				setVisible(true);
			}
			
		}

	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		if (y > 5 && y < graphLineHeigh - 5&&scale<=256) {
			if (!tooltip.isVisible())
				tooltip.setVisible(true);
			GraphBuffer currentBuffer=buffers.get(currentEntry);
			int start=Convert.translateScreenToGenome(x,currentVisible, currentScreen);
			
			tooltip.set(currentBuffer.getRawForward(start),currentBuffer.getRawReverse(start),currentBuffer.getRaw(start),source);
		} else {
			if (tooltip.isVisible())
				tooltip.setVisible(false);
		}
		return false;
	}

	private DataSource source;
	private ReadBuffer readBuffer;

	public ShortReadTrack(Model model, DataSource source) {
		super(model, true, true);
		this.source = source;
		this.readBuffer = new ReadBuffer(source);
	}

	class ReadBuffer {

		class Runner implements Runnable {

			private Entry localEntry;

			public Runner(Entry e) {
				this.localEntry = e;
			}

			@Override
			public void run() {
				buffer.clear();
				ReadGroup rg = localEntry.shortReads.getReadGroup(source);
				GVProgressBar bar = new GVProgressBar("Buffering...", "Buffering " + displayName(), model.getParent());
				StaticUtils.upperRight(bar);
				for (ShortRead sr : rg) {
					if (localEntry != entry) {
						return;
					} else {
						buffer.add(sr);
					}

					if (buffer.size() > 2000000) {
						buffer.clear();
						JOptionPane.showMessageDialog(null, "Buffering is not working out, too much data.", "Warning!", JOptionPane.WARNING_MESSAGE);
						bar.dispose();
						return;
					}

				}
				bar.dispose();
				ready = true;

			}
		}

		private DataSource source;
		private Entry entry = null;
		private boolean ready = false;
		private MemoryReadSet buffer = new MemoryReadSet();

		public ReadBuffer(DataSource source) {
			this.source = source;
		}

		public synchronized Iterable<ShortRead> get(Entry e, Location r) {
			if (entry == e) {
				if (ready)
					return buffer.get(r);
				else
					return qFast(e, r);
			} else {
				entry = e;
				ready = false;
				/* Try caching */
				new Thread(new Runner(entry)).start();
				/* But return something fast anyway */
				return qFast(e, r);
			}

		}

		/* qFast */
		private List<ShortRead> qFastBuffer = new ArrayList<ShortRead>();
		private Location qFastBufferLocation = new Location(-5, -5);
		private byte qFastFail = 0;

		private synchronized Iterable<ShortRead> qFast(Entry e, Location r) {
			if (isQFastFail())
				return null;
			if (!(source instanceof SAMDataSource)) {
				return e.shortReads.getReadGroup(source).get(r);
			} else {
				long time = System.currentTimeMillis();
				if (r.start() != qFastBufferLocation.start() || r.end() != qFastBufferLocation.end()) {
					qFastBuffer.clear();
					BAMreads br = (BAMreads) (e.shortReads.getReadGroup(source));
					SAMFileReader tmpReader = new SAMFileReader(((SAMDataSource) source).getFile());
					CloseableIterator<SAMRecord> it = tmpReader.queryOverlapping(br.getKey(), r.start(), r.end());
					while (it.hasNext()) {
						try {
							SAMRecord tmp = it.next();
							byte[] seq = tmp.getReadBases();
							if (complete(seq)) {
								qFastBuffer.add(new ExtendedShortRead(seq, tmp.getAlignmentStart(), tmp.getAlignmentEnd(), !tmp.getReadNegativeStrandFlag(), tmp.getCigar()));

							}
						} catch (RuntimeException ex) {
							System.err.println(e);

						}
						/* Check how long we have been busy */
						if (System.currentTimeMillis() - time > 1000) {
							/* last query failed, skip a few to catch up */
							qFastFail = 100;
							qFastBuffer.clear();
							qFastBufferLocation = new Location(-5, -5);
							it.close();
							tmpReader.close();
							return null;
						}
					}
					qFastBufferLocation = r;
					it.close();
					tmpReader.close();
				}

				return qFastBuffer;
			}

		}

		public boolean isQFastFail() {
			if (qFastFail > 0)
				qFastFail--;
			return qFastFail > 0;
		}

		private boolean complete(byte[] seq) {
			for (int i = 0; i < seq.length; i++)
				if (seq[i] == 'n' || seq[i] == 'N')
					return false;
			return true;
		}

	}

	/**
	 * Keeps an eye on a read group
	 * 
	 * @author tabeel
	 * 
	 */
	static class GraphBuffer implements Observer {

		private double log(double val) {
			if (val > 0)
				return Math.log(val);
			else
				return 0;
		}

		double LOG2 = Math.log(2);
		int bareScale = 32;
		int bareScaleIndex = 5;

		private ReadGroup rg;

		public GraphBuffer(ReadGroup rg) {
			rg.addObserver(this);
			this.rg = rg;
		}

		private List<float[]> buffer = new ArrayList<float[]>();
		private List<float[]> bufferForward = new ArrayList<float[]>();
		private List<float[]> bufferReverse = new ArrayList<float[]>();

		public synchronized double getReverse(int start, int scale) {
			if (start + scale >= rg.getForwardPileUp().size())
				return 0;
			if (scale < bareScale) {
				double conservation = 0;
				for (int j = 0; j < scale; j++) {
					conservation += log(rg.getReversePileUp().get(start + j));
				}
				return conservation / (scale * log(rg.getMaxPile()));
			}
			if (bufferReverse.size() == 0)
				bufferReverse.add(bareReverse());

			int index = (int) (Math.log(scale) / LOG2) - bareScaleIndex;

			while (bufferReverse.size() <= index + 1) {
				bufferReverse.add(merge(bufferReverse.get(bufferReverse.size() - 1)));
			}

			if (start / scale < bufferReverse.get(index).length)
				return bufferReverse.get(index)[start / scale];
			else
				return -0.02;
		}

		public synchronized double getForward(int start, int scale) {
			if (start + scale >= rg.getForwardPileUp().size())
				return 0;
			if (scale < bareScale) {
				double conservation = 0;
				for (int j = 0; j < scale; j++) {
					conservation += log(rg.getForwardPileUp().get(start + j));

				}
				return conservation / (scale * log(rg.getMaxPile()));
			}
			if (bufferForward.size() == 0)
				bufferForward.add(bareForward());

			int index = (int) (Math.log(scale) / LOG2) - bareScaleIndex;

			while (bufferForward.size() <= index + 1) {
				bufferForward.add(merge(bufferForward.get(bufferForward.size() - 1)));
			}

			if (start / scale < bufferForward.get(index).length)
				return bufferForward.get(index)[start / scale];
			else
				return -0.02;
		}

		public synchronized int getRaw(int start){
			if (start>= rg.getForwardPileUp().size())
				return 0;
			return rg.getForwardPileUp().get(start)+rg.getReversePileUp().get(start);
		}
		public synchronized int getRawForward(int start){
			if (start>= rg.getForwardPileUp().size())
				return 0;
			return rg.getForwardPileUp().get(start);
		}
		public synchronized int getRawReverse(int start){
			if (start >= rg.getReversePileUp().size())
				return 0;
			return rg.getReversePileUp().get(start);
		}
		public synchronized double get(int start, int scale) {
			if (start + scale >= rg.getForwardPileUp().size())
				return 0;
			if (scale < bareScale) {
				double conservation = 0;
				for (int j = 0; j < scale; j++) {
					conservation += log(rg.getForwardPileUp().get(start + j));
					conservation += log(rg.getReversePileUp().get(start + j));
				}
				return conservation / (scale * log(rg.getMaxPile()));
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
				return -0.02;
		}

		private float[] merge(float[] ds) {
			float[] out = new float[(ds.length + 1) / 2];
			double max = 0;
			for (int i = 0; i < ds.length - 1; i += 2) {
				out[i / 2] = (ds[i] + ds[i + 1]) / 2;
				if (out[i / 2] > max)
					max = out[i / 2];
			}
			out[out.length - 1] = ds[ds.length - 1];
			for (int i = 0; i < out.length; i++)
				out[i] /= max;
			return out;
		}

		private float[] bare() {
			int size = rg.getForwardPileUp().size();
			float[] out = new float[size / bareScale + 1];
			for (int i = 0; i < size; i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale && i + j < size; j++) {
					conservation += log(rg.getForwardPileUp().get(i + j));
					conservation += log(rg.getReversePileUp().get(i + j));
				}
				conservation /= bareScale * log(rg.getMaxPile());
				out[i / bareScale] = conservation;

			}
			return out;
		}

		private float[] bareReverse() {
			int size = rg.getForwardPileUp().size();
			float[] out = new float[size / bareScale + 1];
			for (int i = 0; i < size; i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale && i + j < size; j++) {
					conservation += log(rg.getReversePileUp().get(i + j));
				}
				conservation /= bareScale * log(rg.getMaxPile());
				out[i / bareScale] = conservation;

			}
			return out;
		}

		private float[] bareForward() {
			int size = rg.getForwardPileUp().size();
			float[] out = new float[size / bareScale + 1];
			for (int i = 0; i < size; i += bareScale) {
				float conservation = 0;
				for (int j = 0; j < bareScale && i + j < size; j++) {
					conservation += log(rg.getForwardPileUp().get(i + j));
				}
				conservation /= bareScale * log(rg.getMaxPile());
				out[i / bareScale] = conservation;

			}
			return out;
		}

		@Override
		public synchronized void update(Observable o, Object arg) {
			buffer.clear();
			bufferForward.clear();
			bufferReverse.clear();
		}

	}

	private Map<Entry, GraphBuffer> buffers = new HashMap<Entry, GraphBuffer>();

	private int  scale=1;

	private Entry currentEntry;

	private Location currentVisible;

	private double currentScreen;
	@Override
	public int paint(Graphics gg, final Entry entry, int yOffset, double screenWidth) {
		currentEntry=entry;
		currentScreen=screenWidth;
		/* Configuration options */
		int maxReads = Configuration.getInt("shortread:maxReads");
		int maxRegion = Configuration.getInt("shortread:maxRegion");
		int maxStack = Configuration.getInt("shortread:maxStack");

		currentVisible = model.getAnnotationLocationVisible();

		int originalYOffset = yOffset;
		Graphics2D g = (Graphics2D) gg;

		int readLineHeight = 3;
		if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			readLineHeight = 14;
		}

		/*
		 * Draw line plot of coverage
		 */
		g.setColor(new Color(204, 238, 255, 100));
		g.fillRect(0, yOffset, (int) screenWidth, graphLineHeigh);
		double width = screenWidth / (double) currentVisible.length() / 2.0;

		scale = 1;
		while (scale < (int) Math.ceil(1.0 / width))
			scale *= 2;

		int start = currentVisible.start() / scale * scale;
		int end = ((currentVisible.end() / scale) + 1) * scale;

		ReadGroup rg = entry.shortReads.getReadGroup(source);
		// System.out.println(entry.shortReads+"\t"+rg+"\t"+source);
		if (rg != null) {
			if (!buffers.containsKey(entry)) {
				// System.out.println("Construction size: " + tmp);
				buffers.put(entry, new GraphBuffer(rg));

			}
			// write(entry.shortReads.counts());
		} else {
			return 0; /* Not yet ready */
		}
		GraphBuffer b = buffers.get(entry);
		if (b != null) {
			GeneralPath conservationGP = new GeneralPath();
			GeneralPath conservationGPF = new GeneralPath();
			GeneralPath conservationGPR = new GeneralPath();
			for (int i = 0; i < (end - start) / scale; i++) {
				int x = Convert.translateGenomeToScreen(start + i * scale + 1, currentVisible, screenWidth) + 5;
				// conservationGP.lineTo(x, yOffset + (1 - cValues[i]) *
				// (lineHeigh - 4) + 2);
				double v = b.get(start + i * scale, scale);
				double vf = b.getForward(start + i * scale, scale);
				double vr = b.getReverse(start + i * scale, scale);

				// if(i==100)
				// checkValue=v;
				if (i == 0) {
					conservationGP.moveTo(x - 1, yOffset + (1 - v) * (graphLineHeigh - 4) + 2);
					conservationGPF.moveTo(x - 1, yOffset + (1 - vf) * (graphLineHeigh - 4) + 2);
					conservationGPR.moveTo(x - 1, yOffset + (1 - vr) * (graphLineHeigh - 4) + 2);

				}
				conservationGP.lineTo(x, yOffset + (1 - v) * (graphLineHeigh - 4) + 2);
				conservationGPF.lineTo(x, yOffset + (1 - vf) * (graphLineHeigh - 4) + 2);
				conservationGPR.lineTo(x, yOffset + (1 - vr) * (graphLineHeigh - 4) + 2);

			}

			/* Draw coverage lines */
			g.setColor(Color.BLUE);
			g.draw(conservationGPF);

			g.setColor(new Color(0x00, 0x99, 0x00));
			g.draw(conservationGPR);

			g.setColor(Color.BLACK);
			g.draw(conservationGP);

			g.setColor(Color.BLUE);
			g.drawString(source.toString() + " (" + scale + ")", 10, yOffset + 12 - 2);
			yOffset += graphLineHeigh;
		}

		/*
		 * Draw individual reads when possible
		 */
		Iterable<ShortRead> reads = null;
		boolean timeout = false;
		if (!isCollapsed() && (currentVisible.length() > maxRegion)) {
			g.setColor(Color.BLACK);
			g.drawString("Region too big (max " + maxRegion + " nt), zoom in", 10, yOffset + 10);
			yOffset += 20 + 5;
		} else if (!isCollapsed()) {
			/* Access to BAMread is through buffer for performance! */
			if (rg instanceof BAMreads) {
				reads = readBuffer.get(entry, currentVisible);
				timeout = readBuffer.isQFastFail();
				if (timeout) {
					String msg = "Query time-out, too much data in this area, you may want to collapse this track or zoom in";
					FontMetrics metrics = g.getFontMetrics();
					int hgt = metrics.getHeight();
					int adv = metrics.stringWidth(msg);
					g.setColor(Color.WHITE);
					g.fillRect(10, yOffset + 20 - hgt, adv + 2, hgt + 2);
					g.setColor(Color.RED);
					g.drawString(msg, 10, yOffset + 18);
					yOffset += 20 + 5;
				}
			} else {
				reads = rg.get(currentVisible);
			}
		}

		int lines = 0;
		boolean stackExceeded = false;
		if (reads != null) {
			lines = 0;
			int readLength = entry.shortReads.getReadGroup(source).readLength();
			BitSet[] tilingCounter = new BitSet[currentVisible.length()];
			for (int i = 0; i < tilingCounter.length; i++) {
				tilingCounter[i] = new BitSet();
			}
			int visibleReadCount = 0;
			try {
				for (ShortRead rf : reads) {

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

					Color c = Color.GRAY;

					if (rf.strand() == Strand.FORWARD)
						c = Color.BLUE;
					else
						c = new Color(0x00, 0x99, 0x00);

					int x2 = Convert.translateGenomeToScreen(rf.end() + 1, currentVisible, screenWidth);
					if (x2 > 0) {
						/* Find empty line */
						int line = 0;
						int pos = rf.start() - currentVisible.start();
						if (pos >= 0 && pos < tilingCounter.length)
							line = tilingCounter[rf.start() - currentVisible.start()].nextClearBit(line);
						else
							line = tilingCounter[0].nextClearBit(line);

						/*
						 * FIXME add additional checks for Extended reads that
						 * may cover other reads
						 */

						if (line < maxStack) {
							for (int i = rf.start() - 1 - readLength; i <= rf.end() + 1; i++) {
								pos = i - currentVisible.start();
								if (pos >= 0 && pos < tilingCounter.length)
									tilingCounter[pos].set(line);
							}

							if (line > lines)
								lines = line;

							int subX1 = Convert.translateGenomeToScreen(rf.start(), currentVisible, screenWidth);
							int subX2 = Convert.translateGenomeToScreen(rf.end() + 1, currentVisible, screenWidth);
							if (subX2 < subX1) {
								subX2 = subX1;
							}

							if (visibleReadCount + 100 > maxReads)
								g.setColor(Color.RED);
							else
								g.setColor(c);

							int yRec = line * readLineHeight + yOffset;
							g.fillRect(subX1, yRec, subX2 - subX1 + 1, readLineHeight - 1);
							visibleReadCount++;
							/* Check mismatches */
							if (currentVisible.length() < Configuration.getInt("geneStructureNucleotideWindow")) {
								for (int j = rf.start(); j <= rf.end(); j++) {
									char readNt = rf.getNucleotide(j - rf.start() + 1);
									char refNt = Character.toUpperCase(entry.sequence.getNucleotide(j));
									double tx1 = Convert.translateGenomeToScreen(j, currentVisible, screenWidth);
									double tx2 = Convert.translateGenomeToScreen(j + 1, currentVisible, screenWidth);

									if (readNt != refNt) {
										if (readNt == '-') {
											// System.out.println("RED");
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
						} else {
							stackExceeded = true;
						}
					}
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

	@Override
	public String displayName() {
		return "Short reads: " + source.toString();
	}

	public DataSource source() {
		return source;
	}
}
