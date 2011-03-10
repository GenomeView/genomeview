/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Color;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.provider.TDFProvider;
import net.sf.genomeview.data.provider.WiggleProvider;
import net.sf.genomeview.gui.external.JavaScriptHandler;
import net.sf.genomeview.gui.viztracks.TickmarkTrack;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.annotation.FeatureTrack;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.genomeview.gui.viztracks.comparative.MultipleAlignmentTrack;
import net.sf.genomeview.gui.viztracks.comparative.MultipleAlignmentTrack2;
import net.sf.genomeview.gui.viztracks.graph.WiggleTrack;
import net.sf.genomeview.gui.viztracks.hts.PileupTrack;
import net.sf.genomeview.gui.viztracks.hts.ShortReadTrack;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Data;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Location;
import net.sf.jannot.MemoryFeatureAnnotation;
import net.sf.jannot.Strand;
import net.sf.jannot.Type;
import net.sf.jannot.alignment.maf.MAFMultipleAlignment;
import net.sf.jannot.alignment.mfa.AlignmentAnnotation;
import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.pileup.Pile;
import net.sf.jannot.shortread.ReadGroup;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.MultiFileSource;
import net.sf.jannot.tabix.BEDWrapper;
import net.sf.jannot.tabix.GFFWrapper;
import net.sf.jannot.tabix.PileupWrapper;
import net.sf.jannot.tabix.SWigWrapper;
import net.sf.jannot.tdf.TDFData;
import net.sf.jannot.wiggle.Graph;
import be.abeel.net.URIFactory;
import be.abeel.util.DefaultHashMap;

/**
 * The Model.
 * 
 * @author Thomas Abeel
 * 
 */
public class Model extends Observable implements IModel {
	private Logger logger = Logger.getLogger(Model.class.getCanonicalName());

	private EntrySet entries = new EntrySet();

	private SelectionModel selectionModel = new SelectionModel();
	private MouseModel mouseModel = new MouseModel();

	public MouseModel mouseModel() {
		return mouseModel;
	}

