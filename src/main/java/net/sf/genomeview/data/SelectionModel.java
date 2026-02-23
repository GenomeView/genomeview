/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.shortread.ReadGroup;

/**
 * Keeps track of whatever is selected.
 * 
 * @author Thomas Abeel
 * 
 */
public class SelectionModel extends Observable {

	/**
	 * Contains all selected locations, these can be subcomponents of a Feature.
	 */
	private SortedSet<Location> selectedLocation = new ExactSet();
	
	private Location selectedRegion = null;

	public void addLocationSelection(Location rl) {
		selectedLocation.add(rl);
		refresh();
	}

	private void refresh() {
		setChanged();
		notifyObservers();

	}

	class ExactSet extends TreeSet<Location> {
		@Override
		public boolean contains(Object l) {
			for (Location ll : this) {
				if (ll == l)
					return true;

			}
			return false;
		}
	}

	public SortedSet<Location> getLocationSelection() {
		return selectedLocation;
	}

	/**
	 * sets {@link #selectedLocation} to all locations of the Feature
	 * @param rl the Feature to select
	 */
	public void setLocationSelection(Feature rl) {
		selectedLocation.clear();
		setSelectedRegion(null);
		for (Location l : rl.location())
			selectedLocation.add(l);
		refresh();

	}

	/**
	 * selects a single location
	 * @param rl the {@link Location}
	 */
	public void setLocationSelection(Location rl) {
		selectedLocation.clear();
		setSelectedRegion(null);
		this.addLocationSelection(rl);

	}

	/**
	 * remove location rl from the current selection
	 * @param rl a single location
	 */
	public void removeLocationSelection(Location rl) {

		selectedLocation.remove(rl);
		refresh();

	}


	public final Location getSelectedRegion() {
		return selectedRegion;
	}

	public final void setSelectedRegion(Location selectedRegion) {
		this.selectedRegion = selectedRegion;
		refresh();
	}

	/**
	 * 
	 * @return all known {@link Feature}s attached to the current selection
	 */
	public SortedSet<Feature> getFeatureSelection() {
		SortedSet<Feature> out = new TreeSet<Feature>();
		Set<Location> select = new TreeSet<Location>();
		// FIXME is select a broken attempt to avoid race condition? 
		select.addAll(selectedLocation);
		for (Location l : select) {
			out.add(l.getParent());
		}
		return out;
	}

	/**
	 * Return the length of the current selection, either region, or location
	 * 
	 * @return number of selected proteins
	 */
	public int getNumberOfSelectedProts() {
		return getNumberOfSelectedNucs() / 3;
	}

	public void clearLocationSelection() {
		selectedLocation.clear();
		refresh();

	}

	/**
	 * Return the length of the current selection, either region, or location
	 * 
	 * @return number of selected nucleotides
	 */
	public int getNumberOfSelectedNucs() {
		if (getSelectedRegion() != null) {
			return getSelectedRegion().length();
		} else if (getLocationSelection() != null && getLocationSelection().size() != 0) {
			int size = 0;
			for (Location loc : getLocationSelection()) {
				size += loc.length();
			}
			return size;
		} else {
			return 0;
		}
	}

	public void clear() {
		selectedLocation.clear();
		selectedRegion = null;
	}

	/**
	 * Returns true if there is a feature selected, returns false in other
	 * cases.
	 * 
	 * @return
	 */
	public boolean isFeatureSelected() {
		return selectedLocation.size() > 0;
	}
}
