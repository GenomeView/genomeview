/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import net.sf.jannot.source.DataSource;

/**
 * Starts a new thread to save entry data in the background.
 * 
 * @author thpar
 * @author thabe
 */
public class WriteEntriesWorker extends DataSourceWorker {

	public WriteEntriesWorker(DataSource source, Model model) {
		super(source, model);
	}

	@Override
	protected Void doInBackground() {
		return null;
	}

}
