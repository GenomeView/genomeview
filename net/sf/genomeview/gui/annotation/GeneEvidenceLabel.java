/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.TreeMap;

import javax.swing.JViewport;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.AbstractGeneLabel;
import net.sf.genomeview.gui.Convert;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.annotation.track.StructureTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.genomeview.gui.menu.PopUpMenu;
import net.sf.jannot.Feature;
import net.sf.jannot.Location;
/**
 * 
 * @author Thomas Abeel
 *
 */
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
		//this.setPreferredSize(new Dimension(this.getPreferredSize().width, 200));

	}

	public enum FillMode {
		FILL, DRAW, BACKDROP;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		repaint();

	}

	public void actualPaint(Graphics g,JViewport view) {
		tracks.clear();
		framePixelsUsed = 0;
		screenWidth = this.getSize().width + 1;
		if(view==null){
			view=new JViewport(){
				public Rectangle getViewRect(){
					return new Rectangle(0,0,(int)screenWidth,Integer.MAX_VALUE);
				}
			};
		}
		
		super.paintComponent(g);
		int index = 0;
		for (Track track : model.getTrackList()) {
			if (track.isVisible()) {
				int height = track.paint(g, framePixelsUsed, screenWidth, index++,view);
				// FIXME we shouldn't give each paint method the yOffset. We
				// should use the Graphics translate function to make sure we
				// are positioned correctly.
				if (height > 0)
					tracks.put(framePixelsUsed, track);
				framePixelsUsed += height;
			}
		}
	}

	@Override
	public void paintComponent(Graphics g) {		
		actualPaint(g,viewport);

		// FIXME paintSelectedLocation(g, model.getAnnotationLocationVisible());

		if (this.getPreferredSize().height != framePixelsUsed) {
			this.setPreferredSize(new Dimension(this.getPreferredSize().width, framePixelsUsed));
			revalidate();

		}

		/* Highlight current selection */
		g.setColor(new Color(180, 180, 180, 120));
		for (Feature f : model.selectionModel().getFeatureSelection()) {
			for (Location l : f.location()) {
				highlight(l, g);
			}
		}
		if (model.getSelectedRegion() != null)
			highlight(model.getSelectedRegion(), g);

		g.setColor(new Color(120, 120, 120, 120));
		//draw guide line.
		g.drawLine(currentMouseX, 0, currentMouseX, this.getPreferredSize().height);
	}

	private void highlight(Location l, Graphics g) {
		int x1 = Convert.translateGenomeToScreen(l.start(), model.getAnnotationLocationVisible(), screenWidth);
		int x2 = Convert.translateGenomeToScreen(l.end() + 1, model.getAnnotationLocationVisible(), screenWidth);
		g.drawLine(x1-1, 0, x1-1, this.getPreferredSize().height);
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
			if (e.getY() > framePixelsUsed)
				return null;
			int mouseOffset = getMouseOffset(e);
			e.translatePoint(0, -mouseOffset);
			return tracks.get(mouseOffset);
		}
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
		currentMouseX = e.getX();
		int currentGenomeX = Convert.translateScreenToGenome(currentMouseX, model.getAnnotationLocationVisible(), screenWidth);
		
		/* Transfer MouseEvent to corresponding track */
		Track mouseTrack = tracks.get(e);
		boolean consumed = false;
		if (mouseTrack != null)
			consumed = mouseTrack.mouseDragged(e.getX(), e.getY(), e);
		if (consumed){
			//even when consumed, update the mouse position before returning
			model.mouseModel().setCurrentCoord(currentGenomeX);
			return;
		}
		
		
		/* Specific mouse code for this label */
		
		if (pressLoc != null) {
			//shift-drag always selects
			if (e.isShiftDown()){
				model.selectionModel().clearLocationSelection();
			
				int selectionStart = 0;
				int selectionEnd = 0;
				
//				int currentGenomeX = Convert.translateScreenToGenome(currentMouseX, model.getAnnotationLocationVisible(), screenWidth);
				int pressGenomeX = Convert.translateScreenToGenome(pressX, model.getAnnotationLocationVisible(), screenWidth);
				
				int start = pressGenomeX < currentGenomeX ? pressGenomeX : currentGenomeX;
				int end = pressGenomeX < currentGenomeX ? currentGenomeX : pressGenomeX;
				
				selectionStart = start;
				selectionEnd = end + 1;
				
				model.selectionModel().setSelectedRegion(new Location(selectionStart, selectionEnd));
				//when selecting: update the mouse position
				model.mouseModel().setCurrentCoord(currentGenomeX);
				
			} else {
				//drag always pans
				double move = (e.getX() - pressX) / screenWidth;

				int start = (int) (pressLoc.start() - pressLoc.length() * move);
				int end = (int) (pressLoc.end() - pressLoc.length() * move);

				if (start<1){
					start=1;
					end = pressLoc.length();
				}
				if (end>model.getSelectedEntry().getMaximumLength()){
					end = model.getSelectedEntry().getMaximumLength();
					start = end - pressLoc.length();
				}
				model.setAnnotationLocationVisible(new Location(start, end));
				
				//when panning, don't update the mouse position until done. It should stay the same while panning anyway (but in reality,
				//it will slightly lag behind the pointer)
			}

		}

	}

	private Track last = null;

	private JViewport viewport;

	@Override
	public void mouseMoved(MouseEvent e) {
		currentMouseX = e.getX();		
		int currentGenomeX = Convert.translateScreenToGenome(currentMouseX, model.getAnnotationLocationVisible(), screenWidth);
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
			new PopUpMenu(model,mouseTrack).show(this, e.getX(), y);
		} else if (!consumed){
			model.selectionModel().setSelectedRegion(null);
			model.selectionModel().clearLocationSelection();
		}
		
	}

	
	public void setViewport(JViewport viewport) {
		this.viewport=viewport;
		
	}
}