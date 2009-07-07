/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.JFrame;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.annotation.track.FeatureTrack;
import net.sf.genomeview.gui.annotation.track.MultipleAlignmentTrack;
import net.sf.genomeview.gui.annotation.track.SequenceLogoTrack;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.SyntenicTrack;
import net.sf.genomeview.gui.annotation.track.TickmarkTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.gui.annotation.track.WiggleTrack;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.FeatureAnnotation;
import net.sf.jannot.Graph;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.SyntenicAnnotation;
import net.sf.jannot.Type;
import net.sf.jannot.alignment.Alignment;
import net.sf.jannot.alignment.ReferenceSequence;
import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.MultiFileSource;
import be.abeel.util.DefaultHashMap;

public class Model extends Observable implements IModel {
	private Logger logger = Logger.getLogger(Model.class.getCanonicalName());

	private ArrayList<Entry> entries = new ArrayList<Entry>();;

	public Model(JFrame parent) {
		this.parent = parent;
		this.trackList = new TrackList(this);
		for (Entry e : entries) {
			e.addObserver(this);
		}
		StructureTrack strack = new StructureTrack(this);
		trackList.add(strack);
		TickmarkTrack ticks = new TickmarkTrack(this);
		trackList.add(ticks);

		Set<Type> tmp1 = Configuration.getTypeSet("visibleTypesStructure");
		for (Type t : tmp1)
			strack.setTypeVisible(t, true);

		Configuration.getTypeSet("visibleTypes");
		updateTracklist();

	}

	public int noEntries() {
		return entries.size();
	}

	/**
	 * Get an entry by ID
	 * 
	 * @param id
	 * @return
	 */
	public Entry entry(String id) {
		for (Entry e : entries)
			if (e.getID().equalsIgnoreCase(id))
				return e;
		return null;
	}

	public Entry entry(int index) {
		return entries.get(index);
	}

	public void update(Observable arg0, Object arg) {
		if (arg instanceof ChangeEvent) {
			undoStack.push((ChangeEvent) arg);
			redoStack.clear();
		}
		refresh(arg);

	}

	public void clearEntries() {
		for (Entry e : entries) {
			e.deleteObservers();
		}
		this.setAnnotationLocationVisible(new Location(1, 1));
		for (Entry e : entries) {
			e.deleteObservers();
		}
		loadedSources.clear();
		entries.clear();
		selectedEntry = null;
		selectedLocation.clear();
		selectedRegion = null;
		clearTrackList(trackList);
		refresh(NotificationTypes.GENERAL);
	}

	private void clearTrackList(TrackList tracklist) {
		List<Track> remove = new ArrayList<Track>();
		for (Track t : tracklist) {

			if (!(t instanceof FeatureTrack || t instanceof StructureTrack || t instanceof TickmarkTrack))
				remove.add(t);
		}
		tracklist.removeAll(remove);
		refresh();

	}

	public List<Entry> entries() {
		ArrayList<Entry> list = new ArrayList<Entry>();
		for (Entry e : entries)
			list.add(e);
		return Collections.unmodifiableList(list);

	}

	private Entry selectedEntry = null;

	class DummyEntry extends Entry {
		public DummyEntry() {
			super(null);
		}

		@Override
		public String getID() {
			return null;
		}

		@Override
		public String toString() {
			return "Nothing loaded";
		}

	}

	private Entry dummy = new DummyEntry();

	/**
	 * Gives the index of the selected entry
	 * 
	 * @return
	 */

	public Entry getSelectedEntry() {
		if (selectedEntry == null)
			return dummy;
		return selectedEntry;
	}

	public void setSelectedEntry(Entry entry) {
		this.selectedEntry = entry;
		selectedLocation.clear();
		selectedRegion = null;
		refresh();

	}

	/**
	 * The main window of the GUI belonging to this model.
	 */
	private JFrame parent;

	public JFrame getParent() {
		return parent;
	}

	/* Keeps track of individually hidden features. */
	private Map<Feature, Boolean> featureVisible = new DefaultHashMap<Feature, Boolean>(Boolean.TRUE);

	public boolean isFeatureVisible(Feature f) {
		return featureVisible.get(f);
	}

