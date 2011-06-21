/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JViewport;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.provider.Status;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;
import net.sf.jannot.Location;

/**
 * Abstract class for visualization tracks.
 */
public abstract class Track extends Observable {

	protected Model model;

	protected DataKey dataKey;
	protected Entry entry;

	public static void paintStatus(Graphics g,Iterable<Status> status, int y, int returnTrackHeight,Location visible,double screenWidth) {
		for (Status st : status) {
			// System.out.println("Not ready "+st.start()+"\t"+st.end());
			if (!st.isReady()) {
				int x1 = Convert.translateGenomeToScreen(st.start(), visible, screenWidth);
				int x2 = Convert.translateGenomeToScreen(st.end()+1, visible, screenWidth);
				g.setColor(new Color(0, 255, 0, 100));

				g.fillRect(x1,y, x2 - x1 + 1, returnTrackHeight);
				if (visible.overlaps(st.start(), st.end())) {
					g.setColor(Color.BLACK);
					g.drawString("Retrieving data...", 100,y + returnTrackHeight / 2);
				}

			}
		}
		
	}
	
	public Track(DataKey key, Model model, boolean visible, boolean collapsible) {
		this.model = model;
		this.dataKey = key;
		log.log(Level.INFO, "Creating track\t"+key+"\t"+visible+"\t"+collapsible);
		this.entry = model.getSelectedEntry();
		this.collapsible = collapsible;
		this.addObserver(model);
	}

	//private boolean visible;
	private static final Logger log = Logger.getLogger(Track.class.getCanonicalName());

	/**
	 * To pass along mouse clicks from the original panel.
	 */
	public boolean mouseClicked(int x, int y, MouseEvent source) {
		if (collapse != null && collapse.contains(x, y)) {
			log.finest("Track consumes click");
			this.setCollapsed(!this.isCollapsed());
			source.consume();
			return true;
		}
		return false;
	}

	/**
	 * To pass along mouse movements from the original panel.
	 */
	public boolean mouseMoved(int x, int y, MouseEvent source) {
		return false;
	}

	/**
	 * To pass along mouse enters from the original panel.
	 */
	public boolean mouseEntered(int x, int y, MouseEvent source) {
		return false;
	}

	/**
	 * To pass along mouse exits from the original panel.
	 */
	public boolean mouseExited(int x, int y, MouseEvent source) {
		return false;
	}

	/**
	 * To pass along mouse presses from the original panel.
	 */
	public boolean mousePressed(int x, int y, MouseEvent source) {
		return false;
	}

	/**
	 * To pass along mouse releases from the original panel.
	 */
	public boolean mouseReleased(int x, int y, MouseEvent source) {
		return false;
	}

	/**
	 * To pass along mouse dragging from the original panel.
	 */
	public boolean mouseDragged(int x, int y, MouseEvent source) {
		return false;
	}

	private Rectangle collapse = null;
	private Color[] background = new Color[] { new Color(204, 238, 255, 75), new Color(255, 255, 204, 75) };

	private void paintCollapse(Graphics2D g, int yOffset, double width) {
		if (isCollapsible()) {
			g.translate(0, yOffset);
			collapse = new Rectangle((int) width - 15, 5, 10, 10);
			g.setColor(Color.WHITE);
			g.fill(collapse);
			g.setColor(Color.BLACK);
			g.draw(collapse);
			g.drawLine(collapse.x + 2, 10, collapse.x + 8, 10);
			if (isCollapsed())
				g.drawLine(collapse.x + 5, 7, collapse.x + 5, 13);
			g.translate(0, -yOffset);
		}
	}

	/**
	 * Paint this track in the annotation label and return the height it
	 * occupies.
	 * 
	 * @param g
	 *            graphics context to paint on
	 * @param view
	 * @param tcm 
	 * @param model
	 *            the entry that is currently displayed
	 * @return the height that was painted
	 */
	public int paint(Graphics g, int yOffset, double width, int index, JViewport view, TrackCommunicationModel tcm) {

		int used = paintTrack((Graphics2D) g, yOffset, width, view,tcm);

		if (!(this instanceof StructureTrack)) {
			Rectangle r = new Rectangle(0, yOffset, (int) width + 1, used);
			g.setColor(background[index % 2]);
			g.fillRect(r.x, r.y, r.width, r.height);
		}

		paintCollapse((Graphics2D) g, yOffset, width);
		return used;
	}

	/**
	 * Paint the actual track
	 * 
	 * @param g
	 *            graphics environment
	 * @param e
	 *            the currently visible entry
	 * @param yOffset
	 *            the yOffset that should be taken into account when painting
	 * @param width
	 *            the width of the track
	 * @param view
	 * @param tcm 
	 * @return the height this track uses
	 */
	protected abstract int paintTrack(Graphics2D g, int yOffset, double width, JViewport view, TrackCommunicationModel tcm);

	/* Keeps track of whether a track is collapsible */
	private boolean collapsible = false;
	/* Keeps track of the actual collapse state of the track */
	private boolean collapsed = Configuration.getBoolean("track:defaultCollapse");

	protected void setCollapsible(boolean collapsible) {
		this.collapsible = collapsible;
		setChanged();
		notifyObservers();
	}

	public void setCollapsed(boolean collapsed) {
		this.collapsed = collapsed;
		setChanged();
		notifyObservers();
	}

	public boolean isCollapsible() {
		return collapsible;
	}

	public boolean isCollapsed() {
		return collapsed;
	}

	public boolean isVisible() {
		return Configuration.getVisible(dataKey);
	}

	public void setVisible(boolean visible) {
		Configuration.setVisible(dataKey,visible);
		setChanged();
		notifyObservers();
	}

	
	public abstract String displayName();


	public DataKey getDataKey() {
		return dataKey;
	}

	public List<JMenuItem> getMenuItems() {
		return new ArrayList<JMenuItem>();
	}

}
