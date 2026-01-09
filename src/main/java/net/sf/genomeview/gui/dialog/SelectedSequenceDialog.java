/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.SortedSet;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JRadioButton;

import net.sf.genomeview.BufferSeq;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.refseq.MemorySequence;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

public class SelectedSequenceDialog extends JDialog {

    private static SelectedSequenceDialog diag = null;

    /**
     * 
     */
    private static final long serialVersionUID = 2529848844411398233L;

    private SelectedSequenceDialog(final Model model) {
        super(model.getGUIManager().getParent(), MessageManager.getString("selectedsequencedialog.which_seq"), true);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 3, 3, 3);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        add(new JLabel(MessageManager.getString("selectedsequencedialog.location_or_complete")), gc);
        ButtonGroup loc = new ButtonGroup();
        final JRadioButton location = new JRadioButton(MessageManager.getString("selectedsequencedialog.selected_locations"));
        final JRadioButton feature = new JRadioButton(MessageManager.getString("selectedsequencedialog.completed_features"));
        feature.setSelected(true);
        loc.add(location);
        loc.add(feature);
        gc.gridy++;
        add(location, gc);
        gc.gridy++;
        add(feature, gc);
        gc.gridy++;
        add(new JLabel(MessageManager.getString("selectedsequencedialog.nucleotides_or_amino")), gc);

        ButtonGroup trans = new ButtonGroup();
        final JRadioButton nucleotides = new JRadioButton(MessageManager.getString("selectedsequencedialog.nucleotides"));
        nucleotides.setSelected(true);
        final JRadioButton aa = new JRadioButton(MessageManager.getString("selectedsequencedialog.amino"));
        trans.add(nucleotides);
        trans.add(aa);
        gc.gridy++;
        add(nucleotides, gc);
        gc.gridy++;
        add(aa, gc);

        JButton ok = new JButton(MessageManager.getString("button.ok"));
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuffer tmp = new StringBuffer();
                Sequence seq = model.vlm.getSelectedEntry().sequence();
                
               
                
                if (location.isSelected()) {
                    SortedSet<Location> locs = model.selectionModel().getLocationSelection();
                    for (Location l : locs) {
                    	 BufferSeq bs=new BufferSeq(seq,l);
                        Strand s = l.getParent().strand();
                        switch (s) {
                        case FORWARD:
                        case UNKNOWN:
                            for (int i = l.start(); i <= l.end(); i++) {
                                tmp.append(bs.getNucleotide(i));
                            }
                            break;
                        case REVERSE:
                            for (int i = l.end(); i >= l.start(); i--) {
                                tmp.append(bs.getReverseNucleotide(i));
                            }
                            break;
                        }
                    }

                } else {// features...
                    SortedSet<Feature> feats = model.selectionModel().getFeatureSelection();
                    for (Feature f : feats) {
                        tmp.append(SequenceTools.extractSequence(seq, f));

                    }
                }
                
                if (aa.isSelected()) {
                    selectedSequence = SequenceTools.translate(new MemorySequence(tmp),model.getAAMapping());
                } else
                    selectedSequence = tmp.toString();
                setVisible(false);
            }

        });
        gc.gridy++;
        gc.gridwidth = 1;
        add(ok, gc);
        JButton close = new JButton(MessageManager.getString("button.close"));
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

            }

        });
        gc.gridx++;
        add(close, gc);
        pack();
        StaticUtils.center(model.getGUIManager().getParent(),this);

    }

    private static String selectedSequence;

    public static String getSequence() {
        return selectedSequence;
    }

    public static void display(Model model) {
        selectedSequence = null;
        if (diag == null)
            diag = new SelectedSequenceDialog(model);
        diag.setVisible(true);
    }

}
