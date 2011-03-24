/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.viztracks.TickmarkTrack;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.jannot.DataKey;

/***
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackList implements Iterable<Track> {
//	class WeightedDataKey implements Comparable<WeightedDataKey> {
//		private int weight = 0;
//		private DataKey dk = null;
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see java.lang.Object#hashCode()
//		 */
//		@Override
//		public int hashCode() {
//			return dk.hashCode() + 37 * weight;
//		}
//
//		/*
//		 * (non-Javadoc)
//		 * 
//		 * @see java.lang.Object#equals(java.lang.Object)
//		 */
//		@Override
//		public boolean equals(Object obj) {
//			return dk.equals(((WeightedDataKey) obj).dk) && ((WeightedDataKey) obj).weight == weight;
//		}
//
//		/**
//		 * @param weight
//		 * @param dk
//		 */
//		public WeightedDataKey(int weight, DataKey dk) {
//			super();
//			this.weight = weight;
//			this.dk = dk;
//		}
//
//		@Override
//		public int compareTo(WeightedDataKey o) {
//			return Double.compare(weight, o.weight);
//		}
//
//	}

	private Model model;

	private ArrayList<DataKey> order = new ArrayList<DataKey>();
	private Map<DataKey, Track> mapping = new HashMap<DataKey, Track>();
	
	// private List<DataKey> order = new ArrayList<DataKey>();

	public TrackList(Model model) {
		this.model = model;
		init();

	}

	public Track get(int index) {
		return mapping.get(order.get(index));
	}

	private void init() {
		TickmarkTrack ticks = new TickmarkTrack(model);
		add(ticks.getDataKey(), ticks);
		StructureTrack strack = new StructureTrack(model);
		add(strack.getDataKey(), strack);
		if (!Configuration.getBoolean("track:showStructure")) {
			strack.setVisible(false);

		}

	}

	public StructureTrack structure() {
		return (StructureTrack) mapping.get(StructureTrack.key);
	}

	void add(DataKey dk, Track track) {
//		WeightedDataKey wdk = new WeightedDataKey(order.size(), dk);
		mapping.put(dk, track);
		order.add(dk);
//		rawKeys.add(dk);

	}

	@Deprecated
	public void clear() {
		mapping.clear();
		order.clear();
		init();
	}

	private static final long serialVersionUID = 6716276343672660196L;

	public void down(int row) {
		if (row < order.size() - 1) {
			DataKey tmp = order.get(row);
			order.set(row, order.get(row + 1));
			order.set(row + 1, tmp);
			model.refresh();

		}

	}

	public void up(int row) {
		if (row > 0) {
			DataKey tmp = order.get(row);
			order.set(row, order.get(row - 1));
			order.set(row - 1, tmp);
			model.refresh();
		}

	}

	public void remove(DataKey key) {
		order.remove(key);
		mapping.remove(key);
	}

	public boolean containsTrack(DataKey key) {
		return mapping.keySet().contains(key);
	}

	// public boolean containsTrack(String string) {
	// return containsTrack(new StringKey(string));
	// }

	public int size() {
		return order.size();
	}

	@Override
	public Iterator<Track> iterator() {
		return new Iterator<Track>() {
			int index = 0;

			@Override
			public boolean hasNext() {
				return index < order.size();
			}

			@Override
			public Track next() {
				return mapping.get(order.get(index++));
			}

			@Override
			public void remove() {
				throw new UnsupportedOperationException();

			}

		};
	}

	public void printDebug() {
		System.out.println("Tracklist debug:");
		System.out.println("Order:" + order);
		System.out.println("Mapping: " + mapping);

	}
}