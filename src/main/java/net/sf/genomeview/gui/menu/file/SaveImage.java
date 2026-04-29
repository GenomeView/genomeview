/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.logging.Level;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;

import be.abeel.io.ExtensionManager;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.core.MessageManager;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.Hider;
import net.sf.genomeview.gui.viztracks.GeneEvidenceLabel;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public class SaveImage extends AbstractAction {

	private static final long serialVersionUID = -92536467741311140L;
	private Model model;

	public SaveImage(Model model) {
		super(MessageManager.getString("filemenu.save_image"));
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		final Configuration config = model.getConfiguration();
		final JFileChooser chooser = new JFileChooser(
				config.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);

		int result = chooser
				.showSaveDialog(model.getGUIManager().getMainWindow());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File ef = ExtensionManager
					.extension(chooser.getSelectedFile(), ExtensionManager.PNG);
			if (ef.exists()) {
				int confirm = JOptionPane.showConfirmDialog(
						model.getGUIManager().getMainWindow(),
						MessageManager.getString("filemenu.save_image_warn"));
				if (confirm != JOptionPane.YES_OPTION) {
					return;
				}

			}

			final Hider h = new Hider(model,
					MessageManager.getString("filemenu.exporting_image"));
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					try {
						GeneEvidenceLabel mw = model.getGUIManager()
								.getEvidenceLabel();
						int factor = config
								.getInt("general:exportMagnifyFactor");
						BufferedImage bi = new BufferedImage(
								mw.getWidth() * factor, mw.getHeight() * factor,
								BufferedImage.TYPE_INT_RGB);
						Graphics2D g = (Graphics2D) bi.getGraphics();

						RepaintManager currentManager = RepaintManager
								.currentManager(mw);
						currentManager.setDoubleBufferingEnabled(false);
						g.scale(factor, factor);
						mw.paintTracks(g, null);
						ImageIO.write(bi, "PNG", ef);
						currentManager.setDoubleBufferingEnabled(true);
						config.set("lastDirectory", ef.getParentFile());
						h.dispose();
					} catch (Exception ex) {
						model.getLog().log(Level.SEVERE, "save failed", ex);
					}

				}
			});

		}

	}

}
