/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.navigation;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.menu.AbstractModelAction;


public class CenterOnPositionAction extends AbstractModelAction {

    private static final long serialVersionUID = 3533852596396875672L;

    public CenterOnPositionAction(Model model) {
        super("Center on position", model);
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        String input = JOptionPane.showInputDialog("Provide a coordinate");
        if (input != null) {
            int i = Integer.parseInt(input);
            super.model.center(i);
        }

    }

}
