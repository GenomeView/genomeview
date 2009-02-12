/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;

import net.sf.genomeview.core.ColorFactory;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.AbstractGeneLabel;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.CollisionMap;
import net.sf.genomeview.plugin.IValue;
import net.sf.genomeview.plugin.IValueFeature;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Qualifier;
import net.sf.jannot.Type;

public class GeneEvidenceLabel extends AbstractGeneLabel implements MouseListener, MouseMotionListener {

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
        this.setPreferredSize(new Dimension(this.getPreferredSize().width, 200));

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

    /**
     * Uses 32 pixels
     * 
     * @param g
     * @param r
     */
    protected void paintTicks(Graphics g, Location r) {
        g.setColor(Color.BLACK);
        g.drawLine(0, framePixelsUsed + 15, g.getClipBounds().width, framePixelsUsed + 15);

        if (r.start() == r.end()) {
            return;
        }
        // determine the tickDistance, we aim for 10 ticks on screen.
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
            int xpos = translateGenomeToScreen(currentTick, r);
            String s = "" + currentTick;

            if (up) {
                g.drawLine(xpos, framePixelsUsed + 2, xpos, framePixelsUsed + 28);
                g.drawString(s, xpos + 2, framePixelsUsed + 14);
            } else {
                g.drawLine(xpos, framePixelsUsed + 2, xpos, framePixelsUsed + 28);
                g.drawString(s, xpos + 2, framePixelsUsed + 26);
            }
            up = !up;

            currentTick += tickDistance;

        }
        framePixelsUsed += 32;

    }

    @Override
    public void mouseClicked(MouseEvent e) {

        /*
         * first check for painted buttons to expand tracks, if none is clicked
         * check the collision map
         */
        boolean togglePressed = false;
        /**
         * Check for the value features
         */
        for (Rectangle key : displayValueToggle.keySet()) {
            if (key.contains(e.getX(), e.getY())) {
                togglePressed = true;
                switch (model.getValueFeatureDisplayType(displayValueToggle.get(key))) {
                case MultiLineBlocks:
                    model.setValueFeatureDisplayType(displayValueToggle.get(key), DisplayType.OneLineBlocks);
                    break;
                case LineProfile:
                    model.setValueFeatureDisplayType(displayValueToggle.get(key), DisplayType.ColorCodingProfile);
                    break;
                case OneLineBlocks:
                    model.setValueFeatureDisplayType(displayValueToggle.get(key), DisplayType.MultiLineBlocks);
                    break;
                case ColorCodingProfile:
                    model.setValueFeatureDisplayType(displayValueToggle.get(key), DisplayType.LineProfile);
                    break;
                }
            }

        }
        /**
         * Check for the typed features
         */
        for (Rectangle key : displayTypeToggle.keySet()) {
            if (key.contains(e.getX(), e.getY())) {
                togglePressed = true;
                switch (model.getDisplayType(displayTypeToggle.get(key))) {
                case MultiLineBlocks:
                    model.setDisplayType(displayTypeToggle.get(key), DisplayType.OneLineBlocks);
                    break;
                case LineProfile:
                    model.setDisplayType(displayTypeToggle.get(key), DisplayType.ColorCodingProfile);
                    break;
                case OneLineBlocks:
                    model.setDisplayType(displayTypeToggle.get(key), DisplayType.MultiLineBlocks);
                    break;
                case ColorCodingProfile:
                    model.setDisplayType(displayTypeToggle.get(key), DisplayType.LineProfile);
                    break;
                }
            }

        }

        if (!togglePressed) {
            // catch double clicks
            if (e.getClickCount() == 2) {
                doubleClick(e);
            }

            /* try selecting something */
            Location locationHit = super.collisionMap.uniqueLocation(e.getX(), e.getY());
            if (button1(e)) {
                if (locationHit == null && !modifier(e)) {
                    model.clearLocationSelection();
                } else if (locationHit != null && e.isShiftDown()) {
                    if (model.getLocationSelection().contains(locationHit)) {
                        model.removeLocationSelection(locationHit);
                    } else {
                        model.addLocationSelection(locationHit);
                    }
                } else if (locationHit != null && !modifier(e)) {
                    model.setLocationSelection(locationHit);

                }

            }
            if (button2(e) || button3(e)) {
                StaticUtils.popupMenu(model).show(this, e.getX(), e.getY());
            }
        }

    }

    /**
     * A double click on the evidence label wants us to zoom to the double
     * clicked feature. The chromosome view should pan to the correct location,
     * but should not change zoom level.
     * 
     * @param e
     *            the MouseEvent
     */
    private void doubleClick(MouseEvent e) {
        Location locationHit = super.collisionMap.uniqueLocation(e.getX(), e.getY());
        if (locationHit != null) {
            Feature featHit = locationHit.getParent();
            model.setLocationSelection(featHit);
            int l = featHit.length();
            model.setAnnotationLocationVisible(new Location(featHit.start() - (l / 20), featHit.end() + (l / 20)));

            if (!featHit.overlaps(model.getChromosomeLocationVisible())) {
                int featCenter = featHit.start() + featHit.length() / 2;
                model.centerChromView(featCenter);
            }
        }
    }

    private Map<Rectangle, Type> trackMap = new HashMap<Rectangle, Type>();

    private static final long serialVersionUID = 1L;

    private int currentBackgroundIndex = 0;

    private Color[] background = new Color[] { new Color(204, 238, 255, 100), new Color(255, 255, 204, 100) };

    public void paintComponent(Graphics g) {

        super.collisionMap.clear();
        displayValueToggle.clear();
        displayTypeToggle.clear();
        trackMap.clear();
        framePixelsUsed = 0;
        screenWidth = this.getSize().width + 1;
        super.paintComponent(g);

        paintTicks(g, model.getAnnotationLocationVisible());
        renderValueFeatures((Graphics2D) g);
        currentBackgroundIndex = 0;
        for (Type key : Type.values()) {
            if (model.isVisibleOnAnnotation(key)) {
                framePixelsUsed += 5;
                int startY = framePixelsUsed;
                DisplayType dt = model.getDisplayType(key);
                boolean collision = renderTerm(g, key, dt);
                if (collision)
                    paintDisplayToggle(g, key);
                Rectangle r = new Rectangle(0, startY, (int) screenWidth + 1, framePixelsUsed - startY);
                trackMap.put(r, key);
                g.setColor(background[currentBackgroundIndex]);
                g.fillRect(r.x, r.y - 5, r.width, r.height + 5);

                currentBackgroundIndex++;
                currentBackgroundIndex %= background.length;

            }
        }
        // paintSelectedLocation(g, model.getAnnotationLocationVisible());

        if (this.getPreferredSize().height != framePixelsUsed) {
            this.setPreferredSize(new Dimension(this.getPreferredSize().width, framePixelsUsed));
            this.invalidate();
            this.getParent().validate();
            revalidate();

        }
    }

    // Collision map for the displayToggles
    private HashMap<Rectangle, Type> displayTypeToggle = new HashMap<Rectangle, Type>();

    private HashMap<Rectangle, IValueFeature> displayValueToggle = new HashMap<Rectangle, IValueFeature>();

    private void paintDisplayToggle(Graphics2D g, IValueFeature name) {
        // TODO Auto-generated method stub
        DisplayType dt = model.getValueFeatureDisplayType(name);
        g.setColor(Color.WHITE);
        g.fillRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);

        g.setColor(Color.BLACK);
        g.drawRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);

        g.drawLine((int) screenWidth - 15 + 2, framePixelsUsed - 10, (int) screenWidth - 15 + 8, framePixelsUsed - 10);

        if (dt == DisplayType.OneLineBlocks || dt == DisplayType.ColorCodingProfile) {
            g.drawLine((int) screenWidth - 10, framePixelsUsed - 15 + 2, (int) screenWidth - 10,
                    framePixelsUsed - 15 + 8);
        }
        displayValueToggle.put(new Rectangle((int) screenWidth - 15, framePixelsUsed - 15, 10, 10), name);
    }

    private void paintDisplayToggle(Graphics g, Type key) {
        DisplayType dt = model.getDisplayType(key);
        g.setColor(Color.WHITE);
        g.fillRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);

        g.setColor(Color.BLACK);
        g.drawRect((int) screenWidth - 15, framePixelsUsed - 15, 10, 10);

        g.drawLine((int) screenWidth - 15 + 2, framePixelsUsed - 10, (int) screenWidth - 15 + 8, framePixelsUsed - 10);

        if (dt == DisplayType.OneLineBlocks || dt == DisplayType.ColorCodingProfile) {
            g.drawLine((int) screenWidth - 10, framePixelsUsed - 15 + 2, (int) screenWidth - 10,
                    framePixelsUsed - 15 + 8);
        }
        displayTypeToggle.put(new Rectangle((int) screenWidth - 15, framePixelsUsed - 15, 10, 10), key);
    }

    private void renderValueFeatures(Graphics2D g) {
        // System.out.println("Render line: " + key);
        List<IValueFeature> vfList = model.getValueFeatures(model.getSelectedEntry());

        for (IValueFeature vf : vfList) {
            /*
             * Keeps track of the number of pixels that is used vertically to
             * paint this feature.
             */
            int pixelsUsed = 0;

            if (model.isValueFeatureVisible(vf)) {
                DisplayType dt = model.getValueFeatureDisplayType(vf);

                // TODO only paint new color coding tick if is visible
                int lastX = -1;
                int lastY = -1;
                if (dt == DisplayType.LineProfile) {
                    boolean firstValue = true;
                    boolean addLastValue = true;
                    GeneralPath gp = new GeneralPath();
                    for (IValue value : vf) {
                        g.setColor(Color.BLACK);
                        double val = (value.getValue() - vf.min()) / (vf.max() - vf.min());
                        // require inverse of value because we paint
                        // top-down.
                        int y = (int) (-val * 50) + framePixelsUsed + 50;
                        int x = translateGenomeToScreen(value.getStart(), model.getAnnotationLocationVisible());

                        if (x >= 0 && x <= screenWidth) {
                            if (firstValue) {// fix first value
                                gp.moveTo(lastX, lastY);
                                gp.lineTo(x, y);
                                firstValue = false;

                            } else {
                                // if (x != lastX || y != lastY) {
                                if (x != lastX) {
                                    gp.lineTo(x, y);
                                }
                            }

                        } else if (!firstValue && addLastValue) {// fix
                            // ending
                            gp.lineTo(x, y);
                            addLastValue = false;
                        }
                        lastX = x;
                        lastY = y;

                    }
                    // System.out.println();

                    g.draw(gp);
                    // framePixelsUsed += 50;
                    pixelsUsed = 50;
                } else if (dt == DisplayType.ColorCodingProfile) {// color
                    // coding
                    // scheme

                    for (IValue value : vf) {
                        double val = (value.getValue() - vf.min()) / (vf.max() - vf.min());
                        g.setColor(ColorFactory.getColorCoding(val));
                        int x = translateGenomeToScreen(value.getStart(), model.getAnnotationLocationVisible());
                        g.fillRect(lastX, framePixelsUsed, x - lastX, 10);
                        lastX = x;

                    }
                    // framePixelsUsed += 10;
                    pixelsUsed = 10;
                } else {// Barcharts
                    // System.out.println("\tBarchart rendering:
                    // "+vf.getName());
                    if (vf.max() == vf.min()) {
                        for (IValue value : vf) {

                            g.setColor(value.color());
                            int startX = translateGenomeToScreen(value.getStart(), model.getAnnotationLocationVisible());
                            int endX = translateGenomeToScreen(value.getEnd(), model.getAnnotationLocationVisible());
                            g.fillRect(startX, framePixelsUsed, endX - startX + 1, 10);

                        }
                        framePixelsUsed += 10;
                        pixelsUsed = 10;
                    } else {
                        for (IValue value : vf) {

                            g.setColor(value.color());
                            int startX = translateGenomeToScreen(value.getStart(), model.getAnnotationLocationVisible());
                            int endX = translateGenomeToScreen(value.getEnd(), model.getAnnotationLocationVisible());

                            /* only positive values */
                            if (vf.min() >= 0) {
                                int bottomY = framePixelsUsed + 30;
                                double rescaledValue = (value.getValue() - vf.min()) / (vf.max() - vf.min());
                                assert (rescaledValue >= 0);
                                assert (rescaledValue <= 1);
                                int topY = (int) (bottomY - (rescaledValue * 30));
                                // FIXME scaling is not yet perfect
                                g.fillRect(startX, topY, endX - startX + 1, bottomY - topY);
                            } else { /* also negative values */
                                int bottomY, topY;
                                double rescaledValue = (value.getValue() - vf.min()) / (vf.max() - vf.min());
                                rescaledValue *= 2;
                                rescaledValue--;
                                if (rescaledValue >= 0) {
                                    bottomY = framePixelsUsed + 15;
                                    topY = (int) (bottomY - (rescaledValue * 15));
                                } else {
                                    topY = framePixelsUsed + 15;
                                    bottomY = (int) (topY - (rescaledValue * 15));
                                }
                                g.fillRect(startX, topY, endX - startX + 1, bottomY - topY);
                            }

                        }
                        pixelsUsed = 30;
                        // framePixelsUsed += 30;
                    }
                }
                paintDisplayToggle(g, vf);
            }
            framePixelsUsed += pixelsUsed;
            if (Configuration.getBoolean("showTrackName")) {
                g.setColor(Color.black);
                g.drawString(vf.getName(), 10, framePixelsUsed);
            }
        }
    }

    /**
     * 
     * @param gg
     * @param key
     * @param dt
     * @return whether any collision occured while rendering this term
     */
    private boolean renderTerm(Graphics gg, Type key, DisplayType dt) {
        boolean collision = false;
        Graphics2D g = (Graphics2D) gg;
        List<Feature> keys = model.getSelectedEntry().annotation.getByType(key, model.getAnnotationLocationVisible());
        if (keys.size() > Configuration.getInt("annotationview:maximumNoVisibleFeatures")) {
            g.setColor(Color.BLACK);
            g.drawString(key+": Too many features to display, zoom in to see features", 10, framePixelsUsed + 10);
            framePixelsUsed += 20;
            return false;
        } else {
            CollisionMap fullBlockMap = new CollisionMap(model);
            switch (dt) {
            case OneLineBlocks:
            case MultiLineBlocks:

                int lineThickness = Configuration.getInt("evidenceLineHeight");
                if (model.isShowTextOnStructure(key)) {
                    lineThickness += 10;
                }
                int lines = 0;
                for (Feature rf : keys) {
                    if (!model.isSourceVisible(rf.getSource()))
                        continue;
                    // the line on which to paint this feature
                    int thisLine = 0;

                    Color c = Configuration.getColor("TYPE_" + rf.type());
                    if (Configuration.getBoolean("useColorQualifierTag")) {
                        List<Qualifier> notes = rf.qualifier("colour");
                        if (notes.size() > 0) {
                            String[] arr = notes.get(0).getValue().split(" ");
                            if (arr.length == 3)
                                c = new Color(Integer.parseInt(arr[0]), Integer.parseInt(arr[1]), Integer
                                        .parseInt(arr[2]));

                        }
                    }
                    g.setColor(c);
                    int x1 = translateGenomeToScreen(rf.start(), model.getAnnotationLocationVisible());
                    int x2 = translateGenomeToScreen(rf.end() + 1, model.getAnnotationLocationVisible());

                    // TODO is this not always the case?
                    if (x2 > 0) {

                        Qualifier name = rf.singleQualifier("gene");

                        int maxX = x2;

                        // modify collision box only when the names will be
                        // displayed.
                        if (model.isShowTextOnStructure(key) && name != null) {

                            Rectangle2D stringSize = g.getFontMetrics().getStringBounds(name.getValue(), g);
                            if (x1 + stringSize.getMaxX() > maxX)
                                maxX = x1 + (int) stringSize.getMaxX() + 1;

                        }
                        /*
                         * How close can items be together before they are
                         * considered overlapping?
                         */
                        int closenessOverlap = Configuration.getInt("closenessOverlap");
                        Rectangle r = new Rectangle(x1 - closenessOverlap, thisLine * lineThickness + framePixelsUsed,
                                maxX - x1 + 2 * closenessOverlap, lineThickness);
                        // only when the blocks should be tiled, do we need to
                        // determine an empty place.
                        if (!collision)
                            collision = fullBlockMap.collision(r);
                        if (dt == DisplayType.MultiLineBlocks) {

                            while (fullBlockMap.collision(r)) {
                                thisLine++;

                                if (thisLine > lines)
                                    lines = thisLine;
                                r = new Rectangle(x1 - closenessOverlap, thisLine * lineThickness + framePixelsUsed,
                                        maxX - x1 + 2 * closenessOverlap, lineThickness);
                            }
                        }
                        fullBlockMap.addLocation(r, null);
                        /*
                         * Create one or more rectangles, in order not to have
                         * to reproduce them on every drawing occasion. Make
                         * sure they are ordered from left to right.
                         */
                        SortedSet<Location> loc = rf.location();
                        ArrayList<Rectangle> rectList = new ArrayList<Rectangle>();
                        for (Location l : loc) {

                            int subX1 = translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible());
                            int subX2 = translateGenomeToScreen(l.end() + 1, model.getAnnotationLocationVisible());
                            Rectangle rec = new Rectangle(subX1, thisLine * lineThickness + framePixelsUsed, subX2
                                    - subX1, lineThickness - 5);
                            /* Add this rectangle to the location hits */
                            super.collisionMap.addLocation(rec, l);
                            rectList.add(rec);

                        }

                        if (model.getHighlightedFeatures() != null && model.getHighlightedFeatures().contains(rf)) {
                            Color backupColor = g.getColor();
                            Stroke backupStroke = g.getStroke();
                            float[] dashes = { 5f, 2f };
                            Stroke dashedStroke = new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                                    10f, dashes, 0f);
                            // g.setStroke(new BasicStroke(2));
                            g.setStroke(dashedStroke);
                            g.setColor(Color.ORANGE);
                            drawRects(g, rectList, FillMode.DRAW);
                            g.setColor(backupColor);
                            g.setStroke(backupStroke);

                        }
                        if (!model.isFeatureVisible(rf))
                            g.setColor(Color.LIGHT_GRAY);
                        drawRects(g, rectList, FillMode.FILL);
                        Color backColor = g.getColor();
                        g.setColor(g.getColor().darker());
                        drawRects(g, rectList, FillMode.DRAW);

                        /* Put triangle */
                        int trianglehalf = (lineThickness - 5) / 2;
                        switch (rf.strand()) {
                        case REVERSE:// reverse arrow
                            g.drawLine(x1, thisLine * lineThickness + framePixelsUsed, x1 - trianglehalf, thisLine
                                    * lineThickness + framePixelsUsed + trianglehalf);
                            g.drawLine(x1 - trianglehalf, thisLine * lineThickness + framePixelsUsed + trianglehalf,
                                    x1, thisLine * lineThickness + framePixelsUsed + lineThickness - 5);
                            break;
                        case FORWARD:// forward arrow
                            g.drawLine(x2, thisLine * lineThickness + framePixelsUsed, x2 + trianglehalf, thisLine
                                    * lineThickness + framePixelsUsed + trianglehalf);
                            g.drawLine(x2 + trianglehalf, thisLine * lineThickness + framePixelsUsed + trianglehalf,
                                    x2, thisLine * lineThickness + framePixelsUsed + lineThickness - 5);
                            break;
                        default:// do nothing
                            break;

                        }
                        if (model.isShowTextOnStructure(key) && name != null) {
                            g.drawString(name.getValue(), x1, thisLine * lineThickness + framePixelsUsed + 20);
                        }
                        g.setColor(backColor);

                        // Set<Feature> selected = model.getFeatureSelection();
                        Set<Location> intersection = new HashSet<Location>(loc);
                        intersection.retainAll(model.getLocationSelection());

                        if (intersection.size() > 0) {
                            g.setColor(Color.BLACK);
                            drawRects(g, rectList, FillMode.DRAW);

                        }
                    }
                }
                if (Configuration.getBoolean("showTrackName")) {
                    g.setColor(Color.black);
                    g.drawString(key.toString(), 10, framePixelsUsed + lineThickness);
                }
                framePixelsUsed += (lines + 1) * lineThickness;
                break;
            case BarchartProfile:
                int line = 75;
                /* Determine maximum score */
                List<Feature> allfeatures = model.getSelectedEntry().annotation.getByType(key);
                double maxScore = 0;
                for (Feature rf : allfeatures) {
                    if (rf.getScore() > maxScore)
                        maxScore = rf.getScore();

                }
                /* Paint all visible features */
                for (Feature rf : keys) {
                    assert (rf.location().size() == 1);
                    // the line on which to paint this feature
                    int thisLine = 0;
                    double heightScale = 1;

                    Color c = Configuration.getColor("TYPE_" + rf.type());

                    double score = rf.getScore() / maxScore;
                    heightScale = score;
                    c = Color.green;
                    String background = rf.singleQualifierValue("background");
                    if (background != null) {
                        double backgroundScore = Double.parseDouble(background);
                        if (score > backgroundScore)
                            c = Color.GREEN;
                        else
                            c = Color.red;
                    }

                    g.setColor(c);
                    int x1 = translateGenomeToScreen(rf.start(), model.getAnnotationLocationVisible());
                    int x2 = translateGenomeToScreen(rf.end() + 1, model.getAnnotationLocationVisible());
                    if (x2 == x1)
                        x2++;
                    if (x2 > 0) {
                        assert (rf.location().size() == 1);
                        super.collisionMap.addLocation(new Rectangle(x1, thisLine * line + framePixelsUsed, x2 - x1,
                                line), rf.location().first());
                        g.fillRect(x1, (int) (thisLine * line + framePixelsUsed + (1 - heightScale) * line), x2 - x1,
                                (int) (line * heightScale));
                        // Set<Feature> selected = model.getFeatureSelection();
                        // if (selected != null && selected.contains(rf)) {
                        if (model.getLocationSelection().contains(rf.location().first())) {
                            g.setColor(Color.BLACK);
                            g.drawRect(x1, (int) (thisLine * line + framePixelsUsed + (1 - heightScale) * line), x2
                                    - x1, (int) (line * heightScale));

                        }
                    }
                }
                if (Configuration.getBoolean("showTrackName")) {
                    g.setColor(Color.black);
                    g.drawString(key.toString(), 10, framePixelsUsed + line);
                }
                framePixelsUsed += line;
                break;
            default:
                System.err.print("cannot render this type of data: " + dt);
                break;
            }
            return collision;
        }
    }

    private void drawRects(Graphics g, ArrayList<Rectangle> rectList, FillMode fm) {
        Point lastPoint = null;

        for (Rectangle rect : rectList) {
            switch (fm) {
            case FILL:
                g.fillRect(rect.x, rect.y, rect.width, rect.height);
                break;
            case DRAW:
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
                break;
            case BACKDROP:
                // TODO some nice indication that something is highlighted?
                break;
            }
            if (lastPoint != null) {
                g.drawLine(rect.x, rect.y + rect.height / 2, lastPoint.x, lastPoint.y);
            }
            lastPoint = new Point(rect.x + rect.width, rect.y + rect.height / 2);
        }

    }

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