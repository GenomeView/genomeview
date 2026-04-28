/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.logging.Level;

import net.sf.genomeview.gui.StaticUtils;
import net.sf.jannot.source.DataSource;

/**
 * Starts a new thread to load data in the background.
 * 
 * @author Thomas Abeel
 * 
 */
public class ReadWorker extends DataSourceWorker {

	public ReadWorker(DataSource source, Model model) {
		super(source, model);
	}

	@Override
	protected Void doInBackground() {
		StaticUtils.run(() -> model.addData(source), model.getLog(),
				Level.WARNING, "read failed of data");
		return null;
	}

}
