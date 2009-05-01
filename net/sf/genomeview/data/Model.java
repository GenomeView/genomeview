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
import java.util.Observer;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.swing.JFrame;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DisplayType;
import net.sf.genomeview.gui.annotation.track.FeatureTrack;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.TickmarkTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.gui.annotation.track.WiggleTrack;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.genomeview.plugin.IValueFeature;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Annotation;
import net.sf.jannot.Entry;
import net.sf.jannot.Feature;
import net.sf.jannot.Graph;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.MultiFileSource;
import be.abeel.util.DefaultHashMap;

public class Model extends Observable implements Observer, IModel {
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

		Set<Type> tmp = Configuration.getTypeSet("visibleTypes");
		updateTracklist();

	

	}

	public int noEntries() {
		return entries.size();
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
		this.setChromosomeLocationVisible(new Location(1, 1));
		entries.clear();
		selectedEntry = null;
		selectedLocation.clear();
		selectedRegion = null;
		refresh(NotificationTypes.GENERAL);
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

	// private Map<Type, DisplayType> displayTypeMapping = new HashMap<Type,
	// DisplayType>();
	//
	// public DisplayType getDisplayType(Type type) {
	// if (displayTypeMapping.containsKey(type)) {
	// return displayTypeMapping.get(type);
	// } else {
	// String sdt = Configuration.get("DT_" + type);
	// if (sdt != null)
	// return DisplayType.valueOf(sdt);
	// else
	// return DisplayType.MultiLineBlocks;
	// }
	//
	// }
	//
	// @Override
	// public void setDisplayType(Type type, DisplayType dt) {
	// displayTypeMapping.put(type, dt);
	// refresh();
	//
	// }

	private Map<Type, Boolean> visibleOnChromosome = new DefaultHashMap<Type, Boolean>(
			Boolean.FALSE);

	public boolean isVisibleOnChromosome(Type ct) {
		return visibleOnChromosome.get(ct);
	}

	// private Map<Type, Boolean> showTextOnStructure = new DefaultHashMap<Type,
	// Boolean>(
	// Boolean.FALSE);

	// public boolean isShowText(Type ct) {
	// return showTextOnStructure.get(ct);
	// }

	private Map<Type, Boolean> showChromText = new DefaultHashMap<Type, Boolean>(
			Boolean.FALSE);

	public boolean isShowChromText(Type ct) {
		return showChromText.get(ct);
	}

	// private Map<Type, Boolean> visibleOnAnnotation = new DefaultHashMap<Type,
	// Boolean>(
	// Boolean.FALSE);

	// public boolean isVisibleOnAnnotation(Type ct) {
	// return visibleOnAnnotation.get(ct);
	// }
	//
	// public void setShowText(Type type, boolean b) {
	// showTextOnStructure.put(type, b);
	// refresh();
	// }
	//
	public void setShowChromText(Type type, boolean b) {
		showChromText.put(type, b);
		refresh();

	}

	/* Keeps track of individually hidden features. */
	private Map<Feature, Boolean> featureVisible = new DefaultHashMap<Feature, Boolean>(
			Boolean.TRUE);

	public boolean isFeatureVisible(Feature f) {
		return featureVisible.get(f);
	}

	public void setFeatureVisible(Feature rf, boolean b) {
		featureVisible.put(rf, b);
		refresh();

	}

	// /**
	// * Provides a mapping between the Entry and the associated value features.
	// */
	// private Map<Entry, List<IValueFeature>> valueFeatures = new
	// DefaultHashMap<Entry, List<IValueFeature>>(
	// new ArrayList<IValueFeature>());

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
		ZoomChange zc = new ZoomChange(new Location(annotationStart,
				annotationEnd), newZoom);
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

	private boolean annotationAvailable = true;

	private boolean annotationVisible = true;

	public boolean isAnnotationAvailable() {
		return annotationAvailable;
	}

	public boolean isAnnotationVisible() {
		return annotationVisible && annotationAvailable;
	}

	private boolean structureAvailable = true;

	private boolean structureVisible = true;

	public boolean isStructureAvailable() {
		return structureAvailable;
	}

	public boolean isStructureVisible() {
		return structureVisible && structureAvailable;
	}

	public boolean isChromosomeAvailable() {
		return chromosomeAvailable;
	}

	/* Flag to indicate whether the chromosome panel is visible */
	private boolean chromosomeVisible = true;

	private boolean chromosomeAvailable = true;

	public boolean isChromosomeVisible() {
		return chromosomeVisible && chromosomeAvailable;
	}

	public boolean isExitRequested() {
		return exitRequested;
	}

	// public void setVisibleOnAnnotation(Type type, boolean b) {
	// visibleOnAnnotation.put(type, b);
	// refresh();
	//
	// }
	//
	public void setVisibleOnChromosome(Type type, boolean b) {
		visibleOnChromosome.put(type, b);
		refresh();

	}

	//
	// public boolean isShowTextOnStructure(Type type) {
	// return showTextOnStructure.get(type);
	// }

	public int getLongestChromosomeLength() {
		int longest = 0;
		for (Entry e : entries)
			if (e.sequence.size() > longest)
				longest = e.sequence.size();
		return longest;
		// return selectedGenomeCache.getLongestSequenceLength();
	}

	public Location getChromosomeLocationVisible() {
		return new Location(chrStart, chrEnd);

	}

	private int chrStart;

	private int chrEnd;

	public void setChromosomeLocationVisible(Location r) {
		int newStart = r.start();
		int newEnd = r.end();
		if (newStart > newEnd) {
			return;
		}
		if (newStart > 1) {
			this.chrStart = newStart;
		} else {
			this.chrStart = 1;
		}

		if (newEnd < getLongestChromosomeLength()) {
			this.chrEnd = newEnd;
		} else {
			this.chrEnd = getLongestChromosomeLength();
		}
		refresh();
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
			if (f.location.end() > region.start()
					&& f.location.start() < region.end())
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
		setAnnotationLocationVisible(new Location(genomePosition - length,
				genomePosition + length));

	}

	/**
	 * Center the ChromosomeView on a certain position, while maintaining the
	 * zoom level of the view.
	 */
	public void centerChromView(int genomePosition) {
		int chromLengtVis = getChromosomeLocationVisible().length();
		int viewStart = genomePosition - chromLengtVis / 2;
		int viewEnd = viewStart + chromLengtVis - 1;
		if (viewStart < 1) {
			viewStart = 1;
			viewEnd = chromLengtVis;
		}
		if (viewEnd > getLongestChromosomeLength()) {
			viewEnd = getLongestChromosomeLength();
			viewStart = viewEnd - chromLengtVis + 1;
		}
		setChromosomeLocationVisible(new Location(viewStart, viewEnd));
	}

	/**
	 * Contains all selected locations, these can be subcomponents of a Feature.
	 */
	private SortedSet<Location> selectedLocation = new TreeSet<Location>();

	public void addLocationSelection(Location rl) {
		selectedLocation.add(rl);
		checkChromosomeVisibility();
		refresh();
	}

	private void checkChromosomeVisibility() {
		if (this.getLocationSelection().size() > 0) {
			boolean someThingVisible = false;
			Location r = this.getChromosomeLocationVisible();
			for (Location l : this.getLocationSelection()) {
				if (r.overlaps(l)) {
					someThingVisible = true;
				}
			}
			if (!someThingVisible) {
				Location l = this.getLocationSelection().first();
				int center = (l.start() + l.end()) / 2;
				this.centerChromView(center);
			}
		}
	}

	public SortedSet<Location> getLocationSelection() {
		return selectedLocation;
	}

	public void setLocationSelection(Feature rl) {
		selectedLocation.clear();
		for (Location l : rl.location())
			selectedLocation.add(l);
		checkChromosomeVisibility();
		refresh();

	}

	public void setLocationSelection(Location rl) {
		selectedLocation.clear();
		this.addLocationSelection(rl);

	}

	public void setStructureVisible(boolean b) {
		this.structureVisible = b;
		refresh();

	}

	public final void setAnnotationVisible(boolean annotationVisible) {
		this.annotationVisible = annotationVisible;
		refresh();
	}

	public final void setChromosomeVisible(boolean chromosomeVisible) {
		this.chromosomeVisible = chromosomeVisible;
		refresh();
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
				setChromosomeLocationVisible(new Location(1,
						selectedEntry.sequence.size()));
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

	private HashMap<Entry, AminoAcidMapping> aamapping = new DefaultHashMap<Entry, AminoAcidMapping>(
			AminoAcidMapping.STANDARDCODE);

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
	public Entry[] addFeatures(DataSource f) throws ReadFailedException {
		logger.info("adding features: " + f);

		Entry[] data = f.read();
		logger.info("entries read: " + data.length);

		for (Entry e : data) {
			Annotation a = e.annotation;
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
		return data;

	}

	public class TrackList extends ArrayList<Track> {

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

		public TrackList copy() {
			TrackList out=new TrackList(model);
			out.addAll(this);
			return out;
		}
	}

	/**
	 * Returns a list of all tracks. This method creates a copy to make it safe
	 * to iterate the returned list.
	 * 
	 * @return list of tracks
	 */
	public TrackList getTrackList() {
		return trackList.copy();
	}

	private TrackList trackList;

	/*
	 * Method keeps the track list up to date.
	 * 
	 * All types and graphs loaded should have a corresponding track.
	 */
	private void updateTracklist() {
		for (Type t : Type.values()) {
			if (!trackList.containsType(t))
				trackList.add(new FeatureTrack(this, t, true));
		}
		for (Entry e : entries) {
			for (Graph g : e.graphs.getGraphs()) {
				if (!trackList.containsGraph(g.getName()))
					trackList.add(new WiggleTrack(g.getName(), this, true));
			}
		}

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

	private HashMap<DataSource, Boolean> sourceVisibility = new DefaultHashMap<DataSource, Boolean>(
			Boolean.TRUE);

	public boolean isSourceVisible(DataSource dataSource) {
		return sourceVisibility.get(dataSource);
	}

	public void setSourceVisibility(DataSource key, boolean b) {
		sourceVisibility.put(key, b);
		refresh();
	}

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

	/**
	 * The feature that is hovered over in the ChromosomeView
	 */
	private Feature hoveredChromFeature;

	public void setHoveredChromFeature(Feature feat) {
		if (feat != hoveredChromFeature) {
			hoveredChromFeature = feat;
			refresh();
		}
	}

	public Feature getHoveredChromFeature() {
		return hoveredChromFeature;
	}

	private Entry hoveredChromEntry;

	public void setHoverChromEntry(Entry e) {
		if (e != hoveredChromEntry) {
			hoveredChromEntry = e;
			refresh();
		}
	}

	public Entry getHoveredChromEntry() {
		return hoveredChromEntry;
	}

	private GUIManager guimanager = new GUIManager();

	public GUIManager getGUIManager() {
		return guimanager;
	}

}
