/**
 * %HEADER%
 */
package net.sf.genomeview.gui.information;

import java.util.Objects;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.NotificationTypes;
import net.sf.jannot.Feature;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;

/**
 * Wraps an instance of the Model interface and let it act like a ListModel.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public class FeatureTableModel extends AbstractTableModel implements Observer {

	private final String[] columns = { "Name" };
	private final Model model;
	private Type type; // mutable!

	public FeatureTableModel(Model model) {
		this.model = Objects.requireNonNull(model);
		type = model.getGlobal().typeFactory().get("CDS");
		model.addObserver(this);

	}

	@Override
	public String getColumnName(int column) {
		return columns[column];
	}

	@Override
	public void update(Observable o, Object arg) {
		if (arg == NotificationTypes.GENERAL
				|| arg == NotificationTypes.TRANSLATIONTABLECHANGE
				|| arg == NotificationTypes.ENTRYCHANGED
				|| arg == NotificationTypes.JANNOTCHANGE) {
			fireTableDataChanged();
		}

	}

	@Override
	public int getColumnCount() {
		return columns.length;
	}

	@Override
	public int getRowCount() {
		MemoryFeatureAnnotation fa = model.vlm.getVisibleEntry()
				.getMemoryAnnotation(type);
		if (fa == null) {
			return 0;
		}
		return fa.cachedCount();// .noFeatures(type);
	}

	@Override
	public Class<?> getColumnClass(int col) {
		return String.class;
	}

	public Feature getFeature(int row) {
		return model.vlm.getVisibleEntry().getMemoryAnnotation(type)
				.getCached(row);
	}

	@Override
	public Object getValueAt(int row, int col) {
		Feature f = getFeature(row);
		return f;
	}

	public int getRow(Feature first) {
		return model.vlm.getVisibleEntry().getMemoryAnnotation(type)
				.getCachedIndexOf(first);

	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
		model.refresh(NotificationTypes.GENERAL);

	}

}
