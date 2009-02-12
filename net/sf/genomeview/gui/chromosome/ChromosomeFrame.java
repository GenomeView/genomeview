/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.BoxLayout;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JWindow;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.chromosome.ChromosomeView.DrawableChromosome;
import net.sf.genomeview.gui.chromosome.ChromosomeView.ShapeList;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeMoveLeftAction;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeMoveRightAction;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeOverviewAction;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeViewExportJPGAction;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeViewExportPNGAction;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeZoomInAction;
import net.sf.genomeview.gui.chromosome.actions.ChromosomeZoomOutAction;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import be.abeel.gui.GridBagPanel;

/**
 * 
 * Frame that display an chromosome level overview of the available data.
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
public class ChromosomeFrame extends GridBagPanel implements Observer {

    private static final long serialVersionUID = 7548803933643552361L;

    private Model model;

    private JLabel geneLabel;

    // private JLabel warningLabel;

    private ChromosomeView featureView;

    private FeatureInfoWindow floatingWindow;

    // public class WarningPaintLabel extends JLabel{
    //    	
    // @Override
    // public void paintComponent(Graphics gg) {
    //    		
    // }
    // }

    public class GenePaintLabel extends JLabel implements MouseListener, MouseMotionListener {
        private static final long serialVersionUID = -6623601888195462780L;

        private JPopupMenu popup;

        public GenePaintLabel() {
            this.addMouseListener(this);
            this.addMouseMotionListener(this);

            popup = new JPopupMenu();
            popup.add(new ChromosomeViewExportPNGAction(featureView, model));
            popup.add(new ChromosomeViewExportJPGAction(featureView, model));

            Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
            this.setPreferredSize(new Dimension(this.getPreferredSize().width, (int) (d.getHeight() * 0.2)));
        }

        /**
         * Paint the chromosomes on the label
         */
        public void paintComponent(Graphics gg) {

            int windowWidth = this.getParent().getWidth();
            int windowHeight = this.getParent().getHeight();

            // fill shape list calculates the height and reads all shapes!
            int totalHeight = 0;
            totalHeight = featureView.fillShapeList(windowWidth);

            if (totalHeight >= 0) {

                this.setPreferredSize(new Dimension(windowWidth, totalHeight));
                this.revalidate();
                // actually draw the view
                Graphics2D g = (Graphics2D) gg;
                featureView.drawAllChromosomes(g, windowWidth, windowHeight, totalHeight);
            } else {
                // TODO replace this by a density map
                gg.drawString("Too much to draw", 20, 20);
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            // catch double clicks
            if (e.getClickCount() == 2) {
                this.doubleClickFeatureView(e);
            }
        }

        public void doubleClickFeatureView(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                Hits hits = this.getHits(e.getX(), e.getY());
                adjustClickSelection(hits);
                if (hits.isFeatureHit()) {
                    Feature feat = hits.getFeatureHits().get(0);
                    int l = feat.length();
                    model.setAnnotationLocationVisible(new Location(feat.start() - (l / 20), feat.end() + (l / 20)));
                } else if (hits.isTrackHit()) {
                    Location visibleChrom = model.getChromosomeLocationVisible();
                    // visible width in pix
                    int visibleWidthPix = this.getWidth();
                    // how many nucleotides are represented by a pixel
                    double pixWidth = visibleChrom.length() / visibleWidthPix;
                    // number of nucleotides left of the click
                    int nucs = (int) Math.round(pixWidth * e.getX());
                    model.center(visibleChrom.start() + nucs);
                }
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {
        }

        @Override
        public void mouseExited(MouseEvent e) {
            model.setHoverChromEntry(null);
            model.setHoveredChromFeature(null);
            floatingWindow.setVisible(false);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
            mousePressedFeatureView(e);
        }

        public void mousePressedFeatureView(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                Hits hits = this.getHits(e.getX(), e.getY());
                this.adjustClickSelection(hits);
            }
        }

        private void adjustClickSelection(Hits hits) {
            if (hits.isFeatureHit()) {
                model.setLocationSelection(hits.getFeatureHits().get(0));
            } else {
                model.clearLocationSelection();
            }
            if (hits.isTrackHit()) {
                // map tracknr to chromosome and select
                if (!model.getSelectedEntry().equals(hits.getTrackHit())) {
                    model.setSelectedEntry(hits.getTrackHit());
                }
            }

        }

        private Hits getHits(int x, int y) {
            Hits hits = new Hits();
            DrawableChromosome entryHit = getTrackHits(hits, y);
            if (entryHit != null) {
                getFeatureHits(hits, entryHit, x, y);
            }
            return hits;
        }

        private void getFeatureHits(Hits hits, DrawableChromosome entryHit, int x, int y) {
            Point point = new Point(x, y);
            ShapeList shapeList = entryHit.shapeList;

            for (Map<String, Object> shapeMap : shapeList) {
                Shape shape = (Shape) shapeMap.get("shape");
                if (shape.contains(point)) {
                    hits.addFeatureHit((Feature) shapeMap.get("feat"));
                    break;
                }
            }

        }

        private DrawableChromosome getTrackHits(Hits hits, int y) {
            int trackStart = 0;
            int trackEnd = 0;
            DrawableChromosome entryHit = null;
            for (DrawableChromosome dcEntry : featureView.getAllShapes()) {
                int trackThickness = dcEntry.thickness * ChromosomeView.SHAPEHEIGHT + ChromosomeView.MARGIN * 2;
                trackEnd = trackStart + trackThickness;
                if (y >= trackStart && y < trackEnd) {
                    entryHit = dcEntry;
                    hits.setTrackHit(dcEntry.parentChrom);
                    break;
                }
                trackStart = trackEnd;
            }
            return entryHit;
        }

        public class Hits {
            Entry track = null;

            private List<Feature> hitFeats = new ArrayList<Feature>();

            public Entry getTrackHit() {
                return track;
            }

            public boolean isTrackHit() {
                return track != null;
            }

            public void setTrackHit(Entry track) {
                this.track = track;
            }

            public void addFeatureHit(Feature feat) {
                this.hitFeats.add(feat);
            }

            public List<Feature> getFeatureHits() {
                return hitFeats;
            }

            public boolean isFeatureHit() {
                return this.hitFeats.size() > 0;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);

        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(), e.getX(), e.getY());
            }

        }

        @Override
        public void mouseDragged(MouseEvent e) {
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            Hits hits = getHits(e.getX(), e.getY());
            if (hits.isTrackHit()) {
                model.setHoverChromEntry(hits.getTrackHit());
            } else {
                model.setHoverChromEntry(null);
            }
            if (hits.isFeatureHit()) {
                Feature feat = hits.getFeatureHits().get(0);
                model.setHoveredChromFeature(feat);
                floatingWindow.setLocation(e.getXOnScreen() + 5, e.getYOnScreen() + 5);
                floatingWindow.setDisplayName(feat.toString());
                floatingWindow.setDisplayStart(feat.start());
                floatingWindow.setDisplayEnd(feat.end());
                floatingWindow.setVisible(true);
            } else {
                model.setHoveredChromFeature(null);
                floatingWindow.setVisible(false);
            }
        }

    }

    public class FeatureInfoWindow extends JWindow {
        private static final long serialVersionUID = 1L;

        private String displayName = new String();

        private int displayStart = 0;

        private int displayEnd = 0;

        JLabel floater = new JLabel();

        public FeatureInfoWindow() {
            floater.setBackground(Color.GRAY);
            floater.setForeground(Color.BLACK);
            Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
            Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
            floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));

            adjustText();
            add(floater);
            pack();
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
            adjustText();
        }

        private void adjustText() {
            String text = new String();
            text = "<html>";
            text += "Name : " + displayName + "<br />";
            text += "Start : " + displayStart + "<br />";
            text += "End : " + displayEnd + "<br />";
            text += "</html>";
            floater.setText(text);
            this.pack();
        }

        public int getDisplayStart() {
            return displayStart;
        }

        public void setDisplayStart(int displayStart) {
            this.displayStart = displayStart;
            adjustText();
        }

        public int getDisplayEnd() {
            return displayEnd;
        }

        public void setDisplayEnd(int displayEnd) {
            this.displayEnd = displayEnd;
            adjustText();
        }

    }

    public ChromosomeFrame(Model model) {
        this.model = model;
        model.addObserver(this);

        featureView = new ChromosomeView(model);

        floatingWindow = new FeatureInfoWindow();

        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;
        gc.gridheight = 1;

        geneLabel = new GenePaintLabel();
        // warningLabel = new WarningPaintLabel();

        this.scrollPane = new JScrollPane(geneLabel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, gc);

        gc.weightx = 0;
        gc.weighty = 0;
        gc.gridx++;

        Container buttonContainer = new Container();
        buttonContainer.setLayout(new GridBagLayout());
        GridBagConstraints cgc = new GridBagConstraints();
        cgc.insets = new Insets(0, 3, 3, 3);
        cgc.gridx = 0;
        cgc.gridy = 0;
        cgc.fill = GridBagConstraints.BOTH;
        cgc.weightx = 1;
        cgc.weighty = 0;

        gc.gridwidth = 1;
        buttonContainer.add(new JButton(new ChromosomeZoomInAction(model)), cgc);
        cgc.gridx++;
        buttonContainer.add(new JButton(new ChromosomeZoomOutAction(model)), cgc);
        cgc.gridy++;
        cgc.gridx--;
        cgc.gridwidth = 2;
        buttonContainer.add(new JButton(new ChromosomeOverviewAction(model)), cgc);
        cgc.gridy++;
        cgc.gridwidth = 1;
        buttonContainer.add(new JButton(new ChromosomeMoveLeftAction(model)), cgc);
        cgc.gridx++;
        buttonContainer.add(new JButton(new ChromosomeMoveRightAction(model)), cgc);
        cgc.gridx--;

        // place holder to keep the buttons at the top. (not needed)
        cgc.gridy++;
        cgc.weighty = 1;
        buttonContainer.add(new JLabel(), cgc);

        gc.gridheight = 5;
        add(buttonContainer, gc);
        gc.gridheight = 1;
        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 1;

        add(new Slider(model), gc);

    }

    private JScrollPane scrollPane;

    /**
     * Called when model is changed...
     */
    @Override
    public void update(Observable o, Object arg) {

        if (model.isChromosomeVisible() != this.isVisible()) {
            this.setVisible((model.isChromosomeVisible()));
            if (this.isVisible()) {
                JSplitPane splitPane = (JSplitPane) scrollPane.getParent().getParent();
                splitPane.setTopComponent(this);
                this.revalidate();
            }
        }

        // if (model.isChromosomeDrawable()){
        // this.scrollPane.setViewportView(geneLabel);
        // } else {
        // this.scrollPane.setViewportView(warningLabel);
        // }

        repaint();
    }

    public class Slider extends JPanel implements Observer, ChangeListener {

        private static final long serialVersionUID = -1308352169737074308L;

        private Model model;

        private BoundedRangeModel brm;

        public Slider(Model model) {
            this.model = model;
            brm = new DefaultBoundedRangeModel();
            brm.addChangeListener(this);

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            JScrollBar scrollbar = new JScrollBar();
            scrollbar.setOrientation(JScrollBar.HORIZONTAL);
            scrollbar.setModel(brm);
            model.addObserver(this);

            add(scrollbar);
        }

        /**
         * Is called when the slider is moved.
         */
        @Override
        public void stateChanged(ChangeEvent e) {
            model.deleteObserver(this);
            model.setChromosomeLocationVisible(new Location(brm.getValue(), brm.getValue() + brm.getExtent()));
            model.addObserver(this);

        }

        /**
         * Is called when the Model changes.
         */
        @Override
        public void update(Observable o, Object arg) {
            Location r = model.getChromosomeLocationVisible();
            brm.setRangeProperties(r.start(), r.end() - r.start(), 0, model.getLongestChromosomeLength(), false);
        }

    }

}
