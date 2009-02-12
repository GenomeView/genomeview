/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import net.sf.genomeview.data.DataSourceFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.GVProgressBar;
import net.sf.genomeview.gui.task.ReadEntriesWorker;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSource.Sources;

public class LoadEntriesAction extends AbstractAction {

    private static final long serialVersionUID = 4601582100774593419L;

    private Model model;

    public LoadEntriesAction(Model model) {
        super("Load entries...");
        this.model = model;

    }

    public void actionPerformed(ActionEvent arg0) {
        Sources source = (Sources) JOptionPane.showInputDialog(model.getParent(), "Select feature source",
                "Data selection", JOptionPane.INFORMATION_MESSAGE, null, Sources.values(), Sources.values()[0]);
        if (source != null) {
            net.sf.jannot.source.DataSource[] data = DataSourceFactory.create(source, model);
            // if (data != null){
            // GVProgressBar pb = new GVProgressBar("Loading",
            // "Loading additional entries", model.getParent());
            // data.setProgressListener(pb);
            // ReadEntriesWorker rw = new ReadEntriesWorker(data, model);
            // rw.execute();
            // }
            if (data != null) {
                for (DataSource ds : data) {
                    final GVProgressBar pb = new GVProgressBar("Loading", "Loading data", model.getParent());
                    ds.setProgressListener(pb);
                    final ReadEntriesWorker rw = new ReadEntriesWorker(ds, model);
                    rw.execute();
                }
            }
        }
    }

}
