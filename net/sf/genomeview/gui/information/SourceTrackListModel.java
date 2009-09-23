/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.jannot.source.DataSource;

public class SourceTrackListModel extends AbstractTableModel implements Observer {

    @Override
    public String getColumnName(int column) {
        switch (column) {
        case 0:
            return "Data source";
        case 1:
            return "Visible";
        default:
            return "no name";

        }
    }

    private static final long serialVersionUID = 1029669417216038805L;

    private Model model;

    public SourceTrackListModel(Model model) {
        model.addObserver(this);
        this.model = model;
        update(null, null);
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public int getRowCount() {
        return sources.size();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
        case 0:
        	String source=sources.get(rowIndex).toString();
        	if(source.indexOf('/')>=0)
        		return source.substring(source.lastIndexOf('/'));
        	else
        		return source;
        case 1:
            if (model.isSourceVisible(sources.get(rowIndex)))
                return Icons.YES;
            else
                return Icons.NO;
        }
        return null;
    }

    @Override
    public Class<?> getColumnClass(int arg0) {
        switch (arg0) {
        case 0:
            return DataSource.class;

        default:
            return ImageIcon.class;
        }
    }

    private ArrayList<DataSource> sources = new ArrayList<DataSource>();

    
    ArrayList<DataSource> sources(){
    	return sources;
    }
    @Override
    public void update(Observable o, Object arg) {
        ArrayList<DataSource> newSources = new ArrayList<DataSource>();
        newSources.addAll(model.loadedSources());
        sources = newSources;

        fireTableDataChanged();
    }
   

}
