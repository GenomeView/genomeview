/**
 * %HEADER%
 */
package net.sf.genomeview.data;

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
		try {
			model.addData(source);
		} catch (Throwable e) {
			model.daemonException(e);
			
		}
		return null;
	}

}
