/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.MessageManager;

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
		super(MessageManager.getString("filemenu.load_session"));
		this.model = model;
	}

//	private static Logger log = LoggerFactory
//			.getLogger(LoadSessionAction.class.getCanonicalName());

	@Override
	public void actionPerformed(ActionEvent e) {

		JFileChooser chooser = new JFileChooser(
				model.getConfiguration().getFile("lastDirectory"));
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

		int result = chooser
				.showOpenDialog(model.getGUIManager().getMainWindow());

		if (result == JFileChooser.APPROVE_OPTION) {
			if (model.loadedSources().size() != 0) {
				result = JOptionPane.showConfirmDialog(
						model.getGUIManager().getMainWindow(),
						MessageManager.getString("filemenu.load_session_warn"),
						MessageManager.getString("filemenu.clear_session"),
						JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}

			try {
				Session.loadSession(model, chooser.getSelectedFile());
			} catch (FileNotFoundException e1) {
				model.getLog().log(Level.WARNING,
						MessageManager.getString(
								"loadsessionaction.couldnt_load_session_file"),
						e1);
			}

			model.getConfiguration().set("lastDirectory",
					chooser.getSelectedFile().getParentFile());
		}

	}

}
