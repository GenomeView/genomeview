/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import net.sf.genomeview.data.DummyEntry;
import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;

/**
 * 
 * @author Thomas Abeel
 * 
 */
final class EntryListModel implements Observer, ComboBoxModel {

	private Model model;

	public EntryListModel(Model model) {
		model.addObserver(this);
		this.model = model;
	}

	private static final long serialVersionUID = -3028394066023453566L;

	private int lastSize = 0;

	@Override
	public Object getElementAt(int i) {
		if(tmpList.size()==0||i>=tmpList.size())
			return DummyEntry.dummy;
		else
			return tmpList.get(i);
	}

	@Override
	public int getSize() {
		return tmpList.size();
	}

	@Override
	public Object getSelectedItem() {
		return model.getSelectedEntry();
	}

	@Override
	public void setSelectedItem(Object anItem) {
		model.setSelectedEntry((Entry) anItem);

	}
	private ArrayList<Entry>tmpList=new ArrayList<Entry>();

	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		// System.out.println("loop");
		int size = model.noEntries();
		// for (int i = 0; i < size; i++)
		// System.out.println("\t" + getElementAt(i));
		// System.out.println("fire: " + model.noEntries());
		if (size != lastSize) {
			tmpList.clear();
			for(Entry e:model.entries()){
				tmpList.add(e);
			}
			ListDataEvent le = new ListDataEvent(this, ListDataEvent.CONTENTS_CHANGED, 0, model.noEntries());
			for (ListDataListener l : listeners) {
				l.contentsChanged(le);
			}
			lastSize = tmpList.size();
		}
		

	}

	private Set<ListDataListener> listeners = new HashSet<ListDataListener>();

	@Override
	public void addListDataListener(ListDataListener l) {
		listeners.add(l);

	}

	@Override
	public void removeListDataListener(ListDataListener l) {
		listeners.remove(l);

	}

}