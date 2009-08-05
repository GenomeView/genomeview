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
import net.sf.genomeview.data.DataSourceFactory.Sources;
import net.sf.genomeview.gui.task.ReadWorker;
import net.sf.jannot.source.DataSource;

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
            DataSource[] data = DataSourceFactory.create(source, model,new String[]{"fasta","fa","fas","embl","fna","gtf","gff","gff3","maln","syn","wig","mfa","bed"});
            if (data != null) {
                for (DataSource ds : data) {
                	final ReadWorker rw = new ReadWorker(ds, model);
                    rw.execute();
                }

            }
        }
    }
}
