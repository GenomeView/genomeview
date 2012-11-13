/**
 * %HEADER%
 */
package net.sf.genomeview.gui.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class OverlayListener extends MouseAdapter implements ActionListener{

	class Overlay extends JPanel {

		
		private static final long serialVersionUID = 4167311197324722126L;
		private JLabel floater = new JLabel();

		public Overlay(String message) {
			// this.setBounds(bounds);
			
			floater.setHorizontalAlignment(SwingConstants.CENTER);
			floater.setVerticalAlignment(SwingConstants.CENTER);
			// floater.setAlignmentY(CENTER_ALIGNMENT);

			Color c = new Color(200, 200, 200, 50);
			floater.setBackground(c);
			floater.setOpaque(true);
			if (message.length() > 50)
				floater.setText("<html><table width=250>" + message + "</table></html>");
			else
				floater.setText("<html><table>" + message + "</table></html>");
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			// floater.setBorder(colorBorder);
			add(floater);

		}
	}

	private JWindow overlay = null;
	private String message;

	public OverlayListener(String message) {
		this.message = message;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		if (overlay != null)
			overlay.dispose();
		Point p = e.getComponent().getLocationOnScreen();
		int x = p.x + e.getComponent().getWidth();
		int y = p.y;

		overlay = new JWindow();
		overlay.setAlwaysOnTop(true);
		overlay.setContentPane(new Overlay(message));
		overlay.pack();
		overlay.setLocation(x + 2, y - 5);
		Toolkit tk=Toolkit.getDefaultToolkit();
		Dimension screen=tk.getScreenSize();
		if(x+overlay.getWidth()>screen.getWidth())
			overlay.setLocation(p.x - 2-overlay.getWidth(), y - 5);
		overlay.setVisible(true);
	}



	@Override
	public void mouseExited(MouseEvent e) {
		if (overlay != null)
			overlay.dispose();

	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (overlay != null) {
			if (e.getSource() instanceof JMenuItem){
				System.out.println(e.getSource());
				overlay.dispose();
			}
		}
		
	}

}
