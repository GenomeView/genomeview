/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.TreeMap;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.AbstractGeneLabel;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.gui.menu.PopUpMenu;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

public class GeneEvidenceLabel extends AbstractGeneLabel implements MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -8338383664013028337L;

	public GeneEvidenceLabel(Model model) {
		super(model);
		setBackground(Color.WHITE);
		setOpaque(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		model.addObserver(this);
		model.getGUIManager().registerEvidenceLabel(this);
		this.setPreferredSize(new Dimension(this.getPreferredSize().width, 200));

	}

	public enum FillMode {
		FILL, DRAW, BACKDROP;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		repaint();

	}

	public void actualPaint(Graphics g) {
		tracks.clear();
		framePixelsUsed = 0;
		screenWidth = this.getSize().width + 1;
		super.paintComponent(g);
		int index = 0;
		for (Track track : model.getTrackList()) {
			if (track.isVisible()) {
				int height = track.paint(g, model.getSelectedEntry(), framePixelsUsed, screenWidth, index++);

				if (height > 0)
					tracks.put(framePixelsUsed, track);
				framePixelsUsed += height;
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {
	
		

		actualPaint(g);

		// FIXME paintSelectedLocation(g, model.getAnnotationLocationVisible());

		if (this.getPreferredSize().height != framePixelsUsed) {
			this.setPreferredSize(new Dimension(this.getPreferredSize().width, framePixelsUsed));
			revalidate();

		}

		/* Highlight current selection */
		g.setColor(new Color(180, 180, 180, 120));
		for (Feature f : model.getFeatureSelection()) {
			for (Location l : f.location()) {
				highlight(l, g);
			}
		}
		if (model.getSelectedRegion() != null)
			highlight(model.getSelectedRegion(), g);

		g.setColor(new Color(120, 120, 120, 120));
		g.drawLine(currentMouseX, 0, currentMouseX, this.getPreferredSize().height);
	}

	private void highlight(Location l, Graphics g) {
		int x1 = Convert.translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible(), screenWidth);
		int x2 = Convert.translateGenomeToScreen(l.end() + 1, model.getAnnotationLocationVisible(), screenWidth);
		g.drawLine(x1, 0, x1, this.getPreferredSize().height);
		g.drawLine(x2, 0, x2, this.getPreferredSize().height);
		g.setColor(new Color(180, 180, 255, 50));
		g.fillRect(x1, 0, x2 - x1, this.getPreferredSize().height);

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		/* Transfer MouseEvent to corresponding track */

		Track mouseTrack = tracks.get(e);
		if (mouseTrack != null)
			mouseTrack.mouseEntered(e.getX(), e.getY(), e);
		/* Specific mouse code for this label */

	}

	class TrackMap {
		/* A mapping from position to track */
		private TreeMap<Integer, Track> tracks = new TreeMap<Integer, Track>();

		/**
		 * Returns the track where the MouseEvent takes place
		 * 
		 * @param e
		 *            MouseEvent
		 * @return the track that overlaps with the coordinates of the event.
		 */
		private int getMouseOffset(MouseEvent e) {
			int y = e.getY();
			int max = 0;
			for (Integer i : tracks.keySet()) {
				if (i < y && i > max)
					max = i;
			}
			return max;
		}

		public void clear() {
			tracks.clear();

		}

		public void put(int framePixelsUsed, Track track) {
			tracks.put(framePixelsUsed, track);

		}

		public Track get(MouseEvent e) {
			int mouseOffset = getMouseOffset(e);
			e.translatePoint(0, -mouseOffset);
			return tracks.get(mouseOffset);
		}
	}

	private TrackMap tracks = new TrackMap();

	@Override
	public void mouseExited(MouseEvent e) {
		/* Transfer MouseEvent to corresponding track */
		Track mouseTrack = tracks.get(e);
		if (mouseTrack != null)
			mouseTrack.mouseExited(e.getX(), e.getY(), e);
		/* Specific mouse code for this label */

	}

	private Location pressLoc = null;

	private int pressX;

	private int currentMouseX;

	@Override
	public void mousePressed(MouseEvent e) {
		/* Transfer MouseEvent to corresponding track */

		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null)
			consumed = mouseTrack.mousePressed(e.getX(), e.getY(), e);
		if (consumed)
			return;
		/* Specific mouse code for this label */
		pressLoc = model.getAnnotationLocationVisible();
		pressX = e.getX();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		/* Transfer MouseEvent to corresponding track */

		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null)
			consumed = mouseTrack.mouseReleased(e.getX(), e.getY(), e);
		if (consumed)
			return;
		/* Specific mouse code for this label */
		pressLoc = null;

	}

	@Override
	public void mouseDragged(MouseEvent e) {
		/* Transfer MouseEvent to corresponding track */

		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null)
			consumed = mouseTrack.mouseDragged(e.getX(), e.getY(), e);
		if (consumed)
			return;
		/* Specific mouse code for this label */

		currentMouseX = e.getX();
		if (pressLoc != null) {

			double move = (e.getX() - pressX) / screenWidth;
			int start = (int) (pressLoc.start() - pressLoc.length() * move);
			int end = (int) (pressLoc.end() - pressLoc.length() * move);
			if (end - start > 50)
				model.setAnnotationLocationVisible(new Location(start, end));

		}

	}

	private Track last = null;

	@Override
	public void mouseMoved(MouseEvent e) {
		/* Transfer MouseEvent to corresponding track */

		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null) {
			if (last != mouseTrack) {
				if (last != null)
					last.mouseExited(e.getX(), e.getY(), e);
				last = mouseTrack;
				mouseTrack.mouseEntered(e.getX(), e.getY(), e);
			}
			consumed = mouseTrack.mouseMoved(e.getX(), e.getY(), e);
			if (!(mouseTrack instanceof StructureTrack))
				model.getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		if (consumed)
			return;
		/* Specific mouse code for this label */
		currentMouseX = e.getX();
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int y = e.getY();
		/* Transfer MouseEvent to corresponding track */

		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null)
			consumed = mouseTrack.mouseClicked(e.getX(), e.getY(), e);
		if (consumed)
			return;
		/* Specific mouse code for this label */
		if (Mouse.button2(e) || Mouse.button3(e)) {
			new PopUpMenu(model).show(this, e.getX(), y);
		}
	}
}