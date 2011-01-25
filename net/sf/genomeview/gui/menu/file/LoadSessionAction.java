/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.task.ReadWorker;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSourceFactory;
import be.abeel.io.LineIterator;

/**
 * Action to handle loading sessions.
 * 
 * @author Thomas Abeel
 * 
 */
public class LoadSessionAction extends AbstractAction {

	private static final long serialVersionUID = -3508287264527633444L;
	private Model model;

	public LoadSessionAction(Model model) {
		super("Load session");
		this.model = model;
	}

	private static Logger log = Logger.getLogger(LoadSessionAction.class.getCanonicalName());

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser chooser = new JFileChooser(Configuration.getFile("lastDirectory"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileFilter(new FileFilter() {

			@Override
			public boolean accept(File f) {
				return f.getName().endsWith(".gvs") || f.isDirectory();
			}

			@Override
			public String getDescription() {
				return "GenomeView sessions";
			}

		});

		int result = chooser.showOpenDialog(model.getGUIManager().getParent());

		if (result == JFileChooser.APPROVE_OPTION) {
			if (model.loadedSources().size() != 0) {
				result = JOptionPane.showConfirmDialog(model.getGUIManager().getParent(),
						"Do you really want to load a session? This will clear all currently loaded data!",
						"Clear entries?", JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}
			LineIterator it = new LineIterator(chooser.getSelectedFile());

			try {
				String key = it.next();
				if (!key.startsWith("##GenomeView session")) {
					JOptionPane.showMessageDialog(model.getGUIManager().getParent(), "The selected file is not a GenomeView session");
				} else {
					it.next();
					model.clearEntries();
					for (String line : it) {
						char c = line.charAt(0);
						line = line.substring(2);
						DataSource ds = null;
						switch (c) {
						case 'U':
							ds = DataSourceFactory.createURL(new URL(line));
							break;
						case 'F':
							ds = DataSourceFactory.createFile(new File(line));
							break;
						default:
							// Do nothing
							log.info("Could not load session line: " + line);
							break;

						}
						final ReadWorker rw = new ReadWorker(ds, model);
						rw.execute();
					}
				}
			} catch (Exception ex) {
				CrashHandler.crash(Level.SEVERE, "Could not load session", ex);
			}
			Configuration.set("lastDirectory", chooser.getSelectedFile().getParentFile());
		}

	}

}
