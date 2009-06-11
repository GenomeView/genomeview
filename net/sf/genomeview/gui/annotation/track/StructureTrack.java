/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Model.Highlight;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import be.abeel.util.DefaultHashMap;

public class StructureTrack extends Track {
	public StructureTrack(Model model) {
		super(model, true,false);
		collisionMap = new CollisionMap(model);

		// this.addMouseListener(this);
		// this.addMouseMotionListener(this);
		// model.addObserver(this);
	}

	private int lineHeight = Configuration.getInt("geneStructureLineHeight");

	/* The height of the ticks and coordinate drawing */
	private static final int tickHeight = 32;

	/* place that is left within a line */
	private static final int gap = Configuration.getInt("geneStructureLineGap");

	/* information about the the item that we are currently drag-editing */
	private Location borderHit = null;

	// private int currentX = -1;

	private int currentGenomeX = -1;

	// private int pressX = -1;

	private int modifyCoordinate;

	private int pressTrack;

	private boolean dragging;

	private int pressGenomeX;

	private CollisionMap collisionMap = null;

	/**
	 * Uses 32 pixels.
	 * 
	 * @param g
	 * @param r
	 */
	private void paintNucleotideTicks(Graphics g, Location r, int yOffset) {
		g.setColor(Color.BLACK);
		g.drawLine(0, 4 * lineHeight + 15 + yOffset, (int) screenWidth, 4 * lineHeight + 15 + yOffset);

		if (r.start() == r.end()) {
			return;
		}
		/* determine the tickDistance, we aim for 10 ticks on screen. */
		int length = r.length();
		int scale = (int) Math.log10(length / 10.0);
		int multiplier = (int) (length / Math.pow(10, scale + 1));

		int tickDistance = (int) (Math.pow(10, scale) * multiplier);
		if (tickDistance == 0)
			tickDistance = 1;

		// paint the ticks
		int currentTick = (r.start() - r.start() % tickDistance) + 1;
		boolean up = true;
		while (currentTick < r.end()) {
			// System.out.println("tick: " + currentTick);
			int xpos = Convert.translateGenomeToScreen(currentTick, r, screenWidth);
			String s = "" + currentTick;

			if (up) {
				g.drawLine(xpos, 4 * lineHeight + 2 + yOffset, xpos, 4 * lineHeight + 28 + yOffset);
				g.drawString(s, xpos + 2, 4 * lineHeight + 14 + yOffset);
			} else {
				g.drawLine(xpos, 4 * lineHeight + 2 + yOffset, xpos, 4 * lineHeight + 28 + yOffset);
				g.drawString(s, xpos + 2, 4 * lineHeight + 26 + yOffset);
			}
			up = !up;

			currentTick += tickDistance;

		}

	}

	private void paintHighlights(Graphics2D g, List<Highlight> highlights, int yOffset) {
		for (Highlight h : highlights) {
			Color c = h.color;
			g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 100));
			int start = Convert.translateGenomeToScreen(h.location.start(), model.getAnnotationLocationVisible(), screenWidth);
			int end = Convert.translateGenomeToScreen(h.location.end(), model.getAnnotationLocationVisible(), screenWidth);
			int top = 0;
			int high = 0;
			switch (h.strand) {
			case FORWARD:
				top = 0;
				high = 4 * lineHeight;
				break;
			case UNKNOWN:
				top = 0;
				high = 8 * lineHeight + tickHeight;

				break;
			case REVERSE:
				top = 4 * lineHeight + tickHeight;
				high = 4 * lineHeight;
				break;
			}

