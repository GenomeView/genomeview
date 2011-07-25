/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.Observable;

import net.sf.genomeview.data.Model;
/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public abstract class AbstractDataProvider<T> extends Observable implements DataProvider<T>{
	public AbstractDataProvider(Model model) {
		addObserver(model);
	}

	protected void notifyListeners(){
		setChanged();
		notifyObservers();
	}
	
}
