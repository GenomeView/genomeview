/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;


public abstract class AbstractModelAction extends AbstractAction implements Observer {

    /**
	 * 
	 */
	private static final long serialVersionUID = 4772441878890141849L;
	protected Model model;

    public AbstractModelAction(String name, Model model) {
        super(name);
        this.model = model;
        this.model.addObserver(this);
        update(null, null);
    }

    public AbstractModelAction(String name, ImageIcon imageIcon, Model model2) {
        super(name, imageIcon);
        this.model = model2;
        this.model.addObserver(this);
        update(null, null);
    }

    @Override
    public void update(Observable o, Object obj) {

        // do nothing

    }

}
