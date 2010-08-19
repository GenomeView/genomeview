/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.util.Observable;
import java.util.Observer;

import javax.swing.Icon;
import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.core.Icons;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.jannot.Feature;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;
import net.sf.jannot.refseq.Sequence;
import net.sf.jannot.utils.SequenceTools;

/**
 * Wraps an instance of the Model interface and let it act like a ListModel.
 * 
 * @author Thomas Abeel
 * 
 */
public class FeatureTableModel extends AbstractTableModel implements Observer {

	/**
     * 
     */
	private static final long serialVersionUID = 320228141380099074L;

	private String[] columns = { "Name"};

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	private Model model;

	public FeatureTableModel(Model model) {
		this.model = model;
		model.addObserver(this);

	}

	public void update(Observable o, Object arg) {
		if (arg == NotificationTypes.GENERAL || arg == NotificationTypes.TRANSLATIONTABLECHANGE
				|| arg == NotificationTypes.ENTRYCHANGED || arg == NotificationTypes.JANNOTCHANGE) {
			fireTableDataChanged();
		}

	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public int getRowCount() {
		MemoryFeatureAnnotation fa = model.getSelectedEntry().getMemoryAnnotation(type);
		if(fa==null)
			return 0;
		return fa.cachedCount();// .noFeatures(type);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
//		switch (col) {
//		case 0:
//			return String.class;
//		case 1:
//		case 2:
//		case 3:
//		case 4:
//			return Icon.class;
//		default:
//			return String.class;
//
//		}

	}

	public Feature getFeature(int row) {
		return model.getSelectedEntry().getMemoryAnnotation(type).getCached(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Feature f = getFeature(row);
		return f;
	}

	public int getRow(Feature first) {
		return model.getSelectedEntry().getMemoryAnnotation(type).getCachedIndexOf(first);

	}

	private Type type = Type.get("CDS");

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		model.refresh(NotificationTypes.GENERAL);

	}

}
