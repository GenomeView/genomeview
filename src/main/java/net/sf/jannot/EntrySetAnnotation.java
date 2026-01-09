/**
 * %HEADER%
 */
package net.sf.jannot;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.jannot.alignment.mfa.AlignmentAnnotation;
import net.sf.jannot.shortread.ShortRead;

/**
 * Interface for annotation associated with an EntrySet of type T.
 * 
 * @author Thomas Abeel
 * 
 * @param <T>
 *            type of the annotation
 * @see FeatureAnnotation
 * @see AlignmentAnnotation
 * @see SyntenicAnnotation
 * @see GraphAnnotation
 */
public abstract class EntrySetAnnotation<T> implements Iterable<T> {

	
	/**
	 * Returns all object of type T that overlap with the provided location.
	 * 
	 * @param l
	 * @return
	 */
	public Iterable<T> get(Entry e,Location l){
		return getAll(e);
	}
	
	public abstract boolean contains(T t);
	/**
	 * Returns all object of type T that overlap with the provided location.
	 * 
	 * @param l
	 * @return
	 */
	public Iterable<T> get(Entry e,Location l,int limit){
		return get(e,l);
	}
	
//	public void addAll(EntrySetAnnotation<T> ts){
//		addAll(ts.getAll(e))
//	}
//	
//	public void addAll(Iterable<T> list){
//		for(T t:list)
//			this.list.add(t);
//		setChanged();
//		notifyObservers();
//	}

	

	
	public abstract Iterable<T> getAll(Entry e) ;

	
	public abstract void add(T g);



	@Override
	public abstract Iterator<T> iterator();

	

}
