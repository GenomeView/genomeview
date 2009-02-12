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
public class ChromosomeViewExportPNGAction extends AbstractModelAction implements Observer{

	private static final long serialVersionUID = 6052284193240598123L;

	private ChromosomeView view;

	public ChromosomeViewExportPNGAction(ChromosomeView view, Model model) {
	    super("Export as png", new ImageIcon(model.getClass().getResource(
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
		fc.addChoosableFileFilter(new FileNameExtensionFilter("PNG file", "png"));
		fc.showSaveDialog(model.getParent());
		
		File outputFile = fc.getSelectedFile();
		if (outputFile != null) {
			outputFile = ExtensionManager.extension(outputFile, ExtensionManager.PNG);
			int prefHeight = view.getPreferredHeight();
			GraphicsFileExport.exportPNG(view, outputFile.toString(), 800,
					prefHeight);

		}

	}
	
	
	
}
