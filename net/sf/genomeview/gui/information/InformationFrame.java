/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Dimension;
import java.awt.GridBagConstraints;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.sf.genomeview.data.Model;
import be.abeel.gui.GridBagPanel;

/**
 * Panel with an overview of all available tracks and where they should be
 * visible. Also provides additional information regarding selected items.
 * 
 * This frame spans the right side of the application.
 * 
 * 
 * @author thabe
 * 
 */
public class InformationFrame extends GridBagPanel {

    private static final long serialVersionUID = -8504854026653847566L;

    public InformationFrame(final Model model) {
        gc.fill = GridBagConstraints.BOTH;
        gc.weightx = 1;
        gc.weighty = 1;

        final SourceTrackTable sourceTrackList = new SourceTrackTable(model);
        gc.gridy++;
        gc.weighty = 0;
        add(new JLabel("Data sources"), gc);
        gc.gridy++;
        gc.weighty = 0.5;
        add(new JScrollPane(sourceTrackList), gc);
        gc.gridy++;

        final TrackTable featureTrackList = new TrackTable(model);

        gc.gridy++;
        gc.weighty = 0;
        add(new JLabel("Annotation features"), gc);
        gc.gridy++;
        gc.weighty = 1;
        add(new JScrollPane(featureTrackList), gc);
        gc.gridy++;

        CDSOverviewTable annotationTrackList = new CDSOverviewTable(model);

        gc.weighty = 0;
        add(new JLabel("Gene structures"), gc);
        gc.gridy++;
        gc.weighty = 1;
        add(new JScrollPane(annotationTrackList), gc);
        gc.gridy++;
        gc.weighty = 0;
        add(new JLabel("Details on selected items:"), gc);
        gc.gridy++;
        gc.weighty = 1;
        JPanel detail = new FeatureDetailPanel(model);
        add(new JScrollPane(detail), gc);

        gc.gridy++;
        gc.weighty = 0.3;
        add(new CDSView(model), gc);

        setPreferredSize(new Dimension(250, this.getPreferredSize().height));

    }

}
