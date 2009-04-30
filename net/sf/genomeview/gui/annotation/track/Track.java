/**
 * %HEADER%
 */
package net.sf.genomeview.gui.annotation.track;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.util.Observable;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;

/**
 * Abstract class for visualization tracks.
 */
public abstract class Track extends Observable{

	protected Model model;

	public Track(Model model, boolean visible){
		this.model=model;
		this.visible=visible;
		this.addObserver(model);
	}
	private boolean visible;

	/** 
	 * To pass along mouse clicks from the original panel.
	 */
	public void mouseClicked(int x,int y, MouseEvent source){
		//Do nothing
	}
	/** 
	 * To pass along mouse movements from the original panel.
	 */
	public void mouseMoved(int x,int y, MouseEvent source){
		//Do nothing
	}
	/** 
	 * To pass along mouse enters from the original panel.
	 */
	public void mouseEntered(int x,int y, MouseEvent source){
		//Do nothing
	}
	/** 
	 * To pass along mouse exits from the original panel.
	 */
	public void mouseExited(int x,int y, MouseEvent source){
		//Do nothing
	}
	/** 
	 * To pass along mouse presses from the original panel.
	 */
	public void mousePressed(int x,int y, MouseEvent source){
		//Do nothing
	}
	/** 
	 * To pass along mouse releases from the original panel.
	 */
	public void mouseReleased(int x,int y, MouseEvent source){
		//Do nothing
	}
	/** 
	 * To pass along mouse dragging from the original panel.
	 */
	public void mouseDragged(int x,int y, MouseEvent source){
		//Do nothing
	}
	/**
	 * Paint this track in the annotation label and return the height it
	 * occupies.
	 * 
	 * @param g
	 *            graphics context to paint on
	 * @param model
	 *            the entry that is currently displayed
	 * @return the height that was painted
	 */
	public abstract int paint(Graphics g, Entry e,int yOffset,double width);

	/* Keeps track of whether a track is collapsible */
	protected boolean collapsible=false;
	/* Keeps track of the actual collapse state of the track */
	private boolean collapsed=false;
	
	public void setCollapsed(boolean collapsed){
		this.collapsed=collapsed;
		setChanged();
		notifyObservers();
	}
	
	public boolean isCollapsible(){
		return collapsible;
	}
	public boolean isCollapsed(){
		return collapsed;
	}
	
	public boolean isVisible(){
		return visible;
	}
	public void setVisible(boolean visible){
		this.visible=visible;
		setChanged();
		notifyObservers();
	}
	
	public abstract String displayName();
	
	
}
