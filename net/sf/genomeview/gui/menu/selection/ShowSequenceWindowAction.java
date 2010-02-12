/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.selection;

import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.SequenceViewDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;


/**
 * Action to display a dialog with nucleotide/protein sequence of the selected
 * CDS.
 * 
 * 
 */
public class ShowSequenceWindowAction extends AbstractModelAction {

    private static final long serialVersionUID = 4601582100774522419L;

    public ShowSequenceWindowAction(Model model) {
        super("Show DNA or protein sequence", model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Q"));
    }

    public void actionPerformed(ActionEvent arg0) {
        assert ((model.selectionModel().getFeatureSelection() != null  && model.selectionModel().getFeatureSelection().size() > 0) || 
        		(model.getSelectedRegion() != null));
        SequenceViewDialog dialog = new SequenceViewDialog(model);
        dialog.showSequenceViewDialog();

    }

    @Override
    public void update(Observable o, Object obj) {
        setEnabled((model.selectionModel().getFeatureSelection() != null && model.selectionModel().getFeatureSelection().size() > 0) ||
        		(model.getSelectedRegion() != null));
    }
}