			g.fillRect(start, top + yOffset, end - start, high);
		}

	}

	private void paintSelectedLocation(Graphics g, Location r, int yOffset) {
		if (model.getSelectedRegion() != null) {
			int track = model.getPressTrack();
			g.setColor(new Color(0f, 0, 1, 0.5f));
			int start = Convert.translateGenomeToScreen(model.getSelectedRegion().start(), r, screenWidth);
			int end = Convert.translateGenomeToScreen(model.getSelectedRegion().end() + 1, r, screenWidth);

			switch (track) {
			case 0:
				g.fillRect(start, 0, end - start + yOffset, g.getClipBounds().height);
				break;
			case 1:
				g.fillRect(start, 3 * lineHeight + yOffset, end - start, lineHeight);
				break;
			case -1:
				g.fillRect(start, 4 * lineHeight + tickHeight + yOffset, end - start, lineHeight);
				break;
			case -2:
			case -3:
			case -4:
				end += 2;

				g.fillRect(start, (3 - track) * lineHeight + tickHeight + yOffset, end - start, lineHeight);
				break;
			case 2:
			case 3:
			case 4:
				end += 2;

				g.fillRect(start, (4 - track) * lineHeight + yOffset, end - start, lineHeight);
				break;
			}

		}
	}

	private void paintPotentialEdit(Graphics2D g, Location annotationLocationVisible, int yOffset) {
		int end = Convert.translateGenomeToScreen(pressGenomeX > currentGenomeX ? pressGenomeX + 1 : currentGenomeX + 1, annotationLocationVisible, screenWidth);
		int start = Convert.translateGenomeToScreen(pressGenomeX < currentGenomeX ? pressGenomeX : currentGenomeX, annotationLocationVisible, screenWidth);
		if (dragging && borderHit != null && Math.abs(pressTrack) >= 2) {
			g.setColor(Color.GRAY);
			if (pressTrack > 0) {
				g.drawRect(start, (4 - pressTrack) * lineHeight + gap + yOffset, end - start, lineHeight - 2 * gap);
			} else {
				g.drawRect(start, (3 - pressTrack) * lineHeight + tickHeight + gap + yOffset, end - start, lineHeight - 2 * gap);
			}

		}

	}

	private void paintPotentialSelection(Graphics2D g, Location r, int yOffset) {
		if (model.getSelectedRegion() == null) {
			if (borderHit == null && dragging) {
				g.setColor(new Color(0f, 0, 1, 0.5f));

				int start, end;
				if (pressGenomeX > currentGenomeX) {
					start = currentGenomeX;
					end = pressGenomeX + 1;
				} else {
					start = pressGenomeX;
					end = currentGenomeX + 1;
				}
				int screenStart = Convert.translateGenomeToScreen(start, r, screenWidth);
				int screenEnd = Convert.translateGenomeToScreen(end, r, screenWidth);
				switch (pressTrack) {
				case 0:
					g.fillRect(screenStart, 0 + yOffset, screenEnd - screenStart, g.getClipBounds().height);
					break;
				case 1:
					g.fillRect(screenStart, 3 * lineHeight + yOffset, screenEnd - screenStart, lineHeight);
					break;
				case -1:
					g.fillRect(screenStart, 4 * lineHeight + tickHeight + yOffset, screenEnd - screenStart, lineHeight);
					break;
				case -2:
				case -3:
				case -4:
					end += 2;
					screenStart = Convert.translateGenomeToScreen(snapStartAA(start, -pressTrack - 2), r, screenWidth);
					screenEnd = Convert.translateGenomeToScreen(snapEndAA(end, -pressTrack - 2), r, screenWidth);
					g.fillRect(screenStart, (3 - pressTrack) * lineHeight + tickHeight + yOffset, screenEnd - screenStart, lineHeight);
					break;
				case 2:
				case 3:
				case 4:
					end += 2;
					screenStart = Convert.translateGenomeToScreen(snapStartAA(start, pressTrack - 2), r, screenWidth);
					screenEnd = Convert.translateGenomeToScreen(snapEndAA(end, pressTrack - 2), r, screenWidth);
					g.fillRect(screenStart, (4 - pressTrack) * lineHeight + yOffset, screenEnd - screenStart, lineHeight);
					break;
				}
			}
		}

	}

	private int snapStartAA(int start, int offset) {
		int genome = start;
		genome = (genome - offset - 1) / 3;
		genome *= 3;
		genome += 1 + offset;
		return genome;
	}

	private int snapEndAA(int end, int offset) {
		int genome = end;
		genome = (genome - offset - 1) / 3;
		genome *= 3;
		genome += 1 + offset;
		return genome;
	}

	private void paintSequence(Graphics g, boolean forward, int yOffset) {

		Location r = model.getAnnotationLocationVisible();
		double width = screenWidth / (double) r.length();
		boolean spliceSitePaint = Configuration.getBoolean("showSpliceSiteColor");
		boolean nucleotidePaint = Configuration.getBoolean("showNucleotideColor");
		for (int i = r.start(); i <= r.end(); i++) {
			char nt;
			if (forward)
				nt = model.getSelectedEntry().sequence.getNucleotide(i);
			else
				nt = model.getSelectedEntry().sequence.getReverseNucleotide(i);
			if (spliceSitePaint) {
				Color spliceSite = checkSpliceSite(i, model, forward);

				if (spliceSite != null) {
					g.setColor(spliceSite);
					g.fillRect((int) ((i - r.start()) * width), 3 * lineHeight + (forward ? 0 : tickHeight + lineHeight) + gap + yOffset, (int) (2 * width) + 1, lineHeight - 2 * gap);
				}
			} else if (nucleotidePaint) {
				g.setColor(Configuration.getNucleotideColor(nt));
				g.fillRect((int) ((i - r.start()) * width), 3 * lineHeight + (forward ? 0 : tickHeight + lineHeight) + gap + yOffset, (int) width + 1, lineHeight - 2 * gap);

			}

			if (model.getAnnotationLocationVisible().length() < 100) {
				Rectangle2D stringSize = g.getFontMetrics().getStringBounds("" + nt, g);
				g.setColor(Color.black);
				g.drawString("" + nt, (int) (((i - r.start()) * width - stringSize.getWidth() / 2) + (width / 2)), 3 * lineHeight + (forward ? 0 : tickHeight + lineHeight) + lineHeight - 2 * gap + yOffset);
			}
		}

	}

	private Color checkSpliceSite(int i, Model model, boolean forward) {
		char nt, nt2;
		if (forward)
			nt = model.getSelectedEntry().sequence.getNucleotide(i);
		else
			nt = model.getSelectedEntry().sequence.getReverseNucleotide(i);
		switch (nt) {
		case 'A':
			nt = 'a';
			break;
		case 'T':
			nt = 't';
			break;
		case 'C':
			nt = 'c';
			break;
		case 'G':
			nt = 'g';
			break;
		}

		if (forward)
			nt2 = model.getSelectedEntry().sequence.getNucleotide(i + 1);
		else
			nt2 = model.getSelectedEntry().sequence.getReverseNucleotide(i + 1);
		switch (nt2) {
		case 'A':
			nt2 = 'a';
			break;
		case 'T':
			nt2 = 't';
			break;
		case 'C':
			nt2 = 'c';
			break;
		case 'G':
			nt2 = 'g';
			break;
		}
		if (nt == 'g' && nt2 == 't' && forward) {
			return Color.YELLOW;
		}
		if (nt == 't' && nt2 == 'g' && !forward) {
			return Color.YELLOW;
		}
		if (nt == 'a' && nt2 == 'g' && forward) {
			return Color.BLUE;

		}
		if (nt == 'g' && nt2 == 'a' && !forward) {
			return Color.BLUE;

		}
		return null;
	}

	private void paintAminoAcidReadingFrame(Graphics g, boolean forward,int yOffset) {
		Location r = model.getAnnotationLocationVisible();
		/* The width of a single nucleotide */
		double width = screenWidth / (double) r.length();
		for (int i = r.start() - 3; i <= r.end() + 3; i++) {
			int frame = i % 3;

			char aa;
			String codon;
			if (forward){
				aa = model.getSelectedEntry().sequence.getAminoAcid(i, model
						.getAAMapping());
			codon = "" + model.getSelectedEntry().sequence.getNucleotide(i) + model.getSelectedEntry().sequence.getNucleotide(i + 1)
				+ model.getSelectedEntry().sequence.getNucleotide(i + 2);
			}
			else{
				aa = model.getSelectedEntry().sequence.getReverseAminoAcid(i,
						model.getAAMapping());
				 codon = "" + model.getSelectedEntry().sequence.getReverseNucleotide(i + 2)
				+ model.getSelectedEntry().sequence.getReverseNucleotide(i + 1) + model.getSelectedEntry().sequence.getReverseNucleotide(i);
			}
			/* draw amino acid box */
			int x = (int) (((i - r.start()) * width));
			int y;
			if (forward) {
				y = (2 - ((frame + 2) % 3)) * lineHeight + gap;
			} else {
				y = tickHeight + 5 * lineHeight + (frame + 2) % 3 * lineHeight
						+ gap;
			}
			int aa_width = (int) (width * 3);
			int aa_height = lineHeight - 2 * gap;
			/* Only color start and stop codons. */
			if (Configuration.getBoolean("colorStopCodons")
					&& model.getAAMapping().isStop(aa)) {
				g.setColor(Configuration.getAminoAcidColor(aa));
				g.fillRect(x, y+ yOffset, aa_width == 0 ? 1 : aa_width, aa_height);
			}
			
			if (Configuration.getBoolean("colorStartCodons")
					&& model.getAAMapping().isStart(codon)) {
				g.setColor(Configuration.getAminoAcidColor(aa));
				g.fillRect(x, y+ yOffset, aa_width == 0 ? 1 : aa_width, aa_height);
			}

			if (model.getAnnotationLocationVisible().length() < Configuration
					.getInt("geneStructureAminoAcidWindowVerticalBars")) {
				g.setColor(Color.black);
				g
						.drawLine(x + aa_width - 1, y+yOffset, x + aa_width - 1, y+yOffset
								+ aa_height);

			}

			/*
			 * Only show the actual letters when there is less than x bp
			 * visible.
			 */
			if (model.getAnnotationLocationVisible().length() < Configuration
					.getInt("geneStructureAminoAcidWindowLetters")) {
				Rectangle2D stringSize = g.getFontMetrics().getStringBounds(
						"" + aa, g);

				/* draw amino acid letter */
				x = (int) (((i - r.start()) * width - stringSize.getWidth() / 2) + (width * 3 / 2));
				y += lineHeight - 2 * gap;
				g.drawString("" + aa, x, y+yOffset);

			}

		}

	}

	private void paintLines(Graphics g, int yOffset) {
		g.setColor(Color.GRAY);
		g.drawLine(0, lineHeight + yOffset, (int) screenWidth, lineHeight + yOffset);
		g.drawLine(0, 2 * lineHeight + yOffset, (int) screenWidth, 2 * lineHeight + yOffset);
		g.drawLine(0, 3 * lineHeight + yOffset, (int) screenWidth, 3 * lineHeight + yOffset);
		g.drawLine(0, 5 * lineHeight + tickHeight + yOffset, (int) screenWidth, 5 * lineHeight + tickHeight + yOffset);
		g.drawLine(0, 6 * lineHeight + tickHeight + yOffset, (int) screenWidth, 6 * lineHeight + tickHeight + yOffset);
		g.drawLine(0, 7 * lineHeight + tickHeight + yOffset, (int) screenWidth, 7 * lineHeight + tickHeight + yOffset);

	}

	private DefaultHashMap<Type, Boolean> visibleTypes = new DefaultHashMap<Type, Boolean>(Boolean.FALSE);

	private void paintCDS(Graphics2D g, int yOffset) {
		int y = lineHeight - 2;
		for (Type type : Type.values()) {
			if (visibleTypes.get(type)) {
				List<Feature> trackData = model.getSelectedEntry().annotation.getByType(type, model.getAnnotationLocationVisible());// model
				if (trackData.size() <= Configuration.getInt("structureview:maximumNoVisibleFeatures")) {

					for (Feature rf : trackData) {
						if (!model.isSourceVisible(rf.getSource()))
							continue;
						if (model.isFeatureVisible(rf)) {
							g.setColor(Color.BLACK);
							renderCDS(g, rf, yOffset);

						}

					}
				} else {
					g.drawString(type + ": Too many structures to paint, please zoom in", 10, y);
					y += lineHeight;
				}
			}
		}

	}

	/**
	 * Will render a CDS based on a feature.
	 * 
	 * <code>
	 * +-----------------------+
	 * | forward frame 2       |
	 * +-----------------------+
	 * | forward frame 1       |
	 * +-----------------------+
	 * | forward frame 0       |
	 * +-----------------------+
	 * | forward nucleotides   |
	 * +=======================+
	 * | position tick marks   |
	 * +=======================+
	 * | reverse nucleotides   |
	 * +-----------------------+
	 * | reverse frame 0       |
	 * +-----------------------+
	 * | reverse frame 1       |
	 * +-----------------------+
	 * | reverse frame 2       |
	 * +-----------------------+
     * </code>
	 */
	private void renderCDS(Graphics2D g, Feature rf, int yOffset) {
		int middle = 4 * lineHeight + tickHeight / 2;
		boolean featureSelected = model.getFeatureSelection().contains(rf);
		Location last = null;
		int lastY = 0;
		for (Location l : rf.location()) {
			int drawFrame = getDrawFrame(l, rf);
			/* Start of the block */
			int lmin = Convert.translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible(), screenWidth);
			/* End of the block */
			int lmax = Convert.translateGenomeToScreen(l.end() + 1, model.getAnnotationLocationVisible(), screenWidth);
			/* Horizontal position */
			int hor;
			if (rf.strand() == Strand.REVERSE)
				hor = middle + (drawFrame * lineHeight) + tickHeight / 2 + gap;
			else
				hor = middle - lineHeight - (drawFrame * lineHeight) - tickHeight / 2 + gap;
			int height = lineHeight - 2 * gap;
			/* Create box */
			Rectangle r = new Rectangle(lmin, hor + yOffset, lmax - lmin, height);
			/* Draw box */
			Color cdsColor = Configuration.getColor("TYPE_CDS");
			g.setColor(new Color(cdsColor.getRed(), cdsColor.getGreen(), cdsColor.getBlue(), 20));
			g.fill(r);
			/* Draw black box outline */
			g.setColor(Color.BLACK);
			g.draw(r);
			boolean locationSelected = model.getLocationSelection().contains(l);
			/* Selected locations have bold outline */
			if (locationSelected) {
				g.setStroke(new BasicStroke(2.0f));
				g.setColor(Color.BLACK);
				g.draw(r);
				g.setStroke(new BasicStroke(1.0f));
			}
			/* Selected features have colored background */
			if (featureSelected) {
				g.setColor(new Color(0, 0, 1, 0.5f));
				g.fill(r);
			}
			/* Draw line between boxes */
			if (last != null) {
				int lastX = Convert.translateGenomeToScreen(last.end() + 1, model.getAnnotationLocationVisible(), screenWidth);
				int currentX = Convert.translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible(), screenWidth);
				int currentY = hor + height / 2;
				int maxY = Math.min(currentY, lastY) - height / 2;
				int middleX = (lastX + currentX) / 2;
				g.setColor(Color.BLACK);
				g.drawLine(lastX, lastY + yOffset, middleX, maxY + yOffset);
				g.drawLine(middleX, maxY + yOffset, currentX, currentY + yOffset);
			}

			collisionMap.addLocation(r, l);
			last = l;
			lastY = hor + height / 2;
		}

	}

	/**
	 * Calculates the draw frame of the location. Can be 1, 2 or 3.
	 * 
	 * @param l
	 * @param rf
	 * @return
	 */
	private int getDrawFrame(Location l, Feature rf) {
		int locFrame;
		if (rf.strand() == Strand.REVERSE)
			locFrame = (l.end() + 1) % 3;
		else
			locFrame = (l.start()) % 3;
		if (locFrame == 0)
			locFrame = 3;
		int phase = rf.getPhase(l);// 0,1 or 2

		int sum;
		if (rf.strand() == Strand.REVERSE)
			sum = locFrame + 3 - phase;
		else
			sum = locFrame + phase;
		int drawFrame = sum % 3;
		return drawFrame == 0 ? 3 : drawFrame;
	}

	@Override
	public boolean mouseClicked(int x, int y, MouseEvent e) {
		System.out.println("Clicked: " + Convert.translateScreenToGenome(e.getX(), model.getAnnotationLocationVisible(), screenWidth));
		Location rf = collisionMap.uniqueLocation(e.getX(), e.getY());

		if (Mouse.button1(e)) {
			if (rf == null && (!Mouse.modifier(e) || e.isShiftDown())) {
				// model.clearFeatureSelection();
				model.clearLocationSelection();
			} else if (rf != null && e.isShiftDown()) {
				// boolean rfs = model.getFeatureSelection().contains(rf);
				boolean rls = model.getLocationSelection().contains(rf);
				if (rls) {
					// model.removeFeatureSelection(rf);
					model.removeLocationSelection(rf);
				} else
					model.addLocationSelection(rf);

			} else if (rf != null && !Mouse.modifier(e)) {
				model.setLocationSelection(rf);
				if (e.getClickCount() > 1) {
					Feature f = rf.getParent();
					int l = f.length();
					int st = f.start() - (l / 20);
					int en = f.end() + (l / 20);
					model.setAnnotationLocationVisible(new Location(st, en));
					if (!f.overlaps(model.getChromosomeLocationVisible())) {
						int featCenter = f.start() + f.length() / 2;
						model.centerChromView(featCenter);
					}
				}

			}
			model.setSelectedRegion(null);
			return true;
		}
		return false;

	}

	@Override
	public boolean mouseEntered(int x, int y, MouseEvent e) {
		outside = false;
		return false;

	}

	/* Keep track where the mouse pointer is */
	private boolean outside = false;

	private double screenWidth;

	@Override
	public boolean mouseExited(int x, int y, final MouseEvent e) {
		outside = true;
		if (dragging) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					while (dragging && outside) {
						int start = model.getAnnotationLocationVisible().start();
						int end = model.getAnnotationLocationVisible().end();
						int move = (int) ((end - start + 1) / 10.0);
						if (e.getX() < 0) {// left exit
							model.setAnnotationLocationVisible(new Location(start - move, end - move));
						}
						if (e.getX() > screenWidth) {// right exit
							model.setAnnotationLocationVisible(new Location(start + move, end + move));
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}

			}).start();

		}
		return false;

	}

	@Override
	public boolean mousePressed(int x, int y, MouseEvent e) {
		currentGenomeX = Convert.translateScreenToGenome(e.getX(), model.getAnnotationLocationVisible(), screenWidth);
		if (Mouse.button1(e) && !Mouse.modifier(e)) {
			borderHit = collisionMap.borderHit(e.getX(), e.getY());

			if (borderHit != null) {
				int genome = Convert.translateScreenToGenome(e.getX(), model.getAnnotationLocationVisible(), screenWidth);
				if (Math.abs(genome - borderHit.start()) < Math.abs(genome - borderHit.end())) {
					modifyCoordinate = borderHit.start();
				} else {
					modifyCoordinate = borderHit.end();
				}
			}
			pressGenomeX = Convert.translateScreenToGenome(e.getX(), model.getAnnotationLocationVisible(), screenWidth);
			// pressX = e.getX();
			pressTrack = getTrack(e.getY());

		}

		setChanged();
		notifyObservers();
		return false;
	}

	/**
	 * Returns the track
	 * 
	 * 0 for ticks
	 * 
	 * -1 and +1 for forward and reverse strand nucleotides
	 * 
	 * [-2;-4] and [2;4] for forward and reverse strand amino acids.
	 * 
	 * @param y
	 * @return
	 */
	private int getTrack(int y) {
		if (y < lineHeight)
			return 4;
		if (y < 2 * lineHeight)
			return 3;
		if (y < 3 * lineHeight)
			return 2;
		if (y < 4 * lineHeight)
			return 1;
		if (y < 4 * lineHeight + tickHeight)
			return 0;
		if (y < 5 * lineHeight + tickHeight)
			return -1;
		if (y < 6 * lineHeight + tickHeight)
			return -2;
		if (y < 7 * lineHeight + tickHeight)
			return -3;
		if (y < 8 * lineHeight + tickHeight)
			return -4;
		else
			throw new RuntimeException("Should never happen");
	}

	@Override
	public boolean mouseReleased(int x, int y, MouseEvent e) {
		currentGenomeX = Convert.translateScreenToGenome(e.getX(), model.getAnnotationLocationVisible(), screenWidth);
		if (Mouse.button1(e) && dragging) {
			if (borderHit != null) {

				modifyCoordinate(borderHit, modifyCoordinate, currentGenomeX);

			} else {
				int selectionStart = 0;
				int selectionEnd = 0;

				int start = pressGenomeX < currentGenomeX ? pressGenomeX : currentGenomeX;
				int end = pressGenomeX < currentGenomeX ? currentGenomeX : pressGenomeX;
				selectionStart = start;
				selectionEnd = end + 1;
				switch (pressTrack) {
				case 0:
				case 1:
				case -1:
					selectionStart = start;
					selectionEnd = end;
					break;
				case -2:
				case -3:
				case -4:
					selectionStart = snapStartAA(start, -pressTrack - 2);
					selectionEnd = snapEndAA(end, -pressTrack - 2) + 2;
					break;
				case 2:
				case 3:
				case 4:
					selectionStart = snapStartAA(start, pressTrack - 2);
					selectionEnd = snapEndAA(end, pressTrack - 2) + 2;
					break;
				}

				model.setSelectedTrack(pressTrack);
				model.setSelectedRegion(new Location(selectionStart, selectionEnd));

			}
		}
		// pressX = -1;
		pressGenomeX = -1;
		pressTrack = -1;
		dragging = false;
		borderHit = null;
		setChanged();
		notifyObservers();
		return false;
	}

	private void modifyCoordinate(Location y, int oldCoord, int newCoordinate) {

		if (y.start() == oldCoord) {
			if (oldCoord <= newCoordinate)
				y.setStart(newCoordinate + 1);
			else
				y.setStart(newCoordinate);
		} else if (y.end() == oldCoord) {
			if (oldCoord < newCoordinate)
				y.setEnd(newCoordinate);
			else
				y.setEnd(newCoordinate - 1);
		} else
			throw new RuntimeException("This should not happen, sorry, I'm done!");
		borderHit = null;

	}

	@Override
	public boolean mouseDragged(int x, int y, MouseEvent e) {
		model.clearLocationSelection();
		currentGenomeX = Convert.translateScreenToGenome(e.getX(), model.getAnnotationLocationVisible(), screenWidth);
		dragging = true;
		setChanged();
		notifyObservers();
		return true;
	}

	@Override
	public boolean mouseMoved(int x, int y, MouseEvent e) {
		if (!collisionMap.nearBorder(x, y))
			model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		else
			model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.E_RESIZE_CURSOR));
		setChanged();
		notifyObservers();
		return false;
	}

	@Override
	public int paint(Graphics g1, Entry e, int yOffset, double width) {
		this.screenWidth = width;
		// super.paintComponent(g1);
		collisionMap.clear();
		Graphics2D g = (Graphics2D) g1;

		// screenWidth = g.getClipBounds().width;
		// screenWidth = this.getSize().width + 1;

		/* paint amino acids */
		if (model.getAnnotationLocationVisible().length() < Configuration.getInt("geneStructureAminoAcidWindow")) {
			// forward strand
			paintAminoAcidReadingFrame(g, true, yOffset);
			// reverse strand
			paintAminoAcidReadingFrame(g, false, yOffset);
		}

		/* paint sequence */
		if (model.getAnnotationLocationVisible().length() < Configuration.getInt("geneStructureNucleotideWindow")) {
			// forward strand sequence
			paintSequence(g, true, yOffset);
			// reverse strand sequence
			paintSequence(g, false, yOffset);
		}
		/* paint separator lines between different reading frames */
		paintLines(g, yOffset);

		/* paint tick marks and coordinates */
		paintNucleotideTicks(g, model.getAnnotationLocationVisible(), yOffset);

		/* paint CDS */
		paintCDS(g, yOffset);

		paintPotentialSelection(g, model.getAnnotationLocationVisible(), yOffset);
		paintPotentialEdit(g, model.getAnnotationLocationVisible(), yOffset);
		paintSelectedLocation(g, model.getAnnotationLocationVisible(), yOffset);

		paintHighlights(g, model.getHighlight(model.getAnnotationLocationVisible()), yOffset);
		return 8 * lineHeight + tickHeight;
	}

	public boolean isTypeVisible(Type ct) {
		return visibleTypes.get(ct);
	}

	public void setTypeVisible(Type t, boolean b) {
		visibleTypes.put(t, b);
		setChanged();
		notifyObservers();

	}

	@Override
	public String displayName() {
		return "Gene structure";
	}
}
