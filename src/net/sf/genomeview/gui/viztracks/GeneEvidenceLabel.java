/**
 * %HEADER%
 */
package net.sf.genomeview.gui.viztracks;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import java.util.TreeMap;

import javax.swing.JLabel;
import javax.swing.JViewport;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.menu.PopUpMenu;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveLeftAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveRightAction;
import net.sf.genomeview.gui.viztracks.annotation.StructureTrack;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class GeneEvidenceLabel extends JLabel implements Observer, MouseListener, MouseMotionListener {

	private static final long serialVersionUID = -8338383664013028337L;

	protected Model model;

	/**
	 * The MouseWheelListener passed on from the JScrollPane. Only to be invoked
	 * for regular scrolling.
	 */
	private MouseWheelListener scrollPaneListener;

	private TrackCommunicationModel tcm = new TrackCommunicationModel();

	public GeneEvidenceLabel(final Model model) {
		// super(model);
		this.model = model;
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {

				if (e.isControlDown() || e.isMetaDown() || e.isAltDown()) {
					double rot = e.getWheelRotation() / 5.0;
					double center = Convert.translateScreenToGenome(e.getX(), model.vlm.getAnnotationLocationVisible(), screenWidth);
					double start = model.vlm.getAnnotationLocationVisible().start();
					double end = model.vlm.getAnnotationLocationVisible().end();
					double length = end - start + 1;
					double fractionL = (center - start) / length;
					double fractionR = (end - center) / length;
					// System.out.println(fractionL+"\t"+fractionR);
					if (rot < 0 && length < Configuration.getInt("minimumNucleotides")) {
						return;
					}
					double sizeChange = rot * length;
					// System.out.println("SC:"+sizeChange);
					int newStart = (int) (start - fractionL * sizeChange);
					int newEnd = (int) (end + fractionR * sizeChange);

					model.vlm.setAnnotationLocationVisible(new Location(newStart, newEnd));
				} else if (e.isShiftDown()) {
					if (e.getWheelRotation() > 0) {
						AnnotationMoveRightAction.perform(model);
					} else {
						AnnotationMoveLeftAction.perform(model);
					}
				} else {
					scrollPaneListener.mouseWheelMoved(e);
				}

			}

		});

		setBackground(Color.WHITE);
		setOpaque(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		model.addObserver(this);
		model.getGUIManager().registerEvidenceLabel(this);
		// this.setPreferredSize(new Dimension(this.getPreferredSize().width,
		// 200));

	}

	public enum FillMode {
		FILL, DRAW, BACKDROP;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		repaint();

	}

	public void paintTracks(Graphics g, JViewport view) {
		tracks.clear();
		framePixelsUsed = 0;
		screenWidth = this.getSize().width + 1;
		model.vlm.setScreenWidth(screenWidth);

		if (view == null) {
			view = new JViewport() {
				public Rectangle getViewRect() {
					return new Rectangle(0, 0, (int) screenWidth, Integer.MAX_VALUE);
				}
			};
		}

		super.paintComponent(g);
		int index = 0;

		for (Track track : model.getTrackList()) {
			if (track.config.isVisible()) {
				int height = track.paint(g, framePixelsUsed, screenWidth, index++, view, tcm);

				// FIXME we shouldn't give each paint method the yOffset. We
				// should use the Graphics translate function to make sure we
				// are positioned correctly.
				if (height > 0)
					tracks.put(framePixelsUsed,height, track);
				framePixelsUsed += height;
			}
		}
		if (tcm.isChanged()) {
			tcm.resetChanged();
			repaint();
		}
	}

	protected boolean drag = false;

	/**
	 * Keeps track of how many pixels are already used in the Y direction
	 */
	protected int framePixelsUsed = 0;
	protected double screenWidth = 0;

	public void setScrollPaneListener(MouseWheelListener scrollPaneListener) {
		this.scrollPaneListener = scrollPaneListener;
	}

	// public MouseWheelListener getScrollPaneListener() {
	// return scrollPaneListener;
	// }

	@Override
	public void paintComponent(Graphics g) {
		paintTracks(g, viewport);

		// FIXME paintSelectedLocation(g, model.getAnnotationLocationVisible());

		if (this.getPreferredSize().height != framePixelsUsed) {
			this.setPreferredSize(new Dimension(this.getPreferredSize().width, framePixelsUsed));
			revalidate();

		}

		/* Highlight current selection */
		g.setColor(new Color(180, 180, 180, 120));
		for (Feature f : model.selectionModel().getFeatureSelection()) {
			assert f != null;
			assert f.location() != null;
			for (Location l : f.location()) {
				highlight(l, g);
			}
		}
		if (model.getSelectedRegion() != null)
			highlight(model.getSelectedRegion(), g);

		g.setColor(new Color(120, 120, 120, 120));
		// draw guide line.
		g.drawLine(currentMouseX, 0, currentMouseX, this.getPreferredSize().height);

	}

	private void highlight(Location l, Graphics g) {
		int x1 = Convert.translateGenomeToScreen(l.start(), model.vlm.getAnnotationLocationVisible(), screenWidth);
		int x2 = Convert.translateGenomeToScreen(l.end() + 1, model.vlm.getAnnotationLocationVisible(), screenWidth);
		g.drawLine(x1 - 1, 0, x1 - 1, this.getPreferredSize().height);
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
		private HashMap<Track, Integer> reverseTrack = new HashMap<Track, Integer>();
		private HashMap<Track, Integer>heightMap=new HashMap<Track,Integer>();
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

		void clear() {
			tracks.clear();
			reverseTrack.clear();
			heightMap.clear();

		}

		
		void put(int framePixelsUsed,int height, Track track) {
			tracks.put(framePixelsUsed, track);
			reverseTrack.put(track, framePixelsUsed);
			heightMap.put(track, height);

		}

		int getYOffset(Track t){
			if(reverseTrack.containsKey(t))
				return reverseTrack.get(t).intValue();
			else
				return 0;
			
		}
		
		public Track get(MouseEvent e) {
			if (e.getY() > framePixelsUsed)
				return null;
			int mouseOffset = getMouseOffset(e);
			e.translatePoint(0, -mouseOffset);
			return tracks.get(mouseOffset);
		}

		public int getHeight(Track t) {
			return heightMap.get(t);
		}
	}

	public void scroll2track(Track t){
		int y=tracks.getYOffset(t);
		int h=tracks.getHeight(t);
		scrollRectToVisible(new Rectangle(0, y, (int)screenWidth, h));
	}

	private TrackMap tracks = new TrackMap();

	@Override
	public void mouseExited(MouseEvent e) {
		model.mouseModel().setCurrentCoord(-1);
		/* Transfer MouseEvent to corresponding track */
		if (last != null)
			last.mouseExited(e.getX(), e.getY(), e);
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
		pressLoc = model.vlm.getAnnotationLocationVisible();
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
		currentMouseX = e.getX();
		int currentGenomeX = Convert.translateScreenToGenome(currentMouseX, model.vlm.getAnnotationLocationVisible(), screenWidth);

		/* Transfer MouseEvent to corresponding track */
		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null)
			consumed = mouseTrack.mouseDragged(e.getX(), e.getY(), e);
		if (consumed) {
			// even when consumed, update the mouse position before returning
			model.mouseModel().setCurrentCoord(currentGenomeX);
			return;
		}

		/* Specific mouse code for this label */

		if (pressLoc != null) {
			// shift-drag always selects
			if (e.isShiftDown()) {
				model.selectionModel().clearLocationSelection();

				int selectionStart = 0;
				int selectionEnd = 0;

				// int currentGenomeX =
				// Convert.translateScreenToGenome(currentMouseX,
				// model.getAnnotationLocationVisible(), screenWidth);
				int pressGenomeX = Convert.translateScreenToGenome(pressX, model.vlm.getAnnotationLocationVisible(), screenWidth);

				int start = pressGenomeX < currentGenomeX ? pressGenomeX : currentGenomeX;
				int end = pressGenomeX < currentGenomeX ? currentGenomeX : pressGenomeX;

				selectionStart = start;
				selectionEnd = end + 1;

				model.selectionModel().setSelectedRegion(new Location(selectionStart, selectionEnd));
				// when selecting: update the mouse position
				model.mouseModel().setCurrentCoord(currentGenomeX);

			} else {
				// drag always pans
				double move = (e.getX() - pressX) / screenWidth;

				int start = (int) (pressLoc.start() - pressLoc.length() * move);
				int end = (int) (pressLoc.end() - pressLoc.length() * move);

				if (start < 1) {
					start = 1;
					end = pressLoc.length();
				}
				if (end > model.vlm.getSelectedEntry().getMaximumLength()) {
					end = model.vlm.getSelectedEntry().getMaximumLength();
					start = end - pressLoc.length();
				}
				model.vlm.setAnnotationLocationVisible(new Location(start, end));

				// when panning, don't update the mouse position until done. It
				// should stay the same while panning anyway (but in reality,
				// it will slightly lag behind the pointer)
			}

		}

	}

	private Track last = null;

	private JViewport viewport;

	@Override
	public void mouseMoved(MouseEvent e) {
		currentMouseX = e.getX();
		int currentGenomeX = Convert.translateScreenToGenome(currentMouseX, model.vlm.getAnnotationLocationVisible(), screenWidth);
		model.mouseModel().setCurrentCoord(currentGenomeX);

		/* Transfer MouseEvent to corresponding track */
		Track mouseTrack = tracks.get(e);

		if (last != mouseTrack) {
			if (last != null)
				last.mouseExited(e.getX(), e.getY(), e);
			last = mouseTrack;
		}

		boolean consumed = false;
		if (mouseTrack != null) {
			if (last != mouseTrack) {
				mouseTrack.mouseEntered(e.getX(), e.getY(), e);
			}
			consumed = mouseTrack.mouseMoved(e.getX(), e.getY(), e);
			if (!(mouseTrack instanceof StructureTrack))
				model.getGUIManager().getParent().setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		}
		if (consumed)
			return;
		/* Specific mouse code for this label */
		repaint();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int y = e.getY();
		boolean consumed = false;
		/* Transfer MouseEvent to corresponding track */
		Track mouseTrack = tracks.get(e);
		if (mouseTrack != null)
			consumed = mouseTrack.mouseClicked(e.getX(), e.getY(), e);
		/* Specific mouse code for this label */
		if (!e.isConsumed() && (Mouse.button2(e) || Mouse.button3(e))) {
			new PopUpMenu(model, mouseTrack).show(this, e.getX(), y);
		} else if (!consumed) {
			model.selectionModel().setSelectedRegion(null);
			model.selectionModel().clearLocationSelection();
		}

	}

	public void setViewport(JViewport viewport) {
		this.viewport = viewport;

	}
}