/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;

public class MergeFeatureDialog extends JDialog {
    private static final long serialVersionUID = -770863087750087961L;

    public MergeFeatureDialog(final Model model) {
    	super(model.getParent(), "Merge features");
        final MergeFeatureDialog _self = this;
        setModal(true);
        setAlwaysOnTop(true);
        Container c = new Container();
        c.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(3, 3, 3, 3);
        gc.fill = GridBagConstraints.BOTH;

        final TypeCombo select = new TypeCombo(model);
        select.setSelectedItem(model.selectionModel().getFeatureSelection().first().type());

        gc.gridwidth = 2;

        c.add(select, gc);
        final JCheckBox remove = new JCheckBox("Delete features after merger.");
        gc.gridy++;
        c.add(remove, gc);
        gc.gridwidth = 1;
        gc.gridy++;
        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                SortedSet<Location> locations = new TreeSet<Location>();
                /* Create a deep copy of all locations */
                for (Feature rf : model.selectionModel().getFeatureSelection()) {
                    for (Location l : rf.location())
                        locations.add(l.copy());

                }
                /*
                 * Construct a deep copy of the first feature, this one will be
                 * used as a frame for the new feature.
                 */
                Feature copy = model.selectionModel().getFeatureSelection().first().copy();

                copy.setLocation(locations);
                /* Add new feature to the annotation */
                ((MemoryFeatureAnnotation)model.getSelectedEntry().get(copy.type())).add(copy);

                /* If requested, the original features are deleted */
                if (remove.isSelected()) {
                    Set<Feature> toRemove = new HashSet<Feature>();
                    toRemove.addAll(model.selectionModel().getFeatureSelection());
                    for (Feature rf : toRemove)
                    	((MemoryFeatureAnnotation)model.getSelectedEntry().get(rf.type())).remove(rf);
                }
                model.selectionModel().clearLocationSelection();

                _self.dispose();

            }

        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                _self.dispose();

            }

        });
        c.add(ok, gc);
        gc.gridx++;
        c.add(cancel, gc);
        setContentPane(c);
        pack();
        StaticUtils.center(this);

    }

}
