/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;
import java.util.TreeMap;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.AbstractGeneLabel;
import net.sf.genomeview.gui.Mouse;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.annotation.track.FeatureTrack;
import net.sf.genomeview.gui.annotation.track.MultipleAlignmentTrack;
import net.sf.genomeview.gui.annotation.track.Track;
import net.sf.jannot.Location;

public class GeneEvidenceLabel extends AbstractGeneLabel implements
		MouseListener, MouseMotionListener {

	
	private static final long serialVersionUID = -8338383664013028337L;

	public GeneEvidenceLabel(Model model) {
		super(model);
		setBackground(Color.WHITE);
		setOpaque(true);
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		model.addObserver(this);
		// TODO 200 pixels for evidence should be a portion of the screen
		// instead of hard coded
		this
				.setPreferredSize(new Dimension(this.getPreferredSize().width,
						200));

	}

	public enum FillMode {
		FILL, DRAW, BACKDROP;
	}

	@Override
	public void update(Observable arg0, Object arg1) {
		this.setVisible(model.isAnnotationVisible());
		if (this.isVisible()) {
			revalidate();
			repaint();
		}
		super.update(arg0, arg1);

	}

	
	/* A mapping from position to track */
	private TreeMap<Integer, Track> tracks = new TreeMap<Integer, Track>();

	@Override
	public void mouseClicked(MouseEvent e) {
		int y = e.getY();
		int min = Integer.MAX_VALUE;
		for (Integer i : tracks.keySet()) {
			if (i > y && i < min)
				min = i;
		}
		Track clickTrack = tracks.get(min);

		if (clickTrack != null)
			clickTrack.mouseClicked(e.getX(), y - min, e);

		if (Mouse.button2(e) || Mouse.button3(e)) {
			StaticUtils.popupMenu(model).show(this, e.getX(), e.getY());
		}
	}

	private int currentBackgroundIndex = 0;

	private Color[] background = new Color[] { new Color(204, 238, 255, 100),
			new Color(255, 255, 204, 100) };

	@Override
	public void paintComponent(Graphics g) {
		tracks.clear();
		framePixelsUsed = 0;
		screenWidth = this.getSize().width + 1;
		super.paintComponent(g);
		currentBackgroundIndex = 0;
		
		for (Track track : model.getTrackList()) {
			if (track.isVisible()) {
				int startY = framePixelsUsed;
				framePixelsUsed += track.paint(g, model.getSelectedEntry(),
						framePixelsUsed, screenWidth);
				
				if (track instanceof FeatureTrack||track instanceof MultipleAlignmentTrack) {
				
					Rectangle r = new Rectangle(0, startY,
							(int) screenWidth + 1, framePixelsUsed - startY);
					tracks.put(framePixelsUsed, track);
					g.setColor(background[currentBackgroundIndex]);
					g.fillRect(r.x, r.y , r.width, r.height);

					currentBackgroundIndex++;
					currentBackgroundIndex %= background.length;
				}
			}
		}
//	FIXME	 paintSelectedLocation(g, model.getAnnotationLocationVisible());

		if (this.getPreferredSize().height != framePixelsUsed) {
			this.setPreferredSize(new Dimension(this.getPreferredSize().width,
					framePixelsUsed));
			this.invalidate();
			this.getParent().validate();
			revalidate();

		}
		g.setColor(new Color(120,120,120,120));
		g.drawLine(currentMouseX, 0, currentMouseX, this.getPreferredSize().height);
	}


	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	private Location pressLoc = null;

	private int pressX;

	private int currentMouseX;

	@Override
	public void mousePressed(MouseEvent e) {
		pressLoc = model.getAnnotationLocationVisible();
		pressX = e.getX();

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		pressLoc = null;

	}

	@Override
	public void mouseDragged(MouseEvent arg) {
		currentMouseX=arg.getX();
		if (pressLoc != null) {

			double move = (arg.getX() - pressX) / screenWidth;
			int start = (int) (pressLoc.start() - pressLoc.length() * move);
			int end = (int) (pressLoc.end() - pressLoc.length() * move);
			model.setAnnotationLocationVisible(new Location(start, end));

		}

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		currentMouseX=arg0.getX();
		repaint();
	}

}