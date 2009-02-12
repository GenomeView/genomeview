/**
 * %HEADER%
 */
package net.sf.genomeview.gui.task;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
import net.sf.jannot.source.DataSource;

/**
 * Starts a new thread to load entry data in the background.
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
public class ReadEntriesWorker extends DataSourceWorker<Entry[]> {

    public ReadEntriesWorker(DataSource source, Model model) {
        super(source, model);
    }

    @Override
    protected Entry[] doInBackground() {
        try {
            return model.addEntries(source);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
