/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class Hider extends JDialog {

	private JLabel floater = new JLabel();

	public Hider(Model model, String text) {
		this(model, text, Color.BLACK);
	}

	public void setText(String text) {
		floater.setText(text);
		repaint();
	}

	public Hider(Model model, String text, Color color) {
		this(model, text, color, model.getGUIManager().getParent().getBounds().width / 5, model.getGUIManager()
				.getParent().getBounds().height / 5);

	}

	public Hider(Model model, String text, Color color, int width, int height) {
		// Rectangle bounds = model.getGUIManager().getParent().getBounds();
		// this.setBounds(bounds);
		super(null, ModalityType.APPLICATION_MODAL);
		setUndecorated(true);
		// this.setAlwaysOnTop(true);
		floater.setPreferredSize(new Dimension(width, height));
		floater.setHorizontalAlignment(SwingConstants.CENTER);
		floater.setVerticalAlignment(SwingConstants.CENTER);
		floater.setAlignmentY(CENTER_ALIGNMENT);
		// this.setLocation(bounds.x + bounds.width / 5 * 2, bounds.y +
		// bounds.height / 5 * 2);
		Color c= new Color(color.getRed(),color.getGreen(),color.getBlue(),50);
//		Color c = new Color(100, 100, 100, 50);
		
		floater.setBackground(c);
		//floater.setOpaque(true);
		floater.setText("<html>" + text + "</html>");
		floater.setForeground(color);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(color);
		floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		add(floater);
		pack();
		StaticUtils.center(model.getGUIManager().getParent(),this);
		final Hider _self=this;
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				_self.setVisible(true);

			}
		});

	}

}