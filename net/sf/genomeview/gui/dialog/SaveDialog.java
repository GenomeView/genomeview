/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.task.WriteEntriesWorker;
import net.sf.jannot.Entry;
import net.sf.jannot.source.DataSource;

public class SaveDialog extends JDialog {

    private static final long serialVersionUID = -5209291628487502687L;

    class DataSourceCheckbox extends JCheckBox {

        private static final long serialVersionUID = -208816638301437642L;

        private DataSource data;

        public DataSourceCheckbox(DataSource data) {
            super(data.toString());
            this.data = data;
        }

    }

    private SaveDialog(final Model model) {
        super(model.getGUIManager().getParent(), "Save dialog", true);
        setLayout(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(3, 3, 3, 3);
        gc.gridwidth = 2;
        gc.gridheight = 1;
        gc.gridx = 0;
        gc.gridy = 0;

        final ArrayList<DataSourceCheckbox> dss = new ArrayList<DataSourceCheckbox>();
        add(new JLabel("Select sources to save"), gc);
        gc.gridy++;
        for (DataSource ds : model.loadedSources()) {
            DataSourceCheckbox dsb = new DataSourceCheckbox(ds);
            dss.add(dsb);

            add(dsb, gc);
            gc.gridy++;
        }

        JButton save = new JButton("Save");
        JButton close = new JButton("Close");

        gc.gridwidth = 1;
        gc.gridy++;
        add(save, gc);
        gc.gridx++;
        add(close, gc);

//        save.addActionListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//
//                for (DataSourceCheckbox dsb : dss) {
//                    if (dsb.isSelected()) {
//                        boolean continueSave = true;
//                        if (dsb.data.isDestructiveSave()) {
//                            int result = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(), "Overwrite existing file?\n"
//                                    + dsb.data, "Overwrite?", JOptionPane.YES_NO_OPTION);
//                            if (result != JOptionPane.YES_OPTION)
//                                continueSave = false;
//                        }
//
//                        if (continueSave) {
//                            boolean seqSaved = false;
//                            for (Entry en : model.entries()) {
//                                if (en.defaultSource.equals(dsb.data))
//                                    seqSaved = true;
//
//                            }
//                            if (!seqSaved) {
//                                int result = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
//                                        "Save source without sequence?", "No sequence!", JOptionPane.YES_NO_OPTION);
//                                if (result != JOptionPane.YES_OPTION)
//                                    continueSave = false;
//                            }
//                        }
//                        setVisible(false);
//                        if (continueSave) {
//                        	WriteEntriesWorker rw = new WriteEntriesWorker(dsb.data, model);
//                            rw.execute();
//                        } else {
//                            JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "Save aborted!");
//                        }
//
//                    }
//                }
//
//            }
//
//        });

        close.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);

            }

        });

        StaticUtils.center(this);
        pack();
        setVisible(true);
    }

    public static void display(Model model) {
        new SaveDialog(model);

    }

}
