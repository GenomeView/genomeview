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
import javax.swing.JDialog;
import javax.swing.JFrame;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.StrandCombo;
import net.sf.genomeview.gui.components.TypeCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;

/**
 * Dialog window to create a new feature from a selected Location on the genome.
 * 
 * @author Thomas Abeel
 * 
 */
public class NewFeatureDialog extends JDialog {

    private static final long serialVersionUID = -770863087750087961L;

    public NewFeatureDialog(final Model model) {
    	super(model.getGUIManager().getParent(), MessageManager.getString("newfeature.title"));
        final NewFeatureDialog _self = this;
        setModal(true);
        setAlwaysOnTop(true);
        Container c = new Container();
        c.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.insets = new Insets(3, 3, 3, 3);
        gc.fill = GridBagConstraints.BOTH;

        final TypeCombo typeCombo = new TypeCombo(model);
        typeCombo.setSelectedItem(net.sf.jannot.Type.get("CDS"));
        final StrandCombo strandSelection = new StrandCombo();

        gc.gridwidth = 2;
        c.add(strandSelection, gc);
        gc.gridy++;
        c.add(typeCombo, gc);
        gc.gridwidth = 1;
        gc.gridy++;
        JButton ok = new JButton(MessageManager.getString("button.ok"));
        ok.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                SortedSet<Location> loc = new TreeSet<Location>();
                loc.add(model.getSelectedRegion());
                Feature f = new Feature();
                f.setLocation(loc);
                f.setType(typeCombo.getTerm());
                f.setStrand(strandSelection.getStrand());
                MemoryFeatureAnnotation fa = model.vlm.getSelectedEntry().getMemoryAnnotation(f.type());
				fa.add(f);
				model.selectionModel().setSelectedRegion(null);
                _self.dispose();
                model.updateTracks();

            }

        });
        JButton cancel = new JButton(MessageManager.getString("button.cancel"));
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
        StaticUtils.center(model.getGUIManager().getParent(),this);

    }
}
