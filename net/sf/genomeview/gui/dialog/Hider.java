/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.sf.genomeview.data.Model;
/**
 * 
 * @author Thomas Abeel
 *
 */
public class Hider extends JWindow {

	private JLabel floater = new JLabel();

	public Hider(Model model,String text) {
		this(model,text,Color.BLACK);
	}

	public Hider(Model model, String text, Color color) {
		Rectangle bounds = model.getGUIManager().getParent().getBounds();
		// this.setBounds(bounds);
		this.setAlwaysOnTop(true);
		floater.setPreferredSize(new Dimension(bounds.width / 5, bounds.height / 5));
		floater.setHorizontalAlignment(SwingConstants.CENTER);
		floater.setVerticalAlignment(SwingConstants.CENTER);
		floater.setAlignmentY(CENTER_ALIGNMENT);
		this.setLocation(bounds.x + bounds.width / 5 * 2, bounds.y + bounds.height / 5 * 2);
		Color c = new Color(100, 100, 100, 50);
		floater.setBackground(c);
		floater.setOpaque(true);
		floater.setText("<html>"+text+"</html>");
		floater.setForeground(Color.BLACK);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(color);
		floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		add(floater);
		pack();
	}
}