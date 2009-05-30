/**
 * %HEADER%
 */
package net.sf.genomeview.gui.task;

import javax.swing.SwingWorker;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.GVProgressBar;
import net.sf.jannot.source.DataSource;

/**
 * A general SwingWorker to use for loading and saving data without blocking the
 * EDT. This class should be extended, where T is the return type of the method
 * that is executed. doInBackground() has to be overridden containing the long
 * running task. A GVWorker is a ProgressListener, so it is given to the
 * DataSource object that passed it setProgress(int, String) messages (which are
 * fired to any PropertyChangeListener listening to this Worker).
 * 
 * @author thpar
 * @author Thomas Abeel
 * 
 */
public abstract class DataSourceWorker<T> extends SwingWorker<T, Void> {

    protected DataSource source;

    protected Model model;

    protected GVProgressBar pb;

    public DataSourceWorker(DataSource source, Model model) {
        this.source = source;
        this.model = model;
        this.pb= new GVProgressBar("Background task", "Doing background task", model.getParent());
    }

    @Override
    protected abstract T doInBackground();

}
