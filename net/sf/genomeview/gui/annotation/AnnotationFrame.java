/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JButton;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveRightAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomInAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationZoomOutAction;
import net.sf.jannot.Location;
import be.abeel.gui.GridBagPanel;

/**
 * Frame for annotation. This contains the gene structure panel and the evidence
 * panel.
 * 
 * @author Thomas Abeel
 * 
 */
public class AnnotationFrame extends GridBagPanel implements Observer {

    private static final long serialVersionUID = 1L;

    Model model;

    private GeneEvidenceLabel evidenceLabel;

//    private GeneStructureLabel structureLabel;

    /* The supplied index is the index of the window that this panel is located. */
    public AnnotationFrame(int index, Model model) {
        this.model = model;

        model.addObserver(this);

        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 10;
        gc.gridheight = 1;
        /* toolbar for the annotation frame */
        gc.gridy++;
        gc.gridheight = 1;
        gc.weightx = 0;
        gc.weighty = 0;
        gc.gridx = 0;
        add(new AnnotationFrameToolbar(model), gc);

        /* gene structure and evidence panels */
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 10;
        gc.gridheight = 1;
        gc.gridy++;
//        structureLabel = new GeneStructureLabel(model);
        evidenceLabel = new GeneEvidenceLabel(model);
        Container c = new Container();
        c.setLayout(new BorderLayout());
//        c.add(structureLabel, BorderLayout.NORTH);
        c.add(evidenceLabel, BorderLayout.CENTER);
        JScrollPane tmp = new JScrollPane(c);
        tmp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        add(tmp, gc);
        gc.gridy++;

        gc.weighty = 0;
        add(new Slider(model), gc);

    }

    class AnnotationFrameToolbar extends Container {

        private static final long serialVersionUID = -6657071163569615808L;

        public AnnotationFrameToolbar(Model model) {
            super();
            setLayout(new GridBagLayout());
            GridBagConstraints cgc = new GridBagConstraints();
            cgc.fill = GridBagConstraints.BOTH;
            cgc.insets = new Insets(0, 3, 3, 3);
            cgc.gridx = 0;
            cgc.gridy = 0;
            cgc.gridheight = 2;
            cgc.weightx = 1;
            cgc.weighty = 0;
            add(new CDSView(model), cgc);

            cgc.gridheight = 1;
            cgc.gridx++;
            cgc.weightx = 0;
            add(new JButton(new AnnotationZoomInAction(model)), cgc);
            cgc.gridx++;
            add(new JButton(new AnnotationZoomOutAction(model)), cgc);
            cgc.gridy++;

            cgc.gridx--;
            add(new JButton(new net.sf.genomeview.gui.menu.navigation.AnnotationMoveLeftAction(model)), cgc);
            cgc.gridx++;
            add(new JButton(new AnnotationMoveRightAction(model)), cgc);
            cgc.gridx++;

        }
    }

    /**
     * Encapsulation for the slider bar
     * 
     * @author Thomas Abeel
     * 
     */
    class Slider extends GridBagPanel implements Observer, ChangeListener {
        /**
         * 
         */
        private static final long serialVersionUID = 1L;

        private Model model;

        private BoundedRangeModel brm;

        private JScrollBar scrollbar;

        public Slider(Model model) {
            gc.weightx = 1;
            gc.insets = new Insets(0, 0, 0, 0);
            gc.fill = GridBagConstraints.BOTH;
            this.model = model;
            model.addObserver(this);
            brm = new DefaultBoundedRangeModel();
            brm.addChangeListener(this);
            scrollbar = new JScrollBar();
            scrollbar.setModel(brm);

            scrollbar.setOrientation(JScrollBar.HORIZONTAL);
            // scrollbar.addAdjustmentListener(this);
            add(scrollbar, gc);
        }

        /**
         * Called when the model updates.
         * 
         */
        public void update(Observable arg0, Object arg1) {
            Location r = model.getAnnotationLocationVisible();
            brm.setRangeProperties(r.start(), r.end() - r.start(), 0, model.getSelectedEntry().sequence.size(), false);
            scrollbar.setBlockIncrement((r.end() - r.start()) / 4);
            scrollbar.setUnitIncrement((r.end() - r.start()) / 20);

        }

        /**
         * Called when the slider bar is updated.
         */
        public void stateChanged(ChangeEvent e) {
            model.deleteObserver(this);
            model.setAnnotationLocationVisible(new Location(brm.getValue(), brm.getValue() + brm.getExtent()));
            model.addObserver(this);

        }

    }

    @Override
    public void update(Observable arg0, Object arg1) {
        if (!model.isAnnotationVisible() && !model.isStructureVisible()) {
            this.setVisible(false);
        } else {
            this.setVisible(true);
        }
        this.repaint();
    }

}
