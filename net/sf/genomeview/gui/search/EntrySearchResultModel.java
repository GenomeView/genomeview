/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.util.ArrayList;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;

/**
 * 
 * @author Thomas Abeel
 * 
 */
class EntrySearchResultModel extends AbstractSearchResultModel {

	private static final long serialVersionUID = 2628403363607798505L;

	private ArrayList<Entry> entries = new ArrayList<Entry>();

	EntrySearchResultModel(Model model) {
		super(model);
	}

	private String[] columns = new String[] { "Entry" };

	@Override
	public String getColumnName(int col) {

		return columns[col];

	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public int getRowCount() {
		return entries.size();
	}

	@Override
	public Object getValueAt(int row, int col) {
		switch (col) {
		case 0:
			return entries.get(row);
			// case 1:
			// return features.get(row);
		}
		return null;

	}

	Entry getEntry(int row) {
		return entries.get(row);

	}

	void search(String text) {
		String lowerCaseText = text.toLowerCase();
		clear();
		for (Entry e : model.entries()) {
			if (e.getID().toLowerCase().contains(lowerCaseText))
				entries.add(e);

		}

		fireTableDataChanged();

	}

	void clear() {
		entries.clear();
		fireTableDataChanged();
	}
}