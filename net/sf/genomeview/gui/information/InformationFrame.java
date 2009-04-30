/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.plugin.IValueFeature;

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

//        final ValueTrackListModel valueTrackModel = new ValueTrackListModel(model);
//        final JTable valueTrackList = new JTable(valueTrackModel);
//
//        valueTrackList.setCellSelectionEnabled(false);
//        valueTrackList.setRowSelectionAllowed(true);
//        valueTrackList.setColumnSelectionAllowed(false);
//        System.out.println(Arrays.toString(valueTrackList.getMouseListeners()));
//        valueTrackList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//        valueTrackList.addMouseListener(new MouseAdapter() {
//            public void mouseReleased(MouseEvent e) {
//
//                System.out.println("Selected: " + valueTrackList.getSelectedColumn() + "\t"
//                        + valueTrackList.getSelectedRow());
//                int column = valueTrackList.getSelectedColumn();
//                int row = valueTrackList.getSelectedRow();
//                // String name = (String) valueTrackList.getValueAt(row, 0);
//                IValueFeature vFeature = valueTrackModel.getValueFeature(row);
//
//                if (column == 1) {
//
//                    switch (model.getValueFeatureDisplayType(vFeature)) {
//                    case MultiLineBlocks:
//                        model.setValueFeatureDisplayType(vFeature, DisplayType.OneLineBlocks);
//                        break;
//                    case LineProfile:
//                        model.setValueFeatureDisplayType(vFeature, DisplayType.ColorCodingProfile);
//                        break;
//                    case OneLineBlocks:
//                        model.setValueFeatureDisplayType(vFeature, DisplayType.MultiLineBlocks);
//                        break;
//                    case ColorCodingProfile:
//                        model.setValueFeatureDisplayType(vFeature, DisplayType.LineProfile);
//                        break;
//                    }
//                }
//                if (column == 2) {
//                    model.setValueFeatureVisible(vFeature, !model.isValueFeatureVisible(vFeature));
//                }
//
//            }
//
//        });

        gc.weighty = 0;
        add(new JLabel("Details on selected items:"), gc);
        gc.gridy++;
        gc.weighty = 1;
        JPanel detail = new FeatureDetailPanel(model);
        add(new JScrollPane(detail), gc);

        gc.gridy++;
        gc.weighty = 0.5;
//        add(new JLabel("Prediction features"), gc);
//        gc.gridy++;
//        gc.weighty = 0.5;
//        add(new JScrollPane(valueTrackList), gc);

        setPreferredSize(new Dimension(250, this.getPreferredSize().height));

    }

}
