/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.file;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sf.genomeview.data.DataSourceFactory;
import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.task.ReadFeaturesWorker;
import net.sf.jannot.source.DataSource;
import net.sf.jannot.source.DataSource.Sources;

public class LoadFeaturesAction extends AbstractAction {

    private static final long serialVersionUID = 4601582100774593419L;

    private Model model;

    public LoadFeaturesAction(Model model) {
        super("Load features...");
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
        this.model = model;

    }

    public void actionPerformed(ActionEvent arg0) {
        Sources source = (Sources) JOptionPane.showInputDialog(model.getParent(), "Select feature source",
                "Data selection", JOptionPane.INFORMATION_MESSAGE, null, Sources.values(), Sources.values()[0]);
        if (source != null) {
            DataSource[] data = DataSourceFactory.create(source, model,new String[]{"gff","gff3","maln","syn","wig","mfa"});
            if (data != null) {
                for (DataSource ds : data) {
                	final ReadFeaturesWorker rw = new ReadFeaturesWorker(ds, model);
                    rw.execute();
                }

            }
        }
    }
}
