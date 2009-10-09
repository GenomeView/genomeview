/**
 * %HEADER%
 */
package net.sf.genomeview.gui.task;

import net.sf.genomeview.data.Model;
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
			pb.done();
			
		} catch (Throwable e) {
			e.printStackTrace();
			pb.done();
			
		}
		return null;
	}

}
