/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.Graphics2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import be.abeel.io.ExtensionManager;
import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.viztracks.GeneEvidenceLabel;

/**
 * Dialog guiding the user to export model as pdf reverse engineered and
 * extracted from very old plugin code from sourceforge.
 */
public class ExportPdfDialog extends JDialog {

	private static final long serialVersionUID = -4051977836796905816L;

	private static final Logger log = LoggerFactory
			.getLogger(ExportPdfDialog.class.getCanonicalName());

	private final Model model;

	/**
	 * create dialog for export. Call {@link #display()} to show the dialog
	 * 
	 * @param model the Model to export
	 */
	public ExportPdfDialog(final Model model) {
		this.model = model;
	}

	/**
	 * display the dialog
	 */
	public void display() {
		final JFileChooser chooser = new JFileChooser(
				Configuration.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);

		int result = chooser
				.showSaveDialog(model.getGUIManager().getMainWindow());
		if (result == JFileChooser.APPROVE_OPTION) {
			final File ef = ExtensionManager
					.extension(chooser.getSelectedFile(), ExtensionManager.PDF);
			if (ef.exists()) {
				int confirm = JOptionPane.showConfirmDialog(
						model.getGUIManager().getMainWindow(),
						"This file exists, are you certain you want to overwrite it?");
				if (confirm != JOptionPane.YES_OPTION) {
					return;
				}

			}

			export(ef);

		}

	}

	/**
	 * Export the model to a file
	 * 
	 * @param ef the file to export to
	 */
	private void export(final File ef) {
		final Hider h = new Hider(model, "Exporting image...");
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				try {
					GeneEvidenceLabel mw = model.getGUIManager()
							.getEvidenceLabel();
//						int factor = Configuration
//								.getInt("general:exportMagnifyFactor");
//						BufferedImage bi = new BufferedImage(mw.getWidth()
//								* factor, mw.getHeight() * factor,
//								BufferedImage.TYPE_INT_RGB);
//						Graphics2D g = (Graphics2D) bi.getGraphics();

					try {
//							RepaintManager currentManager = RepaintManager
//									.currentManager(mw);
//							currentManager.setDoubleBufferingEnabled(false);
//							g.scale(factor, factor);

//							/ImageIO.write(bi, "PNG", ef);
//							currentManager.setDoubleBufferingEnabled(true);
//							mw.getS
						com.lowagie.text.Rectangle pagesize = new com.lowagie.text.Rectangle(
								mw.getWidth(), mw.getHeight());
						Document document = new Document(pagesize, 50, 50, 50,
								50);
						PdfWriter writer = PdfWriter.getInstance(document,
								new FileOutputStream(ef));
						document.open();
						PdfContentByte cb = writer.getDirectContent();
						document.open();
						PdfTemplate tp = cb.createTemplate(mw.getWidth(),
								mw.getHeight());
						Graphics2D g2 = tp.createGraphics(mw.getWidth(),
								mw.getHeight(), new DefaultFontMapper());
//							Rectangle2D r2D = new Rectangle2D.Double(0, 0, mw.getWidth(), mw.getHeight());
						// System.out.println("ExportFactory.java: "+charts[i]);
						// d.draw(g2, r2D);
//							mw.actualPaint(g2, null);
						mw.paintTracks(g2, null);
						g2.dispose();
						cb.addTemplate(tp, 0, 0);

						//
						document.close();

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
