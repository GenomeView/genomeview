/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.Scrollable;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveLeftAction;
import net.sf.genomeview.gui.menu.navigation.AnnotationMoveRightAction;
import net.sf.jannot.Location;

/**
 * Parent class for GeneEvidencelabel and GeneStructureLabel
 * 
 * @author Thomas Abeel
 * 
 */
public abstract class AbstractGeneLabel extends JLabel implements Observer {


	/**
	 * 
	 */
	private static final long serialVersionUID = 5579565140607328154L;

	protected Model model;
	
	/**
	 * The MouseWheelListener passed on from the JScrollPane. Only to be invoked for regular scrolling.
	 */
	private MouseWheelListener scrollPaneListener;

	/**
	 * Map that contains all TrackKey,ItemName pairs and their corresponding
	 * rectangle.
	 */
//	protected CollisionMap collisionMap;

	public AbstractGeneLabel(final Model model) {
		this.model = model;
//		this.collisionMap = new CollisionMap(model);
		this.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				
				if (e.isControlDown()||e.isMetaDown()||e.isAltDown()){
					double rot = e.getWheelRotation() / 5.0;
					double center = Convert.translateScreenToGenome(e.getX(), model.vlm
							.getAnnotationLocationVisible(),screenWidth);
					double start = model.vlm.getAnnotationLocationVisible().start();
					double end = model.vlm.getAnnotationLocationVisible().end();
					double length = end - start + 1;
					double fractionL = (center - start) / length;
					double fractionR = (end - center) / length;
					// System.out.println(fractionL+"\t"+fractionR);
					if (rot < 0
							&& length < Configuration.getInt("minimumNucleotides")) {
						return;
					}
					double sizeChange = rot * length;
					// System.out.println("SC:"+sizeChange);
					int newStart = (int) (start - fractionL * sizeChange);
					int newEnd = (int) (end + fractionR * sizeChange);

					model.vlm.setAnnotationLocationVisible(new Location(newStart,
							newEnd));
				} else if (e.isShiftDown()){
					if (e.getWheelRotation() > 0){
						AnnotationMoveRightAction.perform(model);
					} else {
						AnnotationMoveLeftAction.perform(model);
					}
				} else {
					scrollPaneListener.mouseWheelMoved(e);
				}

			}

		});
	}

	protected boolean drag = false;

	/**
	 * Keeps track of how many pixels are already used in the Y direction
	 */
	protected int framePixelsUsed = 0;

	protected void paintVisibilityFrame(Graphics g, Location frame,
			Location real) {

		g.setColor(Color.RED);

		int start = Convert.translateGenomeToScreen(frame.start(), real,screenWidth);
		int end = Convert.translateGenomeToScreen(frame.end(), real,screenWidth);
		g
				.drawRect(start, 0, end - start, framePixelsUsed > g
						.getClipBounds().height ? framePixelsUsed - 1 : g
						.getClipBounds().height - 1);

	}

	

//	public void paintComponent(Graphics g) {
//		super.paintComponent(g);
//
//	}

	

	protected double screenWidth = 0;

	public void update(Observable arg0, Object arg1) {
		repaint();
	}

	public void setScrollPaneListener(MouseWheelListener scrollPaneListener) {
		this.scrollPaneListener = scrollPaneListener;
	}

	public MouseWheelListener getScrollPaneListener() {
		return scrollPaneListener;
	}

}