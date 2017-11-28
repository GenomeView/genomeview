/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;

import javax.swing.DefaultListModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.abeel.io.LineIterator;
import be.abeel.util.DefaultHashMap;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.explorer.FilteredListModel;
import net.sf.genomeview.gui.external.JavaScriptHandler;
import net.sf.genomeview.gui.viztracks.TickmarkTrack;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.genomeview.plugin.GUIManager;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;

/**
 * The Model.
 * 
 * @author Thomas Abeel
 * 
 */
public class Model extends Observable implements Observer {
	private Logger logger = LoggerFactory.getLogger(Model.class.getCanonicalName());

	private EntrySet entries = new EntrySet();

	private SelectionModel selectionModel = new SelectionModel();
	private MouseModel mouseModel = new MouseModel();
	private MessageModel messageModel = new MessageModel(this);

	public MessageModel messageModel() {
		return messageModel;
	}

	public MouseModel mouseModel() {
		return mouseModel;
	}

	public Model(String id) {

		guimanager = new GUIManager();

		new JavaScriptHandler(this, id);
		logger.info("JavaScriptHandler started");

		GenomeViewScheduler.start(this);

		selectionModel.addObserver(this);
		messageModel.addObserver(this);
		final Model _this = this;
		vlm.addObserver(new Observer() {

			@Override
			public void update(Observable o, Object arg) {
				_this.refresh();

			}
		});
		this.trackList = new TrackList(this);
		// entries.addObserver(this);

		Configuration.getTypeSet("visibleTypes");
		updateTracks();

		try {

			File recent = new File(Configuration.getDirectory(), "recent.gv");
			if (recent.exists()&&recent.length()>0) {
				LineIterator it = new LineIterator(recent);
				while (it.hasNext())
					recentFiles.addElement(it.next());
			}
		} catch (Exception e) {
			CrashHandler.showErrorMessage("Could not retrieve recently used files", e);
		}

	}

