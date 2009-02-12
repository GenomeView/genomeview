/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Feature;
import net.sf.jannot.Type;
import be.abeel.gui.GridBagPanel;

class EditFeatureContent extends GridBagPanel {

    /**
     * 
     */
    private static final long serialVersionUID = -5075333314536998112L;

    JComboBox term = new JComboBox();

    private Model model;

    public EditFeatureContent(final Model model, final EditFeatureWindow efw) {
        // test = editFeatureWindow;
        this.model = model;

        for (Type ct : Type.values()) {
            term.addItem(ct);

        }
        add(term, gc);
        gc.gridy++;
        gc.weightx = 1;
        gc.weighty = 1;
        final NotesTableModel notesModel = new NotesTableModel(model);
        final JTable notesTable = new JTable(notesModel);
        notesTable.getColumnModel().getColumn(0).setCellRenderer(new TermCellRenderer());
        notesTable.setCellSelectionEnabled(false);
        notesTable.setRowSelectionAllowed(false);
        notesTable.setColumnSelectionAllowed(false);
        notesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        notesTable.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                System.out.println("Selected: " + notesTable.getSelectedColumn() + "\t" + notesTable.getSelectedRow());
                int column = notesTable.getSelectedColumn();
                int row = notesTable.getSelectedRow();

                if (column == 2) {
                    model.getFeatureSelection().first().removeQualifier(notesModel.getQualifier(row));

                    // model.getSelectedEntry().annotation.
                    // model.removeNote(model.getFeatureSelection().iterator().
                    // next(), notesModel.getNote(row));
                }
                if (column == 3) {
                    NoteDialog.showDialog(model, notesModel.getQualifier(row));
                }

            }
        });

        add(new JScrollPane(notesTable), gc);
        gc.gridy++;
        gc.weightx = 0;
        gc.weighty = 0;
        JButton addNote = new JButton("Add note");
        addNote.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                NoteDialog.showDialog(model, null);

            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));

        JButton save = new JButton("Save and close");

        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {

                Feature feat = model.getFeatureSelection().iterator().next();

                Type currentTerm = feat.type();
                Type newTerm = (Type) term.getSelectedItem();

                if (!currentTerm.equals(newTerm)) {
                    feat.setType(newTerm);
                    // model.changeTerm(feat, newTerm);
                }
                // model.commitGroupChange();

                efw.setVisible(false);
                model.refresh();

            }

        });

        JButton cancel = new JButton("Cancel changes");
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // model.rollbackGroupChange();
                efw.setVisible(false);
            }

        });

        buttonPanel.add(Box.createHorizontalGlue());
        buttonPanel.add(addNote);
        buttonPanel.add(Box.createRigidArea(new Dimension(30, 0)));
        buttonPanel.add(cancel);
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPanel.add(save);

        add(buttonPanel, gc);

    }

    public void refresh() {
        if (model.getFeatureSelection().size() == 1) {
            term.setSelectedItem(model.getFeatureSelection().first().type());

        }

    }

    public class TermCellRenderer extends DefaultTableCellRenderer {

        private static final long serialVersionUID = 2498297270179194194L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int rowIndex, int vColIndex) {
            String text = value.toString();
            ;
            return super.getTableCellRendererComponent(table, text, isSelected, hasFocus, rowIndex, vColIndex);

        }
    }

}