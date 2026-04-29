/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks.comparative;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.Collections;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;

import be.abeel.util.LRUCache;
import net.sf.genomeview.core.BiMap;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.genomeview.gui.dialog.MultipleAlignmentOrderingDialog;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.TrackCommunicationModel;
import net.sf.genomeview.gui.viztracks.annotation.FeatureUtils;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.alignment.maf.AbstractAlignmentBlock;
import net.sf.jannot.alignment.maf.AbstractAlignmentSequence;
import net.sf.jannot.alignment.maf.AbstractMAFMultipleAlignment;
import net.sf.jannot.alignment.maf.SequenceTranslator;
import net.sf.jannot.utils.SequenceTools;

/**
 * Multiple-alignment track for MAF files.
 * 
 * @author Thomas Abeel
 * 
 */
public class MultipleAlignmentTrack2 extends Track {
	private static final int LINE_HEIGHT = 15;

	/*
	 * RENDER SETTINGS
	 */
	/* Contains chopped versions of the species names */
	final private ChopChopMap ordering = new ChopChopMap();
	/* Indicates whether all entries should be shown */
	final private AtomicBoolean showAll = new AtomicBoolean(true);;

	/**
	 * SOME CACHE FOR PRECOMPUTED VALUES.
	 */
	private LRUCache<String, SequenceTranslator> stCache = new LRUCache<String, SequenceTranslator>(
			20000, 5 * 60000);

	/*
	 * BELOW THIS ARE RENDERING VARIABLES. THEY ARE SET ACCORDING TO LATEST
	 * RENDER CYCLE = call to {@link #paintTrack(Graphics2D, int, double,
	 * JViewport, TrackCommunicationModel)}. THIS CLASS IS NOT THREAD SAFE AND
	 * RELIES ON paintTrack to be called only by swing thread.
	 */
	private Map<Rectangle, AbstractAlignmentBlock> paintedBlocks = new HashMap<Rectangle, AbstractAlignmentBlock>();

	private MouseEvent lastMouse;

	private Set<AbstractAlignmentSequence> translatorQueue = Collections
			.synchronizedSet(new HashSet<AbstractAlignmentSequence>());

	final private MAComparator macomp = new MAComparator(ordering);

	// last encountered yOffset. Used to correlate mouse clicks to render
	// positions. FIXME this is always equal to yOffset?
	private int currentYOffset;

	private Location lastBuffer = null;
	private MAFVizBuffer mvb = null;

	private int speciesCount = -1;

	private Graphics2D g;

	private int yOffset;

	private double screenWidth;

	private Location visible;

	private int maximumVisibleRange;

	private boolean comparativeAnnotation;

	private Type comparativeAnnotationType;

	private AbstractMAFMultipleAlignment ma;

	/**
	 * @param model the {@link Model}
	 * @param key   the {@link DataKey} for the track to render
	 */
	public MultipleAlignmentTrack2(Model model, DataKey key) {
		super(key, model, true, true);
	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		lastMouse = source;
		return false;
	}

