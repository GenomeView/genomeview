/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JCheckBoxMenuItem;

import net.sf.genomeview.data.Model;


public class StructureToggle extends JCheckBoxMenuItem implements Observer, ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -8987147284052981811L;
    private Model model;

    public StructureToggle(Model model) {
        super("Gene structure view");
        this.model = model;
        this.model.addObserver(this);
        this.addActionListener(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        this.setEnabled(model.isStructureAvailable());
        this.setSelected(model.isStructureVisible());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.setStructureVisible(!model.isStructureVisible());

    }

}
