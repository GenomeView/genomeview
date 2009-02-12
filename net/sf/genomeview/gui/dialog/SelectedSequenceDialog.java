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

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Sequence;
import net.sf.jannot.Strand;
import net.sf.jannot.utils.SequenceTools;

public class SelectedSequenceDialog extends JDialog {

    private static SelectedSequenceDialog diag = null;

    /**
     * 
     */
    private static final long serialVersionUID = 2529848844411398233L;

    private SelectedSequenceDialog(final Model model) {
        super(model.getParent(), "Which sequence?", true);
        setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 3, 3, 3);
        gc.gridx = 0;
        gc.gridy = 0;
        gc.gridwidth = 2;
        gc.gridheight = 1;
        add(new JLabel("Location or complete feature?"), gc);
        ButtonGroup loc = new ButtonGroup();
        final JRadioButton location = new JRadioButton("Selected location(s)");
        final JRadioButton feature = new JRadioButton("Complete feature(s)");
        feature.setSelected(true);
        loc.add(location);
        loc.add(feature);
        gc.gridy++;
        add(location, gc);
        gc.gridy++;
        add(feature, gc);
        gc.gridy++;
        add(new JLabel("Nucleotides or amino acids?"), gc);

        ButtonGroup trans = new ButtonGroup();
        final JRadioButton nucleotides = new JRadioButton("Nucleotides");
        nucleotides.setSelected(true);
        final JRadioButton aa = new JRadioButton("Amino acids");
        trans.add(nucleotides);
        trans.add(aa);
        gc.gridy++;
        add(nucleotides, gc);
        gc.gridy++;
        add(aa, gc);

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuffer tmp = new StringBuffer();
                Sequence seq = model.getSelectedEntry().sequence;
                if (location.isSelected()) {
                    SortedSet<Location> locs = model.getLocationSelection();
                    for (Location l : locs) {
                        Strand s = l.getParent().strand();
                        switch (s) {
                        case FORWARD:
                        case UNKNOWN:
                            for (int i = l.start(); i <= l.end(); i++) {
                                tmp.append(seq.getNucleotide(i));
                            }

                        case REVERSE:
                            for (int i = l.end(); i >= l.start(); i--) {
                                tmp.append(seq.getReverseNucleotide(i));
                            }
                        }
                    }

                } else {// features...
                    SortedSet<Feature> feats = model.getFeatureSelection();
                    for (Feature f : feats) {
                        tmp.append(SequenceTools.extractSequence(seq, f));

                    }
                }
                if (aa.isSelected()) {
                    selectedSequence = SequenceTools.translate(tmp.toString());
                } else
                    selectedSequence = tmp.toString();
                setVisible(false);
            }

        });
        gc.gridy++;
        gc.gridwidth = 1;
        add(ok, gc);
        JButton close = new JButton("Close");
        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

            }

        });
        gc.gridx++;
        add(close, gc);
        pack();
        StaticUtils.center(this);

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