	public Model(String id, String config) {
		/* Load the additional configuration */
		if (config != null) {
			try {
				if (config.startsWith("http") || config.startsWith("ftp")) {
					Configuration.loadExtra(URIFactory.url(config).openStream());
				} else {
					Configuration.loadExtra(new FileInputStream(config));
				}
			} catch (MalformedURLException e) {
				logger.log(Level.SEVERE, "loading extra configuration", e);
			} catch (IOException e) {
				logger.log(Level.SEVERE, "loading extra configuration", e);
			} catch (URISyntaxException e) {
				logger.log(Level.SEVERE, "loading extra configuration", e);
			}
		}

		guimanager = new GUIManager();

		/* JavaScriptInputHandler */
		if (Configuration.getBoolean("integration:monitorJavaScript")) {
			new JavaScriptHandler(this, id);
			logger.info("JavaScriptHandler started");
		} else {
			logger.info("JavaScriptHandler NOT started");
		}

		/* Scheduler booster thread */
		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true && !isExitRequested()) {
					GenomeViewScheduler.boost(getAnnotationLocationVisible());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						/* This happens when restarting the Applet */
						return;
					}
				}

			}
		}).start();

		selectionModel.addObserver(this);
		this.trackList = new TrackList(this);
		// entries.addObserver(this);

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
		logger.warning("Entry not found: " + id);
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
		selectionModel.clear();
		annotationStart = 0;
		annotationEnd = 0;
		loadedSources.clear();
		entries.clear();
		undoStack.clear();
		redoStack.clear();
		trackList.clear();
		refresh(NotificationTypes.GENERAL);
	}

	// private void clearTrackList(TrackList tracklist) {
	// List<Track> remove = new ArrayList<Track>();
	// for (Track t : tracklist) {
	//
	// if (!(t instanceof FeatureTrack || t instanceof StructureTrack || t
	// instanceof TickmarkTrack))
	// remove.add(t);
	// }
	// tracklist.removeAll(remove);
	// refresh();
	//
	// }

	public EntrySet entries() {
		return entries;

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

		loadedSources.clear();
		refresh();

	}

	public Location getAnnotationLocationVisible() {
		return new Location(annotationStart, annotationEnd);
	}

	private int annotationStart = 0, annotationEnd = 0;

	public void setAnnotationLocationVisible(Location r, boolean mayExpand) {
		int modStart = -1;
		int modEnd = -1;
		if (r.start > 1) {
			modStart = r.start;
		} else {
			modStart = 1;
			modEnd = r.length();
		}
		int chromLength = getSelectedEntry().getMaximumLength();
		if (r.end < chromLength || chromLength == 0) {
			modEnd = r.end;
		} else {
			modEnd = chromLength;
			modStart = modEnd - r.length();
			if (modStart < 1)
				modStart = 1;
		}
		Location newZoom = new Location(modStart, modEnd);
		/* When trying to zoom to something really small */
		if (newZoom.length() < 50 && mayExpand) {
			setAnnotationLocationVisible(new Location(modStart - 25, modEnd + 25));
		}
		if (newZoom.length() != annotationEnd - annotationStart + 1 && newZoom.length() < 50)
			return;
		// if (newZoom.length() != annotationEnd - annotationStart + 1
		// && newZoom.length() > Configuration.getInt("general:zoomout"))
		// return;
		if (newZoom.start < 1 || newZoom.end < 1)
			return;

		ZoomChange zc = new ZoomChange(new Location(annotationStart, annotationEnd), newZoom);
		zc.doChange();
		refresh();

	}

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
		setAnnotationLocationVisible(r, false);

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

	private ConcurrentLinkedQueue<Highlight> highlights = new ConcurrentLinkedQueue<Highlight>();

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
	 * Load new entries from a data source.
	 * 
	 * 
	 * This should only be done by a ReadWorker.
	 * 
	 * @param f
	 *            data source to load data from
	 * @throws ReadFailedException
	 * 
	 * FIXME move to read worker
	 */
	void addData(DataSource f) throws ReadFailedException {
		if (entries.size() == 0)
			setAnnotationLocationVisible(new Location(1, 51));
		logger.info("Reading source:" + f);
		try {
			f.read(entries);
		} catch (Exception e) {
			throw new ReadFailedException(e);
		}
		logger.info("Entries: " + entries.size());
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

	private HashMap<Entry, AminoAcidMapping> aamapping = new DefaultHashMap<Entry, AminoAcidMapping>(
			AminoAcidMapping.STANDARDCODE);

	// private Configuration trackMap;

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

	private final TrackList trackList;

	// public void addTrack(Track track) {
	// trackList.add(track);
	// refresh();
	// }

	public class TrackList implements Iterable<Track> {
		private Model model;
		private HashMap<DataKey, Track> mapping = new HashMap<DataKey, Track>();
		private List<DataKey> order = new ArrayList<DataKey>();

		public TrackList(Model model) {
			this.model = model;
			init();

		}

		public Track get(int index) {
			return mapping.get(order.get(index));
		}

		private void init() {
			TickmarkTrack ticks = new TickmarkTrack(model);
			add(ticks.getDataKey(),ticks);
			StructureTrack strack = new StructureTrack(model);
			add(strack.getDataKey(),strack);
			if (!Configuration.getBoolean("track:showStructure")) {
				System.out.println("Not visisble");
				strack.setVisible(false);

			}

		}

		public StructureTrack structure() {
			return (StructureTrack) mapping.get(StructureTrack.key);
		}

		private void add(DataKey dk,Track track) {
			mapping.put(dk, track);
			order.add(dk);

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

	/**
	 * Returns a list of all tracks. This method creates a copy to make it safe
	 * to iterate the returned list.
	 * 
	 * @return list of tracks
	 */
	public TrackList getTrackList() {
		return trackList;
	}

	/**
	 * This method keeps the track list up to date when adding new data to the
	 * entry from outside the model.
	 * 
	 * All types and graphs loaded should have a corresponding track.
	 */
	public synchronized void updateTracks() {
		try {
			int startSize = trackList.size();

			// for (Entry e : entries) {
			Entry e = this.getSelectedEntry();
			System.out.println("Updating tracks for " + e);
			/* Graph tracks */
			for (DataKey key : e) {
				Data<?> data = e.get(key);
				
				if (data instanceof MemoryFeatureAnnotation) {
					if (!trackList.containsTrack(key) && ((MemoryFeatureAnnotation) data).cachedCount() > 0)
						trackList.add(key,new FeatureTrack(this, (Type) key));

				}
				if (data instanceof GFFWrapper || data instanceof BEDWrapper) {
					if (!trackList.containsTrack(key))
						trackList.add(key,new FeatureTrack(this, (Type) key));

				}

				if (data instanceof PileupWrapper || data instanceof SWigWrapper ) {
					if (!trackList.containsTrack(key))
						trackList.add(key,new PileupTrack(new WiggleProvider(e, (Data<Pile>)data,this), this));
				}
				
				if (data instanceof TDFData ) {
					if (!trackList.containsTrack(key))
						trackList.add(key,new PileupTrack(new TDFProvider(e, (TDFData)data,this), this));
				}
				

				if (data instanceof Graph) {
					if (!trackList.containsTrack(key))
						trackList.add(key,new WiggleTrack(key, this, true));
				}
				if (data instanceof AlignmentAnnotation) {
					if (!trackList.containsTrack(key))
						trackList.add(key,new MultipleAlignmentTrack(this, key));
				}
				if (data instanceof ReadGroup) {
					if (!trackList.containsTrack(key)) {
						trackList.add(key,new ShortReadTrack(key, this));
					}
				}
				if (data instanceof MAFMultipleAlignment) {
					if (!trackList.containsTrack(key)) {
						trackList.add(key,new MultipleAlignmentTrack2(this, key));
						logger.info("Added multiple alignment track " + key);
					}
				}
			}
			if (trackList.size() != startSize)
				refresh(NotificationTypes.UPDATETRACKS);
		} catch (ConcurrentModificationException e) {
			logger.log(Level.INFO, "Update tracks interrupted, tracks already changed", e);
		}

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

	private final GUIManager guimanager;

	public GUIManager getGUIManager() {
		return guimanager;
	}

	@Override
	public Location getSelectedRegion() {
		return selectionModel.getSelectedRegion();
	}

	public SelectionModel selectionModel() {
		return selectionModel;
	}

	public Entry getSelectedEntry() {
		if (entries.size() == 0)
			return DummyEntry.dummy;
		return entries.getEntry();
	}

	public synchronized void setSelectedEntry(Entry entry) {
		entries.setDefault(entry);
		selectionModel.clear();

		setAnnotationLocationVisible(getAnnotationLocationVisible());
		trackList.clear();
		updateTracks();
		refresh(NotificationTypes.ENTRYCHANGED);

	}

	/**
	 * Removes a datakey from the visualization.
	 * 
	 * @param track
	 */
	public void remove(Track track) {
		if (!(track instanceof StructureTrack) && !(track instanceof TickmarkTrack)) {
			getSelectedEntry().remove(track.getDataKey());
			trackList.remove(track.getDataKey());
		}
		setChanged();
		notifyObservers(NotificationTypes.UPDATETRACKS);
	}

	public void change(ChangeEvent change) {
		undoStack.push(change);

	}

	private WorkerManager wm = new WorkerManager();

	public WorkerManager getWorkerManager() {
		return wm;

	}

	public synchronized Throwable processException(){
		if(!exceptionStack.isEmpty())
			return exceptionStack.pop();
		return null;
	}
	
	private Stack<Throwable>exceptionStack=new Stack<Throwable>();
	/**
	 * Method to register daemon exceptions to the model.
	 * @param e
	 */
	public synchronized void daemonException(Throwable e) {
		exceptionStack.push(e);
		setChanged();
		notifyObservers(NotificationTypes.EXCEPTION);
		
	}
}
