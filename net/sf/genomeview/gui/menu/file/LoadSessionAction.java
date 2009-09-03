/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import be.abeel.io.LineIterator;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.SAMDataSource;
import net.sf.jannot.source.URLSource;

public class LoadSessionAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3508287264527633444L;
	private Model model;

	public LoadSessionAction(Model model) {
		super("Load session");
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".gvs");
			}

			@Override
			public String getDescription() {
				return "GenomeView sessions";
			}

		});

		int result = chooser.showOpenDialog(model.getParent());

		if (result == JFileChooser.APPROVE_OPTION) {
			if (model.loadedSources().size() != 0) {
				result = JOptionPane.showConfirmDialog(model.getParent(), "Do you really want to load a session? This will clear all currently loaded data!", "Clear entries?", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}
			LineIterator it = new LineIterator(chooser.getSelectedFile());
			try {
				model.clearEntries();
				for (String line : it) {
					char c=line.charAt(0);
					line=line.substring(2);
					switch(c){
					case 'U':
						model.addData(new URLSource(new URL(line)));
						break;
					case 'F':
						model.addData(new FileSource(new File(line)));
						break;
					case 'S':
						model.addData(new SAMDataSource(new File(line)));
						break;
						
					}
				}
			} catch (Exception ex) {
				// TODO fix
				ex.printStackTrace();
			}
			Configuration.set("lastDirectory", chooser.getSelectedFile().getParentFile());
		}

	}

}
