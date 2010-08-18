/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class OverlayListener extends MouseAdapter {

	class Overlay extends JWindow {

		private JLabel floater = new JLabel();

		public Overlay(int x, int y, String message) {
			// this.setBounds(bounds);
			this.setAlwaysOnTop(true);
			floater.setHorizontalAlignment(SwingConstants.CENTER);
			floater.setVerticalAlignment(SwingConstants.CENTER);
			floater.setAlignmentY(CENTER_ALIGNMENT);
			this.setLocation(x, y);
			Color c = new Color(100, 100, 100, 50);
			floater.setBackground(c);
			floater.setOpaque(true);
			floater.setText("<html><table width=400>" + message + "</table></html>");
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
			setVisible(true);
		}
	}

	private Overlay overlay = null;
	private String message;

	public OverlayListener(String message) {
		this.message = message;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (overlay != null)
			overlay.dispose();
		overlay = new Overlay(e.getXOnScreen(), e.getYOnScreen(), message);

	}

	@Override
	public void mouseExited(MouseEvent e) {
		if (overlay != null)
			overlay.dispose();

	}

}
