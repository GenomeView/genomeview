/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.RepaintManager;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.annotation.GeneEvidenceLabel;

public class SaveImage extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -92536467741311140L;
	private Model model;

	public SaveImage(Model model) {
		super("Export image...");
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// int result = JOptionPane.showConfirmDialog(model.getParent(),
		// "Do you really want to clear all loaded data?",
		// "Clear entries?", JOptionPane.YES_NO_OPTION);
		JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);

		// if (result == JOptionPane.YES_OPTION)
		// model.clearEntries();

		int result = chooser.showSaveDialog(model.getParent());

		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				File f = chooser.getSelectedFile();
				GeneEvidenceLabel mw = model.getGUIManager().getEvidenceLabel();
				BufferedImage bi = new BufferedImage(mw.getWidth()*4,mw.getHeight()*4, BufferedImage.TYPE_INT_RGB);
				Graphics2D g = (Graphics2D) bi.getGraphics();
				
				try {
					RepaintManager currentManager = 
						  RepaintManager.currentManager(mw);
						currentManager.setDoubleBufferingEnabled(false);
					g.scale(4, 4);
					mw.actualPaint(g);
					ImageIO.write(bi, "PNG", new File(f+".png"));
					currentManager.setDoubleBufferingEnabled(true);
				} catch (IOException ex) {
					// TODO Auto-generated catch block
					ex.printStackTrace();
				}				

			} catch (Exception ex) {
				// TODO fix
				ex.printStackTrace();
			}
		}

	}

}