	public void setFeatureVisible(Feature rf, boolean b) {
		featureVisible.put(rf, b);
		refresh();

	}


	private boolean silent;

	/**
	 * Set the mode of the model. In silent mode, the model does not pass on
	 * notifications from its observables to its observers.
	 * 
	 * This can be useful to limit the number of repaints in events that there
	 * are a lot of changes in the data. For instance when loading new data.
	 * 
	 * @param silent
	 */
	public void setSilent(boolean silent) {
		this.silent = silent;
		refresh(NotificationTypes.GENERAL);
	}

	public void refresh(Object arg) {
		if (!silent) {
			setChanged();
			notifyObservers(arg);
		}
	}

	/**
	 * Checked way to notify all model observers.
	 */
	public void refresh() {
		refresh(null);

	}

	private boolean exitRequested = false;

	public void exit() {
		this.exitRequested = true;
		refresh();

	}

	// private Map<IValueFeature, Boolean> valueFeatureVisible = new
	// DefaultHashMap<IValueFeature, Boolean>(
	// Boolean.TRUE);
	//
	// public boolean isValueFeatureVisible(IValueFeature name) {
	// return valueFeatureVisible.get(name);
	// }
	//
	// public void setValueFeatureVisible(IValueFeature type, boolean b) {
	// valueFeatureVisible.put(type, b);
	// refresh();
	//
	// }

	// private Map<IValueFeature, DisplayType> valueFeatureDisplayMapping = new
	// DefaultHashMap<IValueFeature, DisplayType>(
	// DisplayType.OneLineBlocks);
	//
	// public DisplayType getValueFeatureDisplayType(IValueFeature type) {
	// return valueFeatureDisplayMapping.get(type);
	//
	// }

	// public void setValueFeatureDisplayType(IValueFeature name, DisplayType
	// dt) {
	// valueFeatureDisplayMapping.put(name, dt);
	// refresh();
	//
	// }
	// @Deprecated
	// public List<IValueFeature> getValueFeatures(Entry entry) {
	// return valueFeatures.get(entry);
	// }

	// @Override
	// @Deprecated
	// public void addFeature(IValueFeature vf) {
	// Entry e = getSelectedEntry();
	// if (!valueFeatures.containsKey(e)) {
	// valueFeatures.put(e, new ArrayList<IValueFeature>());
	// }
	//
	// valueFeatures.get(e).add(vf);
	// valueFeatureDisplayMapping.put(vf, DisplayType.LineProfile);
	// refresh();
	//
	// }

	public Location getAnnotationLocationVisible() {
		return new Location(annotationStart, annotationEnd);
	}

	private int annotationStart = 0, annotationEnd = 0;

	/**
	 * Set the visible area in the evidence and structure frame to the given
	 * Location.
	 * 
	 * start and end one-based [start,end]
	 * 
	 * @param start
	 * @param annotationEnd
	 */

	public void setAnnotationLocationVisible(Location r) {
		int start = r.start();
		int end = r.end();
		if (start > end) {
			return;
		}

		int modStart = -1;
		int modEnd = -1;
		if (start > 1) {
			modStart = start;
		} else {
			modStart = 1;
		}
		int chromLength = getSelectedEntry().sequence.size();
		if (end < chromLength || chromLength == 0) {
			modEnd = end;
		} else {
			modEnd = chromLength;
		}
		Location newZoom = new Location(modStart, modEnd);

		// getAnnotationLocationVisible();
		ZoomChange zc = new ZoomChange(new Location(annotationStart, annotationEnd), newZoom);
		zc.doChange();
		refresh();
	}

	/**
	 * Provides implementation to do/undo zoom changes.
	 * 
	 * @author Thomas Abeel
	 * 
	 */
	class ZoomChange implements ChangeEvent {
		/* The original zoom */
		private Location orig;

		/* The new zoom */
		private Location neww;

		public ZoomChange(Location location, Location newZoom) {
			this.orig = location;
			this.neww = newZoom;
		}

		@Override
		public void doChange() {
			annotationStart = neww.start();
			annotationEnd = neww.end();

		}

		@Override
		public void undoChange() {
			assert (annotationStart == neww.start());
			assert (annotationEnd == neww.end());
			annotationStart = orig.start();
			annotationEnd = orig.end();

		}

	}


