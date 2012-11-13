/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.PriorityQueue;
import java.util.Set;

import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import be.abeel.util.NaturalOrderComparator;

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

	private List<Entry>tmpList=new ArrayList<Entry>();
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
	
	private class EntryComparator implements Comparator<Entry>{

		private Comparator<String> noc=NaturalOrderComparator.NUMERICAL_ORDER;
		@Override
		public int compare(Entry o1, Entry o2) {
			return noc.compare(o1.getID(), o2.getID());
		}
		
	}
	

	@Override
	public synchronized void update(Observable arg0, Object arg1) {
		int size = model.noEntries();
		if (size != lastSize) {
			ArrayList<Entry>tmp=new ArrayList<Entry>();
			for(Entry e:model.entries()){
				tmp.add(e);
			}
			Collections.sort(tmp,new EntryComparator());
			tmpList=tmp;
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