/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JWindow;
import javax.swing.RepaintManager;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import be.abeel.io.ExtensionManager;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.annotation.GeneEvidenceLabel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SaveImage extends AbstractAction {

	private static final long serialVersionUID = -92536467741311140L;
	private Model model;

	public SaveImage(Model model) {
		super("Export image...");
		this.model = model;
	}

	// FIXME Should be made a separate class at some point as a central
	// messaging method
	class Hider extends JWindow {

		private JLabel floater = new JLabel();

		public Hider(Rectangle bounds) {
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
			floater.setText("Exporting image...");
			floater.setForeground(Color.BLACK);
			Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
			Border colorBorder = BorderFactory.createLineBorder(Color.BLACK);
			floater.setBorder(BorderFactory.createCompoundBorder(colorBorder, emptyBorder));
			add(floater);
			pack();
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);

		int result = chooser.showSaveDialog(model.getParent());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File ef = ExtensionManager.extension(chooser.getSelectedFile(), ExtensionManager.PNG);
			if (ef.exists()) {
				int confirm = JOptionPane.showConfirmDialog(model.getParent(),
						"This file exists, are you certain you want to overwrite it?");
				if (confirm != JOptionPane.YES_OPTION) {
					return;
				}

			}

			Rectangle r = model.getParent().getBounds();
			final Hider h = new Hider(r);
			h.setVisible(true);
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						GeneEvidenceLabel mw = model.getGUIManager().getEvidenceLabel();
						int factor = Configuration.getInt("general:exportMagnifyFactor");
						BufferedImage bi = new BufferedImage(mw.getWidth() * factor, mw.getHeight() * factor,
								BufferedImage.TYPE_INT_RGB);
						Graphics2D g = (Graphics2D) bi.getGraphics();

						try {
							RepaintManager currentManager = RepaintManager.currentManager(mw);
							currentManager.setDoubleBufferingEnabled(false);
							g.scale(factor, factor);
							mw.actualPaint(g, null);
							ImageIO.write(bi, "PNG", ef);
							currentManager.setDoubleBufferingEnabled(true);
						} catch (IOException ex) {
							// TODO fix
							ex.printStackTrace();
						}
						Configuration.set("lastDirectory", ef.getParentFile());
					} catch (Exception ex) {
						// TODO fix
						ex.printStackTrace();
					}
					h.dispose();
				}
			});

		}

	}

}