	public boolean isExitRequested() {
		return exitRequested;
	}

	
	private ArrayList<Highlight> highlights = new ArrayList<Highlight>();

	public class Highlight {
		final public Location location;

		final public Strand strand;

		public Highlight(Location location, Color color, Strand strand) {
			super();
			this.color = color;
			this.location = location;
			this.strand = strand;
		}

		final public Color color;
	}

	public List<Highlight> getHighlight(Location region) {
		ArrayList<Highlight> out = new ArrayList<Highlight>();
		for (Highlight f : highlights) {
			if (f.location.end() > region.start() && f.location.start() < region.end())
				out.add(f);
		}
		return Collections.unmodifiableList(out);
	}

	public void clearHighlights() {
		highlights.clear();
		refresh();
	}

	public void addHighlight(Location l, Color c, Strand s) {
		highlights.add(new Highlight(l, c, s));
		refresh();
	}

	/**
	 * Center the model on a certain position. This will cause the nucleotide
	 * start, end and the normal start and end to change.
	 * 
	 * @param genomePosition
	 *            the position to center on
	 */
	public void center(int genomePosition) {
		int length = (annotationEnd - annotationStart) / 2;
		setAnnotationLocationVisible(new Location(genomePosition - length, genomePosition + length));

	}

	
	/**
	 * Contains all selected locations, these can be subcomponents of a Feature.
	 */
	private SortedSet<Location> selectedLocation = new TreeSet<Location>();

	public void addLocationSelection(Location rl) {
		selectedLocation.add(rl);
		refresh();
	}

	

	public SortedSet<Location> getLocationSelection() {
		return selectedLocation;
	}

	public void setLocationSelection(Feature rl) {
		selectedLocation.clear();
		for (Location l : rl.location())
			selectedLocation.add(l);
		refresh();

	}

	public void setLocationSelection(Location rl) {
		selectedLocation.clear();
		this.addLocationSelection(rl);

	}

	
	public void removeLocationSelection(Location rl) {

		selectedLocation.remove(rl);
		refresh();

	}

	/**
	 * Load new entries from a data source.
	 * 
	 * @param f
	 *            data source to load data from
	 * @throws ReadFailedException
	 */
	public Entry[] addEntries(DataSource f) throws ReadFailedException {
		Entry[] es = f.read();
		for (Entry e : es) {
			entries.add(e);
			e.addObserver(this);
			if (selectedEntry == null) {
				selectedEntry = e;
				setAnnotationLocationVisible(new Location(1, 10000));
			}

		}
		logger.info("Model adding entries done!");
		if (f instanceof MultiFileSource)
			for (DataSource ds : ((MultiFileSource) f).getFileSources()) {
				loadedSources.add(ds);
			}
		else
			loadedSources.add(f);
		refresh(NotificationTypes.GENERAL);
		return es;
	}

	private HashMap<Entry, AminoAcidMapping> aamapping = new DefaultHashMap<Entry, AminoAcidMapping>(AminoAcidMapping.STANDARDCODE);

	public AminoAcidMapping getAAMapping(Entry e) {
		return aamapping.get(e);
	}

	public AminoAcidMapping getAAMapping() {
		return aamapping.get(getSelectedEntry());
	}

	public void setAAMapping(Entry e, AminoAcidMapping aamapping) {
		logger.info("setting amino acid mapping: " + aamapping);
		this.aamapping.put(e, aamapping);
		refresh(NotificationTypes.TRANSLATIONTABLECHANGE);

	}

	public void addTrack(Track track) {
		trackList.add(track);
		refresh();
	}

	public void addSyntenic(DataSource source, Entry[] data) {

		for (int i = 0; i < data.length; i++) {
			SyntenicAnnotation sa = data[i].syntenic;
			System.out.println(data[i].getID());
			System.out.println(entry(data[i].getID()));
			Entry add = entry(data[i].getID());
			if (add != null) {
				add.syntenic.addAll(sa);

			}
			System.out.println("adding syntenic: " + sa);
		}

		loadedSources.add(source);
		updateTracklist();

	}

