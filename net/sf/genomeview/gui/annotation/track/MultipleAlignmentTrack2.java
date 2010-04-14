/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.genomeview.gui.dialog.MultipleAlignmentOrderingDialog;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.alignment.AlignmentBlock;
import net.sf.jannot.alignment.AlignmentSequence;
import net.sf.jannot.alignment.MultipleAlignment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class MultipleAlignmentTrack2 extends Track {

	private class MultipleAlignmentPopUp extends JPopupMenu {
		public MultipleAlignmentPopUp() {
			add(new AbstractAction("Toggle all entries mode") {

				@Override
				public void actionPerformed(ActionEvent e) {
					showAll = !showAll;
					model.refresh();
				}

			});
			add(new AbstractAction("Rearrange ordering") {

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

//	private MultipleAlignment ma;

	private MouseEvent lastMouse;

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		lastMouse = source;
		return false;
	}

	private static final Logger log = Logger.getLogger(MultipleAlignmentTrack2.class.getCanonicalName());

	public boolean mouseExited(int x, int y, MouseEvent e) {
		lastMouse = null;
		return false;
	}

	public boolean mouseClicked(int x, int y, MouseEvent e) {
		/* Specific mouse code for this label */
		if (!e.isConsumed() && (Mouse.button2(e) || Mouse.button3(e))) {
			log.finest("Multiple alignment track consumes button2||button3");
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
//
//	public MultipleAlignment getMA() {
//		return ma;
//	}

//	public MultipleAlignmentTrack2(Model model, MultipleAlignment ma) {
//		super(model, true, true);
//		// model.addObserver(tooltip);
//		this.ma = ma;
//	}

	public MultipleAlignmentTrack2(Model model, DataKey key) {
		super(key,model, true, true);
		// model.addObserver(tooltip);
		
	}

	private int lineHeight = 15;

	private Map<Rectangle, AlignmentBlock> paintedBlocks = new HashMap<Rectangle, AlignmentBlock>();

	private int queriedBlocks = 0;

	class MouseHit {
		AlignmentBlock ab;
		Rectangle rec;
		public int x1;
	}

	final private BiMap<Entry, Integer> ordering = HashBiMap.create();

	class MAComparator implements Comparator<AlignmentSequence> {
		private BiMap<Entry, Integer> ordering;

		public MAComparator(BiMap<Entry, Integer> ordering) {
			this.ordering = ordering;
		}

		@Override
		public int compare(AlignmentSequence o1, AlignmentSequence o2) {
			return ordering.get(o1.entry()).compareTo(ordering.get(o2.entry()));
		}
	}

	final private MAComparator macomp = new MAComparator(ordering);
	/* Indicates whether all entries should be shown */
	private boolean showAll;

	private int currentYOffset;

	@Override
	public int paintTrack(Graphics2D g, final Entry entry, int yOffset, double screenWidth) {
		// this.yOffset = yOffset;
		currentYOffset = yOffset;
		MultipleAlignment ma=(MultipleAlignment)entry.data.get(dataKey);
		if (ordering.size() != model.entries().size()) {
			updateOrdering();

		}

		paintedBlocks.clear();
		g.setColor(Color.BLACK);
		Location visible = model.getAnnotationLocationVisible();

		if (ma.getEstimateCount(visible) > 10000) {
			g.drawString("Too many alignment blocks, zoom in to see multiple alignments", 10, yOffset + 10);
			return 20 + 5;
		}
//		TreeSet<AlignmentBlock> abs = ma.get(entry, visible);
		Iterable<AlignmentBlock>abs=ma.get(visible.start,visible.end);

		queriedBlocks =ma.getEstimateCount(visible);
		
		if (queriedBlocks==0) {
			g.drawString("No alignment blocks in this region", 10, yOffset + 10);
			return 20 + 5;
		}
		
		if (queriedBlocks < 500) {
			int yMax = 0;
			CollisionMap hitmap = new CollisionMap(model);
			MouseHit mh = null;
			for (AlignmentBlock ab : abs) {
				int abCount = 0;

				int start = -1;
				int end = -1;
				for (AlignmentSequence as : ab) {
					abCount++;
					// System.out.println(as);
					// int index = model.entries().indexOf(as.entry());
					if (as.entry() == entry) {
						start = as.start();
						end = as.end();

					}
				}

				int x1 = Convert.translateGenomeToScreen(start, visible, screenWidth);
				int x2 = Convert.translateGenomeToScreen(end, visible, screenWidth);
				if (showAll)
					abCount = ordering.size();

				Rectangle rec = new Rectangle(start, yOffset, end - start - 1, abCount * lineHeight);
				while (hitmap.collision(rec)) {
					rec.y += lineHeight;
				}
				if (rec.y + rec.height > yMax)
					yMax = rec.y + rec.height;
				hitmap.addLocation(rec, null);
				paintedBlocks.put(new Rectangle(x1, rec.y - yOffset, x2 - x1, rec.height), ab);
				// rec.x=x1;
				// rec.width=x2-x1;
				g.setColor(Color.BLACK);
				g.drawRect(x1, rec.y, x2 - x1, rec.height);

				/*
				 * Reorder the alignment sequences to whatever the user wants
				 */
				TreeSet<AlignmentSequence> ab2 = new TreeSet<AlignmentSequence>(macomp);
				for (AlignmentSequence as : ab) {
					assert as != null;
					ab2.add(as);
				}
				BitSet lines = new BitSet(ordering.size());
				if (visible.length() < 1000) {
					char[] ref = entry.sequence().getSubSequence(visible.start, visible.end + 1).toCharArray();
					// System.out.println("--block ");
					int line = 1;
					for (AlignmentSequence as : ab2) {
						// System.out.println("\t" + start + "\t" + end + "\t" +
						// as.strand());
						// g.drawString(as.seq().toString(), x1,
						// rec.y+line*lineHeight);
						if (showAll) {
							line = ordering.get(as.entry()) + 1;
							lines.set(line - 1);
						}
						// System.out.println(as+"\t"+line);
						for (int i = visible.start; i <= visible.end; i++) {
							if (i >= start && i < end) {
								double width = screenWidth / (double) visible.length();
								int translated = ab.translate(entry, i - start);
								char nt;
								if (as.strand() == Strand.FORWARD)
									nt = as.seq().getNucleotide(translated + 1);
								else
									nt = as.seq().getReverseNucleotide(as.seq().size() - translated);
								// System.out.println(nt + "\t" + ref[i -
								// visible.start]);
								if (ref[i - visible.start] != nt) {
									if (nt == '-')
										g.setColor(Color.RED);
									else
										g.setColor(Color.DARK_GRAY);
									g.fillRect((int) ((i - visible.start) * width), rec.y + (line - 1) * lineHeight, (int) Math.ceil(width), lineHeight);
									if (visible.length() < 100) {
										Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + nt, g);
										if (nt == '-')
											g.setColor(Color.BLACK);
										else
											g.setColor(Configuration.getNucleotideColor(nt).brighter());
										g.drawString("" + nt, (int) (((i - visible.start) * width - stringSize.getWidth() / 2) + (width / 2)), rec.y + line * lineHeight - 2);
									}
								}
							}
						}
						line++;
					}

				} else {/* Not very much zoomed in, but still close */
					int line = 1;

					for (AlignmentSequence as : ab2) {
						if (showAll) {
							line = ordering.get(as.entry()) + 1;
							lines.set(line - 1);
						}
						if (as.strand() == Strand.FORWARD) {
							g.setColor(Configuration.getColor("ma:forwardColor"));
						} else {
							g.setColor(Configuration.getColor("ma:reverseColor"));
						}

						g.fillRect(x1, rec.y + (line - 1) * lineHeight, x2 - x1, lineHeight);

						line++;
					}

				}
				/* Fill in the blanks when showing all */
				if (showAll) {
					for (int i = 0; i < ordering.size(); i++) {
						if (!lines.get(i)) {
							g.setColor(Color.YELLOW);
							g.fillRect(x1, rec.y + i * lineHeight, x2 - x1, lineHeight);
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
						mh.x1 = x1;

					}
				}

			}
			/* Mouse is over a block and there is some information to display */
			if (mh != null) {

				Set<Entry> shown = new HashSet<Entry>();
				if (!showAll) {
					// arr = new String[mh.ab.size()];
					// size = new Rectangle2D[mh.ab.size()];
					// int index = 0;

					for (AlignmentSequence as : mh.ab) {
						// char addChar = as.strand() == Strand.FORWARD ? '>' :
						// '<';
						// String s = addChar + " " + as.entry().getID() + " " +
						// addChar;
						// arr[index] = s;
						// Rectangle2D stringSize =
						// g.getFontMetrics().getStringBounds(s, g);
						// size[index] = stringSize;
						// index++;
						// if (stringSize.getWidth() > maxWidth)
						// maxWidth = (int) stringSize.getWidth();
						shown.add(as.entry());

					}

				} else {
					for (Entry e : model.entries())
						shown.add(e);

					// arr = new String[ordering.size()];
					// size = new Rectangle2D[ordering.size()];
					// for (AlignmentSequence as : mh.ab) {
					// char addChar = as.strand() == Strand.FORWARD ? '>' : '<';
					// String s = addChar + " " + as.entry().getID() + " " +
					// addChar;
					// int index = ordering.get(as.entry());
					// arr[index] = s;
					// Rectangle2D stringSize =
					// g.getFontMetrics().getStringBounds(s, g);
					// size[index] = stringSize;
					// if (stringSize.getWidth() > maxWidth)
					// maxWidth = (int) stringSize.getWidth();
					//
					// }
					/* Fill in the blanks */
					// for (Entry e : ordering.keySet()) {
					// if (arr[ordering.get(e)] == null) {
					// String s = e.getID();
					// arr[ordering.get(e)] = s;
					// Rectangle2D stringSize =
					// g.getFontMetrics().getStringBounds(s, g);
					// size[ordering.get(e)] = stringSize;
					// if (stringSize.getWidth() > maxWidth)
					// maxWidth = (int) stringSize.getWidth();
					// }
					// }
				}
				String[] arr = new String[model.entries().size()];
				Rectangle2D[] size = new Rectangle2D[model.entries().size()];
				int maxWidth = 0;
				for (Entry e : shown) {
					String s = e.getID();
					arr[ordering.get(e)] = s;
					Rectangle2D stringSize = g.getFontMetrics().getStringBounds(s, g);
					size[ordering.get(e)] = stringSize;
					if (stringSize.getWidth() > maxWidth)
						maxWidth = (int) stringSize.getWidth();
				}

				g.setColor(new Color(192, 192, 192, 175));
				g.fillRect((int) Math.max(mh.x1 - maxWidth, 5), mh.rec.y, maxWidth, shown.size()* lineHeight);
				g.setColor(Color.DARK_GRAY);
				g.drawRect((int) Math.max(mh.x1 - maxWidth, 5), mh.rec.y, maxWidth, shown.size() * lineHeight);
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
			int[] counts = new int[(int) Math.ceil(screenWidth)];
			for (AlignmentBlock ab : abs) {
				AlignmentSequence as = ab.getAlignmentSequence(entry);
				int start = Convert.translateGenomeToScreen(as.start(), visible, screenWidth);
				int end = Convert.translateGenomeToScreen(as.end(), visible, screenWidth);
				for (int i = start; i < end; i++) {
					if (i >= 0 && i < counts.length)
						counts[i] += ab.size();
				}
			}
			int yMax = 0;
			for (int i = 0; i < counts.length; i++) {
				if (counts[i] > yMax)
					yMax = counts[i];
				g.drawLine(i, yOffset, i, yOffset + counts[i] * lineHeight);
			}

			return yMax * lineHeight;
		}
	}

	private void updateOrdering() {
		// TODO keep old ones, only add new ones.
		ordering.clear();
		int i = 0;
		for (Entry e : model.entries()) {
			ordering.put(e, i++);
		}
		// System.out.println(ordering);

	}

	@Override
	public String displayName() {
		return "Multiple alignment";
	}

}
