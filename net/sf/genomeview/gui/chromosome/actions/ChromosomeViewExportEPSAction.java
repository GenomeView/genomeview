/**
 * %HEADER%
 */
package net.sf.genomeview.gui.chromosome.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.chromosome.ChromosomeView;
import net.sf.genomeview.gui.menu.AbstractModelAction;

import be.abeel.graphics.GraphicsFileExport;
import be.abeel.util.ExtensionManager;

/**
 * Action to export a png image of the chromosome view.
 * 
 * @author thpar
 *
 */
public class ChromosomeViewExportEPSAction extends AbstractModelAction implements Observer{

	private static final long serialVersionUID = 6052284193240598123L;

	private ChromosomeView view;

	public ChromosomeViewExportEPSAction(ChromosomeView view, Model model) {
	    super("Export as eps", new ImageIcon(model.getClass().getResource(
				"/images/save.png")),model);
		this.view = view;
		model.addObserver(this);
	}

	/**
	 * Saves the ChromosomeView to a specified image format.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		JFileChooser fc = new JFileChooser();
		fc.addChoosableFileFilter(new FileNameExtensionFilter("EPS file", "eps"));
		fc.showSaveDialog(model.getParent());
		
		File outputFile = fc.getSelectedFile();
		if (outputFile != null) {
			outputFile = ExtensionManager.extension(outputFile, ExtensionManager.EPS);
			int prefHeight = view.getPreferredHeight();
			GraphicsFileExport.exportEPS(view, outputFile.toString(), 800,
					prefHeight);

		}

	}
	
	

	
	
}