	/**
	 * All data from the new stuff will be added to the existing annotation in
	 * the selected entry.
	 * 
	 * If there is a seqid set and there already exists an Entry with this ID,
	 * the features will be added to that Entry instead.
	 * 
	 * 
	 * @throws ReadFailedException
	 */
	public void addFeatures(DataSource f, Entry[] data) throws ReadFailedException {
		logger.info("adding features: " + f);

		logger.info("entries read: " + data.length);

		for (Entry e : data) {
			FeatureAnnotation a = e.annotation;
			Entry addTo = getSelectedEntry();
			/* Check if any existing entry has the same name */
			for (Entry g : entries) {
				if (g.description.getID().equals(e.description.getID())) {
					logger.info("adding to: " + g.getID());
					addTo = g;
				}
			}
			/* Add all features at once silently */
			setSilent(true);
			addTo.annotation.addAll(a);
			addTo.graphs.addAll(e.graphs);
			setSilent(false);
		}
		loadedSources.add(f);

		updateTracklist();
		// return data;

	}

	public void addAlignment(DataSource source, Entry[] data) {

		Entry ref = entry(data[0].getID());
		if (ref != null) {
			List<Alignment> list = new ArrayList<Alignment>();
			ReferenceSequence rs = new ReferenceSequence(data[0].sequence);
			for (int i = 0; i < data.length; i++) {
				System.out.println(data[i].getID());
				Alignment align = new Alignment(data[i].getID(), data[i].sequence, rs);
				list.add(align);
				System.out.println("adding alignment: " + align);
			}
			ref.alignment.addAll(list);

			loadedSources.add(source);
			updateTracklist();
		}

	}

	// /*
	// * Looks in the list of loaded entries and returns the one with a matching
	// * id. Null is returned when no entries match
	// */
	// private Entry findEntry(String id) {
	// if (id == null)
	// return null;
	// for (Entry f : entries) {
	// if (id.equals(f.getID()))
	// return f;
	// }
	// return null;
	//
	// }

	public class TrackList extends CopyOnWriteArrayList<Track> {

		private Model model;

		public TrackList(Model model) {
			this.model = model;
		}

		private static final long serialVersionUID = 6716276343672660196L;

		public boolean containsType(Type type) {
			for (Track track : this) {
				if (track instanceof FeatureTrack) {
					if (((FeatureTrack) track).getType().equals(type))
						return true;

				}
			}
			return false;
		}

		public void down(int row) {
			if (row < this.size() - 1) {
				Track tmp = get(row);
				set(row, get(row + 1));
				set(row + 1, tmp);
				model.refresh();

			}

		}

		public void up(int row) {
			if (row > 0) {
				Track tmp = get(row);
				set(row, get(row - 1));
				set(row - 1, tmp);
				model.refresh();
			}

		}

		public boolean containsGraph(String name) {
			for (Track track : this) {
				if (track instanceof WiggleTrack) {
					if (((WiggleTrack) track).displayName().equals(name))
						return true;

				}
			}
			return false;
		}

		public boolean containsAlignment(int index) {
			for (Track track : this) {
				if (track instanceof MultipleAlignmentTrack) {
					if (((MultipleAlignmentTrack) track).getIndex() == index)
						return true;

				}
			}
			return false;
		}

		public boolean containsSyntenicTarget(String ref, String target) {
			for (Track track : this) {
				if (track instanceof SyntenicTrack) {
					SyntenicTrack st = ((SyntenicTrack) track);
					if (st.reference().equals(ref) && st.target().equals(target))
						return true;

				}
			}
			return false;
		}

		public boolean containsSequenceLogo() {
			for (Track track : this) {
				if (track instanceof SequenceLogoTrack) {

					return true;

				}
			}
			return false;
		}
	}

	/**
	 * Returns a list of all tracks. This method creates a copy to make it safe
	 * to iterate the returned list.
	 * 
	 * @return list of tracks
	 */
	public TrackList getTrackList() {
		return trackList;
	}

	private TrackList trackList;