	public int noEntries() {
		return entries.size();
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

	public final VisualLocationModel vlm = new VisualLocationModel();

	public void clearEntries() {
		selectionModel.clear();
		vlm.clear();
		// visible=new Location(0,0);
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

		try {
			if (Configuration.getBoolean("session:enableRememberLast")) {

				/*
				 * Write recent files to disk
				 */
				PrintWriter pw = new PrintWriter(new File(Configuration.getDirectory(), "recent.gv"));

				for (int i = 0; i < recentFiles.getSize(); i++) {

					pw.println(recentFiles.getElementAt(i));
				}
				pw.close();

				/* Only store session if there is something to store */
				if (this.loadedSources().size() > 0)
					Session.save(new File(Configuration.getDirectory(), "previous.gvs"), this);
			}
		} catch (IOException e) {
			logger.error("Problem saving last session", e);
		}
		loadedSources.clear();
		refresh();

	}

	// public Location getAnnotationLocationVisible() {
	// return visible;
	// }

	// private Location visible=new Location(0,0);
	// private int annotationStart = 0, annotationEnd = 0;

	// /**
	// * Set the visible area in the evidence and structure frame to the given
	// * Location.
	// *
	// * start and end one-based [start,end]
	// *
	// * @param start
	// * @param annotationEnd
	// */
	//
	// public void setAnnotationLocationVisible(Location r) {
	// setAnnotationLocationVisible(r, false);
	//
	// }

	// /**
	// * Provides implementation to do/undo zoom changes.
	// *
	// * @author Thomas Abeel
	// *
	// */
	// class ZoomChange implements ChangeEvent {
	// /* The original zoom */
	// private Location orig;
	//
	// /* The new zoom */
	// private Location neww;
	//
	// public ZoomChange(Location location, Location newZoom) {
	// this.orig = location;
	// this.neww = newZoom;
	// }
	//
	// @Override
	// public void doChange() {
	// // annotationStart = neww.start();
	// // annotationEnd = neww.end();
	// visible=neww;
	//
	// }
	//
	// @Override
	// public void undoChange() {
	// assert (visible.start == neww.start());
	// assert (visible.end == neww.end());
	// visible=orig;
	//
	//
	// }
	//
	// }

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
	 * Load new entries from a data source.
	 * 
	 * 
	 * This should only be done by a ReadWorker.
	 * 
	 * @param f
	 *            data source to load data from
	 * @throws ReadFailedException
	 * 
	 *             FIXME move to read worker
	 */
	void addData(DataSource f) throws ReadFailedException {
		if (entries.size() == 0)
			vlm.setAnnotationLocationVisible(new Location(1, 51));
		logger.info("Reading source:" + f);
		recentFiles.removeElement(f.getLocator().toString());
		recentFiles.add(0, f.getLocator().toString());
		try {
			f.read(entries);
			if (entries.size() > 0 && vlm.getVisibleEntry() instanceof DummyEntry) {
				vlm.setVisibleEntry(entries.firstEntry());
				Entry selected = vlm.getVisibleEntry();
				int len = selected.getMaximumLength();
				if (len > 5000) {
					int randomStart = StaticUtils.rg.nextInt((len / 2) - 1000) + len / 4;
					logger.info("Setting random location at data load: " + selected + "\t" + randomStart);
					vlm.setAnnotationLocationVisible(new Location(randomStart, randomStart + 1000));
				}

			}
		} catch (Exception e) {
			throw new ReadFailedException(e);
		}
		logger.info("Entries: " + entries.size());
		logger.info("Model adding data done!");
		loadedSources.add(f);
		updateTracks();
		refresh(NotificationTypes.GENERAL);

	}

	private HashMap<Entry, AminoAcidMapping> aamapping = new DefaultHashMap<Entry, AminoAcidMapping>(AminoAcidMapping.valueOf(Configuration.get("translationTable:default")));

	// private Configuration trackMap;

	public AminoAcidMapping getAAMapping(Entry e) {
		return aamapping.get(e);
	}

	public AminoAcidMapping getAAMapping() {
		return aamapping.get(vlm.getSelectedEntry());
	}

	public void setAAMapping(Entry e, AminoAcidMapping aamapping) {
		logger.info("setting amino acid mapping: " + aamapping);
		this.aamapping.put(e, aamapping);
		refresh(NotificationTypes.TRANSLATIONTABLECHANGE);

	}

	private final TrackList trackList;

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

			Entry e = vlm.getSelectedEntry();
			boolean changed = trackList.update(e);

			if (changed)
				refresh(NotificationTypes.UPDATETRACKS);
		} catch (ConcurrentModificationException e) {
			logger.error("Update tracks interrupted, tracks already changed", e);
			refresh(NotificationTypes.UPDATETRACKS);
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

	public Location getSelectedRegion() {
		return selectionModel.getSelectedRegion();
	}

	public SelectionModel selectionModel() {
		return selectionModel;
	}

	//
	// public Entry getSelectedEntry() {
	// if (entries.size() == 0)
	// return DummyEntry.dummy;
	// return entries.getEntry();
	// }

	public synchronized void setSelectedEntry(Entry entry) {
		logger.info("Setting selected entry: " + entry);
		vlm.setVisibleEntry(entry);
		// entries.setDefault(entry);
		selectionModel.clear();

		vlm.setAnnotationLocationVisible(vlm.getVisibleLocation());
		trackList.clear();
		// FIXME updateTracks also does update :-/
		updateTracks();
		// FIXME likely double notification
		refresh(NotificationTypes.ENTRYCHANGED);

	}

	/**
	 * Removes a datakey from the visualization.
	 * 
	 * @param track
	 */
	public void remove(Track track) {
		if (!(track instanceof StructureTrack) && !(track instanceof TickmarkTrack)) {
			trackList.remove(track.getDataKey());
			for (Entry e : entries) {
				e.remove(track.getDataKey());
			}

		}

		GenomeViewScheduler.submit(Task.GC);
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

	public synchronized Throwable processException() {
		if (!exceptionStack.isEmpty())
			return exceptionStack.pop();
		return null;
	}

	private Stack<Throwable> exceptionStack = new Stack<Throwable>();

	// private Stack<Throwable>exceptionStack=new Stack<Throwable>();
	/**
	 * Method to register daemon exceptions to the model.
	 * 
	 * @param e
	 */
	public synchronized void daemonException(Throwable e) {
		exceptionStack.push(e);
		logger.error("Exception in daemon thread", e);
		setChanged();
		notifyObservers(NotificationTypes.EXCEPTION);

	}

	private AnnotationModel annotationModel = new AnnotationModel();

	public AnnotationModel annotationModel() {
		return annotationModel;
	}

	private FilteredListModel<String> recentFiles = new FilteredListModel<String>(new DefaultListModel<String>());

	public FilteredListModel<String> getRecentFiles() {
		return recentFiles;
	}

	private FilteredListModel<String> extraFiles = new FilteredListModel<String>(new DefaultListModel<String>());

	public FilteredListModel<String> getExtraSessionFiles() {
		return extraFiles;
	}
}
