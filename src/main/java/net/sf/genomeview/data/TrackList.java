/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.provider.BigWigProvider;
import net.sf.genomeview.data.provider.ShortReadProvider;
import net.sf.genomeview.data.provider.TDFProvider;
import net.sf.genomeview.data.provider.WiggleProvider;
import net.sf.genomeview.gui.viztracks.TickmarkTrack;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.annotation.FeatureTrack;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.genomeview.gui.viztracks.comparative.MultipleAlignmentTrack;
import net.sf.genomeview.gui.viztracks.comparative.MultipleAlignmentTrack2;
import net.sf.genomeview.gui.viztracks.comparative.SyntenicTrack;
import net.sf.genomeview.gui.viztracks.graph.WiggleTrack;
import net.sf.genomeview.gui.viztracks.hts.PileupTrack;
import net.sf.genomeview.gui.viztracks.hts.ShortReadTrack;
import net.sf.genomeview.gui.viztracks.variation.VariationTrack;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.SyntenicData;
import net.sf.jannot.Type;
import net.sf.jannot.alignment.maf.AbstractMAFMultipleAlignment;
import net.sf.jannot.alignment.mfa.AlignmentAnnotation;
import net.sf.jannot.bigwig.BigWigData;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.tabix.BEDWrapper;
import net.sf.jannot.tabix.GFFWrapper;
import net.sf.jannot.tabix.PileupWrapper;
import net.sf.jannot.tabix.SWigWrapper;
import net.sf.jannot.tabix.VCFWrapper;
import net.sf.jannot.tdf.TDFData;
import net.sf.jannot.wiggle.Graph;