	/*
	 * Method keeps the track list up to date.
	 * 
	 * All types and graphs loaded should have a corresponding track.
	 */
	private synchronized void updateTracklist() {
		for (Type t : Type.values()) {
			if (!trackList.containsType(t))
				trackList.add(new FeatureTrack(this, t, true));
		}
		for (Entry e : entries) {
			/* Graph tracks */
			for (Graph g : e.graphs.getGraphs()) {
				if (!trackList.containsGraph(g.getName()))
					trackList.add(new WiggleTrack(g.getName(), this, true));
			}
			/* Alignment and conservation tracks */
			for (int i = 0; i < e.alignment.numAlignments(); i++) {
				if (!trackList.containsAlignment(i))
					trackList.add(new MultipleAlignmentTrack(e.alignment.getAlignment(i).name(), i, this, true));
			}
			if (!trackList.containsSequenceLogo() && e.alignment.numAlignments() > 0)
				trackList.add(new SequenceLogoTrack(this));
			/* Syntenic tracks */
			Set<String> targets = e.syntenic.getTargets();
			if (targets.size() > 0 && !trackList.containsSyntenicTarget(e.getID(), e.getID())) {
				trackList.add(new SyntenicTrack(this, e.getID(), e.getID()));
			}
			for (String t : targets) {
				if (!trackList.containsSyntenicTarget(e.getID(), t))
					trackList.add(new SyntenicTrack(this, e.getID(), t));
			}

		}
		refresh();

	}

	public void clearLocationSelection() {
		selectedLocation.clear();
		refresh();

	}

	private Location selectedRegion = null;

	public final Location getSelectedRegion() {
		return selectedRegion;
	}

	public final void setSelectedRegion(Location selectedRegion) {
		this.selectedRegion = selectedRegion;
		refresh();
	}

	public Set<Feature> getHighlightedFeatures() {
		return highlightedRich;
	}

	/**
	 * selected features and children/parents of selected features
	 */
	private SortedSet<Feature> highlightedRich = null;

	public SortedSet<Feature> getFeatureSelection() {
		SortedSet<Feature> out = new TreeSet<Feature>();
		for (Location l : selectedLocation) {
			out.add(l.getParent());
		}
		return out;
	}

	private Stack<ChangeEvent> undoStack = new Stack<ChangeEvent>();

	private Stack<ChangeEvent> redoStack = new Stack<ChangeEvent>();

	public boolean hasRedo() {
		return redoStack.size() > 0;
	}

	public boolean hasUndo() {
		return undoStack.size() > 0;
	}

	public void undo() {
		ChangeEvent e = undoStack.pop();
		e.undoChange();
		redoStack.push(e);
		refresh();
	}

	public void redo() {
		ChangeEvent e = redoStack.pop();
		e.doChange();
		undoStack.push(e);
		refresh();
	}

	public String getUndoDescription() {
		if (hasUndo())
			return "Undo: " + undoStack.peek();
		else
			return "";
	}

	public String getRedoDescription() {
		if (hasRedo())
			return "Redo: " + redoStack.peek();
		else
			return "";
	}

	private HashMap<DataSource, Boolean> sourceVisibility = new DefaultHashMap<DataSource, Boolean>(Boolean.TRUE);

	public boolean isSourceVisible(DataSource dataSource) {
		return sourceVisibility.get(dataSource);
	}

	public void setSourceVisibility(DataSource key, boolean b) {
		sourceVisibility.put(key, b);
		refresh();
	}

	/* Cache of the sources that are currently loaded */
	private Set<DataSource> loadedSources = new HashSet<DataSource>();

	private int pressTrack;

	public Set<DataSource> loadedSources() {
		return loadedSources;

	}

	public int getPressTrack() {
		return pressTrack;
	}

	/**
	 * Keeps track of which track was used for selecting a region. <code>
	 * 4 -> AA
	 * 3 -> AA
	 * 2 -> AA
	 * 1 -> forward nucleotides
	 * 0 ->tick marks
	 * -1 -> reverse nucleotides
	 * -2 -> AA
	 * -3 -> AA
	 * -4 -> AA
     * </code>
	 * 
	 * @param pressTrack
	 */
	public void setSelectedTrack(int pressTrack) {
		this.pressTrack = pressTrack;
	}
	
	private GUIManager guimanager = new GUIManager();

	public GUIManager getGUIManager() {
		return guimanager;
	}

}
