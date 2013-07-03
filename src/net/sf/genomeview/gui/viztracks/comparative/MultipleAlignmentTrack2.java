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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JViewport;

import net.sf.genomeview.core.BiMap;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.GenomeViewScheduler;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Task;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.Mouse;
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
import be.abeel.util.LRUCache;

/**
 * Multiple-alignment track for MAF files.
 * 
 * @author Thomas Abeel
 * 
 */
public class MultipleAlignmentTrack2 extends Track {

	private class MultipleAlignmentPopUp extends JPopupMenu {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1103364926466070222L;

		public MultipleAlignmentPopUp() {
			add(new AbstractAction(MessageManager.getString("multiplealignmenttrack.toggle_all_entries")) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 3910281037023553159L;

				@Override
				public void actionPerformed(ActionEvent e) {
					showAll = !showAll;
					model.refresh();
				}

			});
			add(new AbstractAction(MessageManager.getString("multiplealignmenttrack.rearrange_ordering")) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 8906163594262830307L;

				@Override
				public void actionPerformed(ActionEvent e) {
					MultipleAlignmentOrderingDialog mad = new MultipleAlignmentOrderingDialog(model, ordering);
					mad.pack();
					mad.setVisible(true);
					model.refresh();
				}

			});
		}
	}

	private MouseEvent lastMouse;

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		lastMouse = source;
		return false;
	}

	private static final Logger log = LoggerFactory.getLogger(MultipleAlignmentTrack2.class.getCanonicalName());

	public boolean mouseExited(int x, int y, MouseEvent e) {
		lastMouse = null;
		return false;
	}

	private static String chopchop(String as) {
		if (as.indexOf('.') >= 0)
			return as.substring(0, as.indexOf('.'));
		else
			return as;
	}

	static private class ChopChopMap extends BiMap<String, Integer> {
		@Override
		public Integer getForward(String key) {
			return super.getForward(chopchop(key));
		}

		@Override
		public void putForward(String e, Integer i) {
			super.putForward(chopchop(e), i);
		}

		@Override
		public void putReverse(Integer i, String e) {
			super.putReverse(i, chopchop(e));
		}

		public boolean contains(String e) {
			return super.containsForward(chopchop(e));
		}

	}

	public boolean mouseClicked(int x, int y, MouseEvent e) {
		/* Specific mouse code for this label */
		if (!e.isConsumed() && (Mouse.button2(e) || Mouse.button3(e))) {
			log.debug("Multiple alignment track consumes button2||button3");
			new MultipleAlignmentPopUp().show(e.getComponent(), e.getX(), currentYOffset + e.getY());
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

	public MultipleAlignmentTrack2(Model model, DataKey key) {
		super(key, model, true, true);
	}

	private int lineHeight = 15;

	private Map<Rectangle, AbstractAlignmentBlock> paintedBlocks = new HashMap<Rectangle, AbstractAlignmentBlock>();

	class MouseHit {
		AbstractAlignmentBlock ab;
		Rectangle rec;
		public int x1;
	}

	/* Contains chopped versions of the species names */
	final private ChopChopMap ordering = new ChopChopMap();

	class MAComparator implements Comparator<AbstractAlignmentSequence> {
		private BiMap<String, Integer> ordering;

		public MAComparator(BiMap<String, Integer> ordering) {
			this.ordering = ordering;
		}

		@Override
		public int compare(AbstractAlignmentSequence o1, AbstractAlignmentSequence o2) {
			return ordering.getForward(o1.getName()).compareTo(ordering.getForward(o2.getName()));
		}
	}

	final private MAComparator macomp = new MAComparator(ordering);
	/* Indicates whether all entries should be shown */
	private boolean showAll;

	private int currentYOffset;

	private Location lastBuffer = null;
	private MAFVizBuffer mvb = null;

	private int speciesCount = -1;

	@Override
	protected void paintDisplayName(Graphics2D g, int yOffset) {
		// Do nothing
	}

	@Override
	public int paintTrack(Graphics2D g, int yOffset, double screenWidth, JViewport view, TrackCommunicationModel tcm) {
		boolean comparativeAnnotation = Configuration.getBoolean("maf:enableAnnotation");
		Type comparativeAnnotationType = Type.get(Configuration.get("maf:annotationType"));

		currentYOffset = yOffset;
		AbstractMAFMultipleAlignment ma = (AbstractMAFMultipleAlignment) entry.get(dataKey);
		if (ma == null) {
			g.drawString(MessageManager.getString("multiplealignmenttrack.no_multiple_alignment_loaded_warn"), 10, yOffset + 10);
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
					if (!ordering.contains(e))
						ordering.putForward(e, x++);
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
		Location visible = model.vlm.getAnnotationLocationVisible();

		double frac = model.vlm.getAnnotationLocationVisible().length() / (double) entry.getMaximumLength();

		int estCount = (int) (frac * ma.noAlignmentBlocks());
		Iterable<AbstractAlignmentBlock> abs = ma.get(visible.start, visible.end);

		if (!abs.iterator().hasNext()) {
			g.drawString(MessageManager.getString("multiplealignmenttrack.no_alignment_blocks_warn"), 10, yOffset + 10);
			return 20 + 5;
		}

		if (estCount < 250) {
			int yMax = 0;
			CollisionMap hitmap = new CollisionMap(model);
			MouseHit mh = null;
			for (AbstractAlignmentBlock ab : abs) {
				SequenceTranslator st = getSequenceTranslator(ab.getAlignmentSequence(0));
				int abCount = 0;

				int start = ab.start();
				int end = ab.end();

				for (AbstractAlignmentSequence as : ab) {
					abCount++;

				}

				int blockScreenStart = Convert.translateGenomeToScreen(start, visible, screenWidth);
				int blockScreenEnd = Convert.translateGenomeToScreen(end, visible, screenWidth);
				if (showAll)
					abCount = ordering.size();

				Rectangle rec = new Rectangle(start, yOffset, end - start - 1, abCount * lineHeight);
				while (hitmap.collision(rec)) {
					rec.y += lineHeight;
				}
				if (rec.y + rec.height > yMax)
					yMax = rec.y + rec.height;
				hitmap.addLocation(rec, null);
				paintedBlocks.put(new Rectangle(blockScreenStart, rec.y - yOffset, blockScreenEnd - blockScreenStart, rec.height), ab);
				// rec.x=x1;
				// rec.width=x2-x1;
				g.setColor(Color.BLACK);
				g.drawRect(blockScreenStart, rec.y, blockScreenEnd - blockScreenStart, rec.height);

				/*
				 * Reorder the alignment sequences to whatever the user wants
				 */
				TreeSet<AbstractAlignmentSequence> ab2 = new TreeSet<AbstractAlignmentSequence>(macomp);
				for (AbstractAlignmentSequence as : ab) {
					assert as != null;
					ab2.add(as);
				}
				// System.out.println(ab2);

				BitSet lines = new BitSet(ordering.size());

				/* Very detailed view */
				char[] ref = null;
				if (visible.length() < 1000) {
					// char[] ref =
					// entry.sequence().getSubSequence(visible.start,
					// visible.end + 1).toCharArray();
					Iterable<Character> bufferedSeq = entry.sequence().get(visible.start, visible.end + 1);

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
				for (AbstractAlignmentSequence as : ab2) {

					if (showAll) {
						line = ordering.getForward(as.getName()) + 1;
						// System.out.println("ASLINES: "+as+"\t"+line);
						lines.set(line - 1);
					}

					if (visible.length() < 1000) {

						if (st != null) {

							for (int i = visible.start; i <= visible.end; i++) {
								if (i >= start && i < end) {
									double width = screenWidth / (double) visible.length();
									int translated = st.translate(i - start) + 1;

									char nt;

									if (as.strand() == Strand.FORWARD)
										nt = as.seq().get(translated, translated + 1).iterator().next();
									else
										nt = SequenceTools.complement(as.seq()
												.get(as.seq().size() - translated + 1, as.seq().size() - translated + 2).iterator().next());

									// System.out.println("NT: "+translated+"\t"+nt);
									if (ref[i - visible.start] != nt) {
										if (nt == '-')
											g.setColor(Color.RED);
										else
											g.setColor(Color.DARK_GRAY);
										g.fillRect((int) ((i - visible.start) * width), rec.y + (line - 1) * lineHeight,
												(int) Math.ceil(width), lineHeight);
										if (visible.length() < 100) {
											Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + nt, g);
											if (nt == '-')
												g.setColor(Color.BLACK);
											else
												g.setColor(Configuration.getNucleotideColor(nt).brighter());
											g.drawString("" + nt,
													(int) (((i - visible.start) * width - stringSize.getWidth() / 2) + (width / 2)), rec.y
															+ line * lineHeight - 2);
										}
									}
								}
							}
						} else {
							Color or = Color.orange;
							g.setColor(new Color(or.getRed(), or.getGreen(), or.getBlue(), 100));
							g.fillRect(blockScreenStart, rec.y + (line - 1) * lineHeight, blockScreenEnd - blockScreenStart, lineHeight);
						}
					} else {
						// FIXME redundant?
						if (showAll) {
							line = ordering.getForward(as.getName()) + 1;
							lines.set(line - 1);
						}
						if (as.strand() == Strand.FORWARD) {
							g.setColor(Configuration.getColor("ma:forwardColor"));
						} else {
							g.setColor(Configuration.getColor("ma:reverseColor"));
						}

						g.fillRect(blockScreenStart, rec.y + (line - 1) * lineHeight, blockScreenEnd - blockScreenStart, lineHeight);

					}

					if (comparativeAnnotation && st != null) {
						SequenceTranslator localTranslator = getSequenceTranslator(as);

						if (localTranslator != null) {
							Entry e = model.entries().getEntry(as.getName());
							if (e != null) {
								int[] revtable = st.getReverseTranslationTable();
								MemoryFeatureAnnotation mfa = e.getMemoryAnnotation(comparativeAnnotationType);
								for (Feature f : mfa.get(as.start(), as.end())) {

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
										 * Reverse coordinates in reversed
										 * sections
										 */
										if (as.strand() == Strand.REVERSE) {
											featureStart = as.end() - f.end() + as.start() - 1;
											featureEnd = as.end() - f.start() + as.start() - 1;

										}

										if (featureStart < as.start())
											featureStart = as.start();
										if (featureStart > as.end())
											featureStart = as.end();

										if (featureEnd > as.end())
											featureEnd = as.end();

										if (featureEnd < as.start()) {
											featureEnd = as.start();
										}

										featureStart = localTranslator.translate(featureStart - as.start());
										featureEnd = localTranslator.translate(featureEnd - as.start());

										/*
										 * Translate back to reference genome
										 * space
										 */
										featureStart = revtable[featureStart];
										featureEnd = revtable[featureEnd] + 1;

										int featureScreenStart = Convert.translateGenomeToScreen(featureStart + ab.start(), visible,
												screenWidth);
										int featureScreenEnd = Convert.translateGenomeToScreen(featureEnd + ab.start(), visible,
												screenWidth);

										if (featureScreenStart < blockScreenStart) {
											featureScreenStart = blockScreenStart;

										}

										if (featureScreenEnd > blockScreenEnd)
											featureScreenEnd = blockScreenEnd;

										if (featureScreenEnd > featureScreenStart && featureScreenEnd >= 0
												&& featureScreenStart <= screenWidth) {
											Color c = Color.CYAN;
											g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 80));
											if (i % 2 == 0) {
												g.fillRect(featureScreenStart, rec.y + (line - 1) * lineHeight + 3, featureScreenEnd
														- featureScreenStart, lineHeight - 6);
												if (visible.length() < 10000) {
													g.setColor(Color.CYAN.darker().darker());
													g.drawString(FeatureUtils.displayName(f), (int) featureScreenStart, rec.y + (line) * lineHeight - 4);
												}
											} else {
												g.drawLine(featureScreenStart, rec.y + (line - 1) * lineHeight + lineHeight/2, featureScreenEnd, rec.y
														+ (line - 1) * lineHeight + lineHeight/2);
											}

										}
									}

								}

							}
						} else {
							Color or = Color.orange;
							g.setColor(new Color(or.getRed(), or.getGreen(), or.getBlue(), 100));
							g.fillRect(blockScreenStart, rec.y + (line - 1) * lineHeight, blockScreenEnd - blockScreenStart, lineHeight);
						}
					}

					line++;
				}

				g.setFont(font);

				/* Fill in the blanks when showing all */
				if (showAll) {
					for (int i = 0; i < ordering.size(); i++) {
						if (!lines.get(i)) {
							g.setColor(new Color(255, 255, 0, 100));
							g.fillRect(blockScreenStart, rec.y + i * lineHeight, blockScreenEnd - blockScreenStart, lineHeight);
						}
					}
				}
				if (lastMouse != null) {

					int xMouse = Convert.translateScreenToGenome(lastMouse.getX(), visible, screenWidth);
					// System.out.println(rec + "\t" + xMouse + "\t" +
					// rec.contains(xMouse, lastMouse.getY() + yOffset));
					if (rec.contains(xMouse, lastMouse.getY() + yOffset)) {
						mh = new MouseHit();
						mh.ab = ab;
						mh.rec = rec;
						mh.x1 = blockScreenStart;

					}
				}

			}
			/* Mouse is over a block and there is some information to display */
			if (mh != null) {

				HashMap<String, AbstractAlignmentSequence> shown = new HashMap<String, AbstractAlignmentSequence>();
				if (showAll) {
					for (String e : ma.species()) {
						shown.put(e, null);

					}
				}

				for (AbstractAlignmentSequence as : mh.ab) {
					shown.put(as.getName(), as);

				}

				String[] arr = new String[ma.species().size()];
				Rectangle2D[] size = new Rectangle2D[ma.species().size()];
				int maxWidth = 0;
				for (String e : shown.keySet()) {
					// String s = e.getID();
					arr[ordering.getForward(e)] = e;
					AbstractAlignmentSequence as = shown.get(e);
					if (as != null)
						arr[ordering.getForward(e)] = shown.get(e).toString();

					arr[ordering.getForward(e)] = chopchop(arr[ordering.getForward(e)]);

					Rectangle2D stringSize = g.getFontMetrics().getStringBounds(arr[ordering.getForward(e)], g);
					size[ordering.getForward(e)] = stringSize;
					if (stringSize.getWidth() > maxWidth)
						maxWidth = (int) stringSize.getWidth();
				}

				g.setColor(new Color(192, 192, 192, 175));
				g.fillRect((int) Math.max(mh.x1 - maxWidth, 5), mh.rec.y, maxWidth, ordering.size() * lineHeight);
				g.setColor(Color.DARK_GRAY);
				g.drawRect((int) Math.max(mh.x1 - maxWidth, 5), mh.rec.y, maxWidth, ordering.size() * lineHeight);
				g.setColor(Color.black);
				int index = 0;
				for (int i = 0; i < arr.length; i++) {
					if (arr[i] != null) {
						g.drawString(arr[i], (int) Math.max(mh.x1 - size[i].getWidth(), 5), mh.rec.y + (index + 1) * lineHeight);
						index++;
					}

				}
			}
			return yMax - yOffset;
		} else {/* More than 500 blocks on screen */

			if (lastBuffer == null || mvb == null || !lastBuffer.equals(visible)) {
				mvb = new MAFVizBuffer(abs, screenWidth, visible);
				lastBuffer = visible;
			}
			return mvb.draw(g, yOffset, lineHeight);

		}
	}

	private LRUCache<String, SequenceTranslator> stCache = new LRUCache<String, SequenceTranslator>(20000, 5 * 60000);

	private Set<AbstractAlignmentSequence> translatorQueue = Collections.synchronizedSet(new HashSet<AbstractAlignmentSequence>());

	private SequenceTranslator getSequenceTranslator(final AbstractAlignmentSequence ab) {
		final String key = ab.getName() + ":" + ab.start() + "-" + ab.end();
		SequenceTranslator st = stCache.get(key);
		if (st == null) {
			GenomeViewScheduler.submit(new Task(new Location(ab.start(), ab.end())) {
				private boolean cancelled = false;

				public void cancel() {
					cancelled = true;
				}

				public boolean isCancelled() {
					return cancelled;
				}

				@Override
				public void run() {
					if (cancelled)
						return;
					if (!translatorQueue.contains(ab)) {
						translatorQueue.add(ab);
						// System.out.println("Calculating new translator " +
						// stCache.size());
						SequenceTranslator tmp = new SequenceTranslator(ab);
						stCache.put(key, tmp);
						translatorQueue.remove(ab);
						model.refresh();
					}

				}
			});
			return null;

		}
		return st;
	}

	// @Override
	// public String displayName() {
	// return "Multiple alignment";
	// }

}
