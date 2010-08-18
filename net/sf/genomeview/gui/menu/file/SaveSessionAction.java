/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.SAMDataSource;
import net.sf.jannot.source.URLSource;

public class SaveSessionAction extends AbstractAction {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1634805658386414327L;
	private Model model;

	public SaveSessionAction(Model model) {
		super("Save session");
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// int result = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
		// "Do you really want to clear all loaded data?",
		// "Clear entries?", JOptionPane.YES_NO_OPTION);
		JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".gvs")||f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "GenomeView sessions";
			}

		});
		// if (result == JOptionPane.YES_OPTION)
		// model.clearEntries();

		int result = chooser.showSaveDialog(model.getGUIManager().getParent());

		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				File f=chooser.getSelectedFile();
				if(!f.getName().endsWith(".gvs")){
					f=new File(f+".gvs");
				}
				PrintWriter out = new PrintWriter(f);
				for (DataSource ds : model.loadedSources()) {
					if (ds instanceof FileSource) {
						out.println("F:"+((FileSource) ds).getFile().toString());
					}
					if (ds instanceof URLSource) {
						out.println("U:"+((URLSource) ds).getURL().toString());
					}
					if (ds instanceof SAMDataSource) {
						out.println("S:"+ ds.toString());
					}
				}
				Configuration.set("lastDirectory", f.getParentFile());
				out.close();
			} catch (Exception ex) {
				// TODO fix
				ex.printStackTrace();
			}
		}

	}

}
