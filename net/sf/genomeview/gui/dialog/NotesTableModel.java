/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.util.List;
import java.util.Map.Entry;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import be.abeel.util.Pair;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Feature;
import net.sf.jannot.Qualifier;

class NotesTableModel extends AbstractTableModel implements Observer {

    /**
	 * 
	 */
    private static final long serialVersionUID = -3531717171399285615L;

    private List<Pair<String, String>> list = new Vector<Pair<String, String>>();

    private Model model;

    NotesTableModel(Model model) {
        super();
        this.model = model;

        model.addObserver(this);
        update(null, null);

    }

    private final String[] names = { "Key", "Value", "Delete", "Edit" };

    @Override
    public int getColumnCount() {
        return names.length;
    }

    @Override
    public int getRowCount() {
        return list.size();
    }

//    public Qualifier getQualifier(int row) {
//        return list.get(row);
//    }

    @Override
    public Object getValueAt(int row, int col) {

        switch (col) {
        case 0:
            return list.get(row).x();
        case 1:
            return list.get(row).y();
        case 2:
            return Icons.DELETE;
        case 3:
            return Icons.EDIT;
        default:

            return null;
        }
    }

    private void refresh(Feature rf) {

        list.clear();
        for (String key : rf.getQualifiersKeys()) {
            list.add(new Pair<String,String>(key,rf.qualifier(key)));
        }

    }

    @Override
    public void update(Observable o, Object arg) {
        if (model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() == 1)
            refresh(model.selectionModel().getFeatureSelection().iterator().next());
        fireTableDataChanged();
    }

    @Override
    public Class<?> getColumnClass(int col) {
        switch (col) {

        case 2:
        case 3:
            return Icon.class;
        default:
            return String.class;
        }
    }

    @Override
    public String getColumnName(int col) {
        return names[col];
    }

}