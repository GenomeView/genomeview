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
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JViewport;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.jannot.DataKey;
import net.sf.jannot.Entry;

/**
 * Abstract class for visualization tracks.
 */
public abstract class Track extends Observable {

	protected Model model;

	protected DataKey dataKey;
	protected Entry entry;


	
	public Track(DataKey key, Model model, boolean visible, boolean collapsible) {
		this.model = model;
		this.dataKey = key;
		this.visible = visible;
		this.entry = model.getSelectedEntry();
		this.collapsible = collapsible;
		this.addObserver(model);
	}

//	public Track(Model model, boolean visible, boolean collapsible) {
//		this.model = model;
//		this.visible = visible;
//		this.entry = model.getSelectedEntry();
//		this.collapsible = collapsible;
//		this.addObserver(model);
//	}

	private boolean visible;
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
	 * @param model
	 *            the entry that is currently displayed
	 * @return the height that was painted
	 */
	public int paint(Graphics g, int yOffset, double width, int index, JViewport view) {

		int used = paintTrack((Graphics2D) g, yOffset, width, view);

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
	 * @return the height this track uses
	 */
	protected abstract int paintTrack(Graphics2D g, int yOffset, double width, JViewport view);

	/* Keeps track of whether a track is collapsible */
	private boolean collapsible = false;
	/* Keeps track of the actual collapse state of the track */
	private boolean collapsed = false;

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
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
		setChanged();
		notifyObservers();
	}

	
	public abstract String displayName();

	@Deprecated
	public DataKey getDataKey() {
		return dataKey;
	}

	public List<JMenuItem> getMenuItems() {
		return new ArrayList<JMenuItem>();
	}

}
