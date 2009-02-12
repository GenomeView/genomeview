/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.components.StrandCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import be.abeel.gui.GridBagPanel;

/**
 * JFrame that allows to edit a CDS using a table view.
 * 
 * @author Thomas Abeel
 * 
 */
public class EditFeatureWindow extends JDialog implements Observer {

    private static final long serialVersionUID = -8014200598699402818L;

    private final Model model;

    private final JDialog _self;

    class TableModel extends AbstractTableModel {
        private static final long serialVersionUID = -6417322941515446991L;

        private JComboBox strandSelection;

        public TableModel(JComboBox strandSelection) {
            this.strandSelection = strandSelection;
        }

        private List<Location> location = new ArrayList<Location>();

        public void removeRow(int i) {
            location.remove(i);
            fireTableDataChanged();
        }

        public int getColumnCount() {
            return 2;
        }

        /*
         * Don't need to implement this method unless your table's editable.
         */
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        public void setValueAt(Object value, int row, int col) {
            if (col == 0)
                location.get(row).setStart(Integer.parseInt(value.toString()));
            else
                location.get(row).setEnd(Integer.parseInt(value.toString()));
        }

        public int getRowCount() {
            return location.size();

        }

        /**
         * Will refresh the table model with the newly selected Feature.
         */
        private void refresh(Feature rf) {
            location.clear();
            if (rf != null) {
                for (Location l : rf.location()) {
                    location.add(l.copy());
                }
                Strand s = rf.strand();
                if (s == Strand.FORWARD) {
                    strandSelection.setSelectedItem("forward");
                } else
                    strandSelection.setSelectedItem("reverse");
            }

            fireTableDataChanged();

        }

        public Object getValueAt(int row, int column) {

            if (column == 0)
                return location.get(row).start();
            else
                return location.get(row).end();

        }

        public void addRow(int i) {
            if (i < 0)
                i = 0;
            location.add(i, new Location(0, 0));

            fireTableDataChanged();

        }

        public void removeRows(int[] selectedRows) {
            System.out.println("SelectedRows=" + selectedRows.length);
            Arrays.sort(selectedRows);
            for (int i = selectedRows.length - 1; i >= 0; i--) {
                location.remove(selectedRows[i]);

            }
            fireTableDataChanged();

        }

    }

    class EditStructureContent extends GridBagPanel {
        void refresh() {
            tm.refresh(model.getFeatureSelection().iterator().next());
        }

        private TableModel tm;

        private static final long serialVersionUID = -6910194246570017601L;

        public EditStructureContent(final Model model) {

            add(new JLabel("Strand: "), gc);
            gc.gridx++;
            final StrandCombo strandSelection = new StrandCombo();
            strandSelection.setSelectedItem(model.getFeatureSelection().first().strand());
            add(strandSelection, gc);
            gc.gridx = 0;
            gc.gridy++;

            gc.gridwidth = 4;
            // field = new JTextField("test");

            tm = new TableModel(strandSelection);
            final JTable jt = new JTable(tm);
            jt.setPreferredSize(new Dimension(400, 400));
            add(new JScrollPane(jt), gc);

            JButton addTop = new JButton("Insert top");
            addTop.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    tm.addRow(0);

                }

            });

            JButton addSelection = new JButton("Insert selection");
            addSelection.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    tm.addRow(jt.getSelectedRow());

                }

            });

            JButton addBottom = new JButton("Insert bottom");
            addBottom.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    tm.addRow(jt.getRowCount());

                }

            });

            JButton removeButton = new JButton("Remove selected");
            removeButton.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent e) {
                    if (jt.getSelectedRow() >= 0) {
                        tm.removeRows(jt.getSelectedRows());
                    }

                }

            });

            // JButton addSelection=new JButton("Start");

            JButton ok = new JButton("Save & Close");
            ok.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {

                    SortedSet<Location> loc = new TreeSet<Location>();
                    loc.addAll(tm.location);

                    assert (model.getFeatureSelection().size() == 1);
                    Feature f = model.getFeatureSelection().first();
                    System.out.println("Selected feature: " + f);
                    _self.setVisible(false);
                    f.setLocation(loc);
                    f.setStrand(strandSelection.getStrand());

                 
                }

            });

            JButton cancel = new JButton("Close");
            cancel.addActionListener(new ActionListener() {

                public void actionPerformed(ActionEvent arg0) {
                    _self.setVisible(false);

                }

            });
            gc.gridwidth = 1;
            gc.gridy++;
            add(addTop, gc);
            gc.gridx++;
            add(addSelection, gc);
            gc.gridx++;
            add(addBottom, gc);
            gc.gridx++;
            add(removeButton, gc);

            gc.gridx = 0;
            gc.gridwidth = 2;
            gc.gridy++;
            add(ok, gc);
            gc.gridx += 2;
            // gc.gridwidth = 1;
            add(cancel, gc);

        }

    }

    EditStructureContent ec;

    EditFeatureContent etc;

    public EditFeatureWindow(Model model) {
        super((JFrame) model.getParent(), "Edit structure");
        _self = this;
        setModal(true);
        this.model = model;
        JTabbedPane pane = new JTabbedPane();
        ec = new EditStructureContent(model);
        etc = new EditFeatureContent(model, this);
        pane.add("Information", etc);
        pane.add("Structure", ec);
        this.setContentPane(pane);
        this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        pack();

    }

    public void setVisible(boolean b) {
        if (b) {
            // this.selectedFeature = model.getSelectedFeature();
            model.addObserver(this);
            ec.refresh();
            etc.refresh();

        } else {
            model.deleteObserver(this);
        }

        super.setVisible(b);
    }

    public void update(Observable arg0, Object arg1) {
        // this.selectedFeature = ;
        ec.refresh();
        etc.refresh();
    }

}
