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
import net.sf.genomeview.gui.MessageManager;
import net.sf.genomeview.gui.StaticUtils;
import net.sf.genomeview.gui.components.QualifierCombo;
import net.sf.jannot.Feature;
import net.sf.jannot.Type;
import be.abeel.util.Pair;

public class NoteDialog extends JDialog {

    /**
     * 
     */
    private static final long serialVersionUID = -1331026064119986714L;

    private final JTextField value;

    private final QualifierCombo term;

    private NoteDialog(final Model model) {
    	super(model.getGUIManager().getParent(), MessageManager.getString("notedialog.title"));
        setModal(true);

        term = new QualifierCombo();

        setLayout(new BorderLayout());
        add(term, BorderLayout.NORTH);

        value = new JTextField();
        add(value, BorderLayout.CENTER);
        JButton ok = new JButton(MessageManager.getString("button.ok"));
        this.getRootPane().setDefaultButton(ok);
        ok.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
              
                Feature rf = model.selectionModel().getFeatureSelection().iterator().next();
                if (currentNote != null) {
                	currentNote=new Pair(term.getQualifierKey(),value.getText());
                    
                } else {
                   
                    rf.addQualifier(term.getQualifierKey(), value.getText());
                }
                setVisible(false);

            }

        });
        add(ok, BorderLayout.SOUTH);
        pack();
        StaticUtils.center(model.getGUIManager().getParent(),this);
    }

    private static NoteDialog dialog = null;

    private static Pair<String,String> currentNote;

    public static void showDialog(Model model, Pair<String,String> note) {
        if (dialog == null)
            dialog = new NoteDialog(model);
        currentNote = note;
        if (note != null) {
            dialog.value.setText(note.y());
            dialog.term.setSelectedItem(Type.valueOf(note.x()));
        }
        dialog.setVisible(true);

    }

}
