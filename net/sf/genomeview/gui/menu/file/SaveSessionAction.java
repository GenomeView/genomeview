/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.FileSource;
import net.sf.jannot.source.URLSource;
/**
 * Action that takes care of saving all loaded data into a session file.
 * 
 * @author Thomas Abeel
 *
 */
public class SaveSessionAction extends AbstractAction {

	
	private static final long serialVersionUID = 1634805658386414327L;
	private Model model;

	public SaveSessionAction(Model model) {
		super("Save session");
		this.model = model;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
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
		
		int result = chooser.showSaveDialog(model.getGUIManager().getParent());

		if (result == JFileChooser.APPROVE_OPTION) {
			try {
				File f=chooser.getSelectedFile();
				if(!f.getName().endsWith(".gvs")){
					f=new File(f+".gvs");
				}
				PrintWriter out = new PrintWriter(f);
				out.println("##GenomeView session       ##");
				out.println("##Do not remove header lines##");
				for (DataSource ds : model.loadedSources()) {
					if (ds instanceof FileSource) {
						out.println("F:"+((FileSource) ds).getFile().toString());
					}
					if (ds instanceof URLSource) {
						out.println("U:"+((URLSource) ds).getURL().toString());
					}
				}
				Configuration.set("lastDirectory", f.getParentFile());
				out.close();
			} catch (Exception ex) {
				CrashHandler.crash(Level.SEVERE, "Could not save session", ex);
			}
		}

	}

}
