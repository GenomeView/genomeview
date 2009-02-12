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


public class EvidenceToggle extends JCheckBoxMenuItem implements Observer,ActionListener{

    /**
     * 
     */
    private static final long serialVersionUID = 3896493515290056367L;
    private Model model;
    public EvidenceToggle(Model model) {
        super("Evidence view");
        this.model = model;
        this.model.addObserver(this);
        this.addActionListener(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        this.setEnabled(model.isAnnotationAvailable());
        this.setSelected(model.isAnnotationVisible());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.setAnnotationVisible(!model.isAnnotationVisible());

    }

}
