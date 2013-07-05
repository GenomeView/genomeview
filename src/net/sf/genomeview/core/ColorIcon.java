/**
 * %HEADER%
 */
package net.sf.genomeview.core;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;

import javax.swing.ImageIcon;

/**
 * A square colored box icon.
 * 
 * @author Thomas Abeel
 *
 */
public class ColorIcon extends ImageIcon {

	private static final long serialVersionUID = 1L;

	private Color c;

	private int size;

	public ColorIcon(Color c, int size) {
		this.c = c;
		this.size = size;
	}

	@Override
	public int getIconHeight() {
		return size;
	}

	@Override
	public int getIconWidth() {
		return size;
	}

	@Override
	public synchronized void paintIcon(Component comp, Graphics g, int x, int y) {
		g.setColor(c);
		g.fillRect(0, 0, size, size);
	}

}