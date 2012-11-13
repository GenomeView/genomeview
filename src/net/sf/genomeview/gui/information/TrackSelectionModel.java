package net.sf.genomeview.gui.information;

import java.util.ArrayList;

import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;

public class TrackSelectionModel implements ListSelectionModel {

	private int selectedRow = -1;
	private ArrayList<ListSelectionListener> listeners = new ArrayList<ListSelectionListener>();

	@Override
	public void setValueIsAdjusting(boolean valueIsAdjusting) {
		// Do nothing

	}

	@Override
	public void setSelectionMode(int selectionMode) {
		// Ignore

	}

	@Override
	public void setSelectionInterval(int index0, int index1) {
		if (index0 != index1)
			throw new UnsupportedOperationException("Can't select ranges!");
		selectedRow = index0;

	}

	@Override
	public void setLeadSelectionIndex(int index) {
		selectedRow = index;

	}

	@Override
	public void setAnchorSelectionIndex(int index) {
		selectedRow = index;

	}

	@Override
	public void removeSelectionInterval(int index0, int index1) {
		throw new UnsupportedOperationException("Can't select ranges!");

	}

	@Override
	public void removeListSelectionListener(ListSelectionListener x) {
		listeners.remove(x);

	}

	@Override
	public void removeIndexInterval(int index0, int index1) {
		throw new UnsupportedOperationException("Can't do this");

	}

	@Override
	public boolean isSelectionEmpty() {
		return selectedRow < 0;
	}

	@Override
	public boolean isSelectedIndex(int index) {
		return selectedRow == index;
	}

	@Override
	public void insertIndexInterval(int index, int length, boolean before) {
		throw new UnsupportedOperationException("Can't do this");

	}

	@Override
	public boolean getValueIsAdjusting() {

		return false;
	}

	@Override
	public int getSelectionMode() {
		return 0;
	}

	@Override
	public int getMinSelectionIndex() {
		return selectedRow;
	}

	@Override
	public int getMaxSelectionIndex() {
		return selectedRow;
	}

	@Override
	public int getLeadSelectionIndex() {
		return selectedRow;
	}

	@Override
	public int getAnchorSelectionIndex() {
		return selectedRow;
	}

	@Override
	public void clearSelection() {
//		System.out.println("Clearing selection!!!!");
		selectedRow = -1;

	}

	@Override
	public void addSelectionInterval(int index0, int index1) {
		throw new UnsupportedOperationException("Can't do this");

	}

	@Override
	public void addListSelectionListener(ListSelectionListener x) {
		listeners.add(x);

	}

}
