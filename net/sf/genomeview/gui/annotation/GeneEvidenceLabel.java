/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.TreeMap;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.AbstractGeneLabel;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.annotation.track.FeatureTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.plugin.IValueFeature;
import net.sf.jannot.Location;
import net.sf.jannot.Type;

public class GeneEvidenceLabel extends AbstractGeneLabel implements
		MouseListener, MouseMotionListener {

	public GeneEvidenceLabel(Model model) {
		super(model);

		setBackground(Color.WHITE);
		setOpaque(true);
		// this.model = model;
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		model.addObserver(this);
		// TODO 200 pixels for evidence should be a portion of the screen
		// instead of hard coded
		this
				.setPreferredSize(new Dimension(this.getPreferredSize().width,
						200));

	}

	public enum FillMode {
		FILL, DRAW, BACKDROP;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		this.setVisible(model.isAnnotationVisible());
		if (this.isVisible()) {
			// scrollPane.revalidate();
			revalidate();
			repaint();
		}
		super.update(arg0, arg1);

	}

	// /**
	// * Uses 32 pixels
	// *
	// * @param g
	// * @param r
	// */
	// protected void paintTicks(Graphics g, Location r) {
	// g.setColor(Color.BLACK);
	// g.drawLine(0, framePixelsUsed + 15, g.getClipBounds().width,
	// framePixelsUsed + 15);
	//
	// if (r.start() == r.end()) {
	// return;
	// }
	// // determine the tickDistance, we aim for 10 ticks on screen.
	// int length = r.length();
	// int scale = (int) Math.log10(length / 10.0);
	// int multiplier = (int) (length / Math.pow(10, scale + 1));
	// int tickDistance = (int) (Math.pow(10, scale) * multiplier);
	// if (tickDistance == 0)
	// tickDistance = 1;
	// // paint the ticks
	// int currentTick = (r.start() - r.start() % tickDistance) + 1;
	// boolean up = true;
	// while (currentTick < r.end()) {
	// int xpos = translateGenomeToScreen(currentTick, r,screenWidth);
	// String s = "" + currentTick;
	//
	// if (up) {
	// g.drawLine(xpos, framePixelsUsed + 2, xpos, framePixelsUsed + 28);
	// g.drawString(s, xpos + 2, framePixelsUsed + 14);
	// } else {
	// g.drawLine(xpos, framePixelsUsed + 2, xpos, framePixelsUsed + 28);
	// g.drawString(s, xpos + 2, framePixelsUsed + 26);
	// }
	// up = !up;
	//
	// currentTick += tickDistance;
	//
	// }
	// framePixelsUsed += 32;
	//
	// }
	/* A mapping from position to track */
	private TreeMap<Integer, Track> tracks = new TreeMap<Integer, Track>();

	@Override
	public void mouseClicked(MouseEvent e) {
		int y = e.getY();
		int min = Integer.MAX_VALUE;
		for (Integer i : tracks.keySet()) {
			if (i > y && i < min)
				min = i;
		}
		Track clickTrack = tracks.get(min);

		if (clickTrack != null)
			clickTrack.mouseClicked(e.getX(), y - min, e);

		if (Mouse.button2(e) || Mouse.button3(e)) {
			StaticUtils.popupMenu(model).show(this, e.getX(), e.getY());
		}

		// /*
		// * first check for painted buttons to expand tracks, if none is
		// clicked
		// * check the collision map
		// */
		// boolean togglePressed = false;
		// /**
		// * Check for the value features
		// */
		// for (Rectangle key : displayValueToggle.keySet()) {
		// if (key.contains(e.getX(), e.getY())) {
		// togglePressed = true;
		// switch (model.getValueFeatureDisplayType(displayValueToggle
		// .get(key))) {
		// case MultiLineBlocks:
		// model.setValueFeatureDisplayType(displayValueToggle
		// .get(key), DisplayType.OneLineBlocks);
		// break;
		// case LineProfile:
		// model.setValueFeatureDisplayType(displayValueToggle
		// .get(key), DisplayType.ColorCodingProfile);
		// break;
		// case OneLineBlocks:
		// model.setValueFeatureDisplayType(displayValueToggle
		// .get(key), DisplayType.MultiLineBlocks);
		// break;
		// case ColorCodingProfile:
		// model.setValueFeatureDisplayType(displayValueToggle
		// .get(key), DisplayType.LineProfile);
		// break;
		// }
		// }
		//
		// }
		// /**
		// * Check for the typed features
		// */
		// for (Rectangle key : displayTypeToggle.keySet()) {
		// if (key.contains(e.getX(), e.getY())) {
		// togglePressed = true;
		// switch (model.getDisplayType(displayTypeToggle.get(key))) {
		// case MultiLineBlocks:
		// model.setDisplayType(displayTypeToggle.get(key),
		// DisplayType.OneLineBlocks);
		// break;
		// case LineProfile:
		// model.setDisplayType(displayTypeToggle.get(key),
		// DisplayType.ColorCodingProfile);
		// break;
		// case OneLineBlocks:
		// model.setDisplayType(displayTypeToggle.get(key),
		// DisplayType.MultiLineBlocks);
		// break;
		// case ColorCodingProfile:
		// model.setDisplayType(displayTypeToggle.get(key),
		// DisplayType.LineProfile);
		// break;
		// }
		// }
		//
		// }
		//
		// if (!togglePressed) {

		// }

	}

	private static final long serialVersionUID = 1L;

	private int currentBackgroundIndex = 0;

	private Color[] background = new Color[] { new Color(204, 238, 255, 100),
			new Color(255, 255, 204, 100) };

	@Override
	public void paintComponent(Graphics g) {
		displayValueToggle.clear();
		displayTypeToggle.clear();
		tracks.clear();
		framePixelsUsed = 0;
		screenWidth = this.getSize().width + 1;
		super.paintComponent(g);
		currentBackgroundIndex = 0;
		
		for (Track track : model.getTrackList()) {
			if (track.isVisible()) {
				
				int startY = framePixelsUsed;
				framePixelsUsed += track.paint(g, model.getSelectedEntry(),
						framePixelsUsed, screenWidth);
				
				if (track instanceof FeatureTrack) {
					framePixelsUsed += 5;
					Rectangle r = new Rectangle(0, startY,
							(int) screenWidth + 1, framePixelsUsed - startY);
					tracks.put(framePixelsUsed, track);
					g.setColor(background[currentBackgroundIndex]);
					g.fillRect(r.x, r.y , r.width, r.height);

					currentBackgroundIndex++;
					currentBackgroundIndex %= background.length;
				}
			}
			// }
		}
		// paintSelectedLocation(g, model.getAnnotationLocationVisible());

		if (this.getPreferredSize().height != framePixelsUsed) {
			this.setPreferredSize(new Dimension(this.getPreferredSize().width,
					framePixelsUsed));
			this.invalidate();
			this.getParent().validate();
			revalidate();

		}
	}

	// Collision map for the displayToggles
	private HashMap<Rectangle, Type> displayTypeToggle = new HashMap<Rectangle, Type>();

	private HashMap<Rectangle, IValueFeature> displayValueToggle = new HashMap<Rectangle, IValueFeature>();

	// private void paintDisplayToggle(Graphics2D g, IValueFeature name) {
	// // TODO Auto-generated method stub
	// DisplayType dt = model.getValueFeatureDisplayType(name);
	// g.setColor(Color.WHITE);
	// g.fillRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);
	//
	// g.setColor(Color.BLACK);
	// g.drawRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);
	//
	// g.drawLine((int) screenWidth - 15 + 2, framePixelsUsed - 10,
	// (int) screenWidth - 15 + 8, framePixelsUsed - 10);
	//
	// if (dt == DisplayType.OneLineBlocks
	// || dt == DisplayType.ColorCodingProfile) {
	// g.drawLine((int) screenWidth - 10, framePixelsUsed - 15 + 2,
	// (int) screenWidth - 10, framePixelsUsed - 15 + 8);
	// }
	// displayValueToggle.put(new Rectangle((int) screenWidth - 15,
	// framePixelsUsed - 15, 10, 10), name);
	// }

	// private void paintDisplayToggle(Graphics g, Type key) {
	// DisplayType dt = model.getDisplayType(key);
	// g.setColor(Color.WHITE);
	// g.fillRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);
	//
	// g.setColor(Color.BLACK);
	// g.drawRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);
	//
	// g.drawLine((int) screenWidth - 15 + 2, framePixelsUsed - 10,
	// (int) screenWidth - 15 + 8, framePixelsUsed - 10);
	//
	// if (dt == DisplayType.OneLineBlocks
	// || dt == DisplayType.ColorCodingProfile) {
	// g.drawLine((int) screenWidth - 10, framePixelsUsed - 15 + 2,
	// (int) screenWidth - 10, framePixelsUsed - 15 + 8);
	// }
	// displayTypeToggle.put(new Rectangle((int) screenWidth - 15,
	// framePixelsUsed - 15, 10, 10), key);
	// }

	// /**
	// *
	// * @param gg
	// * @param key
	// * @param dt
	// * @return whether any collision occured while rendering this term
	// */
	// private boolean renderTerm(Graphics gg, Type key, DisplayType dt) {
	// boolean collision = false;
	// Graphics2D g = (Graphics2D) gg;
	// List<Feature> keys = model.getSelectedEntry().annotation.getByType(key,
	// model.getAnnotationLocationVisible());
	// if (keys.size() > Configuration
	// .getInt("annotationview:maximumNoVisibleFeatures")) {
	// g.setColor(Color.BLACK);
	// g
	// .drawString(
	// key
	// + ": Too many features to display, zoom in to see features",
	// 10, framePixelsUsed + 10);
	// framePixelsUsed += 20;
	// return false;
	// } else {
	// CollisionMap fullBlockMap = new CollisionMap(model);
	// switch (dt) {
	// case OneLineBlocks:
	// case MultiLineBlocks:
	//
	// int lineThickness = Configuration.getInt("evidenceLineHeight");
	// if (model.isShowTextOnStructure(key)) {
	// lineThickness += 10;
	// }
	// int lines = 0;
	// for (Feature rf : keys) {
	// if (!model.isSourceVisible(rf.getSource()))
	// continue;
	// // the line on which to paint this feature
	// int thisLine = 0;
	//
	// Color c = Configuration.getColor("TYPE_" + rf.type());
	// if (Configuration.getBoolean("useColorQualifierTag")) {
	// List<Qualifier> notes = rf.qualifier("colour");
	// if (notes.size() > 0) {
	// String[] arr = notes.get(0).getValue().split(" ");
	// if (arr.length == 3)
	// c = new Color(Integer.parseInt(arr[0]), Integer
	// .parseInt(arr[1]), Integer
	// .parseInt(arr[2]));
	//
	// }
	// }
	// g.setColor(c);
	// int x1 = Convert.translateGenomeToScreen(rf.start(), model
	// .getAnnotationLocationVisible(), screenWidth);
	// int x2 = Convert.translateGenomeToScreen(rf.end() + 1,
	// model.getAnnotationLocationVisible(), screenWidth);
	//
	// // TODO is this not always the case?
	// if (x2 > 0) {
	//
	// Qualifier name = rf.singleQualifier("gene");
	//
	// int maxX = x2;
	//
	// // modify collision box only when the names will be
	// // displayed.
	// if (model.isShowTextOnStructure(key) && name != null) {
	//
	// Rectangle2D stringSize = g.getFontMetrics()
	// .getStringBounds(name.getValue(), g);
	// if (x1 + stringSize.getMaxX() > maxX)
	// maxX = x1 + (int) stringSize.getMaxX() + 1;
	//
	// }
	// /*
	// * How close can items be together before they are
	// * considered overlapping?
	// */
	// int closenessOverlap = Configuration
	// .getInt("closenessOverlap");
	// Rectangle r = new Rectangle(x1 - closenessOverlap,
	// thisLine * lineThickness + framePixelsUsed,
	// maxX - x1 + 2 * closenessOverlap, lineThickness);
	// // only when the blocks should be tiled, do we need to
	// // determine an empty place.
	// if (!collision)
	// collision = fullBlockMap.collision(r);
	// if (dt == DisplayType.MultiLineBlocks) {
	//
	// while (fullBlockMap.collision(r)) {
	// thisLine++;
	//
	// if (thisLine > lines)
	// lines = thisLine;
	// r = new Rectangle(x1 - closenessOverlap,
	// thisLine * lineThickness
	// + framePixelsUsed, maxX - x1
	// + 2 * closenessOverlap,
	// lineThickness);
	// }
	// }
	// fullBlockMap.addLocation(r, null);
	// /*
	// * Create one or more rectangles, in order not to have
	// * to reproduce them on every drawing occasion. Make
	// * sure they are ordered from left to right.
	// */
	// SortedSet<Location> loc = rf.location();
	// ArrayList<Rectangle> rectList = new ArrayList<Rectangle>();
	// for (Location l : loc) {
	//
	// int subX1 = Convert.translateGenomeToScreen(l
	// .start(), model
	// .getAnnotationLocationVisible(),
	// screenWidth);
	// int subX2 = Convert.translateGenomeToScreen(
	// l.end() + 1, model
	// .getAnnotationLocationVisible(),
	// screenWidth);
	// Rectangle rec = new Rectangle(subX1, thisLine
	// * lineThickness + framePixelsUsed, subX2
	// - subX1, lineThickness - 5);
	// /* Add this rectangle to the location hits */
	// super.collisionMap.addLocation(rec, l);
	// rectList.add(rec);
	//
	// }
	//
	// if (model.getHighlightedFeatures() != null
	// && model.getHighlightedFeatures().contains(rf)) {
	// Color backupColor = g.getColor();
	// Stroke backupStroke = g.getStroke();
	// float[] dashes = { 5f, 2f };
	// Stroke dashedStroke = new BasicStroke(2f,
	// BasicStroke.CAP_ROUND,
	// BasicStroke.JOIN_ROUND, 10f, dashes, 0f);
	// // g.setStroke(new BasicStroke(2));
	// g.setStroke(dashedStroke);
	// g.setColor(Color.ORANGE);
	// drawRects(g, rectList, FillMode.DRAW);
	// g.setColor(backupColor);
	// g.setStroke(backupStroke);
	//
	// }
	// if (!model.isFeatureVisible(rf))
	// g.setColor(Color.LIGHT_GRAY);
	// drawRects(g, rectList, FillMode.FILL);
	// Color backColor = g.getColor();
	// g.setColor(g.getColor().darker());
	// drawRects(g, rectList, FillMode.DRAW);
	//
	// /* Put triangle */
	// int trianglehalf = (lineThickness - 5) / 2;
	// switch (rf.strand()) {
	// case REVERSE:// reverse arrow
	// g.drawLine(x1, thisLine * lineThickness
	// + framePixelsUsed, x1 - trianglehalf,
	// thisLine * lineThickness + framePixelsUsed
	// + trianglehalf);
	// g.drawLine(x1 - trianglehalf, thisLine
	// * lineThickness + framePixelsUsed
	// + trianglehalf, x1, thisLine
	// * lineThickness + framePixelsUsed
	// + lineThickness - 5);
	// break;
	// case FORWARD:// forward arrow
	// g.drawLine(x2, thisLine * lineThickness
	// + framePixelsUsed, x2 + trianglehalf,
	// thisLine * lineThickness + framePixelsUsed
	// + trianglehalf);
	// g.drawLine(x2 + trianglehalf, thisLine
	// * lineThickness + framePixelsUsed
	// + trianglehalf, x2, thisLine
	// * lineThickness + framePixelsUsed
	// + lineThickness - 5);
	// break;
	// default:// do nothing
	// break;
	//
	// }
	// if (model.isShowTextOnStructure(key) && name != null) {
	// g.drawString(name.getValue(), x1, thisLine
	// * lineThickness + framePixelsUsed + 20);
	// }
	// g.setColor(backColor);
	//
	// // Set<Feature> selected = model.getFeatureSelection();
	// Set<Location> intersection = new HashSet<Location>(loc);
	// intersection.retainAll(model.getLocationSelection());
	//
	// if (intersection.size() > 0) {
	// g.setColor(Color.BLACK);
	// drawRects(g, rectList, FillMode.DRAW);
	//
	// }
	// }
	// }
	// if (Configuration.getBoolean("showTrackName")) {
	// g.setColor(Color.black);
	// g.drawString(key.toString(), 10, framePixelsUsed
	// + lineThickness);
	// }
	// framePixelsUsed += (lines + 1) * lineThickness;
	// break;
	// case BarchartProfile:
	// int line = 75;
	// /* Determine maximum score */
	// List<Feature> allfeatures = model.getSelectedEntry().annotation
	// .getByType(key);
	// double maxScore = 0;
	// for (Feature rf : allfeatures) {
	// if (rf.getScore() > maxScore)
	// maxScore = rf.getScore();
	//
	// }
	// /* Paint all visible features */
	// for (Feature rf : keys) {
	// assert (rf.location().size() == 1);
	// // the line on which to paint this feature
	// int thisLine = 0;
	// double heightScale = 1;
	//
	// Color c = Configuration.getColor("TYPE_" + rf.type());
	//
	// double score = rf.getScore() / maxScore;
	// heightScale = score;
	// c = Color.green;
	// String background = rf.singleQualifierValue("background");
	// if (background != null) {
	// double backgroundScore = Double.parseDouble(background);
	// if (score > backgroundScore)
	// c = Color.GREEN;
	// else
	// c = Color.red;
	// }
	//
	// g.setColor(c);
	// int x1 = Convert.translateGenomeToScreen(rf.start(), model
	// .getAnnotationLocationVisible(), screenWidth);
	// int x2 = Convert.translateGenomeToScreen(rf.end() + 1,
	// model.getAnnotationLocationVisible(), screenWidth);
	// if (x2 == x1)
	// x2++;
	// if (x2 > 0) {
	// assert (rf.location().size() == 1);
	// super.collisionMap.addLocation(new Rectangle(x1,
	// thisLine * line + framePixelsUsed, x2 - x1,
	// line), rf.location().first());
	// g
	// .fillRect(x1, (int) (thisLine * line
	// + framePixelsUsed + (1 - heightScale)
	// * line), x2 - x1,
	// (int) (line * heightScale));
	// // Set<Feature> selected = model.getFeatureSelection();
	// // if (selected != null && selected.contains(rf)) {
	// if (model.getLocationSelection().contains(
	// rf.location().first())) {
	// g.setColor(Color.BLACK);
	// g.drawRect(x1, (int) (thisLine * line
	// + framePixelsUsed + (1 - heightScale)
	// * line), x2 - x1,
	// (int) (line * heightScale));
	//
	// }
	// }
	// }
	// if (Configuration.getBoolean("showTrackName")) {
	// g.setColor(Color.black);
	// g.drawString(key.toString(), 10, framePixelsUsed + line);
	// }
	// framePixelsUsed += line;
	// break;
	// default:
	// System.err.print("cannot render this type of data: " + dt);
	// break;
	// }
	// return collision;
	// }
	// }

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private Location pressLoc = null;

	private int pressX;

	@Override
	public void mousePressed(MouseEvent e) {
		pressLoc = model.getAnnotationLocationVisible();
		pressX = e.getX();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressLoc = null;

	}

	@Override
	public void mouseDragged(MouseEvent arg) {
		if (pressLoc != null) {

			double move = (arg.getX() - pressX) / screenWidth;
			int start = (int) (pressLoc.start() - pressLoc.length() * move);
			int end = (int) (pressLoc.end() - pressLoc.length() * move);
			model.setAnnotationLocationVisible(new Location(start, end));

		}

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}