/***
 * List of all available tracks.
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackList implements Iterable<Track> {

	private final Model model;

	/**
	 * The order of the tracks. I suppose this list must always match
	 * mapping.keys()
	 */
	private ArrayList<DataKey> order = new ArrayList<DataKey>();

	/**
	 * Each datakey maps to a track.
	 */
	private Map<DataKey, Track> mapping = new HashMap<DataKey, Track>();

	public TrackList(Model model) {
		this.model = model;
		init();

	}

	/**
	 * 
	 * @param index 0-based track number
	 * @return the track[index] or null if index=-1 (??).
	 */
	public synchronized Track get(int index) {
		if (index == -1) {
			return null;
		}
		return mapping.get(order.get(index));
	}

	/**
	 * Adds the default {@link TickmarkTrack} and {@link StructureTrack} to the
	 * tracklist.
	 */
	private void init() {
		TickmarkTrack ticks = new TickmarkTrack(model);
		add(ticks.getDataKey(), ticks);
		StructureTrack strack = new StructureTrack(model);
		add(strack.getDataKey(), strack);
		if (!model.getConfiguration().getBoolean("track:showStructure")) {
			strack.config().setVisible(false);
		}

	}

	public synchronized StructureTrack structure() {
		return (StructureTrack) mapping.get(StructureTrack.key);
	}

	/**
	 * add/replace a track with given data key to our mapping and order
	 * 
	 * @param dk    the {@link DataKey}
	 * @param track a visualization {@link Track}
	 */
	private synchronized void add(DataKey dk, Track track) {
		mapping.put(dk, track);
		if (!order.contains(dk)) {
			int x = findIndex(dk);
			order.add(x, dk);
		}

	}

	/**
	 * 
	 * @param dk a {@link Data}
	 * @return a proper index for the data key, based on the weight of the
	 *         entries we already have in {@link #order}. The weights are set in
	 *         the preferences track:weight for all {@link DataKey}s
	 */
	private synchronized int findIndex(DataKey dk) {
		int w = model.getConfiguration().getWeight(dk);

		int count = 0;
		while (count < order.size()
				&& model.getConfiguration().getWeight(order.get(count)) <= w) {
			count++;
		}

		return count;

	}

	/**
	 * reset tracks to the default
	 */
	@Deprecated
	public synchronized void clear() {
		mapping.clear();
		order.clear();
		init();
	}

	/**
	 * Change weight of row and the row after that. Nothing happens if row >=
	 * last row
	 * 
	 * @param row the row number.
	 */
	public synchronized void down(int row) {
		if (row < order.size() - 1) {
			final Configuration conf = model.getConfiguration();
			DataKey tmp = order.get(row);

			int tmpWeight = conf.getWeight(order.get(row));
			conf.setWeight(order.get(row), tmpWeight + 1);
			conf.setWeight(order.get(row + 1), tmpWeight);

			order.set(row, order.get(row + 1));
			order.set(row + 1, tmp);

			model.refresh();

		}

	}

	public synchronized void up(int row) {
		if (row > 0) {
			final Configuration conf = model.getConfiguration();
			DataKey tmp = order.get(row);

			// int tmpWeight = Configuration.getWeight(order.get(row));
			conf.setWeight(order.get(row), conf.getWeight(order.get(row - 1)));
			conf.setWeight(order.get(row - 1),
					conf.getWeight(order.get(row - 1)) + 1);

			order.set(row, order.get(row - 1));
			order.set(row - 1, tmp);

			model.refresh();
		}

	}

	/**
	 * remove key from the mapping and ordering.
	 * 
	 * @param key the key to remove.
	 */
	public synchronized void remove(DataKey key) {
		order.remove(key);
		mapping.remove(key);
	}

	private boolean containsTrack(DataKey key) {
		return mapping.keySet().contains(key);
	}

	/**
	 * 
	 * @return the number of {@link Track}s in this list..
	 */
	public int size() {
		return order.size();
	}

	@Override
	public synchronized Iterator<Track> iterator() {
		List<Track> sorted = new LinkedList<Track>();
		for (int index = 0; index < order.size(); index++) {
			sorted.add(mapping.get(order.get(index)));
		}
		return sorted.iterator();
	}

	/**
	 * Update the tracks to show all tracks for data in e. Creates the correct
	 * visualization {@link Track} for all available {@link Data} in the entry
	 * 
	 * @param e the current {@link Entry} (usually this is the currently
	 *          selected chromosome)
	 * @return true iff the final size equals the start size.
	 */
	public synchronized boolean update(Entry e) {
		model.getLog().log(Level.INFO, "track update, show " + e.getID());
		int startSize = this.size();
		/* Graph tracks */
		for (DataKey key : e) {
			Data<?> data = e.get(key);
			model.getLog().log(Level.FINE, "track includes Data " + key
					+ " of type " + data.getClass());

			if (data instanceof MemoryFeatureAnnotation) {
				if (!this.containsTrack(key)
						&& ((MemoryFeatureAnnotation) data).cachedCount() > 0) {
					this.add(key, new FeatureTrack(model, (Type) key));
				}

			} else if (data instanceof VCFWrapper) {
				if (!this.containsTrack(key)) {
					this.add(key, new VariationTrack(model, (Type) key));
				}
			} else if (data instanceof GFFWrapper
					|| data instanceof BEDWrapper) {
				if (!this.containsTrack(key)) {
					this.add(key, new FeatureTrack(model, (Type) key));
				}
			} else if (data instanceof PileupWrapper
					|| data instanceof SWigWrapper) {
				if (!this.containsTrack(key)) {
					this.add(key, new PileupTrack(key,
							new WiggleProvider(e, (Data<Pile>) data, model),
							model));
				}
			} else if (data instanceof TDFData) {
				if (!this.containsTrack(key)) {
					this.add(key, new PileupTrack(key,
							new TDFProvider(e, (TDFData) data, model), model));
				}
			} else if (data instanceof BigWigData) {
				if (!this.containsTrack(key)) {
					this.add(key, new PileupTrack(key,
							new BigWigProvider(e, (BigWigData) data, model),
							model));
				}
			} else if (data instanceof Graph) {
				if (!this.containsTrack(key)) {
					this.add(key, new WiggleTrack(key, model, true));
				}
			} else if (data instanceof AlignmentAnnotation) {
				if (!this.containsTrack(key)) {
					this.add(key, new MultipleAlignmentTrack(model, key));
				}
			} else if (data instanceof ReadGroup) {
				if (!this.containsTrack(key)) {
					this.add(key, new ShortReadTrack(key,
							new ShortReadProvider(e, (ReadGroup) data, model),
							model));
				}
			} else if (data instanceof AbstractMAFMultipleAlignment) {
				if (!this.containsTrack(key)) {
					this.add(key, new MultipleAlignmentTrack2(model, key));
					model.getLog().log(Level.INFO,
							"Added multiple alignment track " + key);
				}
			} else if (data instanceof SyntenicData) {
				if (!this.containsTrack(key)) {
					this.add(key, new SyntenicTrack(model, key));
				}
			} else {
				model.getLog().log(Level.WARNING,
						"unhandled data type Data type " + data.getClass());
			}
		}
		/* Fix weight to make sure they are different */
		final Configuration conf = model.getConfiguration();
		for (int i = 1; i < order.size(); i++) {
			if (conf.getWeight(order.get(i - 1)) >= conf
					.getWeight(order.get(i))) {
				conf.setWeight(order.get(i),
						conf.getWeight(order.get(i - 1)) + 1);

			}
		}
		return this.size() != startSize;

	}
}
