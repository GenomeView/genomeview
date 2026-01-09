/**
 * %HEADER%
 */
package net.sf.genomeview.gui.search;

import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import net.sf.genomeview.data.Model;

/**
 * 
 * @author Thomas Abeel
 * 
 */
public abstract class AbstractSearchResultModel extends AbstractTableModel implements Observer {

	private static final long serialVersionUID = 7880866868740238251L;
	Model model;

	public AbstractSearchResultModel(Model model) {
		this.model = model;
		model.addObserver(this);
	}

	/* (non-Javadoc)
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	@Override
	public void update(Observable o, Object arg) {
		if(model.entries().size()==0)
			clear();
		
	}

	abstract void clear();
	
	

}
