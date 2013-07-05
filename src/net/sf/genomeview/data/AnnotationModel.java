/**
 * %HEADER%
 */
package net.sf.genomeview.data;

import java.util.Observable;

import net.sf.jannot.Type;

/**
 * Keeps track of changes of that the user makes to the feature annotation and
 * notifies interested parties.
 * 
 * @author Thomas Abeel
 * 
 */
public class AnnotationModel extends Observable {

	public void typeUpdated(Type type) {
		setChanged();
		notifyObservers(type);

	}

}