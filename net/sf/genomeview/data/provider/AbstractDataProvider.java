/**
 * %HEADER%
 */
package net.sf.genomeview.data.provider;

import java.util.Observable;
/**
 * 
 * @author Thomas Abeel
 *
 * @param <T>
 */
public abstract class AbstractDataProvider<T> extends Observable implements DataProvider<T>{
	protected void notifyListeners(){
		setChanged();
		notifyObservers();
	}
	
}
