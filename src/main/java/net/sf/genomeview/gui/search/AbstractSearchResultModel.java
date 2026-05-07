/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.data.Model;

/**
 * A Table showing search results.
 * 
 * @author Thomas Abeel
 * 
 */
@SuppressWarnings("serial")
public abstract class AbstractSearchResultModel extends AbstractTableModel
		implements Observer {

	protected final Model model;

	public AbstractSearchResultModel(Model model) {
		this.model = model;
		model.addObserver(this);
	}

	@Override
	public void update(Observable o, Object arg) {
		if (model.entries().size() == 0) {
			clear();
		}

	}

	abstract void clear();

}
