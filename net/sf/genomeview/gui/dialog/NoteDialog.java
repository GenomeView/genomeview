/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTextField;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.QualifierCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Qualifier;
import net.sf.jannot.Type;

public class NoteDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = -1331026064119986714L;

    private final JTextField value;

    private final QualifierCombo term;

    private NoteDialog(final Model model) {
        super(model.getParent(), "Note editor");
        setModal(true);

        term = new QualifierCombo();

        setLayout(new BorderLayout());
        add(term, BorderLayout.NORTH);

        value = new JTextField();
        add(value, BorderLayout.CENTER);
        JButton ok = new JButton("OK");
        this.getRootPane().setDefaultButton(ok);
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              
                Feature rf = model.selectionModel().getFeatureSelection().iterator().next();
                if (currentNote != null) {
                    currentNote.setKey(term.getQualifierKey());
                    currentNote.setValue(value.getText());
                } else {
                    Qualifier neww = new Qualifier(term.getQualifierKey(), value.getText());
                    rf.addQualifier(neww);
                }
                setVisible(false);

            }

        });
        add(ok, BorderLayout.SOUTH);
        pack();
        StaticUtils.center(this);
    }

    private static NoteDialog dialog = null;

    private static Qualifier currentNote;

    public static void showDialog(Model model, Qualifier note) {
        if (dialog == null)
            dialog = new NoteDialog(model);
        currentNote = note;
        if (note != null) {
            dialog.value.setText(note.getValue());
            dialog.term.setSelectedItem(Type.valueOf(note.getKey()));
        }
        dialog.setVisible(true);

    }

}