	@Override
	public boolean mouseExited(int x, int y, MouseEvent e) {
		lastMouse = null;
		return false;
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent e) {
		/* Specific mouse code for this label */
		if (!e.isConsumed() && (Mouse.button2(e) || Mouse.button3(e))) {
			// log.debug("Multiple alignment track consumes button2||button3");
			new MultipleAlignmentPopUp(model, ordering, showAll).show(
					e.getComponent(), e.getX(), currentYOffset + e.getY());
			e.consume();
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseDragged(int x, int y, MouseEvent source) {
		lastMouse = source;
		return false;
	}

	@Override
	protected void paintDisplayName(Graphics2D g, int yOffset) {
		// Do nothing
	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth,
			JViewport view, TrackCommunicationModel tcm) {
		final Configuration conf = model.getConfiguration();
		this.g = g;
		this.yOffset = yOffset;
		this.screenWidth = screenWidth;
		comparativeAnnotation = conf.getBoolean("maf:enableAnnotation");
		comparativeAnnotationType = Type.get(conf.get("maf:annotationType"));
		maximumVisibleRange = conf.getInt("maf:maximumVisibleRange");

		currentYOffset = yOffset;
		ma = (AbstractMAFMultipleAlignment) entry.get(dataKey);
		if (ma == null) {
			g.drawString(MessageManager.getString(
					"multiplealignmenttrack.no_multiple_alignment_loaded_warn"),
					10, yOffset + 10);
			return 20 + 5;
		}
		/*
		 * Make sure there is an ordering, start with the one from the species
		 * in the MA
		 */
		if (speciesCount != ma.species().size()) {
			ordering.clear();

			try {
				int x = 0;
				for (String e : ma.species()) {
					if (!ordering.contains(e)) {
						ordering.putForward(e, x++);
					}
				}
			} catch (ConcurrentModificationException e) {
				// Something changed while we were compiling the ordering, we
				// should repaint.
				return 0;
			}
			speciesCount = ma.species().size();
		}

		paintedBlocks.clear();
		g.setColor(Color.BLACK);
		visible = model.vlm.getAnnotationLocationVisible();

		double frac = model.vlm.getAnnotationLocationVisible().length()
				/ (double) entry.getMaximumLength();

		int estCount = (int) (frac * ma.noAlignmentBlocks());
		Iterable<AbstractAlignmentBlock> abs = ma.get(visible.start,
				visible.end);

		if (!abs.iterator().hasNext()) {
			g.drawString(
					MessageManager.getString(
							"multiplealignmenttrack.no_alignment_blocks_warn"),
					10, yOffset + 10);
			return 20 + 5;
		}

		if (estCount < 250) {
			return paintBlocks(abs);
		} else {/* Many blocks on screen */

			if (lastBuffer == null || mvb == null
					|| !lastBuffer.equals(visible)) {
				mvb = new MAFVizBuffer(abs, screenWidth, visible);
				lastBuffer = visible;
			}
			return mvb.draw(g, yOffset, LINE_HEIGHT);

		}
	}

	/**
	 * paints the blocks in abs,
	 * 
	 * @param abs the iterator of blocks to paint
	 * @return height of painted area
	 */
	private int paintBlocks(Iterable<AbstractAlignmentBlock> abs) {
		int yMax = 0;
		final CollisionMap hitmap = new CollisionMap(model);
		MouseHit mh = null;

		for (final AbstractAlignmentBlock ab : abs) {
			SequenceTranslator st = getSequenceTranslator(
					ab.getAlignmentSequence(0));
			int abCount = 0;

			int start = ab.start();
			int end = ab.end();

			for (AbstractAlignmentSequence as : ab) {
				abCount++;

			}

			int blockScreenStart = Convert.translateGenomeToScreen(start,
					visible, screenWidth);
			int blockScreenEnd = Convert.translateGenomeToScreen(end, visible,
					screenWidth);
			if (showAll.get()) {
				abCount = ordering.size();
			}

			Rectangle rec = new Rectangle(start, yOffset, end - start - 1,
					abCount * LINE_HEIGHT);
			while (hitmap.collision(rec)) {
				rec.y += LINE_HEIGHT;
			}
			if (rec.y + rec.height > yMax) {
				yMax = rec.y + rec.height;
			}
			hitmap.addLocation(rec, null);
			paintedBlocks.put(new Rectangle(blockScreenStart, rec.y - yOffset,
					blockScreenEnd - blockScreenStart, rec.height), ab);
			g.setColor(Color.BLACK);
			g.drawRect(blockScreenStart, rec.y,
					blockScreenEnd - blockScreenStart, rec.height);

			/*
			 * Reorder the alignment sequences to whatever the user wants
			 */
			TreeSet<AbstractAlignmentSequence> sortedblocks = new TreeSet<AbstractAlignmentSequence>(
					macomp);
			for (AbstractAlignmentSequence as : ab) {
				assert as != null;
				sortedblocks.add(as);
			}

			BitSet lines = new BitSet(ordering.size());

			/* Very detailed view */
			char[] ref = null;
			if (visible.length() < 1000) {
				Iterable<Character> bufferedSeq = entry.sequence()
						.get(visible.start, visible.end + 1);

				ref = new char[visible.length()];

				int idx = 0;
				for (char c : bufferedSeq) {
					ref[idx++] = c;
				}
			}

			int line = 1;
			Font font = g.getFont();
			Font tmpFont = font.deriveFont(10f);
			g.setFont(tmpFont);

			for (AbstractAlignmentSequence as : sortedblocks) {
				if (showAll.get()) {
					line = ordering.getForward(as.getName()) + 1;
					// System.out.println("ASLINES: "+as+"\t"+line);
					lines.set(line - 1);
				}

				line = paintAS(st, start, end, blockScreenStart, blockScreenEnd,
						rec, lines, ref, line, as);

				if (comparativeAnnotation && st == null) {
					Color or = Color.orange;
					g.setColor(new Color(or.getRed(), or.getGreen(),
							or.getBlue(), 100));
					g.fillRect(blockScreenStart,
							rec.y + (line - 1) * LINE_HEIGHT,
							blockScreenEnd - blockScreenStart, LINE_HEIGHT);
				} else if (comparativeAnnotation && st != null
						&& visible.length() >= maximumVisibleRange) {
					g.setColor(Color.BLACK);
					g.drawString("Zoom in to see comparative annotation", 15,
							yOffset + 10);
				} else if (comparativeAnnotation && st != null
						&& visible.length() < maximumVisibleRange) {
					SequenceTranslator localTranslator = getSequenceTranslator(
							as);

					if (localTranslator != null) {
						Entry e = model.entries().getEntry(as.getName());
						if (e != null) {
							int[] revtable = st.getReverseTranslationTable();
							MemoryFeatureAnnotation mfa = e.getMemoryAnnotation(
									comparativeAnnotationType);
							for (Feature f : mfa.get(as.start(), as.end())) {

								paintFeature(ab, blockScreenStart,
										blockScreenEnd, rec, line, as,
										localTranslator, revtable, f);
							}
						}
					}
				}

				line++;
			}

			g.setFont(font);

			/* Fill in the blanks when showing all */
			if (showAll.get()) {
				for (int i = 0; i < ordering.size(); i++) {
					if (!lines.get(i)) {
						g.setColor(new Color(255, 255, 0, 100));
						g.fillRect(blockScreenStart, rec.y + i * LINE_HEIGHT,
								blockScreenEnd - blockScreenStart, LINE_HEIGHT);
					}
				}
			}

			// check if mouse clicked on this ab
			if (lastMouse != null) {
				int xMouse = Convert.translateScreenToGenome(lastMouse.getX(),
						visible, screenWidth);
				// System.out.println(rec + "\t" + xMouse + "\t" +
				// rec.contains(xMouse, lastMouse.getY() + yOffset));
				if (rec.contains(xMouse, lastMouse.getY() + yOffset)) {
					mh = new MouseHit(ab, rec, blockScreenStart);
				}
			}

		}
		paintMouseOverInfo(mh);
		return yMax - yOffset;
	}

	private void paintFeature(final AbstractAlignmentBlock ab,
			int blockScreenStart, int blockScreenEnd, Rectangle rec, int line,
			AbstractAlignmentSequence as, SequenceTranslator localTranslator,
			int[] revtable, Feature f) {
		Location[] larr = f.location();
		for (int i = 0; i < 2 * larr.length - 1; i++) {
			// for (Location l : f.location()) {

			int featureStart, featureEnd;

			if (i % 2 == 0) {/* feature */
				featureStart = larr[i / 2].start();
				featureEnd = larr[i / 2].end();
			} else {/* connection */
				featureStart = larr[i / 2].end();
				featureEnd = larr[i / 2 + 1].start();
			}
			/*
			 * Reverse coordinates in reversed sections
			 */
			if (as.strand() == Strand.REVERSE) {
				featureStart = as.end() - f.end() + as.start() - 1;
				featureEnd = as.end() - f.start() + as.start() - 1;

			}

			if (featureStart < as.start()) {
				featureStart = as.start();
			}
			if (featureStart > as.end()) {
				featureStart = as.end();
			}

			if (featureEnd > as.end()) {
				featureEnd = as.end();
			}

			if (featureEnd < as.start()) {
				featureEnd = as.start();
			}

			featureStart = localTranslator.translate(featureStart - as.start());
			featureEnd = localTranslator.translate(featureEnd - as.start());

			/*
			 * Translate back to reference genome space
			 */
			featureStart = revtable[featureStart];
			featureEnd = revtable[featureEnd] + 1;

			int featureScreenStart = Convert.translateGenomeToScreen(
					featureStart + ab.start(), visible, screenWidth);
			int featureScreenEnd = Convert.translateGenomeToScreen(
					featureEnd + ab.start(), visible, screenWidth);

			if (featureScreenStart < blockScreenStart) {
				featureScreenStart = blockScreenStart;

			}

			if (featureScreenEnd > blockScreenEnd) {
				featureScreenEnd = blockScreenEnd;
			}

			if (featureScreenEnd > featureScreenStart && featureScreenEnd >= 0
					&& featureScreenStart <= screenWidth) {
				Color c = Color.CYAN;
				g.setColor(
						new Color(c.getRed(), c.getGreen(), c.getBlue(), 150));
				if (i % 2 == 0) {
					g.fillRect(featureScreenStart,
							rec.y + (line - 1) * LINE_HEIGHT + 3,
							featureScreenEnd - featureScreenStart,
							LINE_HEIGHT - 6);
					if (visible.length() < 10000) {
						g.setColor(Color.CYAN.darker().darker());
						g.drawString(
								FeatureUtils.displayName(f,
										model.getConfiguration()),
								featureScreenStart,
								rec.y + (line) * LINE_HEIGHT - 4);
					}
				} else {
					g.drawLine(featureScreenStart,
							rec.y + (line - 1) * LINE_HEIGHT + LINE_HEIGHT / 2,
							featureScreenEnd,
							rec.y + (line - 1) * LINE_HEIGHT + LINE_HEIGHT / 2);
				}

			}
		}
	}

	private int paintAS(SequenceTranslator st, int start, int end,
			int blockScreenStart, int blockScreenEnd, Rectangle rec,
			BitSet lines, char[] ref, int line, AbstractAlignmentSequence as) {
		Configuration configu = model.getConfiguration();
		if (visible.length() < 1000) {

			if (st != null) {

				for (int i = visible.start; i <= visible.end; i++) {
					if (i >= start && i < end) {
						double width = screenWidth / visible.length();
						int translated = st.translate(i - start) + 1;

						char nt;

						if (as.strand() == Strand.FORWARD) {
							nt = as.seq().get(translated, translated + 1)
									.iterator().next();
						} else {
							nt = SequenceTools.complement(as.seq()
									.get(as.seq().size() - translated + 1,
											as.seq().size() - translated + 2)
									.iterator().next());
						}

						// System.out.println("NT:
						// "+translated+"\t"+nt);
						if (ref[i - visible.start] != nt) {
							if (nt == '-') {
								g.setColor(Color.RED);
							} else {
								g.setColor(Color.DARK_GRAY);
							}
							g.fillRect((int) ((i - visible.start) * width),
									rec.y + (line - 1) * LINE_HEIGHT,
									(int) Math.ceil(width), LINE_HEIGHT);
							if (visible.length() < 100) {
								Rectangle2D stringSize = g.getFontMetrics()
										.getStringBounds("" + nt, g);
								if (nt == '-') {
									g.setColor(Color.BLACK);
								} else {
									g.setColor(configu.getNucleotideColor(nt)
											.brighter());
								}
								g.drawString("" + nt,
										(int) (((i - visible.start) * width
												- stringSize.getWidth() / 2)
												+ (width / 2)),
										rec.y + line * LINE_HEIGHT - 2);
							}
						}
					}
				}
			} else {
				Color or = Color.orange;
				g.setColor(new Color(or.getRed(), or.getGreen(), or.getBlue(),
						100));
				g.fillRect(blockScreenStart, rec.y + (line - 1) * LINE_HEIGHT,
						blockScreenEnd - blockScreenStart, LINE_HEIGHT);
			}
		} else {
			// FIXME redundant?
			if (showAll.get()) {
				line = ordering.getForward(as.getName()) + 1;
				lines.set(line - 1);
			}
			if (as.strand() == Strand.FORWARD) {
				Color or = configu.getColor("ma:forwardColor");
				g.setColor(new Color(or.getRed(), or.getGreen(), or.getBlue(),
						100));

			} else {
				Color or = configu.getColor("ma:reverseColor");
				g.setColor(new Color(or.getRed(), or.getGreen(), or.getBlue(),
						100));
			}

			g.fillRect(blockScreenStart, rec.y + (line - 1) * LINE_HEIGHT,
					blockScreenEnd - blockScreenStart, LINE_HEIGHT);

		}
		return line;
	}

	/**
	 * Add info about where the mouse is over
	 * 
	 * @param mh
	 */
	private void paintMouseOverInfo(MouseHit mh) {
		/* Mouse is over a block and there is some information to display */
		if (mh != null) {
			boolean fullNames = model.getConfiguration()
					.getBoolean("maf:extendedNames");
			HashMap<String, AbstractAlignmentSequence> shown = new HashMap<String, AbstractAlignmentSequence>();
			if (showAll.get()) {
				for (String e : ma.species()) {
					shown.put(e, null);

				}
			}

			for (AbstractAlignmentSequence as : mh.ab) {
				shown.put(as.getName(), as);
//					System.out.println("AS: "+as.getName());

			}

			String[] arr = new String[ma.species().size()];
			Rectangle2D[] size = new Rectangle2D[ma.species().size()];
			int maxWidth = 0;
			for (String e : shown.keySet()) {
				// String s = e.getID();
				arr[ordering.getForward(e)] = e;
				AbstractAlignmentSequence as = shown.get(e);
				if (as != null) {
					arr[ordering.getForward(e)] = shown.get(e).toString();
				}

				arr[ordering.getForward(e)] = fullNames
						? arr[ordering.getForward(e)]
						: StaticUtils.chopchop(arr[ordering.getForward(e)]);

				Rectangle2D stringSize = g.getFontMetrics()
						.getStringBounds(arr[ordering.getForward(e)], g);
				size[ordering.getForward(e)] = stringSize;
				if (stringSize.getWidth() > maxWidth) {
					maxWidth = (int) stringSize.getWidth();
				}
			}

			g.setColor(new Color(192, 192, 192, 175));
			g.fillRect(Math.max(mh.x1 - maxWidth, 5), mh.rec.y, maxWidth,
					ordering.size() * LINE_HEIGHT);
			g.setColor(Color.DARK_GRAY);
			g.drawRect(Math.max(mh.x1 - maxWidth, 5), mh.rec.y, maxWidth,
					ordering.size() * LINE_HEIGHT);
			g.setColor(Color.black);
			int index = 0;
			for (int i = 0; i < arr.length; i++) {
				if (arr[i] != null) {
					g.drawString(arr[i],
							(int) Math.max(mh.x1 - size[i].getWidth(), 5),
							mh.rec.y + (index + 1) * LINE_HEIGHT);
					index++;
				}

			}
		}
	}

	private synchronized SequenceTranslator getSequenceTranslator(
			final AbstractAlignmentSequence ab) {
		final String key = ab.getName() + ":" + ab.start() + "-" + ab.end();
		SequenceTranslator st = stCache.get(key);
		if (st == null) {
//			System.out.println(" Mafix load/cache: " + GenomeViewScheduler.queueLength() + "\t" + stCache.size());
//			System.out.println("\t"+translatorQueue);

			if (!translatorQueue.contains(ab)) {
				translatorQueue.add(ab);

				GenomeViewScheduler
						.submit(new Task(new Location(ab.start(), ab.end())) {
							private boolean cancelled = false;

							@Override
							public void cancel() {
								translatorQueue.remove(ab);
								cancelled = true;
							}

							@Override
							public boolean isCancelled() {
								return cancelled;
							}

							@Override
							public void run() {
								translatorQueue.remove(ab);
								if (cancelled) {
									return;
								}

								// System.out.println("Calculating new
								// translator " +
								// stCache.size());
								SequenceTranslator tmp = new SequenceTranslator(
										ab);
								stCache.put(key, tmp);

								model.refresh();

							}
						});
			}
			return null;

		}
		return st;
	}

}

/**
 * {@link BiMap} but uses only the names up to the first ".".
 */
class ChopChopMap extends BiMap<String, Integer> {
	@Override
	public Integer getForward(String key) {
		return super.getForward(StaticUtils.chopchop(key));
	}

	@Override
	public void putForward(String e, Integer i) {
		super.putForward(StaticUtils.chopchop(e), i);
	}

	@Override
	public void putReverse(Integer i, String e) {
		super.putReverse(i, StaticUtils.chopchop(e));
	}

	public boolean contains(String e) {
		return super.containsForward(StaticUtils.chopchop(e));
	}

}

/**
 * records info about last known mouse position
 */
class MouseHit {

	final AbstractAlignmentBlock ab;
	final Rectangle rec;
	final int x1;

	public MouseHit(AbstractAlignmentBlock ab, Rectangle rec, int x1) {
		this.ab = ab;
		this.rec = rec;
		this.x1 = x1;
	}

}

class MAComparator implements Comparator<AbstractAlignmentSequence> {
	private BiMap<String, Integer> ordering;

	/**
	 * @param ordering the ordering to use. The referred ordering is used, no
	 *                 copy is made, so changes in the original map will be
	 *                 tracked.
	 */
	public MAComparator(BiMap<String, Integer> ordering) {
		this.ordering = ordering;
	}

	@Override
	public int compare(AbstractAlignmentSequence o1,
			AbstractAlignmentSequence o2) {
		return ordering.getForward(o1.getName())
				.compareTo(ordering.getForward(o2.getName()));
	}
}

/**
 * Popup menu when button2 or button3 clicked in track area.
 */
@SuppressWarnings("serial")
class MultipleAlignmentPopUp extends JPopupMenu {

	private final Model model;
	private final BiMap<String, Integer> ordering;
	private final AtomicBoolean showAll;

	/**
	 * 
	 * @param model          the {@link Model}, used to force refresh after
	 *                       changes.
	 * @param ordering       {@link BiMap}, can be changed by this popup
	 * @param showAll,{@link AtomicBoolean}, can be changed by this popup
	 */
	public MultipleAlignmentPopUp(Model model, BiMap<String, Integer> ordering,
			AtomicBoolean showAll) {
		this.model = model;
		this.ordering = ordering;
		this.showAll = showAll;

		add(new AbstractAction(MessageManager
				.getString("multiplealignmenttrack.toggle_all_entries")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				showAll.set(!showAll.get());
				model.refresh();
			}

		});
		add(new AbstractAction(MessageManager
				.getString("multiplealignmenttrack.rearrange_ordering")) {

			@Override
			public void actionPerformed(ActionEvent e) {
				MultipleAlignmentOrderingDialog mad = new MultipleAlignmentOrderingDialog(
						model, ordering);
				mad.pack();
				mad.setVisible(true);
				model.refresh();
			}

		});
	}
}
