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


public class ChromosomeToggle extends JCheckBoxMenuItem implements Observer, ActionListener {

    /**
     * 
     */
    private static final long serialVersionUID = -3723865866473623425L;

    private Model model;

    public ChromosomeToggle(Model model) {
        super("Chromosome view");
        this.model = model;
        this.model.addObserver(this);
        this.addActionListener(this);
    }

    @Override
    public void update(Observable o, Object arg) {
        this.setEnabled(model.isChromosomeAvailable());
        this.setSelected(model.isChromosomeVisible());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        model.setChromosomeVisible(!model.isChromosomeVisible());

    }

}
