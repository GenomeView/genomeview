/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import net.sf.genomeview.data.DummyEntry;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;

/**
 * 
 * @author Thomas Abeel
 * 
 */
final class EntryListModel extends DefaultComboBoxModel implements Observer {

	private Model model;

	public EntryListModel(Model model) {
		model.addObserver(this);
		this.model = model;
	}

	private static final long serialVersionUID = -3028394066023453566L;

	@Override
	public Object getElementAt(int i) {
		int count=0;
		for(Entry e:model.entries()){
			if(count==i)
				return e;
			count++;
		}
		return DummyEntry.dummy;
	}

	@Override
	public int getSize() {
		return model.noEntries();
	}

	@Override
	public Object getSelectedItem() {
		return model.getSelectedEntry();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		model.setSelectedEntry((Entry) anItem);

	}

	@Override
	public void update(Observable arg0, Object arg1) {
		fireContentsChanged(arg0, 0, model.noEntries());

	}

}