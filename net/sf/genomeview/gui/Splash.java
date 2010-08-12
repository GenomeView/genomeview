/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.border.Border;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class Splash extends JWindow {

	private static final long serialVersionUID = 2469220030126994849L;

	public Splash() {
		super();
		floater = new JLabel(new ImageIcon(this.getClass().getResource("/images/splash.png")));
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
		this.setAlwaysOnTop(true);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
		floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		add(floater);
		pack();
		this.setLocation(bounds.x + bounds.width/2 -floater.getWidth()/2, bounds.y + bounds.height /2 -floater.getHeight()/2);
		setVisible(true);
	
	}

	private JLabel floater = new JLabel();

	
}
