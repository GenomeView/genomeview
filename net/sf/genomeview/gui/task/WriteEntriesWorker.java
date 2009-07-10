/**
 * %HEADER%
 */
package net.sf.genomeview.gui.task;

import net.sf.genomeview.data.Model;
import net.sf.jannot.Entry;
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
        try {
            source.saveOwn(model.entries());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        pb.done();
        return null;
    }

}
