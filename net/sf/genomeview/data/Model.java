/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

import javax.swing.JFrame;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.annotation.track.FeatureTrack;
import net.sf.genomeview.gui.annotation.track.MultipleAlignmentTrack;
import net.sf.genomeview.gui.annotation.track.MultipleAlignmentTrack2;
import net.sf.genomeview.gui.annotation.track.SequenceLogoTrack;
import net.sf.genomeview.gui.annotation.track.ShortReadTrack;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.SyntenicTrack;
import net.sf.genomeview.gui.annotation.track.TickmarkTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.gui.annotation.track.WiggleTrack;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.alignment.MultipleAlignment;
import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.MultiFileSource;
import net.sf.jannot.wiggle.Graph;
import be.abeel.util.DefaultHashMap;

public class Model extends Observable implements IModel {
	private Logger logger = Logger.getLogger(Model.class.getCanonicalName());

	private EntrySet entries = new EntrySet(null);

	public Model(JFrame parent) {
		this.parent = parent;
		this.trackList = new TrackList(this);
		entries.addObserver(this);
		StructureTrack strack = new StructureTrack(this);
		trackList.add(strack);
		TickmarkTrack ticks = new TickmarkTrack(this);
		trackList.add(ticks);

		Set<Type> tmp1 = Configuration.getTypeSet("visibleTypesStructure");
		for (Type t : tmp1)
			strack.setTypeVisible(t, true);

		Configuration.getTypeSet("visibleTypes");
		updateTracks();

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
		logger.warning("Entry not found: "+id);
		return null;
	}

	public void update(Observable arg0, Object arg) {
		if (arg instanceof ChangeEvent) {
			undoStack.push((ChangeEvent) arg);
			redoStack.clear();
			while (undoStack.size() > 100)
				undoStack.remove(0);
			refresh(NotificationTypes.JANNOTCHANGE);
		} else {
			refresh(arg);
		}

	}

	public void clearEntries() {
		this.setAnnotationLocationVisible(new Location(1, 1));
		loadedSources.clear();
		entries.clear();
		selectedLocation.clear();
		selectedRegion = null;
		undoStack.clear();
		redoStack.clear();
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

	public EntrySet entries() {
		return entries;

	}

	public Entry getSelectedEntry() {
		if (entries.size() == 0)
			return DummyEntry.dummy;
		return entries.getEntry();
	}

	public void setSelectedEntry(Entry entry) {
		for (Entry e : entries)
			for (ReadGroup rg : e.shortReads.getReadGroups()) {
				rg.release();
			}
		entries.setDefault(entry);
		selectedLocation.clear();
		selectedRegion = null;

		setAnnotationLocationVisible(getAnnotationLocationVisible());

		refresh(NotificationTypes.ENTRYCHANGED);

	}

	/**
	 * The main window of the GUI belonging to this model.
	 */
	private JFrame parent;

	public JFrame getParent() {
		return parent;
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
			notifyObservers(arg == null ? NotificationTypes.GENERAL : arg);
		}
	}

	/**
	 * Checked way to notify all model observers.
	 */
	@Deprecated
	public void refresh() {
		refresh(NotificationTypes.GENERAL);

	}

	private boolean exitRequested = false;

	public void exit() {
		this.exitRequested = true;
		for (DataSource ds : loadedSources) {
			ds.finalize();
		}
		loadedSources.clear();
		refresh();

	}

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

		int modStart = -1;
		int modEnd = -1;
		if (r.start > 1) {
			modStart = r.start;
		} else {
			modStart = 1;
			modEnd = r.length();
		}
		int chromLength = getSelectedEntry().size();
		if (r.end < chromLength || chromLength == 0) {
			modEnd = r.end;
		} else {
			modEnd = chromLength;
			modStart = modEnd - r.length();
			if (modStart < 1)
				modStart = 1;
		}
		Location newZoom = new Location(modStart, modEnd);
		if (newZoom.length() != annotationEnd - annotationStart + 1 && newZoom.length() < 50)
			return;
		if (newZoom.length() != annotationEnd - annotationStart + 1 && newZoom.length() >Configuration.getInt("general:zoomout"))
			return;
		if(newZoom.start<1||newZoom.end<1)
			return;
		
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
	public void addData(DataSource f) throws ReadFailedException {
		boolean firstEntry = entries.size() == 0;
		logger.info("Reading source:" + f);
		f.read(entries);
		if (firstEntry) {
			entries.getEntry();// select a default
			setAnnotationLocationVisible(new Location(1, 10000));
		}

		logger.info("Model adding data done!");
		if (f instanceof MultiFileSource)
			for (DataSource ds : ((MultiFileSource) f).getFileSources()) {
				loadedSources.add(ds);
			}
		else
			loadedSources.add(f);
		updateTracks();
		refresh(NotificationTypes.GENERAL);
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

		public boolean containShortReadTrack(DataSource rg) {
			for (Track track : this) {
				if (track instanceof ShortReadTrack) {
					ShortReadTrack srt = (ShortReadTrack) track;
					if (srt.source().equals(rg))
						return true;

				}
			}
			return false;
		}

		public boolean containsMultipleAlignment(MultipleAlignment ma) {
			for (Track track : this) {
				if (track instanceof MultipleAlignmentTrack2) {
					MultipleAlignmentTrack2 srt = (MultipleAlignmentTrack2) track;
					
					if (srt.getMA().equals(ma))
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

	/**
	 * This method keeps the track list up to date when adding new data to the
	 * entry from outside the model.
	 * 
	 * All types and graphs loaded should have a corresponding track.
	 */
	public synchronized void updateTracks() {
		int startSize=trackList.size();
		for (Type t : Type.values()) {
			if (!trackList.containsType(t))
				trackList.add(new FeatureTrack(this, t, true));
		}
		for (Entry e : entries) {
			/* Graph tracks */
			for (Graph g : e.graphs.getAll()) {
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

			/* Short read tracks */
			for (DataSource rg : e.shortReads.getSources()) {
				if (!trackList.containShortReadTrack(rg)) {
					trackList.add(new ShortReadTrack(this, rg));
				}
			}

		}
		/* Syntenic tracks */
		Set<String> targets = entries.syntenic.getTargets();
		for (String s : targets) {
			for (String t : targets) {
				if (!trackList.containsSyntenicTarget(s, t))
					trackList.add(new SyntenicTrack(this, s, t));
			}
		}

		/* Multiple alignments tracks */
		for (MultipleAlignment ma : entries().multiplealignment) {
			if (!trackList.containsMultipleAlignment(ma)){
				trackList.add(new MultipleAlignmentTrack2(this, ma));
				logger.info("Added multiple alignment track "+ma);
			}
		}
		for (String s : targets) {
			for (String t : targets) {
				if (!trackList.containsSyntenicTarget(s, t))
					trackList.add(new SyntenicTrack(this, s, t));
			}
		}
		if(trackList.size()!=startSize)
			refresh(NotificationTypes.UPDATETRACKS);

	}

	public void clearLocationSelection() {
		selectedLocation.clear();
		refresh();

	}

	private Location selectedRegion = null;

	@Override
	public final Location getSelectedRegion() {
		return selectedRegion;
	}

	public final void setSelectedRegion(Location selectedRegion) {
		this.selectedRegion = selectedRegion;
		refresh();
	}

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
	private ConcurrentSkipListSet<DataSource> loadedSources = new ConcurrentSkipListSet<DataSource>();

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
