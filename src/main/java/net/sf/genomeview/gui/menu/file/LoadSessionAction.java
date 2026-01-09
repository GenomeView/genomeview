/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import net.sf.genomeview.core.Configuration;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.data.ReadWorker;
import net.sf.genomeview.data.Session;
import net.sf.genomeview.gui.CrashHandler;
import net.sf.genomeview.gui.MessageManager;
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
		super(MessageManager.getString("filemenu.load_session"));
		this.model = model;
	}

	private static Logger log = LoggerFactory.getLogger(LoadSessionAction.class.getCanonicalName());

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
						MessageManager.getString("filemenu.load_session_warn"),
						MessageManager.getString("filemenu.clear_session"), JOptionPane.YES_NO_OPTION);
				if (result != JOptionPane.YES_OPTION) {
					return;
				}
			}
			
			try {
				Session.loadSession(model,chooser.getSelectedFile());
			} catch (FileNotFoundException e1) {
				CrashHandler.showErrorMessage(MessageManager.getString("loadsessionaction.couldnt_load_session_file"), e1);
			}
			
			Configuration.set("lastDirectory", chooser.getSelectedFile().getParentFile());
		}

	}

}
