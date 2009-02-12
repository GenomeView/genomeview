/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.sf.genomeview.core.ColorFactory;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.source.DataSource;
import be.abeel.graphics.Drawable;

/**
 * This is a GUI independent, <code>Drawable</code> visualization of the
 * Chromosome view. It ca be drawn on a Swing component, or any other object
 * that uses Graphics2D.
 * 
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
public class ChromosomeView implements Drawable {

    public static final int SHAPEHEIGHT = 30;

    public static final int MARGIN = 10;

	private static final int DRAWING_TRESHOLD = 300;

    private Model model;

    private ArrayList<DrawableChromosome> allShapes;

    /* Should the viewport be hidden? */
    private boolean hideViewPort;

    @Override
    public void draw(Graphics2D g, Rectangle2D rec) {
        /* This method is only called from the graphical export functions */
        this.hideViewPort = true;
        int contentHeight = this.fillShapeList((int) rec.getWidth());
        this.drawAllChromosomes(g, (int) rec.getWidth(), 0, contentHeight);
        this.hideViewPort = false;
    }

    public int getPreferredHeight() {
        return this.fillShapeList(800);
    }

    public ArrayList<DrawableChromosome> getAllShapes() {
        return allShapes;
    }

    public ChromosomeView(Model model) {
        this.model = model;
        this.allShapes = new ArrayList<DrawableChromosome>();

    }

    /**
     * 
     * @param windowWidth
     * @return int the total height of the image in pixels. Returns a negative value
     *  when it's would be better not to draw this view.
     */
    public int fillShapeList(int windowWidth) {
        this.allShapes = new ArrayList<DrawableChromosome>();
        int yOffset = 0;

        double nucLength = getNucleotideLenght(windowWidth);

        //estimate the number of features to be drawn (without having to
        //iterate them all...
        
        int totalSize = 0;
        for (Entry chrom : model.entries()){
        	Set<DataSource> sources = chrom.annotation.getSources();
        	boolean visibleSource = false;
        	for (DataSource source : sources){
        		if (model.isSourceVisible(source)) visibleSource = true;
        	}
        	if (visibleSource){
        		for (Type type : Type.values()){
        			if (model.isVisibleOnChromosome(type)){
        				List<Feature> features = chrom.annotation.getByType(type, model.getChromosomeLocationVisible());
        				totalSize+= features.size();
        			}
        		}
        	}
        }
        //don't bother iterating all features...
        if (totalSize>=DRAWING_TRESHOLD){
        	return -1;
        }
        

        
        
        for (Entry chrom : model.entries()) {
            int chromThickness = addChromToShapeList(chrom, yOffset, nucLength);
            int chromHeight = chromThickness * SHAPEHEIGHT + (2 * MARGIN);
            yOffset += chromHeight;
        }

        return yOffset;
    }

    private double getNucleotideLenght(int windowWidth) {
        int visLength = 0;
        Location r = model.getChromosomeLocationVisible();
        int chrStart = r.start();
        int chrEnd = r.end();
        visLength = chrEnd - chrStart;

        double nucLength = (double) windowWidth / (double) visLength;
        return nucLength;
    }

    /**
     * 
     * 
     * @param chrom
     * @param yOffset
     * @param windowWidth
     * @return the thickness of the chromosome (over how many tracks the genes
     *         are drawn)
     */
    private int addChromToShapeList(Entry chrom, int yOffset, double nucLength) {
        // initial axis position. Will be shifted after all shapes are created
        int axis = yOffset + SHAPEHEIGHT / 2;

        // create features and genes and store them in a list
        ShapeList shapeList = new ShapeList();
        Vector<List<Location>> occLocations = new Vector<List<Location>>();

        int thickness = 1;
        for (Type term : Type.values()) {
        	if (model.isVisibleOnChromosome(term)) {
        		List<Feature> features = chrom.annotation.getByType(term, model.getChromosomeLocationVisible());
        		for (Feature feat : features) {

        			DataSource source = feat.getSource();
        			if (model.isSourceVisible(source)){

        				int x = (int) ((feat.start() - model.getChromosomeLocationVisible().start()) * nucLength);
        				int y = axis - (SHAPEHEIGHT / 2);
        				int w = (int) (((feat.end()) - feat.start()) * nucLength);

        				/* Create the shape depending on the strand of the feature */
        				Shape featShape;
        				if (feat.strand() == Strand.UNKNOWN) {
        					featShape = new RoundRectangle2D.Double(x, y, w, SHAPEHEIGHT, 10, 10);
        				} else {
        					featShape = new Arrow(x, y, w, SHAPEHEIGHT, feat.strand());

        				}
        				/*
        				 * translate figure when overlapping and chromosome is
        				 * expanded.
        				 */
        				int trackNr = findTrackNr(feat, occLocations);
        				/* Empty middle track for tick marks */
        				if (trackNr >= 0)
        					trackNr++;
        				if (trackNr != 0) {
        					AffineTransform shift = new AffineTransform();
        					int shiftY = (trackNr % 2 == 0) ? (SHAPEHEIGHT) * (trackNr / 2) : (-1) * (SHAPEHEIGHT)
        							* ((trackNr + 1) / 2);
        					shift.translate(0, shiftY);
        					featShape = shift.createTransformedShape(featShape);
        					thickness = Math.max(thickness, trackNr + 1);
        				}

        				/* Add to shape list */
        				Map<String, Object> shapeMap = new HashMap<String, Object>();
        				shapeMap.put("feat", feat);
        				shapeMap.put("color", Configuration.getColor("TYPE_" + term));

        				if (feat.strand() == Strand.UNKNOWN) {
        					shapeMap.put("shapeString", "rbox");

        				} else {
        					shapeMap.put("shapeString", "arrow");

        				}
        				shapeMap.put("shape", featShape);
        				shapeList.add(shapeMap);
        			}
        		}
        	}
        }

        // we now have a list of genes (shapes) relative to an axis.
        // the axis (and the shapes) should now be shifted to have the topmost
        // figure on y=0
        int generalShift = (thickness / 2) * SHAPEHEIGHT + MARGIN;
        for (Map<String, Object> shape : shapeList) {
            AffineTransform tr = new AffineTransform();
            tr.translate(0, generalShift);
            shape.put("shape", tr.createTransformedShape((Shape) shape.get("shape")));
        }

        // add the newly constructed chromosome to the list
        DrawableChromosome dcEntry = new DrawableChromosome();
        dcEntry.thickness = thickness;
        dcEntry.parentChrom = chrom;
        dcEntry.shapeList = shapeList;

        allShapes.add(dcEntry);

        return thickness;
    }

    /**
     * Should only need the allShapes list and Graphics2D object to draw the
     * whole view.
     * 
     * @param screenshot
     *            don't draw any selections when taking a screenshot
     */
    public void drawAllChromosomes(Graphics2D g, int windowWidth, int windowHeight, int contentHeight) {
        int yOffset = 0;
        int colorIndex = 0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (DrawableChromosome dcEntry : allShapes) {

            Color[] backgroundColors = { Color.decode("#CCEEFF"), Color.decode("#CCDDDD") };
            Color backgroundColor = backgroundColors[colorIndex];
            colorIndex = (colorIndex + 1) % backgroundColors.length;

            drawChromosome(dcEntry.parentChrom, dcEntry.shapeList, dcEntry.thickness, yOffset, windowWidth,
                    backgroundColor, g);

            /* Draw annotation viewport on the selected chromosome */
            if (!hideViewPort && dcEntry.parentChrom == model.getSelectedEntry()) {
                if ((model.isStructureVisible() || model.isAnnotationVisible())) {
                    double nucLength = this.getNucleotideLenght(windowWidth);
                    g.setColor(Color.RED);
                    g.drawRect((int) (nucLength * (model.getAnnotationLocationVisible().start() - model
                            .getChromosomeLocationVisible().start())), yOffset, (int) (nucLength * (model
                            .getAnnotationLocationVisible().length())), dcEntry.thickness * SHAPEHEIGHT + (MARGIN * 2)
                            - 1);
                }
            }

            yOffset += dcEntry.thickness * SHAPEHEIGHT + (MARGIN * 2);
        }

    }

    private void drawChromosome(Entry chrom, ShapeList shapeList, int thickness, int yOffset, int windowWidth,
            Color background, Graphics2D g) {
        int chromHeight = thickness * SHAPEHEIGHT + (MARGIN * 2);
        int generalShift = (thickness / 2) * SHAPEHEIGHT + MARGIN;
        int axis = yOffset + SHAPEHEIGHT / 2 + generalShift;
        double nucLength = this.getNucleotideLenght(windowWidth);
        // draw background
        // select a different background color for the active chromosome
        if (chrom.equals(model.getSelectedEntry())) {
            g.setColor(Color.decode("#FFFFCC"));
        } else {
            g.setColor(background);
        }
        g.fillRect(0, yOffset, windowWidth, chromHeight);
        /* Draw chromosome line */
        g.setColor(Color.BLACK);
        float[] dashes = { 5f, 2f };
        g.setColor(Color.DARK_GRAY);
        g.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dashes, 0f));
        g.drawLine(0, axis, (int) (chrom.sequence.size() * nucLength), axis);
        g.setStroke(new BasicStroke());

        /* Draw tick marks */
        int tickCount=6;
        Location l = model.getChromosomeLocationVisible();
        double basesPerTick = (l.length() / tickCount) ;
        for (double i = 0; i <10; i ++) {
            int x = (int) (i * windowWidth / tickCount);
            g.drawLine(x, axis - 5, x, axis + 5);
            g.drawString(""+(int)(l.start()+basesPerTick*i), x, axis-5);
        }

        /* Draw chromosome name in background */
        if (!chrom.equals(model.getHoveredChromEntry())) {
            int nameX = 5;
            int nameY = yOffset + 15;
            g.setColor(Color.GRAY);
            g.drawString("" + chrom.toString(), nameX, nameY);
        }

        for (Map<String, Object> shapeMap : shapeList) {
            Font basicFont = g.getFont();
            Shape drawShape = (Shape) shapeMap.get("shape");
            Rectangle2D bounds = drawShape.getBounds2D();
            Color shapeColor = (Color) shapeMap.get("color");
            Paint shapePaint = new GradientPaint((float) bounds.getCenterX(), (float) bounds.getMinY(), Color.WHITE,
                    (float) bounds.getCenterX(), (float) bounds.getCenterY(), shapeColor);

            Color outline = shapeColor.darker();

            g.setPaint(shapePaint);
            g.fill(drawShape);
            g.setColor(outline);
            g.draw(drawShape);
            if (model.getHoveredChromFeature() != null && model.getHoveredChromFeature().equals(shapeMap.get("feat"))) {
                g.setColor(Color.RED);
                shade(drawShape, g);
            } else if (model.getFeatureSelection() != null
                    && model.getFeatureSelection().contains(shapeMap.get("feat"))) {
                g.setColor(ColorFactory.getShadeColor(shapeColor));
                shade(drawShape, g);
            }

            // draw gene name
            Feature feat = (Feature) shapeMap.get("feat");
            Type typeTerm = feat.type();
            if (model.isShowChromText(typeTerm)) {
                g.setColor(ColorFactory.getTextColor(shapeColor));
                g.setFont(basicFont.deriveFont(8f));

                String displayName = feat.toString();

                TextLayout tl = new TextLayout(displayName, g.getFont(), g.getFontRenderContext());
                Rectangle2D stringBounds = tl.getBounds();

                // rescale the font
                if (bounds.getWidth() < stringBounds.getWidth()) {
                    AffineTransform tx = new AffineTransform();
                    double resize = bounds.getWidth() / stringBounds.getWidth();
                    tx.scale(resize, resize);
                    g.setFont(g.getFont().deriveFont(tx));
                    tl = new TextLayout(displayName, g.getFont(), g.getFontRenderContext());
                    stringBounds = tl.getBounds();

                }

                FontMetrics met = g.getFontMetrics();
                int asc = met.getAscent();
                int stringX = (int) Math.round(bounds.getCenterX() - stringBounds.getWidth() / 2);

                // correction for arrows and triangles
                if (((String) shapeMap.get("shapeString")).equals("arrow")
                        || ((String) shapeMap.get("shapeString")).equals("triangle")) {
                    Strand strand = feat.strand();
                    if (strand == Strand.FORWARD || strand == Strand.UNKNOWN) {
                        stringX -= 5;
                    } else {
                        stringX += 5;
                    }
                }
                int stringY = (int) Math.round(bounds.getCenterY() + asc / 2);
                g.drawString(displayName, stringX, stringY);
            }
            g.setFont(basicFont);
        }

        // draw chromosome name in foreground
        if (chrom.equals(model.getHoveredChromEntry())) {
            int nameX = 5;
            int nameY = yOffset + 15;
            g.setColor(Color.BLACK);
            g.drawString("" + chrom.toString(), nameX, nameY);
        }

    }

    private void shade(Shape drawShape, Graphics2D g) {
        Shape savedClip = g.getClip();
        g.clip(drawShape);
        Rectangle rect = drawShape.getBounds();
        int rectX = (int) rect.getMinX();
        int rectMaxX = (int) rect.getMaxX();
        int rectY = (int) rect.getMinY();
        int rectMaxY = (int) rect.getMaxY();
        for (int i = rectX; i < rectMaxX; i += 2) {
            g.drawLine(i, rectY, i, rectMaxY);
        }
        g.setClip(savedClip);

    }

    private int findTrackNr(Feature f, Vector<List<Location>> occLocations) {
        int trackNr = 0;
        boolean placed = false;
        while (trackNr < occLocations.size() && !placed) {
            List<Location> list = occLocations.get(trackNr);
            Iterator<Location> i = list.iterator();
            boolean overlap = false;
            while (i.hasNext() && !overlap) {
                Location occLoc = (Location) i.next();
                if (f.overlaps(occLoc)) {
                    overlap = true;
                }
            }
            if (!overlap) {
                placed = true;
                list.add(new Location(f.start(), f.end()));
            } else {
                trackNr++;
            }
        }

        if (!placed) {
            List<Location> newList = new ArrayList<Location>();
            newList.add(new Location(f.start(), f.end()));
            occLocations.add(newList);
        }

        return trackNr;
    }

    /**
     * A list of shapes, where a shape is nothing more than a map, containing
     * information to draw the shape (color, shape, related feature, ...)
     * 
     * @author thpar
     * 
     */
    public class ShapeList extends ArrayList<Map<String, Object>> {

        private static final long serialVersionUID = 5897829235035315098L;

    }

    /**
     * A DrawableChromosome contains all information to draw a single chromosome
     * in the chromosome view. It has the list of shapes, refers to the Entry
     * object it's about to draw and the thickness (in number of tracks) of the
     * chrom.
     * 
     * @author thpar
     * 
     */
    public class DrawableChromosome {
        public ShapeList shapeList;

        public int thickness;

        public Entry parentChrom;
    }
}
