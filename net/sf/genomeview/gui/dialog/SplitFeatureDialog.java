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

public class SplitFeatureDialog extends JDialog {
    private static final long serialVersionUID = -770863087750087961L;

    public SplitFeatureDialog(final Model model) {
    	super(model.getGUIManager().getParent(), "Split feature");
        final SplitFeatureDialog _self = this;
        setModal(true);
        setAlwaysOnTop(true);
        Container c = new Container();
        c.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(3, 3, 3, 3);
        gc.fill = GridBagConstraints.BOTH;

        Feature selected = model.selectionModel().getFeatureSelection().iterator().next();

        final TypeCombo select = new TypeCombo(model);

        select.setSelectedItem(selected.type());

        final JCheckBox remove = new JCheckBox("Delete original feature after split.");

        gc.gridwidth = 2;
        c.add(select, gc);
        gc.gridy++;
        c.add(remove, gc);
        gc.gridy++;
        gc.gridwidth = 1;

        JButton ok = new JButton("OK");
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {

                assert (model.selectionModel().getLocationSelection().size() == 2);
                assert (model.selectionModel().getFeatureSelection().size() == 1);

                Feature f = model.selectionModel().getFeatureSelection().first();
                /* Create left feature */
                SortedSet<Location> upstream = new TreeSet<Location>();
                for (Location l : f.location().headSet(model.selectionModel().getLocationSelection().last())) {
                    upstream.add(l.copy());
                }
                Feature left = f.copy();
                left.setLocation(upstream);
                model.getSelectedEntry().getMemoryAnnotation(left.type()).add(left);////.annotation.add(left);

                /* Create right feature */
                SortedSet<Location> downstream = new TreeSet<Location>();
                for (Location l : f.location().tailSet(model.selectionModel().getLocationSelection().last())) {
                    downstream.add(l.copy());
                }
                Feature right = f.copy();
                right.setLocation(downstream);
                model.getSelectedEntry().getMemoryAnnotation(right.type()).add(right);

                /* Remove original features if requested */
                if (remove.isSelected()) {
                    model.getSelectedEntry().getMemoryAnnotation(f.type()).remove(f);
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
