/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.edit;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.util.Observable;

import javax.swing.KeyStroke;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.dialog.SelectedSequenceDialog;
import net.sf.genomeview.gui.menu.AbstractModelAction;
import net.sf.jannot.Location;
import net.sf.jannot.Sequence;
/**
 * Copies the selected sequence to the system clip board.
 * 
 * @author Thomas Abeel
 *
 */
public class CopySequenceAction extends AbstractModelAction {

    public CopySequenceAction(Model model) {
        super("Copy sequence", model);
        super.putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
    }

    private static final long serialVersionUID = -4864220753372131046L;

    @Override
    public void actionPerformed(ActionEvent e) {

        if (model.getSelectedRegion() != null) {
            Location l = model.getSelectedRegion();
            Sequence seq = model.getSelectedEntry().sequence();
            StringBuffer sb = new StringBuffer(l.length());
            int track = model.getPressTrack();
            switch (track) {
            case 0:
            case 1:
                for (int i = l.start(); i <= l.end(); i++)
                    sb.append(seq.getNucleotide(i));
                break;
            case 2:
            case 3:
            case 4:
                for (int i = l.start(); i <= l.end(); i += 3)
                    sb.append(seq.getAminoAcid(i,model.getAAMapping()));
                break;
            case -1:
            	 for (int i = l.start(); i <= l.end(); i++)
                    sb.append(seq.getReverseNucleotide(i));
                break;
            case -2:
            case -3:
            case -4:
                for (int i = l.end(); i >= l.start(); i -= 3)
                    sb.append(seq.getReverseAminoAcid(i - 2,model.getAAMapping()));
                break;
            }

            Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
            StringSelection ss = new StringSelection(sb.toString());
            cp.setContents(ss, null);

        } else {

            SelectedSequenceDialog.display(model);
            String seq = SelectedSequenceDialog.getSequence();

            if (seq != null) {
                Clipboard cp = Toolkit.getDefaultToolkit().getSystemClipboard();
                StringSelection ss = new StringSelection(seq);
                cp.setContents(ss, null);
            }

        }

    }

    @Override
    public void update(Observable o, Object obn) {
        setEnabled(model.selectionModel().getLocationSelection().size() > 0 || model.getSelectedRegion() != null);
    }

}
