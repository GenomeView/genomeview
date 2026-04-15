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
import java.util.logging.Level;

import javax.swing.DefaultListModel;

import be.abeel.io.LineIterator;
import be.abeel.util.DefaultHashMap;
import htsjdk.samtools.util.StringUtil;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.DistributingReporter;
import net.sf.genomeview.gui.GUIManager;
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.ConnectionMonitor;
import net.sf.genomeview.gui.explorer.FilteredListModel;
import net.sf.genomeview.gui.external.JavaScriptHandler;
import net.sf.genomeview.gui.menu.file.ExitAction;
import net.sf.genomeview.gui.viztracks.TickmarkTrack;
import net.sf.genomeview.gui.viztracks.Track;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.jannot.AminoAcidMapping;
import net.sf.jannot.Entry;
import net.sf.jannot.EntrySet;
import net.sf.jannot.Location;
import net.sf.jannot.Strand;
import net.sf.jannot.event.ChangeEvent;
import net.sf.jannot.exception.ReadFailedException;
import net.sf.jannot.source.DataSource;

/**
 * The Model. This seems to contain all the data sets loaded, and other models
 * to handle user inputs (clicking, selecting), a messagemodel for statusbar
 * messages.
 * 
 * @author Thomas Abeel
 * 
 */
public class Model extends Observable implements Observer {

	// the main logger for the system.
	private final DistributingReporter log;

	/**
	 * The EntrySet which contains all loaded 'chromosomes'.
	 */
	private final EntrySet entries = new EntrySet();

	private final SelectionModel selectionModel = new SelectionModel();
	private final MouseModel mouseModel = new MouseModel();
	private final MessageModel messageModel = new MessageModel(this);

	public final VisualLocationModel vlm = new VisualLocationModel();

	/**
	 * List of all available tracks. Some might be hidden from view.
	 */
	private final TrackList trackList;

	/**
	 * undo and redo support
	 */
	private final Stack<ChangeEvent> undoStack = new Stack<ChangeEvent>();

	private final Stack<ChangeEvent> redoStack = new Stack<ChangeEvent>();

	private final AnnotationModel annotationModel = new AnnotationModel();

	/**
	 * files used by the user
	 */
	private final FilteredListModel<String> recentFiles = new FilteredListModel<String>(
			new DefaultListModel<String>());

	private final FilteredListModel<String> extraFiles = new FilteredListModel<String>(
			new DefaultListModel<String>());

	/**
	 * Exceptions
	 */
	private Stack<Throwable> exceptionStack = new Stack<Throwable>();

	/* Cache of the sources that are currently loaded */
	private ConcurrentSkipListSet<DataSource> loadedSources = new ConcurrentSkipListSet<DataSource>();

	/**
	 * Keep track of where the user click in the (main?) track. See
	 * {@link #setSelectedTrack(int)}
	 */
	private int pressTrack;

	/**
	 * see {@link #setSilent(boolean)}
	 */
	private boolean silent;

	private boolean exitRequested = false;

	/**
	 * highlights contains results from {@link MotifSearchResultModel} and
	 * {@link SequenceSearchResultModel}
	 */
	private final ConcurrentLinkedQueue<Highlight> highlights = new ConcurrentLinkedQueue<Highlight>();

	/**
	 * amino-acid mapping - names of std sequences. See {@link AminoAcidMapping}
	 */
	private HashMap<Entry, AminoAcidMapping> aamapping = new DefaultHashMap<Entry, AminoAcidMapping>(
			AminoAcidMapping.valueOf(
					Configuration.instance().get("translationTable:default")));

	private final GUIManager guimanager;

	private WorkerManager wm = new WorkerManager();

	private final ConnectionMonitor connectionMonitor;

	/*
	 * 
	 * 
	 * FIELDS ABOVE THIS POINT. CODE STARTS HERE.
	 * 
	 */
	/**
	 * @param id  the name of this
	 * @param log a {@link DistributingReporter} to be used for general logging.
	 *            Not null
	 */

