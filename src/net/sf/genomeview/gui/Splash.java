/**
 * %HEADER%
 */
package net.sf.genomeview.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JWindow;
import javax.swing.border.Border;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class Splash extends JDialog {

	private static final long serialVersionUID = 2469220030126994849L;

	private JLabel text = null;

	public Splash() {
		super(null, ModalityType.APPLICATION_MODAL);
		this.setUndecorated(true);
		
		JPanel content = new JPanel();
		content.setBackground(Color.WHITE);
		content.setLayout(new BorderLayout());
		floater = new JLabel(new ImageIcon(this.getClass().getResource("/images/splash.png")));
		text = new JLabel(MessageManager.getString("splash.starting"));
		Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();

		// this.setAlwaysOnTop(true);
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
		content.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
		content.add(floater, BorderLayout.CENTER);
		content.add(text, BorderLayout.SOUTH);
		this.setContentPane(content);
		pack();
		this.setLocation(bounds.x + bounds.width / 2 - floater.getWidth() / 2,
				bounds.y + bounds.height / 2 - floater.getHeight() / 2);
		final JDialog _self=this;
		EventQueue.invokeLater(new Runnable() {

			@Override
			public void run() {
				_self.setVisible(true);

			}
		});

	}

	private JLabel floater = new JLabel();

	public void setText(String string) {
		text.setText(string);

	}

}
