/**
 * %HEADER%
 */
package net.sf.genomeview.gui.menu;

import java.awt.event.ActionEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;

import net.sf.genomeview.data.Model;
import net.sf.genomeview.gui.StaticUtils;

public abstract class AbstractModelAction extends AbstractAction
		implements Observer {

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
		StaticUtils.run(() -> updateSafe(o, obj), model.getLog(), Level.WARNING,
				"failed to update " + this.getClass().getSimpleName());

	}

	/**
	 * Subclasses should implement this instead of
	 * {@link #update(Observable, Object)} to be safe if the method throws an
	 * exception.
	 */
	public abstract void updateSafe(Observable o, Object obj);

	@Override
	public void actionPerformed(ActionEvent arg0) {
		StaticUtils.run(() -> actionPerformedSafe(arg0), model.getLog(),
				Level.WARNING,
				"failed to perform action " + this.getClass().getSimpleName());
	}

	/**
	 * Subclasses should implement this instead of
	 * {@link #actionPerformed(ActionEvent)} to be safe if the method throws an
	 * exception.
	 */
	public abstract void actionPerformedSafe(ActionEvent arg0);

}
