/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

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
import net.sf.genomeview.gui.viztracks.graph.WiggleTrack;
import net.sf.genomeview.gui.viztracks.hts.PileupTrack;
import net.sf.genomeview.gui.viztracks.hts.ShortReadTrack;
import net.sf.genomeview.gui.viztracks.variation.VariationTrack;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Type;
import net.sf.jannot.alignment.maf.AbstractMAFMultipleAlignment;
import net.sf.jannot.alignment.mfa.AlignmentAnnotation;
import net.sf.jannot.bigwig.BigWigData;
import net.sf.jannot.pileup.DoublePile;
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
 * 
 * @author Thomas Abeel
 * 
 */
public class TrackList implements Iterable<Track> {

	private Logger log = Logger.getLogger(TrackList.class.getCanonicalName());

	private Model model;

	private ArrayList<DataKey> order = new ArrayList<DataKey>();
	private Map<DataKey, Track> mapping = new HashMap<DataKey, Track>();

	// private List<DataKey> order = new ArrayList<DataKey>();

	public TrackList(Model model) {
		this.model = model;
		init();

	}

	public Track get(int index) {
		if (index == -1)
			return null;
		return mapping.get(order.get(index));
	}

	private void init() {
		TickmarkTrack ticks = new TickmarkTrack(model);
		add(ticks.getDataKey(), ticks);
		StructureTrack strack = new StructureTrack(model);
		add(strack.getDataKey(), strack);
		if (!Configuration.getBoolean("track:showStructure")) {
			strack.config().setVisible(false);

		}

	}

	public StructureTrack structure() {
		return (StructureTrack) mapping.get(StructureTrack.key);
	}

	private void add(DataKey dk, Track track) {
		mapping.put(dk, track);
		if(!order.contains(dk)){
			int x = findIndex(dk);
			order.add(x, dk);
		}

	}

	private int findIndex(DataKey dk) {
		int w = Configuration.getWeight(dk);

		int count = 0;
		while (count < order.size() && Configuration.getWeight(order.get(count)) <= w) {
			count++;
		}

		return count;

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

			int tmpWeight = Configuration.getWeight(order.get(row));
			Configuration.setWeight(order.get(row), tmpWeight + 1);
			Configuration.setWeight(order.get(row + 1), tmpWeight);

			
			order.set(row, order.get(row + 1));
			order.set(row + 1, tmp);

			model.refresh();

		}

	}

	public synchronized void up(int row) {
		if (row > 0) {
			DataKey tmp = order.get(row);

			// int tmpWeight = Configuration.getWeight(order.get(row));
			Configuration.setWeight(order.get(row), Configuration.getWeight(order.get(row - 1)));
			Configuration.setWeight(order.get(row - 1), Configuration.getWeight(order.get(row - 1)) + 1);

			order.set(row, order.get(row - 1));
			order.set(row - 1, tmp);
			
			model.refresh();
		}

	}

	public void remove(DataKey key) {
		order.remove(key);
		mapping.remove(key);
	}

	private boolean containsTrack(DataKey key) {
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

	public boolean update(Entry e) {
		System.out.println("Updating tracks for " + e);
		int startSize = this.size();
		/* Graph tracks */
		for (DataKey key : e) {
			Data<?> data = e.get(key);

			if (data instanceof MemoryFeatureAnnotation) {
				if (!this.containsTrack(key) && ((MemoryFeatureAnnotation) data).cachedCount() > 0)
					this.add(key, new FeatureTrack(model, (Type) key));

			}
			if(data instanceof VCFWrapper){
				if (!this.containsTrack(key))
					this.add(key, new VariationTrack(model, (Type) key));
			}
			if (data instanceof GFFWrapper || data instanceof BEDWrapper) {
				if (!this.containsTrack(key))
					this.add(key, new FeatureTrack(model, (Type) key));

			}

			if (data instanceof PileupWrapper || data instanceof SWigWrapper) {
				if (!this.containsTrack(key))
					this.add(key, new PileupTrack(key, new WiggleProvider(e, (Data<Pile>) data, model), model));
			}

			if (data instanceof TDFData) {
				if (!this.containsTrack(key))
					this.add(key, new PileupTrack(key, new TDFProvider(e, (TDFData) data, model), model));
			}

			if (data instanceof BigWigData) {
				if (!this.containsTrack(key))
					this.add(key, new PileupTrack(key, new BigWigProvider(e, (BigWigData) data, model), model));
			}

			if (data instanceof Graph) {
				if (!this.containsTrack(key))
					this.add(key, new WiggleTrack(key, model, true));
			}
			if (data instanceof AlignmentAnnotation) {
				if (!this.containsTrack(key))
					this.add(key, new MultipleAlignmentTrack(model, key));
			}
			if (data instanceof ReadGroup) {
				if (!this.containsTrack(key)) {
					this.add(key, new ShortReadTrack(key, new ShortReadProvider(e, (ReadGroup) data, model), model));
				}
			}
			if (data instanceof AbstractMAFMultipleAlignment) {
				if (!this.containsTrack(key)) {
					this.add(key, new MultipleAlignmentTrack2(model, key));
					log.info("Added multiple alignment track " + key);
				}
			}
		}
		/* Fix weight to make sure they are different */
		for (int i = 1; i < order.size(); i++) {
			// if (Configuration.getWeight(order.get(i - 1)) < Configuration
			// .getWeight(order.get(i))) {
			// System.err
			// .println("This is not supposed to happen, why are we sorting?!!?");
			// }
			if (Configuration.getWeight(order.get(i - 1)) >= Configuration.getWeight(order.get(i))) {
				Configuration.setWeight(order.get(i), Configuration.getWeight(order.get(i - 1)) + 1);

			}
		}
		return this.size() != startSize;

	}
}