	public Model(String id, DistributingReporter log) {
		if (log == null)
			throw new NullPointerException("log must not be null");
		this.log = log;
		this.connectionMonitor = new ConnectionMonitor(log);
		guimanager = new GUIManager();

		new JavaScriptHandler(this, id);
		log.log(Level.INFO, "JavaScriptHandler started");

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

		Configuration.instance().getTypeSet("visibleTypes");
		updateTracks();

		try {

			File recent = new File(Configuration.instance().getDirectory(),
					"recent.gv");
			if (recent.exists() && recent.length() > 0) {
				LineIterator it = new LineIterator(recent);
				while (it.hasNext())
					recentFiles.addElement(it.next());
			}
		} catch (Exception e) {
			log.log(Level.WARNING, "Could not retrieve recently used files", e);
		}

	}

	public DistributingReporter getLog() {
		return log;
	}

	/**
	 * @return the {@link MessageModel} for statusbar
	 * 
	 */
	public MessageModel messageModel() {
		return messageModel;
	}

	public MouseModel mouseModel() {
		return mouseModel;
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

	/**
	 * Exit the system. Close resources. Triggered by {@link ExitAction}
	 */
	public void exit() {
		this.exitRequested = true;

		try {
			if (Configuration.instance()
					.getBoolean("session:enableRememberLast")) {

				/*
				 * Write recent files to disk
				 */
				PrintWriter pw = new PrintWriter(new File(
						Configuration.instance().getDirectory(), "recent.gv"));

				for (int i = 0; i < recentFiles.getSize(); i++) {

					pw.println(recentFiles.getElementAt(i));
				}
				pw.close();

				/* Only store session if there is something to store */
				if (this.loadedSources().size() > 0)
					Session.save(
							new File(Configuration.instance().getDirectory(),
									"previous.gvs"),
							this);
			}
		} catch (IOException e) {
			log.log(Level.WARNING, "Problem saving last session", e);
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

	/**
	 * 
	 * @param region {@link Location} of the highlight
	 * @return highlights / search results
	 */
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
	 * Load new entries from a data source.
	 * 
	 * 
	 * This should only be done by a ReadWorker.
	 * 
	 * @param f data source to load data from
	 * @throws ReadFailedException
	 * 
	 *                             FIXME move to read worker
	 */
	void addData(DataSource f) throws ReadFailedException {
		if (entries.size() == 0)
			vlm.setAnnotationLocationVisible(new Location(1, 51));
		log.log(Level.INFO, "Reading source:" + f);
		recentFiles.removeElement(f.getLocator().toString());
		recentFiles.add(0, f.getLocator().toString());
		try {
			f.read(entries);
			if (entries.size() > 0
					&& vlm.getVisibleEntry() instanceof DummyEntry) {
				vlm.setVisibleEntry(entries.firstEntry());
				Entry selected = vlm.getVisibleEntry();
				int len = selected.getMaximumLength();
				if (len > 5000) {
					int randomStart = StaticUtils.rg.nextInt((len / 2) - 1000)
							+ len / 4;
					log.log(Level.INFO, "Setting random location at data load: "
							+ selected + "\t" + randomStart);
					vlm.setAnnotationLocationVisible(
							new Location(randomStart, randomStart + 1000));
				}

			}
		} catch (Exception e) {
			throw new ReadFailedException(e);
		}
		log.log(Level.INFO, "Entries: " + entries.size());
		log.log(Level.INFO, "Model adding data done!");
		loadedSources.add(f);
		updateTracks();
		refresh(NotificationTypes.GENERAL);

	}

	// private Configuration trackMap;

	public AminoAcidMapping getAAMapping(Entry e) {
		return aamapping.get(e);
	}

	public AminoAcidMapping getAAMapping() {
		return aamapping.get(vlm.getVisibleEntry());
	}

	public void setAAMapping(Entry e, AminoAcidMapping aamapping) {
		log.log(Level.INFO, "setting amino acid mapping: " + aamapping);
		this.aamapping.put(e, aamapping);
		refresh(NotificationTypes.TRANSLATIONTABLECHANGE);

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

			Entry e = vlm.getVisibleEntry();
			boolean changed = trackList.update(e);

			if (changed)
				refresh(NotificationTypes.UPDATETRACKS);
		} catch (ConcurrentModificationException e) {
			log.log(Level.WARNING,
					"Update tracks interrupted, tracks already changed", e);
			refresh(NotificationTypes.UPDATETRACKS);
		}

	}

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
	 * @return GUIManager. THe main connection to the main window.
	 */
	public GUIManager getGUIManager() {
		return guimanager;
	}

	public Location getSelectedRegion() {
		return selectionModel.getSelectedRegion();
	}

	public SelectionModel selectionModel() {
		return selectionModel;
	}

	/**
	 * set current selected Entry.
	 * 
	 * @param entry the selected {@link Entry}
	 */
	public synchronized void setSelectedEntry(Entry entry) {
		log.log(Level.INFO, "Setting selected entry: " + entry);
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
		if (!(track instanceof StructureTrack)
				&& !(track instanceof TickmarkTrack)) {
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

	public WorkerManager getWorkerManager() {
		return wm;

	}

// not used and buggy. Disabled to be sure
//	public synchronized Throwable processException() {
//		if (!exceptionStack.isEmpty())
//			return exceptionStack.pop();
//		return null;
//	}

	/**
	 * Method to register daemon exceptions to the model. WARNING seems buggy as
	 * nobody handles exceptionStack events.
	 * 
	 * @param e
	 */
	public synchronized void daemonException(Throwable e) {
		exceptionStack.push(e);
		log.log(Level.SEVERE, "Exception in daemon thread", e);
		setChanged();
		notifyObservers(NotificationTypes.EXCEPTION);

	}

	public AnnotationModel annotationModel() {
		return annotationModel;
	}

	public FilteredListModel<String> getRecentFiles() {
		return recentFiles;
	}

	public FilteredListModel<String> getExtraSessionFiles() {
		return extraFiles;
	}

	/**
	 * 
	 * @param position the position to set to. This string can contain ":-" and
	 *                 is a special format (TODO reverse engineer this)
	 */
	public void setPosition(final String position) {

		Thread t = new Thread(new Runnable() {

			public void run() {
				try {
					boolean success = false;
					while (!success) {
						String[] tmp = StringUtil.reverseString(position)
								.split("[:-]", 3);
						String[] arr = new String[Math.min(tmp.length, 3)];
						for (int i = 0; i < arr.length; i++)
							arr[i] = StringUtil
									.reverseString(tmp[(arr.length - 1) - i]);
						/*
						 * If the location is not 2 or 3 tokens long, just stop
						 */
						if (arr.length > 3 || arr.length < 2) {
							getLog().log(Level.WARNING,
									MessageManager.getString(
											"externalhelper.couldnt_parse_location")
											+ " " + position);
							return;

						}
						if (hasEntry(arr)) {
							if (inRange(arr)) {
								if (arr.length == 3) {
									setSelectedEntry(
											entries().getEntry(arr[0]));
									vlm.setAnnotationLocationVisible(
											new Location(
													Integer.parseInt(arr[1]),
													Integer.parseInt(arr[2])));

								} else if (arr.length == 2) {
									vlm.setAnnotationLocationVisible(
											new Location(
													Integer.parseInt(arr[0]),
													Integer.parseInt(arr[1])));
								}
								success = true;

							}
						}
						try {
							Thread.sleep(250);
						} catch (InterruptedException e) {
							// Nothing to do in this case
						}
						if (!success) {
							getLog().log(Level.WARNING,
									"Failed to move to location: " + position
											+ ". This instruction has been requeued and will be retried.");
						}
					}
				} catch (NumberFormatException ne) {
					getLog().log(Level.WARNING,
							MessageManager.getString(
									"externalhelper.couldnt_parse_location")
									+ " " + position,
							ne);
				}
			}

			private boolean hasEntry(String[] arr) {
				if (entries().size() == 0)
					return false;

				if (arr.length == 2)
					return true;

				return (arr.length == 3 && entries().getEntry(arr[0]) != null);

			}

			private boolean inRange(String[] arr) {
				int max = Integer.parseInt(arr[arr.length - 1]);
				Entry e = null;
				if (arr.length == 2)
					e = vlm.getVisibleEntry();// model.entries().getEntry();
				else
					e = entries().getEntry(arr[0]);
				return max <= e.getMaximumLength();

			}
		});
		t.start();
	}

	public ConnectionMonitor getConnectionMonitor() {
		return connectionMonitor;
	}

}
