/**
 * %HEADER%
 */
package net.sf.genomeview.gui.dialog;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import net.sf.genomeview.core.Icons;


public class HelpDialog extends JLabel {

    private static final long serialVersionUID = -7184331819811186958L;

    public HelpDialog(final JDialog parent, final String message) {
        super(Icons.HELP);
        super.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent arg0) {
                JOptionPane.showMessageDialog(parent, "<html><table width=400>"+message+"</table></html>", "Help!", JOptionPane.QUESTION_MESSAGE);
            }

        });
    }
  
}
