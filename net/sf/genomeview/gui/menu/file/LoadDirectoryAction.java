/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import net.sf.genomeview.data.DataMenu;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.task.ReadWorker;
import net.sf.jannot.source.DataSource;

public class LoadDirectoryAction extends AbstractAction {

	private Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

	private static final long serialVersionUID = 4601582100774593419L;

	private Model model;

	public LoadDirectoryAction(Model model) {
		super("Load directory...");
		super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
		this.model = model;

	}

	public void actionPerformed(ActionEvent arg0) {
		DataSource[] data = DataMenu.create(
				net.sf.jannot.source.DataSourceFactory.Sources.DIRECTORY, model,null);

		if (data != null) {
			logger.info("Datasources=" + data.length);
			for (DataSource ds : data) {
				final ReadWorker rw = new ReadWorker(ds, model);
				rw.execute();
			}
		}
	}

}
